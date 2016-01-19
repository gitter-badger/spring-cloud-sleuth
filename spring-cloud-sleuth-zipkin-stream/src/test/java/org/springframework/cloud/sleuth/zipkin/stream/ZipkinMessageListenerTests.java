/*
 * Copyright 2013-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.sleuth.zipkin.stream;

import io.zipkin.BinaryAnnotation;
import io.zipkin.Endpoint;
import org.junit.Test;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.stream.Host;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class ZipkinMessageListenerTests {
	Span span = new Span(1, 3, "name", 1L, Collections.<Long>emptyList(), 2L, true, true,
			"process");
	Host host = new Host("myservice", "1.2.3.4", 8080);
	Endpoint endpoint = Endpoint.create("myservice", 1 << 24 | 2 << 16 | 3 << 8 | 4, 8080);

	/** Sleuth timestamps are millisecond granularity while zipkin is microsecond. */
	@Test
	public void convertsTimestampAndDurationToMicroseconds() {
		long start = System.currentTimeMillis();
		span.log("http/request/retry"); // System.currentTimeMillis

		io.zipkin.Span result = ZipkinMessageListener.convert(span, host);

		assertThat(result.timestamp)
				.isEqualTo(span.getBegin() * 1000);
		assertThat(result.duration)
				.isEqualTo((span.getEnd() - span.getBegin()) * 1000);
		assertThat(result.annotations.get(0).timestamp)
				.isGreaterThanOrEqualTo(start * 1000)
				.isLessThanOrEqualTo(System.currentTimeMillis() * 1000);
	}

	/** Sleuth host corresponds to annotation/binaryAnnotation.host in zipkin. */
	@Test
	public void annotationsIncludeHost() {
		span.log("http/request/retry");
		span.tag("spring-boot/version", "1.3.1.RELEASE");

		io.zipkin.Span result = ZipkinMessageListener.convert(span, host);

		assertThat(result.annotations.get(0).endpoint)
				.isEqualTo(endpoint);
		assertThat(result.binaryAnnotations.get(0).endpoint)
				.isEqualTo(result.annotations.get(0).endpoint);
	}

	/**
	 * In zipkin, the service context is attached to annotations. Sleuth spans
	 * that have no annotations will get an "lc" one, which allows them to be
	 * queryable in zipkin by service name.
	 */
	@Test
	public void spanWithoutAnnotationsLogsComponent() {
		io.zipkin.Span result = ZipkinMessageListener.convert(span, host);

		assertThat(result.binaryAnnotations).hasSize(1);
		assertThat(result.binaryAnnotations.get(0)).isEqualToComparingFieldByField(
				BinaryAnnotation.create("lc", span.getProcessId(), endpoint));
	}

	// TODO: "unknown" bc process id, documented as not nullable, is null in some tests.
	@Test
	public void nullProcessIdCoercesToUnknownServiceName() {
		Span noProcessId = Span.builder().traceId(1L).name("parent").remote(true).build();

		io.zipkin.Span result = ZipkinMessageListener.convert(noProcessId, host);

		assertThat(result.binaryAnnotations)
				.containsOnly(BinaryAnnotation.create("lc", "unknown", endpoint));
	}
}