package io.codearte.accurest.builder

import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.dsl.internal.DslProperty
import io.codearte.accurest.dsl.internal.ExecutionProperty
import io.codearte.accurest.util.ContentType

import java.util.regex.Pattern

import static io.codearte.accurest.util.ContentUtils.extractValue

/**
 * @author Jakub Kubrynski
 * @author Olga Maciaszek-Sharma
 */
@PackageScope
@TypeChecked
abstract class JUnitMethodBodyBuilder extends MethodBodyBuilder {

	private List<GString> assertions = new ArrayList<>()

	JUnitMethodBodyBuilder(GroovyDsl stubDefinition) {
		super(stubDefinition)
	}

	@Override
	protected void thenBlock(BlockBuilder bb) {
		thenBlock(bb, '// then:')
	}

	@Override
	protected void whenBlock(BlockBuilder bb) {
		whenBlock(bb, '// when:')
	}

	@Override
	protected void givenBlock(BlockBuilder bb) {
		givenBlock(bb, '// given:')
	}

	@Override
	protected void then(BlockBuilder bb) {
		then(bb, '// and:')
	}


	protected void validateResponseBodyBlock(BlockBuilder bb) {
		def responseBody = response.body.serverValue
		ContentType contentType = getResponseContentType()
		if (responseBody instanceof GString) {
			responseBody = extractValue(responseBody, contentType, { DslProperty dslProperty -> dslProperty.serverValue })
		}
		if (contentType == ContentType.JSON) {
			processBodyElement(assertions, "", responseBody)
			bb.addLine("Object responseBody = new JsonSlurper().parseText($responseAsString);")
			bb.addLines(new AssertionBodyTypeDecorator(assertions).decorate() as List<GString>)
		} else if (contentType == ContentType.XML) {
			bb.addLine("Object responseBody = new XmlSlurper().parseText($responseAsString);")
			// TODO xml validation
		} else {
			bb.addLine("String responseBody = ($responseAsString);")
			processBodyElement(assertions, "", responseBody)
			bb.addLines(assertions)
		}
	}

	protected void processBodyElement(List<GString> assertions, String property, Map.Entry entry) {
		processBodyElement(assertions, property + """.get("$entry.key")""", entry.value)
	}

	protected void processBodyElement(List<GString> assertions, String property, Pattern pattern) {
		assertions.add("""assertThat(responseBody$property).matches("${pattern.pattern()}");""")
	}

	protected void processBodyElement(List<GString> assertions, String property, Object value) {
		assertions.add("assertThat(responseBody${property}).isEqualTo(\"${value}\");")
	}

	protected void processBodyElement(List<GString> assertions, String property, String value) {
		if (value.startsWith('$')) {
			value = value.substring(1).replaceAll('\\$value', "responseBody$property")
			assertions.add(value as GString)
		} else {
			assertions.add("assertThat(responseBody${property}).isEqualTo(\"${value}\");")
		}
	}

	protected void processBodyElement(List<GString> assertions, String property, DslProperty dslProperty) {
		processBodyElement(assertions, property, dslProperty.serverValue)
	}

	protected void processBodyElement(List<GString> assertions, String property, ExecutionProperty exec) {
		assertions.add("${exec.insertValue("responseBody$property")}")
	}

	protected void processBodyElement(List<GString> assertions, String property, Map map) {
		map.each {
			processBodyElement(assertions, property, it)
		}
	}

	protected void processBodyElement(List<GString> assertions, String property, List list) {
		list.eachWithIndex { listElement, listIndex ->
			String prop = "${property}.get($listIndex)" ?: ''
			processBodyElement(assertions, prop, listElement)
		}
	}
}
