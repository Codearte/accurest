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


	JUnitMethodBodyBuilder(GroovyDsl stubDefinition) {
		super(stubDefinition)
	}

	@Override
	protected void thenBlock(BlockBuilder bb) {
		bb.addLine('// then:')
		bb.startBlock()
		then(bb)
		bb.endBlock().addEmptyLine()
	}

	@Override
	protected void whenBlock(BlockBuilder bb) {
		bb.addLine('// when:')
		bb.startBlock()
		when(bb)
		bb.endBlock().addEmptyLine()
	}

	@Override
	protected void givenBlock(BlockBuilder bb) {
		bb.addLine('// given:')
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
					.startBlock()
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
			bb.addLine("Map responseBody = (Map) new JsonSlurper().parseText($responseAsString)")
			processBodyElement(bb, "", responseBody)
		} else if (contentType == ContentType.XML) {
			bb.addLine("Map responseBody = (Map) new XmlSlurper().parseText($responseAsString)")
			// TODO xml validation
		} else {
			bb.addLine("String responseBody = ($responseAsString)")
			processBodyElement(bb, "", responseBody)
		}
	}

	@Override
	protected void processBodyElement(BlockBuilder blockBuilder, String property, Map.Entry entry) {
		processBodyElement(blockBuilder, property + """.get("$entry.key")""", entry.value)
	}

	@Override
	protected void processBodyElement(BlockBuilder blockBuilder, String property, Pattern pattern) {
		blockBuilder.addLine("""assertTrue(java.util.regex.Pattern.matches(java.util.regex.Pattern.compile("${pattern.pattern()}"), responseBody$property)""")
	}

	protected void processBodyElement(BlockBuilder blockBuilder, String property, Object value) {
		blockBuilder.addLine("assertTrue(responseBody${property}.equals(\"${value}\"))")
	}


	protected void processBodyElement(BlockBuilder blockBuilder, String property, String value) {
		if (value.startsWith('$')) {
			value = value.substring(1).replaceAll('\\$value', "responseBody$property")
			blockBuilder.addLine(value)
		} else {
			blockBuilder.addLine("assertTrue(responseBody${property}.equals(\"${value}\"))")
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

/*
	given()
	.contentType("application/json")
	.header("Accept", "application/json")
	.body("{'name': 'MyApp', 'description' : 'awesome app'}".replaceAll("'", "\"")).

	expect()
	.statusCode(200).body("id", is(not(nullValue()))).

	when()
	.post(root.toString() + "rest/applications").asString();*/
}
