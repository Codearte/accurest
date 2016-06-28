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

package org.springframework.cloud.contract.stubrunner.messaging.stream

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.toomuchcoding.jsonassert.JsonAssertion
import com.toomuchcoding.jsonassert.JsonVerifiable
import groovy.transform.CompileStatic
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.messaging.ContractVerifierObjectMapper
import org.springframework.cloud.contract.spec.util.JsonPaths
import org.springframework.cloud.contract.spec.util.JsonToJsonPathsConverter
import org.springframework.integration.core.MessageSelector
import org.springframework.messaging.Message

import java.util.regex.Pattern

/**
 * Passes through a message that matches the one defined in the DSL
 *
 * @author Marcin Grzejszczak
 */
@CompileStatic
class StubRunnerStreamMessageSelector implements MessageSelector {

	private final Contract groovyDsl
	private final ContractVerifierObjectMapper objectMapper = new ContractVerifierObjectMapper()

	StubRunnerStreamMessageSelector(Contract groovyDsl) {
		this.groovyDsl = groovyDsl
	}

	@Override
	boolean accept(Message<?> message) {
		if(!headersMatch(message)){
			return false
		}
		Object inputMessage = message.getPayload()
		JsonPaths jsonPaths = JsonToJsonPathsConverter.transformToJsonPathWithStubsSideValues(groovyDsl.input.messageBody)
		DocumentContext parsedJson = JsonPath.parse(objectMapper.writeValueAsString(inputMessage))
		return jsonPaths.every { matchesJsonPath(parsedJson, it) }
	}

	private boolean matchesJsonPath(DocumentContext parsedJson, JsonVerifiable jsonVerifiable) {
		try {
			JsonAssertion.assertThat(parsedJson).matchesJsonPath(jsonVerifiable.jsonPath())
			return true
		} catch (Exception e) {
			return false
		}
	}

	private boolean headersMatch(Message message) {
		Map<String, Object> headers = message.getHeaders()
		return groovyDsl.input.messageHeaders.entries.every {
			String name = it.name
			Object value = it.clientValue
			Object valueInHeader = headers.get(name)
			return value instanceof Pattern ?
					value.matcher(valueInHeader.toString()).matches() :
							valueInHeader == value
		}
	}
}
