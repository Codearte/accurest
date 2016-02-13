package io.codearte.accurest.util.jsonpath

import java.util.regex.Pattern

/**
 * @author Olga Maciaszek-Sharma 
 @since 29.12.15
 */
class JUnitJsonPathEntry extends JsonPathEntry {

	JUnitJsonPathEntry(String jsonPath, String optionalSuffix, Object value) {
		super(jsonPath, optionalSuffix, value)
	}

	@Override
	List<String> buildJsonPathComparison(String parsedJsonVariable) {
		if (optionalSuffix) {
			return ["assertThat(${parsedJsonVariable}.read(\"${jsonPath}\", JSONArray.class)).isNotEmpty();"]
		} else if (traversesOverCollections()) {
			return ["${assertionStatement()}(${parsedJsonVariable}.read(\"${jsonPath}\", JSONArray.class).get(0), ${potentiallyWrappedWithQuotesValue()});"]
		}
		return ["${assertionStatement()}(${parsedJsonVariable}.read(\"${jsonPath}\"), ${potentiallyWrappedWithQuotesValue()});"]
	}

	private String assertionStatement() {
		return value instanceof Pattern ? "assertMatches" : "assertEquals"
	}

	private String potentiallyWrappedWithQuotesValue() {
		return value instanceof Number ? value : "\"${value}\""
	}
}
