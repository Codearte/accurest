package io.codearte.accurest.util.jsonpath

/**
 * @author Olga Maciaszek-Sharma 
 @since 29.12.15
 */
interface JsonPathEntryFactory {

 JsonPathEntry createJsonPathEntry(String jsonPath, String optionalSuffix, Object value)

}