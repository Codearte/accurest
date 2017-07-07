package io.codearte.accurest.plugin.config

import groovy.transform.CompileStatic
import io.codearte.accurest.config.AccurestConfigProperties
import io.codearte.accurest.config.TestFramework
import io.codearte.accurest.config.TestMode
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory

@CompileStatic
class AccurestGradleConfigProperties extends AccurestConfigProperties {

	@Override
	@Input
	TestFramework getTargetFramework() {
		return super.getTargetFramework()
	}

	@Override
	@Input
	TestMode getTestMode() {
		return super.getTestMode()
	}

	@Override
	@Input
	String getBasePackageForTests() {
		return super.getBasePackageForTests()
	}

	@Override
	@Input
	@Optional
	String getBaseClassForTests() {
		return super.getBaseClassForTests()
	}

	@Override
	@Input
	@Optional
	String getNameSuffixForTests() {
		return super.getNameSuffixForTests()
	}

	@Override
	@Input
	@Optional
	String getRuleClassForTests() {
		return super.getRuleClassForTests()
	}

	@Override
	@Input
	String getJsonAssertVersion() {
		return super.getJsonAssertVersion()
	}

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
	@Input
	String[] getImports() {
		return super.getImports()
	}

	@Override
	@Input
	String[] getStaticImports() {
		return super.getStaticImports()
	}

	@Override
	@InputDirectory
	File getContractsDslDir() {
		return super.getContractsDslDir()
	}

	@Override
	@OutputDirectory
	File getGeneratedTestSourcesDir() {
		return super.getGeneratedTestSourcesDir()
	}

	@Override
	@OutputDirectory
	File getStubsOutputDir() {
		return super.getStubsOutputDir()
	}
}
