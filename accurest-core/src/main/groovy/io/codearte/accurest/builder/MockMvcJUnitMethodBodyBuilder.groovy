package io.codearte.accurest.builder

import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.dsl.internal.Header

import java.util.regex.Pattern

/**
 * @author Olga Maciaszek-Sharma
 * @since 2015-08-07
 */
class MockMvcJUnitMethodBodyBuilder extends JUnitMethodBodyBuilder {

	MockMvcJUnitMethodBodyBuilder(GroovyDsl stubDefinition) {
		super(stubDefinition)
	}

	@Override
	protected void given(BlockBuilder bb) {
		bb.addLine('MockMvcRequestSpecification request = given();')
		bb.indent()
		request.headers?.collect { Header header ->
			bb.addLine(".header('${header.name}', '${header.serverValue}')")
		}
		if (request.body) {
			bb.addLine(".body('$bodyAsString')")
		}
		bb.unindent()
	}

	@Override
	protected void when(BlockBuilder bb) {
		bb.addLine('ResponseOptions response = given().spec(request)')
		bb.indent()

		String url = buildUrl(request)
		String method = request.method.serverValue.toString().toLowerCase()

		bb.addLine(/.${method}("$url");/)
		bb.unindent()
	}


	@Override
	protected void validateResponseCodeBlock(BlockBuilder bb) {
		bb.addLine("assertTrue(response.statusCode.equals($response.status.serverValue));")
	}

	@Override
	protected void validateResponseHeadersBlock(BlockBuilder bb) {
		response.headers?.collect { Header header ->
			bb.addLine(createHeader(header.name, header.serverValue))
		}
	}

	private String createHeader(String headerName, Object headerValue) {
		return "assertTrue(response.header(\"$headerName\").equals(\"$headerValue\"))"
	}

	private String createHeader(String headerName, Pattern headerValue) {
		return "assertTrue(java.util.regex.Pattern.matches(java.util.regex.Pattern.compile(\"$headerValue\"), response.header(\"$headerName\"))"
	}

	@Override
	protected String getResponseAsString() {
		return 'response.getBody().asString()'
	}

}
