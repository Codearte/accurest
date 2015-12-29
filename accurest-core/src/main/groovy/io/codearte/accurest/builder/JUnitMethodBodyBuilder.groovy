package io.codearte.accurest.builder

import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.dsl.internal.DslProperty
import io.codearte.accurest.dsl.internal.ExecutionProperty
import io.codearte.accurest.dsl.internal.NamedProperty
import io.codearte.accurest.util.ContentType
import io.codearte.accurest.util.JsonPaths
import io.codearte.accurest.util.JsonToJsonPathsConverter

import static io.codearte.accurest.util.ContentUtils.extractValue
import static io.codearte.accurest.util.ContentUtils.getMultipartFileParameterContent

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
			appendJsonPath(bb, responseAsString)
			JsonPaths jsonPaths = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(responseBody)
			jsonPaths.each {
				it.buildJsonPathComparison('parsedJson').each {
					bb.addLine(it)
				}
			}
			processBodyElement(bb, "", responseBody)
		} else if (contentType == ContentType.XML) {
			bb.addLine("Object responseBody = new XmlSlurper().parseText($responseAsString);")
			// TODO xml validation
		} else {
			bb.addLine("Object responseBody = ($responseAsString);")
			processText(bb, "", responseBody as String)
		}
	}

	@Override
	protected void processText(BlockBuilder blockBuilder, String property, String value) {
		if (value.startsWith('$')) {
			value = value.substring(1).replaceAll('\\$value', "responseBody$property")
			blockBuilder.addLine(value)
		} else {
			blockBuilder.addLine("assertThat(responseBody${property}).isEqualTo(\"${value}\");")
		}
	}

	protected Map<String, Object> getMultipartParameters() {
		return (Map<String, Object>) request?.multipart?.serverValue
	}

	protected String getMultipartParameterLine(Map.Entry<String, Object> parameter) {
		if (parameter.value instanceof NamedProperty) {
			return ".multiPart(${getMultipartFileParameterContent(parameter.key, (NamedProperty) parameter.value)})"
		}
		return ".param('$parameter.key', '$parameter.value')"
	}

	@Override
	protected void processBodyElement(BlockBuilder blockBuilder, String property, Object value) {

	}

	@Override
	protected void appendJsonPath(BlockBuilder blockBuilder, String json) {
		blockBuilder.addLine("DocumentContext parsedJson = JsonPath.parse($json);")
	}

	protected void processBodyElement(BlockBuilder blockBuilder, String property, ExecutionProperty exec) {
		blockBuilder.addLine("${exec.insertValue("parsedJson.read('\\\$$property')")};")
	}

	protected void processBodyElement(BlockBuilder blockBuilder, String property, Map.Entry entry) {
		processBodyElement(blockBuilder, property + """.get("$entry.key")""", entry.value)
	}

	protected void processBodyElement(BlockBuilder blockBuilder, String property, Map map) {
		map.each {
			processBodyElement(blockBuilder, property, it)
		}
	}

	protected void processBodyElement(BlockBuilder blockBuilder, String property, List list) {
		list.eachWithIndex { listElement, listIndex ->
			String prop = "${property}.get($listIndex)" ?: ''
			processBodyElement(blockBuilder, prop, listElement)
		}
	}
	////////
//	protected void processBodyElement(List<GString> assertions, String property, Map.Entry entry) {
//		processBodyElement(assertions, property + """.get("$entry.key")""", entry.value)
//	}
//
//	protected void processBodyElement(List<GString> assertions, String property, Pattern pattern) {
//		assertions.add("""assertThat(responseBody$property).matches("${pattern.pattern()}");""")
//	}
//
//	protected void processBodyElement(List<GString> assertions, String property, Object value) {
//		assertions.add("assertThat(responseBody${property}).isEqualTo(\"${value}\");")
//	}
//
//	}
//
//	protected void processBodyElement(List<GString> assertions, String property, String value) {
//		if (value.startsWith('$')) {
//			value = value.substring(1).replaceAll('\\$value', "responseBody$property")
//			assertions.add(value as GString)
//		} else {
//			assertions.add("assertThat(responseBody${property}).isEqualTo(\"${value}\");")
//		}
//	}
//
//	protected void processBodyElement(List<GString> assertions, String property, DslProperty dslProperty) {
//		processBodyElement(assertions, property, dslProperty.serverValue)
//	}
//
//	protected void processBodyElement(List<GString> assertions, String property, ExecutionProperty exec) {
//		assertions.add("${exec.insertValue("responseBody$property")}")
//	}
//
//	protected void processBodyElement(List<GString> assertions, String property, Map map) {
//		map.each {
//			processBodyElement(assertions, property, it)
//		}
//	}
//
//	//////////////////////////
//	protected void processBodyElement(List<GString> assertions, String property, List list) {
//		list.eachWithIndex { listElement, listIndex ->
//			String prop = "${property}.get($listIndex)" ?: ''
//			processBodyElement(assertions, prop, listElement)
//		}
//	}
}
