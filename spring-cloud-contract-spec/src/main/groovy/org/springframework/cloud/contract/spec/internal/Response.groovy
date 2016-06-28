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

/**
 * Represents the response side of the HTTP communication
 *
 * @since 1.0.0
 */
@TypeChecked
@EqualsAndHashCode
@ToString(includePackage = false, includeFields = true)
class Response extends Common {

	DslProperty status
	DslProperty delay
	Headers headers
	Body body
	boolean async

	Response() {
	}

	Response(Response response) {
		this.status = response.status
		this.headers = response.headers
		this.body = response.body
	}

	void status(int status) {
		this.status = toDslProperty(status)
	}

	void status(DslProperty status) {
		this.status = toDslProperty(status)
	}

	void headers(@DelegatesTo(Headers) Closure closure) {
		this.headers = new Headers()
		closure.delegate = headers
		closure()
	}

	void body(Map<String, Object> body) {
		this.body = new Body(convertObjectsToDslProperties(body))
	}

	void body(List body) {
		this.body = new Body(convertObjectsToDslProperties(body))
	}

	void body(Object bodyAsValue) {
		this.body = new Body(bodyAsValue)
	}

	void fixedDelayMilliseconds(int timeInMilliseconds) {
		this.delay = toDslProperty(timeInMilliseconds)
	}

	void async() {
		this.async = true
	}

	void assertThatSidesMatch(OptionalProperty stubSide, Object testSide) {
		throw new IllegalStateException("Optional can be used only in the test side of the response!")
	}
}

@CompileStatic
@EqualsAndHashCode
@ToString(includePackage = false)
class ServerResponse extends Response {
	ServerResponse(Response request) {
		super(request)
	}
}

@CompileStatic
@EqualsAndHashCode
@ToString(includePackage = false)
class ClientResponse extends Response {
	ClientResponse(Response request) {
		super(request)
	}
}
