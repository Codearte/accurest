package io.codearte.accurest.util.jsonpath

/**
 * @author Olga Maciaszek-Sharma 
 @since 29.12.15
 */
interface JsonPathEntryFactory {

	JsonPathEntry create(String jsonPath, String optionalSuffix, Object value)

	JsonPathEntry simple(String jsonPath, Object value)

}