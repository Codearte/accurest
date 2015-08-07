package io.codearte.accurest.builder

import groovy.json.JsonOutput
import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.dsl.internal.DslProperty
import io.codearte.accurest.dsl.internal.ExecutionProperty
import io.codearte.accurest.dsl.internal.MatchingStrategy
import io.codearte.accurest.dsl.internal.QueryParameter
import io.codearte.accurest.dsl.internal.Request
import io.codearte.accurest.dsl.internal.Response
import io.codearte.accurest.dsl.internal.UrlPath
import io.codearte.accurest.util.ContentType
import io.codearte.accurest.util.JsonConverter

import java.util.regex.Pattern

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

	protected abstract void processBodyElement(BlockBuilder blockBuilder, String property, Map.Entry entry)

	protected abstract void processBodyElement(BlockBuilder blockBuilder, String property, Pattern pattern)

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

	protected String buildUrl(Request request) {
		if (request.url)
			return request.url.serverValue;
		if (request.urlPath)
			return buildUrlFromUrlPath(request.urlPath)
		throw new IllegalStateException("URL is not set!")
	}


	@TypeChecked(TypeCheckingMode.SKIP)
	protected String buildUrlFromUrlPath(UrlPath urlPath) {
		String params = urlPath.queryParameters.parameters
				.findAll(this.&allowedQueryParameter)
				.inject([] as List<String>) { List<String> result, QueryParameter param ->
			result << "${param.name}=${resolveParamValue(param).toString()}"
		}
		.join('&')
		return "$urlPath.serverValue?$params"
	}

	protected String getBodyAsString() {
		Object bodyValue = extractServerValueFromBody(request.body.serverValue)
		return trimRepeatedQuotes(new JsonOutput().toJson(bodyValue))
	}


	protected String trimRepeatedQuotes(String toTrim) {
		return toTrim.startsWith('"') ? toTrim.replaceAll('"', '') : toTrim
	}

	protected Object extractServerValueFromBody(bodyValue) {
		if (bodyValue instanceof GString) {
			bodyValue = extractValue(bodyValue, { DslProperty dslProperty -> dslProperty.serverValue })
		} else {
			bodyValue = JsonConverter.transformValues(bodyValue, { it instanceof DslProperty ? it.serverValue : it })
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
