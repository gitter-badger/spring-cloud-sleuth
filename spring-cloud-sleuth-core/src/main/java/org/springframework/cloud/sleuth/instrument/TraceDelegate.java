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

package org.springframework.cloud.sleuth.instrument;

import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Trace;
import org.springframework.cloud.sleuth.Tracer;

import lombok.Getter;
import org.springframework.cloud.sleuth.trace.TraceContextHolder;

/**
 * @author Spencer Gibb
 */
@Getter
public abstract class TraceDelegate<T> {

	private final Tracer tracer;
	private final T delegate;
	private final String name;
	private final Span parent;

	public TraceDelegate(Tracer tracer, T delegate) {
		this(tracer, delegate, null);
	}

	public TraceDelegate(Tracer tracer, T delegate, String name) {
		this.tracer = tracer;
		this.delegate = delegate;
		this.name = name;
		this.parent = tracer.getCurrentSpan();
	}

	protected void close(Trace trace) {
		this.tracer.close(trace);
	}

	protected void closeAll(Trace trace) {
		trace = this.tracer.close(trace);
		while (trace != null) {
			trace = this.tracer.detach(trace);
		}
	}

	protected Trace startSpan() {
		return this.tracer.joinTrace(getSpanName(), this.parent);
	}

	protected String getSpanName() {
		return this.name == null ? Thread.currentThread().getName() : this.name;
	}

	protected void ensureThatThreadIsNotPollutedByPreviousTraces() {
		TraceContextHolder.removeCurrentTrace();
	}
}
