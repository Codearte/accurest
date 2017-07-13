package io.codearte.accurest.plugin.config

import groovy.transform.CompileStatic
import io.codearte.accurest.config.AccurestConfigProperties

@CompileStatic
class AccurestGenericGradleConfigProperties extends AccurestConfigProperties {

	protected void clonePropertiesFrom(AccurestGenericGradleConfigProperties configToCloneFrom) {
		//shallow copy which is enough to make workaround for overlapping outputs in tasks
		targetFramework = configToCloneFrom.targetFramework
		testMode = configToCloneFrom.testMode
		basePackageForTests = configToCloneFrom.basePackageForTests
		baseClassForTests = configToCloneFrom.baseClassForTests
		nameSuffixForTests = configToCloneFrom.nameSuffixForTests
		ruleClassForTests = configToCloneFrom.ruleClassForTests
		jsonAssertVersion = configToCloneFrom.jsonAssertVersion
		excludedFiles = configToCloneFrom.excludedFiles
		ignoredFiles = configToCloneFrom.ignoredFiles
		imports = configToCloneFrom.imports
		staticImports = configToCloneFrom.staticImports
		contractsDslDir = configToCloneFrom.contractsDslDir
		generatedTestSourcesDir = configToCloneFrom.generatedTestSourcesDir
		stubsOutputDir = configToCloneFrom.stubsOutputDir
	}
}
