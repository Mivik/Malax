package com.mivik.malax;

import com.mivik.mlexer.StringProvider;

public class DocumentStringProvider extends StringProvider {
	private Document D;
	private Cursor C;
	private int ind;

	public DocumentStringProvider(Document doc) {
		this.D = doc;
		this.C = doc.getBeginCursor();
		this.ind = 0;
	}

	@Override
	public void moveCursor(int x) {
		this.C = D.Index2Cursor(x);
		this.ind = x;
	}

	@Override
	public int getCursor() {
		return ind;
	}

	@Override
	public char get() {
		return D.charAt(C);
	}

	@Override
	public void moveLeft() {
		D.moveLeft(C);
		--ind;
	}

	@Override
	public void moveRight() {
		D.moveRight(C);
		++ind;
	}

	@Override
	public String substring(int st, int en) {
		return D.substring(st, en - st);
	}

	@Override
	public int length() {
		return D.length();
	}
}
