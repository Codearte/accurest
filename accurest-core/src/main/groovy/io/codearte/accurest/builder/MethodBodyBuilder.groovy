package io.codearte.accurest.builder

import groovy.json.JsonOutput
import groovy.json.StringEscapeUtils
import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.dsl.internal.DslProperty
import io.codearte.accurest.dsl.internal.MatchingStrategy
import io.codearte.accurest.dsl.internal.QueryParameter
import io.codearte.accurest.dsl.internal.Request
import io.codearte.accurest.dsl.internal.Response
import io.codearte.accurest.dsl.internal.Url
import io.codearte.accurest.util.ContentType
import io.codearte.accurest.util.MapConverter
import io.codearte.accurest.util.jsonpath.JsonPaths

import static io.codearte.accurest.util.ContentUtils.extractValue
import static io.codearte.accurest.util.ContentUtils.recognizeContentTypeFromContent
import static io.codearte.accurest.util.ContentUtils.recognizeContentTypeFromHeader

/**
 * @author Olga Maciaszek-Sharma
 * @since 2015-08-07
 */
@PackageScope
@TypeChecked
abstract class MethodBodyBuilder {

	protected final Request request
	protected final Response response

	MethodBodyBuilder(GroovyDsl stubDefinition) {
		this.request = stubDefinition.request
		this.response = stubDefinition.response
	}

	protected void given(BlockBuilder bb) {}

	protected abstract void when(BlockBuilder bb)

	protected abstract void validateResponseCodeBlock(BlockBuilder bb)

	protected abstract void validateResponseHeadersBlock(BlockBuilder bb)

	protected abstract String getResponseAsString()

	protected abstract void givenBlock(BlockBuilder bb)

	protected abstract void whenBlock(BlockBuilder bb)

	protected abstract void thenBlock(BlockBuilder bb)

	protected abstract void then(BlockBuilder bb)

	protected abstract void appendJsonPath(BlockBuilder bb, String responseAsString)

	protected abstract void processBodyElement(BlockBuilder bb, String property, Object value)

	protected abstract void processText(BlockBuilder bb, String property, String value)

	protected abstract JsonPaths transformToJsonPathWithTestSideValues(Object responseBody);

	protected abstract String getParsedXmlResponseBodyString(String response);

	protected abstract String getTextResponseBodyString(String response);

	protected void then(BlockBuilder bb, String label) {
		validateResponseCodeBlock(bb)
		if (response.headers) {
			validateResponseHeadersBlock(bb)
		}
		if (response.body) {
			bb.endBlock()
			bb.addLine(label).startBlock()
			validateResponseBodyBlock(bb)
		}
	}

	protected void thenBlock(BlockBuilder bb, String label) {
		bb.addLine(label)
		bb.startBlock()
		then(bb)
		bb.endBlock()
	}

	protected void whenBlock(BlockBuilder bb, String label) {
		bb.addLine(label)
		bb.startBlock()
		when(bb)
		bb.endBlock().addEmptyLine()
	}

	protected void givenBlock(BlockBuilder bb, String label) {
		bb.addLine(label)
		bb.startBlock()
		given(bb)
		bb.endBlock().addEmptyLine()
	}

	void appendTo(BlockBuilder blockBuilder) {
		blockBuilder.startBlock()

		givenBlock(blockBuilder)
		whenBlock(blockBuilder)
		thenBlock(blockBuilder)

		blockBuilder.endBlock()
	}

	protected ContentType getResponseContentType() {
		ContentType contentType = recognizeContentTypeFromHeader(response.headers)
		if (contentType == ContentType.UNKNOWN) {
			contentType = recognizeContentTypeFromContent(response.body.serverValue)
		}
		return contentType
	}

	protected ContentType getRequestContentType() {
		ContentType contentType = recognizeContentTypeFromHeader(request.headers)
		if (contentType == ContentType.UNKNOWN) {
			contentType = recognizeContentTypeFromContent(request.body.serverValue)
		}
		return contentType
	}

	private void validateResponseBodyBlock(BlockBuilder bb) {
		def responseBody = response.body.serverValue
		ContentType contentType = getResponseContentType()
		if (responseBody instanceof GString) {
			responseBody = extractValue(responseBody, contentType, { DslProperty dslProperty -> dslProperty.serverValue })
		}
		if (contentType == ContentType.JSON) {
			appendJsonPath(bb, responseAsString)
			JsonPaths jsonPaths = transformToJsonPathWithTestSideValues(responseBody)
			jsonPaths.each {
				it.buildJsonPathComparison('parsedJson').each {
					bb.addLine(it)
				}
			}
			processBodyElement(bb, "", responseBody)
		} else if (contentType == ContentType.XML) {
			bb.addLine(getParsedXmlResponseBodyString(responseAsString))
			// TODO xml validation
		} else {
			bb.addLine(getTextResponseBodyString(responseAsString))
			processText(bb, "", responseBody as String)
		}
	}

  protected String buildUrl(Request request) {
		if (request.url)
			return getTestSideValue(buildUrlFromUrlPath(request.url))
		if (request.urlPath)
			return getTestSideValue(buildUrlFromUrlPath(request.urlPath))
		throw new IllegalStateException("URL is not set!")
	}

	protected String getTestSideValue(Object object) {
		return MapConverter.getTestSideValues(object).toString()
	}

	@TypeChecked(TypeCheckingMode.SKIP)
	protected String buildUrlFromUrlPath(Url url) {
		if (hasQueryParams(url)) {
			String params = url.queryParameters.parameters
					.findAll(this.&allowedQueryParameter)
					.inject([] as List<String>) { List<String> result, QueryParameter param ->
				result << "${param.name}=${resolveParamValue(param).toString()}"
			}
			.join('&')
			return "${MapConverter.getTestSideValues(url.serverValue)}?$params"
		}
		return MapConverter.getTestSideValues(url.serverValue)
	}

	private boolean hasQueryParams(Url url) {
		return url.queryParameters
	}

	protected String getBodyAsString() {
		Object bodyValue = extractServerValueFromBody(request.body.serverValue)
		String json = new JsonOutput().toJson(bodyValue)
		json = convertUnicodeEscapes(json)
		return trimRepeatedQuotes(json)
	}

	private String convertUnicodeEscapes(String json) {
		return StringEscapeUtils.unescapeJavaScript(json)
	}

	private String trimRepeatedQuotes(String toTrim) {
		return toTrim.startsWith('"') ? toTrim.replaceAll('"', '') : toTrim
	}

	private Object extractServerValueFromBody(bodyValue) {
		if (bodyValue instanceof GString) {
			bodyValue = extractValue(bodyValue, { DslProperty dslProperty -> dslProperty.serverValue })
		} else {
			bodyValue = MapConverter.transformValues(bodyValue, { it instanceof DslProperty ? it.serverValue : it })
		}
		return bodyValue
	}

	protected boolean allowedQueryParameter(QueryParameter param) {
		return allowedQueryParameter(param.serverValue)
	}

	protected boolean allowedQueryParameter(MatchingStrategy matchingStrategy) {
		return matchingStrategy.type != MatchingStrategy.Type.ABSENT
	}

	protected boolean allowedQueryParameter(Object o) {
		return true
	}

	protected String resolveParamValue(QueryParameter param) {
		return resolveParamValue(param.serverValue)
	}

	protected String resolveParamValue(Object value) {
		return value.toString()
	}

	protected String resolveParamValue(MatchingStrategy matchingStrategy) {
		return matchingStrategy.serverValue.toString()
	}
}
