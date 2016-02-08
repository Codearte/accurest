package io.codearte.accurest.util.jsonpath

/**
 * @author Olga Maciaszek-Sharma 
 @since 08.02.16
 */
class JUnitJsonToJsonPathConverter extends JsonToJsonPathsConverter {

    @Override
    protected JsonPathEntryFactory getJsonPathEntryFactory() {
        return new JUnitJsonPathEntryFactory();
    }
}
