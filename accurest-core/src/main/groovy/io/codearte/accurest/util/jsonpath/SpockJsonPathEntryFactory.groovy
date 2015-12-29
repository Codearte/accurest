package io.codearte.accurest.util.jsonpath

/**
 * @author Olga Maciaszek-Sharma 
 @since 29.12.15
 */
class SpockJsonPathEntryFactory implements JsonPathEntryFactory {

    @Override
    JsonPathEntry createJsonPathEntry(String jsonPath, String optionalSuffix, Object value) {
        return new SpockJsonPathEntry(jsonPath, optionalSuffix, value)
    }
}
