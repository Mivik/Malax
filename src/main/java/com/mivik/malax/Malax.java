package com.mivik.malax;

import com.mivik.mlexer.MLexer;
import com.mivik.mlexer.RangeSelection;

public class Malax extends WrappedEditable<BaseMalax.Cursor> {

	public Malax() {
		super(new BaseMalax());
	}

	public Malax(BaseMalax malax) {
		super(malax);
	}

	private Malax(Editable<BaseMalax.Cursor> editable) {
		super(editable);
	}

	public BaseMalax getMalax() {
		return (BaseMalax) E;
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

	public LineManager getLineManager() {
		return getMalax().getLineManager();
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

	public RangeSelection<BaseMalax.Cursor> getLineSelection(int line) {
		return getMalax().getLineSelection(line);
	}

	public char[] getLineChars(int line) {
		return getMalax().getLineChars(line);
	}

	public String getLine(int line) {
		return getMalax().getLine(line);
	}

	public char[][] getRawChars() {
		return getMalax().getRawChars();
	}

	public void setRawChars(char[][] cs) {
		getMalax().setRawChars(cs);
	}

	public void set(Malax another) {
		A = another.A;
		E = another.E;
	}

	public void setContentChangeListener(BaseMalax.ContentChangeListener listener) {
		getMalax().setContentChangeListener(listener);
	}

	public BaseMalax.ContentChangeListener getContentChangeListener() {
		return getMalax().getContentChangeListener();
	}

	@Override
	public void delete(BaseMalax.Cursor en) {
		if (en.line==0&&en.column==0) return;
		super.delete(en);
	}
}