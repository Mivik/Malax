package com.mivik.malax;

import com.mivik.mlexer.MLexer;

public class UndoableMalax extends UndoableEditable<Malax.Cursor> {

	public UndoableMalax(Malax malax) {
		super(malax);
	}

	private UndoableMalax(Editable<Malax.Cursor> editable) {
		super(editable);
	}

	public Malax getMalax() {
		return (Malax) E;
	}

	public void ensureParsed() {
		getMalax().ensureParsed();
	}

	public void setLexer(MLexer lexer) {
		getMalax().setLexer(lexer);
	}

	public MLexer getLexer() {
		return getMalax().getLexer();
	}

	public SplayTree getLineTree() {
		return getMalax().getLineTree();
	}

	public char[][] getLines() {
		return getMalax().getLines();
	}

	public int getLineStart(int line) {
		return getMalax().getLineStart(line);
	}

	public int getLineEnd(int line) {
		return getMalax().getLineEnd(line);
	}

	public int getLineCount() {
		return getMalax().getLineCount();
	}
}