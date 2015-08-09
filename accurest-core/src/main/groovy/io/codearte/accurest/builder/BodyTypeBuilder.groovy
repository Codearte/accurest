package io.codearte.accurest.builder

import groovy.transform.PackageScope
import groovy.transform.TypeChecked

/**
 * @author Olga Maciaszek-Sharma
 * @since 2015-08-08
 */
@PackageScope
@TypeChecked
class BodyTypeBuilder {

	static final String DEFAULT_TYPE_PLACEHOLDER = 'Object>'
	static final String LIST = 'List<Object>'
	static final String MAP = 'Map<String, Object>'

	private StringBuilder bodyType = new StringBuilder('')

	void addMapType() {
		if (bodyType.toString().endsWith(DEFAULT_TYPE_PLACEHOLDER)){
			int index = bodyType.lastIndexOf(DEFAULT_TYPE_PLACEHOLDER)
			bodyType.replace(index, index + MAP.length() - 2, MAP)
		}
		bodyType.append(MAP)
	}

	void addListType() {
		if (bodyType.toString().endsWith(DEFAULT_TYPE_PLACEHOLDER)) {
			int index = bodyType.lastIndexOf(DEFAULT_TYPE_PLACEHOLDER)
			bodyType.replace(index, index + LIST.length() - 2, LIST)
		} else {
			bodyType.append(LIST)
		}
	}

	String build() {
		return bodyType.toString()
	}

}
