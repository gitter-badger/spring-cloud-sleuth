package org.springframework.cloud.sleuth.instrument.integration;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.cloud.sleuth.trace.SpanContextHolder;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.ExecutorSubscribableChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.BDDAssertions.then;

@RunWith(SpringJUnit4ClassRunner.class)
public abstract class AbstractTraceStompIntegrationTests {

	@Autowired
	@Qualifier("executorSubscribableChannel")
	ExecutorSubscribableChannel channel;
	@Autowired
	Tracer tracer;
	@Autowired StompMessageHandler stompMessageHandler;
	@Autowired AlwaysSampler sampler;

	@Before
	public void init() {
		this.channel.subscribe(this.stompMessageHandler);
	}

	@After
	public void close() {
		SpanContextHolder.removeCurrentSpan();
		this.channel.unsubscribe(this.stompMessageHandler);
	}

	Span givenALocallyStartedSpan() {
		return this.tracer.startTrace("testSendMessage", this.sampler);
	}

	Message<?> givenMessageToBeSampled() {
		return StompMessageBuilder.fromMessage(new GenericMessage<>("Message2")).build();
	}

	void whenTheMessageWasSent(Message<?> message) {
		this.channel.send(message);
		then(this.stompMessageHandler.message).isNotNull();
	}

	Long thenSpanIdFromHeadersIsNotEmpty() {
		Long header = getValueFromHeaders(Span.SPAN_ID_NAME, Long.class);
		then(header).as("Span id should not be empty").isNotNull();
		return header;
	}

	Long thenTraceIdFromHeadersIsNotEmpty() {
		Long header = getValueFromHeaders(Span.TRACE_ID_NAME, Long.class);
		then(header).as("Trace id should not be empty").isNotNull();
		return header;
	}

	<T> T getValueFromHeaders(String headerName, Class<T> type) {
		return this.stompMessageHandler.message.getHeaders().get(headerName, type);
	}
}
