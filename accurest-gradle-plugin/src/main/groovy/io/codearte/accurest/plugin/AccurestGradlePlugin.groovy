package io.codearte.accurest.plugin

import io.codearte.accurest.plugin.config.AccurestGradleConfigProperties
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
/**
 * @author Jakub Kubrynski
 */
class AccurestGradlePlugin implements Plugin<Project> {

	private static final String GENERATE_SERVER_TESTS_TASK_NAME = 'generateAccurest'
	private static final String DSL_TO_WIREMOCK_CLIENT_TASK_NAME = 'generateWireMockClientStubs'

	private static final Class IDEA_PLUGIN_CLASS = org.gradle.plugins.ide.idea.IdeaPlugin
	private static final String GROUP_NAME = "Verification"

	private Project project

	@Override
	void apply(Project project) {
		this.project = project
		AccurestGradleConfigProperties extension = project.extensions.create('accurest', AccurestGradleConfigProperties)

		project.check.dependsOn(GENERATE_SERVER_TESTS_TASK_NAME)

		setConfigurationDefaults(extension)
		createGenerateTestsTask(extension)
		createAndConfigureGenerateWireMockClientStubsFromDslTask(extension)
		deprecatedCreateAndConfigureGenerateWiremockClientStubsFromDslTask()
		project.dependencies.add("testCompile", "com.github.tomakehurst:wiremock:2.0.10-beta")
		project.dependencies.add("testCompile", "com.toomuchcoding.jsonassert:jsonassert:${extension.getJsonAssertVersion()}")
		project.dependencies.add("testCompile", "org.assertj:assertj-core:2.3.0")

		project.afterEvaluate {
			def hasIdea = project.plugins.findPlugin(IDEA_PLUGIN_CLASS)
			if (hasIdea) {
				project.idea {
					module {
						testSourceDirs += extension.generatedTestSourcesDir
						testSourceDirs += extension.contractsDslDir
					}
				}
			}
		}
	}

	void setConfigurationDefaults(AccurestGradleConfigProperties extension) {
		extension.with {
			generatedTestSourcesDir = project.file("${project.buildDir}/generated-test-sources/accurest")
			contractsDslDir = defaultAccurestContractsDir() //TODO: Use sourceset
			basePackageForTests = 'io.codearte.accurest.tests'
		}
	}

	private File defaultAccurestContractsDir() {
		project.file("${project.rootDir}/src/test/resources/accurest")
	}

	private void createGenerateTestsTask(AccurestGradleConfigProperties extension) {
		Task task = project.tasks.create(GENERATE_SERVER_TESTS_TASK_NAME, GenerateServerTestsTask)
		task.description = "Generate server tests from GroovyDSL"
		task.group = GROUP_NAME
		task.conventionMapping.with {
			configProperties = { extension }
		}
	}

	private void createAndConfigureGenerateWireMockClientStubsFromDslTask(AccurestGradleConfigProperties extension) {
		Task task = project.tasks.create(DSL_TO_WIREMOCK_CLIENT_TASK_NAME, GenerateWireMockClientStubsFromDslTask)
		task.description = "Generate WireMock client stubs from GroovyDSL"
		task.group = GROUP_NAME
		task.conventionMapping.with {
			configProperties = { extension }
		}
	}

	private void deprecatedCreateAndConfigureGenerateWiremockClientStubsFromDslTask() {
		Task task = project.tasks.create('generateWiremockClientStubs')
		task.dependsOn('generateWireMockClientStubs')
		task.description = "DEPRECATED - Generates WireMock client stubs. - DEPRECATED - use 'generateWireMockClientStubs' task"
		task.group = GROUP_NAME
		task.doFirst {logger.warn("DEPRECATION WARNING. Task 'generateWiremockClientStubs' is deprecated. Use 'generateWireMockClientStubs' task instead.")}
	}
}
