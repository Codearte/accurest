package io.codearte.accurest.util.jsonpath

/**
 * @author Olga Maciaszek-Sharma 
 @since 29.12.15
 */
class JUnitJsonPathEntryFactory implements JsonPathEntryFactory {

	@Override
	JsonPathEntry create(String jsonPath, String optionalSuffix, Object value) {
		return new JUnitJsonPathEntry(jsonPath, optionalSuffix, value)
	}


	JUnitJsonPathEntry simple(String jsonPath, Object value) {
		return new JUnitJsonPathEntry(jsonPath, "", value)
	}
}
