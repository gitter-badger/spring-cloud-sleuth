/*
 * Copyright 2015 the original author or authors.
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
package org.springframework.cloud.sleuth.instrument.integration;

import java.util.Random;

import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Trace;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.instrument.TraceKeys;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;

/**
 * Interceptor for Stomp Messages sent over websocket
 *
 * @author Gaurav Rai Mazra
 * @author Marcin Grzejszczak
 *
 */
public class TraceStompMessageChannelInterceptor extends AbstractTraceChannelInterceptor implements ChannelInterceptor {
	private ThreadLocal<Trace> traceScopeHolder = new ThreadLocal<Trace>();

	public TraceStompMessageChannelInterceptor(Tracer tracer, TraceKeys traceKeys, Random random) {
		super(tracer, traceKeys, random);
	}

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		if (getTracer().isTracing() || message.getHeaders().containsKey(Trace.NOT_SAMPLED_NAME)) {
			return StompMessageBuilder.fromMessage(message).setHeadersFromSpan(getTracer().getCurrentSpan()).build();
		}
		String name = getMessageChannelName(channel);
		Trace trace = startSpan(buildSpan(message), name);
		this.traceScopeHolder.set(trace);
		return StompMessageBuilder.fromMessage(message).setHeadersFromSpan(trace.getSpan()).build();
	}

	private Trace startSpan(Span span, String name) {
		if (span != null) {
			return getTracer().joinTrace(name, span);
		}
		return getTracer().startTrace(name);
	}

	@Override
	public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
		final ThreadLocal<Trace> traceScopeHolder = this.traceScopeHolder;
		Trace traceInScope = traceScopeHolder.get();
		getTracer().close(traceInScope);
		traceScopeHolder.remove();
	}
}
