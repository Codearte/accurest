package io.codearte.accurest.builder

import groovy.transform.PackageScope
import groovy.transform.TypeChecked

/**
 * @author Olga Maciaszek-Sharma
 * @since 2015-08-08
 */
@PackageScope
@TypeChecked
class AssertionBodyTypeDecorator {

	private static final String DEFAULT_TYPE_PLACEHOLDER = 'Object'
	private static final String LIST_STRING = 'List<Object>'
	private static final String MAP_STRING = 'Map<String, Object>'

	private List<GString> assertions

	AssertionBodyTypeDecorator(List<GString> assertions) {
		this.assertions = assertions
	}

	List<String> decorate() {
		List<String> decoratedAssertions = new ArrayList<>()
		assertions.each {
			String bodyType = generateBodyTypeString(evaluateResponseBodyType(it))
			String decoratedAssertion = it.replace("responseBody", "(($bodyType) responseBody)")
			decoratedAssertions.add(decoratedAssertion)
		}
		return decoratedAssertions
	}

	private List<CollectionType> evaluateResponseBodyType(String assertion) {
		List<CollectionType> collectionTypes = []
		List<String> elements = assertion.tokenize('.')
		elements.each {
			if (it.contains('get')) {
				if (it.contains('get("') || it.contains("get('")) {
					collectionTypes.add(CollectionType.MAP)
				} else {
					collectionTypes.add(CollectionType.LIST)
				}
			}
		}
		return collectionTypes
	}

	private String generateBodyTypeString(List<CollectionType> collectionTypes) {
		StringBuilder bodyTypeBuilder = new StringBuilder('')
		collectionTypes.each {
			if (it == CollectionType.MAP) {
				addMapType(bodyTypeBuilder)
			} else if (it == CollectionType.LIST) {
				addListType(bodyTypeBuilder)
			}
		}
		return bodyTypeBuilder.toString()
	}

	private void addType(StringBuilder bodyTypeBuilder, String type) {
		if (bodyTypeBuilder.toString() == '') {
			bodyTypeBuilder << type
		} else {
			Integer index = bodyTypeBuilder.lastIndexOf(DEFAULT_TYPE_PLACEHOLDER)
			bodyTypeBuilder.replace(index, index + DEFAULT_TYPE_PLACEHOLDER.length(), type)
		}
	}

	void addMapType(StringBuilder bodyTypeBuilder) {
		addType(bodyTypeBuilder, MAP_STRING)
	}

	void addListType(StringBuilder bodyTypeBuilder) {
		addType(bodyTypeBuilder, LIST_STRING)
	}
}

enum CollectionType {
	MAP,
	LIST
}
