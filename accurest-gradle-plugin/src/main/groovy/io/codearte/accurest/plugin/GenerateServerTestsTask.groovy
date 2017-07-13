package io.codearte.accurest.plugin

import io.codearte.accurest.AccurestException
import io.codearte.accurest.TestGenerator
import io.codearte.accurest.plugin.config.AccurestServerTestsTaskConfigProperties
import org.gradle.api.GradleException
import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction

@CacheableTask
class GenerateServerTestsTask extends ConventionTask {

	@Nested
	AccurestServerTestsTaskConfigProperties configProperties

	@TaskAction
	void generate() {
		project.logger.info("Accurest Plugin: Invoking test sources generation")

		project.sourceSets.test.groovy {
			project.logger.info("Registering ${getConfigProperties().generatedTestSourcesDir} as test source directory")
			srcDir getConfigProperties().generatedTestSourcesDir
		}

		try {
			//TODO: What with that? How to pass?
			TestGenerator generator = new TestGenerator(getConfigProperties())
			int generatedClasses = generator.generate()
			project.logger.info("Generated {} test classes", generatedClasses)
		} catch (AccurestException e) {
			throw new GradleException("Accurest Plugin exception: ${e.message}", e)
		}
	}
}
