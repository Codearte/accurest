package io.codearte.accurest.util.jsonpath

/**
 * @author Olga Maciaszek-Sharma 
 @since 29.12.15
 */
abstract class JsonPathEntry {

    final protected String jsonPath
    final protected String optionalSuffix
    final protected Object value

    JsonPathEntry(String jsonPath, String optionalSuffix, Object value) {
        this.jsonPath = jsonPath
        this.optionalSuffix = optionalSuffix
        this.value = value
    }

    abstract List<String> buildJsonPathComparison(String parsedJsonVariable)

    protected boolean traversesOverCollections() {
        return jsonPath.contains('[*]')
    }
}