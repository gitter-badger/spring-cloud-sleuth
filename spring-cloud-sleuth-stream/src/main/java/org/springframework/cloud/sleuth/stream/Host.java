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

package org.springframework.cloud.sleuth.stream;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Dave Syer
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
public class Host {

	private String serviceName;
	private String address;
	private Integer port;

	public int getIpv4() {
		InetAddress inetAddress = null;
		try {
			inetAddress = InetAddress.getByName(this.address);
		}
		catch (final UnknownHostException e) {
			throw new IllegalArgumentException(e);
		}
		return ByteBuffer.wrap(inetAddress.getAddress()).getInt();
	}

}
