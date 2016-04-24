package io.codearte.accurest.util

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
/**
 * Creates Groovy Shell with default import of GroovyDsl
 *
 * @author Marcin Grzejszczak
 */
class GroovyShellBuilder {

	static GroovyShell build() {
		return build(this.class.classLoader)
	}

	static GroovyShell build(ClassLoader classLoader) {
		CompilerConfiguration compilerConfiguration = new CompilerConfiguration(sourceEncoding:'UTF-8')
		ImportCustomizer importCustomizer = new ImportCustomizer()
		importCustomizer.addImports('io.codearte.accurest.dsl.GroovyDsl')
		compilerConfiguration.addCompilationCustomizers(importCustomizer)
		return new GroovyShell(classLoader, new Binding(), compilerConfiguration)
	}
}
