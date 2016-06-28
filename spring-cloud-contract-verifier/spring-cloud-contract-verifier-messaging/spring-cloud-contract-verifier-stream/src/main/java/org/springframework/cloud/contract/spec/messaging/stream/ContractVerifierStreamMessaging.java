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

package org.springframework.cloud.contract.spec.messaging.stream;

import org.springframework.cloud.contract.spec.messaging.ContractVerifierMessage;
import org.springframework.cloud.contract.spec.messaging.ContractVerifierMessageBuilder;
import org.springframework.cloud.contract.spec.messaging.ContractVerifierMessaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.config.BindingProperties;
import org.springframework.cloud.stream.config.ChannelBindingServiceProperties;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Marcin Grzejszczak
 */
public class ContractVerifierStreamMessaging<T> implements
		ContractVerifierMessaging<T, Message<T>> {

	private static final Logger log = LoggerFactory.getLogger(ContractVerifierStreamMessaging.class);

	private final ApplicationContext context;
	private final MessageCollector messageCollector;
	private final ContractVerifierMessageBuilder builder;

	@Autowired
	@SuppressWarnings("unchecked")
	public ContractVerifierStreamMessaging(ApplicationContext context, ContractVerifierMessageBuilder builder) {
		this.context = context;
		this.messageCollector = context.getBean(MessageCollector.class);
		this.builder = builder;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void send(T payload, Map<String, Object> headers, String destination) {
		send(builder.create(payload, headers), destination);
	}

	@Override
	public void send(ContractVerifierMessage<T, Message<T>> message, String destination) {
		try {
			MessageChannel messageChannel = context.getBean(resolvedDestination(destination), MessageChannel.class);
			messageChannel.send(message.convert());
		} catch (Exception e) {
			log.error("Exception occurred while trying to send a message [" + message + "] " +
					"to a channel with name [" + destination + "]", e);
			throw e;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public ContractVerifierMessage<T, Message<T>> receiveMessage(String destination, long timeout, TimeUnit timeUnit) {
		try {
			MessageChannel messageChannel = context.getBean(resolvedDestination(destination), MessageChannel.class);
			return builder.create(messageCollector.forChannel(messageChannel).poll(timeout, timeUnit));
		} catch (Exception e) {
			log.error("Exception occurred while trying to read a message from " +
					" a channel with name [" + destination + "]", e);
			throw new RuntimeException(e);
		}
	}

	private String resolvedDestination(String destination) {
		ChannelBindingServiceProperties channelBindingServiceProperties = context.getBean(ChannelBindingServiceProperties.class);
		String resolvedDestination = destination;
		for (Map.Entry<String, BindingProperties> entry : channelBindingServiceProperties.getBindings().entrySet()) {
			if (entry.getValue().getDestination().equals(destination)) {
				log.debug("Found a channel named [{}] with destination [{}]", entry.getKey(), destination);
				return entry.getKey();
			}
		}
		log.debug("No destination named [{}] was found. Assuming that the destination equals the channel name", destination);
		return resolvedDestination;
	}

	@Override
	public ContractVerifierMessage<T, Message<T>> receiveMessage(String destination) {
		return receiveMessage(destination, 5, TimeUnit.SECONDS);
	}

	@Override
	@SuppressWarnings("unchecked")
	public ContractVerifierMessage<T, Message<T>> create(T t, Map<String, Object> headers) {
		return builder.create(t, headers);
	}

	@Override
	@SuppressWarnings("unchecked")
	public ContractVerifierMessage<T, Message<T>> create(Message<T> message) {
		return builder.create(message);
	}
}
