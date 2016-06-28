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

package org.springframework.cloud.contract.stubrunner.junit;

import java.io.InputStream;
import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.assertj.core.api.BDDAssertions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import static org.assertj.core.api.BDDAssertions.then;

/**
 * @author Marcin Grzejszczak
 */
public class StubRunnerRuleJUnitTest {

	@BeforeClass
	@AfterClass
	public static void setupProps() {
			System.getProperties().setProperty("stubrunner.stubs.repository.root", "");
			System.getProperties().setProperty("stubrunner.stubs.classifier", "stubs");
	}

	// tag::classrule[]
	@ClassRule public static StubRunnerRule rule = new StubRunnerRule()
			.repoRoot(repoRoot())
			.downloadStub("org.springframework.cloud.contract.spec.stubs", "loanIssuance")
			.downloadStub("org.springframework.cloud.contract.spec.stubs:fraudDetectionServer");
	// end::classrule[]

	// tag::test[]
	@Test
	public void should_start_wiremock_servers() throws Exception {
		// expect: 'WireMocks are running'
			then(rule.findStubUrl("org.springframework.cloud.contract.spec.stubs", "loanIssuance")).isNotNull();
			then(rule.findStubUrl("loanIssuance")).isNotNull();
			then(rule.findStubUrl("loanIssuance")).isEqualTo(rule.findStubUrl("org.springframework.cloud.contract.spec.stubs", "loanIssuance"));
			then(rule.findStubUrl("org.springframework.cloud.contract.spec.stubs:fraudDetectionServer")).isNotNull();
		// and:
			BDDAssertions.then(rule.findAllRunningStubs().isPresent("loanIssuance")).isTrue();
			BDDAssertions.then(rule.findAllRunningStubs().isPresent("org.springframework.cloud.contract.spec.stubs", "fraudDetectionServer")).isTrue();
			BDDAssertions.then(rule.findAllRunningStubs().isPresent("org.springframework.cloud.contract.spec.stubs:fraudDetectionServer")).isTrue();
		// and: 'Stubs were registered'
			then(httpGet(rule.findStubUrl("loanIssuance").toString() + "/name")).isEqualTo("loanIssuance");
			then(httpGet(rule.findStubUrl("fraudDetectionServer").toString() + "/name")).isEqualTo("fraudDetectionServer");
	}
	// end::test[]

	private static String repoRoot()  {
		try {
			return StubRunnerRuleJUnitTest.class.getResource("/m2repo/repository/").toURI().toString();
		} catch (Exception e) {
			return "";
		}
	}

	private String httpGet(String url) throws Exception {
		try(InputStream stream = URI.create(url).toURL().openStream()) {
			return IOUtils.toString(stream);
		}
	}
}
