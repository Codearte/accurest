package io.codearte.accurest.builder

import groovy.transform.PackageScope

/**
 * @author Jakub Kubrynski
 */
@PackageScope
class BlockBuilder {

	private final StringBuilder builder
	private final String spacer
	private int indents

	BlockBuilder(String spacer) {
		this.spacer = spacer
		builder = new StringBuilder()
	}

	BlockBuilder startBlock() {
		indents++
		return this
	}

	BlockBuilder endBlock() {
		indents--
		return this
	}

	BlockBuilder indent() {
		startBlock().startBlock()
		return this
	}

	BlockBuilder unindent() {
		endBlock().endBlock()
		return this
	}

	BlockBuilder addLine(String line) {
		addIndentation()
		builder << "$line\n"
		return this
	}

	BlockBuilder addLines(List<GString> lines) {
		lines.each {line -> addLine(line)}
		return this
	}

	BlockBuilder addEmptyLine() {
		builder << '\n'
		return this
	}

	private void addIndentation() {
		indents.times {
			builder << spacer
		}
	}

	BlockBuilder addBlock(MethodBuilder methodBuilder) {
		startBlock()
		methodBuilder.appendTo(this)
		endBlock()
		addEmptyLine()
		return this
	}

	@Override
	String toString() {
		return builder.toString()
	}
}
