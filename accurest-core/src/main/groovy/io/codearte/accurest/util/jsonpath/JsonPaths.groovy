package io.codearte.accurest.util.jsonpath

class JsonPaths extends HashSet<JsonPathEntry> {

	final JsonPathEntryFactory jsonPathEntryFactory

	JsonPaths(JsonPathEntryFactory jsonPathEntryFactory) {
		this.jsonPathEntryFactory = jsonPathEntryFactory
	}

	Object getAt(String key) {
		return find {
			it.jsonPath == key
		}?.value
	}

	Object putAt(String key, Object value) {
		JsonPathEntry entry = find {
			it.jsonPath == key
		}
		if (!entry) {
			return null
		}
		Object oldValue = entry.value
		add(jsonPathEntryFactory.createJsonPathEntry(entry.jsonPath, entry.optionalSuffix, value))
		return oldValue
	}
}

