package io.codearte.accurest.plugin.config

import groovy.transform.CompileStatic
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory

@CompileStatic
class AccurestClientStubsTaskConfigProperties extends AccurestGenericGradleConfigProperties {

	@Override
	@Input
	List<String> getExcludedFiles() {
		return super.getExcludedFiles()
	}

	@Override
	@Input
	List<String> getIgnoredFiles() {
		return super.getIgnoredFiles()
	}

	@Override
	@InputDirectory
	File getContractsDslDir() {
		return super.getContractsDslDir()
	}

	@Override
	@OutputDirectory
	File getStubsOutputDir() {
		return super.getStubsOutputDir()
	}

	static AccurestClientStubsTaskConfigProperties fromGenericConfig(AccurestGenericGradleConfigProperties configToCloneFrom) {
		return new AccurestClientStubsTaskConfigProperties().with {
			it.clonePropertiesFrom(configToCloneFrom)
			return it
		}
	}
}
