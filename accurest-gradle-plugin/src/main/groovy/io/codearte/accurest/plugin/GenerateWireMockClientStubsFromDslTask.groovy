package io.codearte.accurest.plugin

import io.codearte.accurest.config.AccurestConfigProperties
import io.codearte.accurest.wiremock.DslToWireMockClientConverter
import io.codearte.accurest.wiremock.RecursiveFilesConverter
import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

//TODO: Implement as an incremental task: https://gradle.org/docs/current/userguide/custom_tasks.html#incremental_tasks ?
@CacheableTask
class GenerateWireMockClientStubsFromDslTask extends ConventionTask {

	@InputDirectory
	File contractsDslDir
	@OutputDirectory
	File stubsOutputDir

	AccurestConfigProperties configProperties

	@TaskAction
	void generate() {
		logger.info("Accurest Plugin: Invoking GroovyDSL to WireMock client stubs conversion")
		logger.debug("From '${getContractsDslDir()}' to '${getStubsOutputDir()}'")
		RecursiveFilesConverter converter = new RecursiveFilesConverter(new DslToWireMockClientConverter(), getConfigProperties())
		converter.processFiles()
	}
}
