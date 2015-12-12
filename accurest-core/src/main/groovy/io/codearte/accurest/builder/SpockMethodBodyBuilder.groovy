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
 */
@PackageScope
@TypeChecked
abstract class SpockMethodBodyBuilder extends MethodBodyBuilder {

	SpockMethodBodyBuilder(GroovyDsl stubDefinition) {
		super(stubDefinition)
	}

	@Override
	protected void thenBlock(BlockBuilder bb) {
		thenBlock(bb, 'then:')
	}

	@Override
	protected void whenBlock(BlockBuilder bb) {
		whenBlock(bb, 'when:')
	}

	@Override
	protected void givenBlock(BlockBuilder bb) {
		givenBlock(bb, 'given:')
	}

	@Override
	protected void then(BlockBuilder bb) {
		then(bb, 'and:')
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
			bb.addLine("def responseBody = new XmlSlurper().parseText($responseAsString)")
			// TODO xml validation
		} else {
			bb.addLine("def responseBody = ($responseAsString)")
			processText(bb, "", responseBody as String)
		}
	}

	protected void processText(BlockBuilder blockBuilder, String property, String value) {
		if (value.startsWith('$')) {
			value = value.substring(1).replaceAll('\\$value', "responseBody$property")
			blockBuilder.addLine(value)
		} else {
			blockBuilder.addLine("responseBody$property == \"${value}\"")
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

	protected void processBodyElement(BlockBuilder blockBuilder, String property, Object value) {

	}

	protected void appendJsonPath(BlockBuilder blockBuilder, String json) {
		blockBuilder.addLine("DocumentContext parsedJson = JsonPath.parse($json)")
	}

	protected void processBodyElement(BlockBuilder blockBuilder, String property, ExecutionProperty exec) {
		blockBuilder.addLine("${exec.insertValue("parsedJson.read('\\\$$property')")}")
	}

	protected void processBodyElement(BlockBuilder blockBuilder, String property, Map.Entry entry) {
		processBodyElement(blockBuilder, property + "." + entry.key, entry.value)
	}

	protected void processBodyElement(BlockBuilder blockBuilder, String property, Map map) {
		map.each {
			processBodyElement(blockBuilder, property, it)
		}
	}

	protected void processBodyElement(BlockBuilder blockBuilder, String property, List list) {
		list.eachWithIndex { listElement, listIndex ->
			String prop = "$property[$listIndex]" ?: ''
			processBodyElement(blockBuilder, prop, listElement)
		}
	}
}
