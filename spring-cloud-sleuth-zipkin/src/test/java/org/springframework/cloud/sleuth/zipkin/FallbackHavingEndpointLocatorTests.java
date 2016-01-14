package org.springframework.cloud.sleuth.zipkin;

import io.zipkin.Endpoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.BDDAssertions.then;

@RunWith(MockitoJUnitRunner.class)
public class FallbackHavingEndpointLocatorTests {

	@Mock DiscoveryClientEndpointLocator discoveryClientEndpointLocator;
	@Mock ServerPropertiesEndpointLocator serverPropertiesEndpointLocator;
	Endpoint expectedEndpoint = Endpoint.create("my-tomcat", 127 << 24 | 1, 8080);

	@Test
	public void should_use_system_property_locator_if_discovery_client_locator_is_not_present() {
		BDDMockito.given(serverPropertiesEndpointLocator.local()).willReturn(expectedEndpoint);
		FallbackHavingEndpointLocator sut = new FallbackHavingEndpointLocator(null,
				serverPropertiesEndpointLocator);

		Endpoint endpoint = sut.local();

		then(endpoint).isSameAs(expectedEndpoint);
	}

	@Test
	public void should_use_system_property_locator_if_discovery_client_locator_throws_an_exception() {
		BDDMockito.given(discoveryClientEndpointLocator.local()).willThrow(new RuntimeException());
		BDDMockito.given(serverPropertiesEndpointLocator.local()).willReturn(expectedEndpoint);
		FallbackHavingEndpointLocator sut = new FallbackHavingEndpointLocator(discoveryClientEndpointLocator,
				serverPropertiesEndpointLocator);

		Endpoint endpoint = sut.local();

		then(endpoint).isSameAs(expectedEndpoint);
	}

	@Test
	public void should_use_discovery_client_locator_by_default() {
		BDDMockito.given(discoveryClientEndpointLocator.local()).willReturn(expectedEndpoint);
		BDDMockito.given(serverPropertiesEndpointLocator.local()).willThrow(new RuntimeException());
		FallbackHavingEndpointLocator sut = new FallbackHavingEndpointLocator(discoveryClientEndpointLocator,
				serverPropertiesEndpointLocator);

		Endpoint endpoint = sut.local();

		then(endpoint).isSameAs(expectedEndpoint);
	}
}