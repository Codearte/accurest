package io.codearte.accurest.util.jsonpath

import java.util.regex.Pattern

class SpockJsonPathEntry extends JsonPathEntry {


	SpockJsonPathEntry(String jsonPath, String optionalSuffix, Object value) {
		super(jsonPath, optionalSuffix, value)
	}

	@Override
	List<String> buildJsonPathComparison(String parsedJsonVariable) {
		if (optionalSuffix) {
			return ["!${parsedJsonVariable}.read('''${jsonPath}''', JSONArray).empty"]
		} else if (traversesOverCollections()) {
			return ["${parsedJsonVariable}.read('''${jsonPath}''', JSONArray).get(0) ${operator()} ${potentiallyWrappedWithQuotesValue()}"]
		}
		return ["${parsedJsonVariable}.read('''${jsonPath}''') ${operator()} ${potentiallyWrappedWithQuotesValue()}"]
	}

	private String operator() {
		return value instanceof Pattern ? "==~" : "=="
	}

	private String potentiallyWrappedWithQuotesValue() {
		return value instanceof Number ? value : "'''$value'''"
	}

}
