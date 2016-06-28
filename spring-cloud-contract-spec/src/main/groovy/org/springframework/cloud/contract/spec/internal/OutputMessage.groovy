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

package org.springframework.cloud.contract.spec.internal

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TypeChecked

@TypeChecked
@EqualsAndHashCode
@ToString(includePackage = false, includeNames = true)
class OutputMessage extends Common {

	DslProperty<String> sentTo
	Headers headers
	DslProperty body
	ExecutionProperty assertThat

	OutputMessage() {}

	OutputMessage(OutputMessage outputMessage) {
		this.sentTo = outputMessage.sentTo
		this.headers = outputMessage.headers
		this.body = outputMessage.body
	}

	void sentTo(String sentTo) {
		this.sentTo = new DslProperty(sentTo)
	}

	void sentTo(DslProperty sentTo) {
		this.sentTo = sentTo
	}

	ServerDslProperty producer(Object clientValue) {
		return new ServerDslProperty(clientValue)
	}

	ClientDslProperty consumer(Object clientValue) {
		return new ClientDslProperty(clientValue)
	}

	void body(Object bodyAsValue) {
		this.body = new DslProperty(bodyAsValue)
	}

	void body(DslProperty bodyAsValue) {
		this.body = bodyAsValue
	}

	void headers(@DelegatesTo(Headers) Closure closure) {
		this.headers = new Headers()
		closure.delegate = headers
		closure()
	}

	void assertThat(String assertThat) {
		this.assertThat = new ExecutionProperty(assertThat)
	}
}

@CompileStatic
@EqualsAndHashCode
@ToString(includePackage = false)
class ServerOutputMessage extends OutputMessage {
	ServerOutputMessage(OutputMessage request) {
		super(request)
	}
}

@CompileStatic
@EqualsAndHashCode
@ToString(includePackage = false)
class ClientOutputMessage extends OutputMessage {
	ClientOutputMessage(OutputMessage request) {
		super(request)
	}
}
