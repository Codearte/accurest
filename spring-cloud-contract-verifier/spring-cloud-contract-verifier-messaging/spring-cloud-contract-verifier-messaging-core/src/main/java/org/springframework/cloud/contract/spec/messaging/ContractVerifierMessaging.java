/*
 *  Copyright 2013-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.spec.messaging;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Core interface that allows you to build, send and receive messages.
 *
 * Destination is relevant to the underlaying implementation. Might be a channel, queue, topic etc.
 *
 * @author Marcin Grzejszczak
 */
public interface ContractVerifierMessaging<PAYLOAD, TYPE_TO_CONVERT_INTO> extends
		ContractVerifierMessageBuilder<PAYLOAD, TYPE_TO_CONVERT_INTO> {
	/**
	 * Sends the {@link ContractVerifierMessage} to the given destination.
	 */
	void send(ContractVerifierMessage<PAYLOAD, TYPE_TO_CONVERT_INTO> message, String destination);

	/**
	 * Sends the given payload with headers, to the given destination.
	 */
	void send(PAYLOAD payload, Map<String, Object> headers, String destination);

	/**
	 * Receives the {@link ContractVerifierMessage} from the given destination. You can provide the timeout
	 * for receiving that message.
	 */
	ContractVerifierMessage<PAYLOAD, TYPE_TO_CONVERT_INTO> receiveMessage(String destination, long timeout, TimeUnit timeUnit);

	/**
	 * Receives the {@link ContractVerifierMessage} from the given destination. A default timeout will be applied.
	 */
	ContractVerifierMessage<PAYLOAD, TYPE_TO_CONVERT_INTO> receiveMessage(String destination);
}
