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

package org.springframework.cloud.contract.spec.builder

import groovy.transform.Canonical
import groovy.transform.EqualsAndHashCode
import groovy.transform.PackageScope
import groovy.util.logging.Slf4j
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.spec.config.TestFramework
import org.springframework.cloud.contract.spec.file.ContractMetadata
import org.springframework.cloud.contract.spec.util.ContractVerifierDslConverter
import org.springframework.cloud.contract.spec.config.TestMode

import static ClassBuilder.createClass
import static MethodBuilder.createTestMethod
import static org.springframework.cloud.contract.spec.util.NamesUtil.capitalize

/**
 * Builds a single test for the given {@link ContractVerifierConfigProperties properties}
 *
 * @since 1.0.0
 */
@Slf4j
class SingleTestGenerator {

	private static final String JSON_ASSERT_STATIC_IMPORT = 'com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson'
	private static final String JSON_ASSERT_CLASS = 'com.toomuchcoding.jsonassert.JsonAssertion'

	private final ContractVerifierConfigProperties configProperties

	SingleTestGenerator(ContractVerifierConfigProperties configProperties) {
		this.configProperties = configProperties
	}

	/**
	 * Returns String code representing a test class with test methods for
	 * each {@link ContractMetadata}
	 */
	@PackageScope
	String buildClass(Collection<ContractMetadata> listOfFiles, String className, String classPackage) {
		ClassBuilder clazz = createClass(capitalize(className), classPackage, configProperties)

		if (configProperties.imports) {
			configProperties.imports.each {
				clazz.addImport(it)
			}
		}

		if (listOfFiles.ignored.find { it }) {
			clazz.addImport(configProperties.targetFramework.getIgnoreClass())
		}

		if (configProperties.staticImports) {
			configProperties.staticImports.each {
				clazz.addStaticImport(it)
			}
		}

		if (isScenarioClass(listOfFiles)) {
			clazz.addImport(configProperties.targetFramework.getOrderAnnotationImport())
			clazz.addClassLevelAnnotation(configProperties.targetFramework.getOrderAnnotation())
		}

		addJsonPathRelatedImports(clazz)

		Map<ParsedDsl, TestType> contracts = mapContractsToTheirTestTypes(listOfFiles)

		boolean conditionalImportsAdded = false
		contracts.each { ParsedDsl key, TestType value ->
			if (!conditionalImportsAdded) {
				if (contracts.values().contains(TestType.HTTP)) {
					if (configProperties.testMode == TestMode.JAXRSCLIENT) {
						clazz.addStaticImport('javax.ws.rs.client.Entity.*')
						if (configProperties.targetFramework == TestFramework.JUNIT) {
							clazz.addImport('javax.ws.rs.core.Response')
						}
					} else if (configProperties.testMode == TestMode.MOCKMVC) {
						clazz.addStaticImport('com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.*')
					} else {
						clazz.addStaticImport('com.jayway.restassured.RestAssured.*')
					}
				}
				if (configProperties.targetFramework == TestFramework.JUNIT) {
					if (contracts.values().contains(TestType.HTTP) && configProperties.testMode == TestMode.MOCKMVC) {
						clazz.addImport('com.jayway.restassured.module.mockmvc.specification.MockMvcRequestSpecification')
						clazz.addImport('com.jayway.restassured.response.ResponseOptions')
					}
					clazz.addImport('org.junit.Test')
					clazz.addStaticImport('org.assertj.core.api.Assertions.assertThat')
				}
				if (configProperties.ruleClassForTests) {
					clazz.addImport('org.junit.Rule').addRule(configProperties.ruleClassForTests)
				}
				if (contracts.values().contains(TestType.MESSAGING)) {
					addMessagingRelatedEntries(clazz)
				}
				conditionalImportsAdded = true
			}
			clazz.addMethod(createTestMethod(key.contract, key.stubsFile, key.groovyDsl, configProperties))
		}
		return clazz.build()
	}

	private Map<ParsedDsl, TestType> mapContractsToTheirTestTypes(Collection<ContractMetadata> listOfFiles) {
		return listOfFiles.collectEntries {
			File stubsFile = it.path.toFile()
			log.debug("Stub content from file [${stubsFile.text}]")
			Contract stubContent = ContractVerifierDslConverter.convert(stubsFile)
			TestType testType = (stubContent.input || stubContent.outputMessage) ? TestType.MESSAGING : TestType.HTTP
			return [(new ParsedDsl(it, stubContent, stubsFile)): testType]
		}
	}

	@Canonical
	@EqualsAndHashCode
	private static class ParsedDsl {
		ContractMetadata contract
		Contract groovyDsl
		File stubsFile
	}

	private static enum TestType {
		MESSAGING, HTTP
	}

	private boolean isScenarioClass(Collection<ContractMetadata> listOfFiles) {
		listOfFiles.find({ it.order != null }) != null
	}

	private ClassBuilder addJsonPathRelatedImports(ClassBuilder clazz) {
		clazz.addImport(['com.jayway.jsonpath.DocumentContext',
		                 'com.jayway.jsonpath.JsonPath',
		])
		if (jsonAssertPresent()) {
			clazz.addStaticImport(JSON_ASSERT_STATIC_IMPORT)
		}
	}

	private ClassBuilder addMessagingRelatedEntries(ClassBuilder clazz) {
		clazz.addField(['@Inject ContractVerifierMessaging contractVerifierMessaging',
						'ContractVerifierObjectMapper contractVerifierObjectMapper = new ContractVerifierObjectMapper()'
		])
		clazz.addImport([ 'javax.inject.Inject',
						  'org.springframework.cloud.contract.spec.messaging.ContractVerifierObjectMapper',
						  'org.springframework.cloud.contract.spec.messaging.ContractVerifierMessage',
						  'org.springframework.cloud.contract.spec.messaging.ContractVerifierMessaging',
		])
		clazz.addStaticImport('org.springframework.cloud.contract.spec.messaging.ContractVerifierMessagingUtil.headers')
	}

	private static boolean jsonAssertPresent() {
		try {
			Class.forName(JSON_ASSERT_CLASS)
			return true
		} catch (ClassNotFoundException e) {
			log.debug("JsonAssert is not present on classpath. Will not add a static import.")
			return false
		}
	}

}
