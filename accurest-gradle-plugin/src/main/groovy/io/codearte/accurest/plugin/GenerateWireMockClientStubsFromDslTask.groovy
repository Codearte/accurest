package io.codearte.accurest.plugin

import io.codearte.accurest.plugin.config.AccurestClientStubsTaskConfigProperties
import io.codearte.accurest.wiremock.DslToWireMockClientConverter
import io.codearte.accurest.wiremock.RecursiveFilesConverter
import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction

//TODO: Implement as an incremental task: https://gradle.org/docs/current/userguide/custom_tasks.html#incremental_tasks ?
@CacheableTask
class GenerateWireMockClientStubsFromDslTask extends ConventionTask {

	@Nested
	AccurestClientStubsTaskConfigProperties configProperties

	@TaskAction
	void generate() {
		logger.info("Accurest Plugin: Invoking GroovyDSL to WireMock client stubs conversion")
		logger.debug("From '${getConfigProperties().getContractsDslDir()}' to '${getConfigProperties().getStubsOutputDir()}'")
		RecursiveFilesConverter converter = new RecursiveFilesConverter(new DslToWireMockClientConverter(), getConfigProperties())
		converter.processFiles()
	}
}
