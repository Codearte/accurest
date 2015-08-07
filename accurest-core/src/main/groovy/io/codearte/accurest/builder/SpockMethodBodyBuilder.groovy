package io.codearte.accurest.builder
import groovy.json.JsonOutput
import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.dsl.internal.DslProperty
import io.codearte.accurest.dsl.internal.ExecutionProperty
import io.codearte.accurest.dsl.internal.MatchingStrategy
import io.codearte.accurest.dsl.internal.QueryParameter
import io.codearte.accurest.dsl.internal.Request
import io.codearte.accurest.dsl.internal.Response
import io.codearte.accurest.util.ContentType
import io.codearte.accurest.util.JsonConverter

import java.util.regex.Pattern

import static io.codearte.accurest.util.ContentUtils.extractValue
import static io.codearte.accurest.util.ContentUtils.recognizeContentTypeFromContent
import static io.codearte.accurest.util.ContentUtils.recognizeContentTypeFromHeader

/**
 * @author Jakub Kubrynski
 */
@PackageScope
@TypeChecked
abstract class SpockMethodBodyBuilder extends MethodBodyBuilder{

	SpockMethodBodyBuilder(GroovyDsl stubDefinition) {
		super(stubDefinition)
	}

	@Override
	protected void thenBlock(BlockBuilder bb) {
		bb.addLine('then:')
		bb.startBlock()
		then(bb)
		bb.endBlock()
	}

	@Override
	protected void whenBlock(BlockBuilder bb) {
		bb.addLine('when:')
		bb.startBlock()
		when(bb)
		bb.endBlock().addEmptyLine()
	}

	@Override
	protected void givenBlock(BlockBuilder bb) {
		bb.addLine('given:')
		bb.startBlock()
		given(bb)
		bb.endBlock().addEmptyLine()
	}

	protected void then(BlockBuilder bb) {
		validateResponseCodeBlock(bb)
		if (response.headers) {
			validateResponseHeadersBlock(bb)
		}
		if (response.body) {
			bb.endBlock()
			bb.addLine('and:').startBlock()
			validateResponseBodyBlock(bb)
		}
	}

	protected void validateResponseBodyBlock(BlockBuilder bb) {
		def responseBody = response.body.serverValue
		ContentType contentType = getResponseContentType()
		if (responseBody instanceof GString) {
			responseBody = extractValue(responseBody, contentType, { DslProperty dslProperty -> dslProperty.serverValue })
		}
		if (contentType == ContentType.JSON) {
			bb.addLine("def responseBody = new JsonSlurper().parseText($responseAsString)")
			processBodyElement(bb, "", responseBody)
		} else if (contentType == ContentType.XML) {
			bb.addLine("def responseBody = new XmlSlurper().parseText($responseAsString)")
			// TODO xml validation
		}   else {
			bb.addLine("def responseBody = ($responseAsString)")
			processBodyElement(bb, "", responseBody)
		}
	}

	@Override
	protected void processBodyElement(BlockBuilder blockBuilder, String property, Map.Entry entry) {
		processBodyElement(blockBuilder, property + "." + entry.key, entry.value)
	}


	@Override
	protected void processBodyElement(BlockBuilder blockBuilder, String property, Pattern pattern) {
		blockBuilder.addLine("responseBody$property ==~ java.util.regex.Pattern.compile('${pattern.pattern()}')")
	}


	protected void processBodyElement(BlockBuilder blockBuilder, String property, Object value) {
		blockBuilder.addLine("responseBody$property == ${value}")
	}


	protected void processBodyElement(BlockBuilder blockBuilder, String property, String value) {
		if (value.startsWith('$')) {
			value = value.substring(1).replaceAll('\\$value', "responseBody$property")
			blockBuilder.addLine(value)
		} else {
			blockBuilder.addLine("responseBody$property == \"${value}\"")
		}
	}

	protected void processBodyElement(BlockBuilder blockBuilder, String property, DslProperty dslProperty) {
		processBodyElement(blockBuilder, property, dslProperty.serverValue)
	}

	protected void processBodyElement(BlockBuilder blockBuilder, String property, ExecutionProperty exec) {
		blockBuilder.addLine("${exec.insertValue("responseBody$property")}")
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
