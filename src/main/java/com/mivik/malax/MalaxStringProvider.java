package com.mivik.malax;

import com.mivik.mlexer.Document;
import com.mivik.mlexer.DocumentAccessor;

public class MalaxStringProvider implements Document {
	private Malax D;
	private Cursor C;
	private int ind;

	public MalaxStringProvider(Malax doc) {
		this.D = doc;
	}

	@Override
	public DocumentAccessor getAccessor() {
		return new Accessor();
	}

	class Accessor extends DocumentAccessor {
		private Cursor C;
		private int ind;

		Accessor() {
			this.C = D.getBeginCursor();
			this.ind = 0;
		}

		Accessor(Cursor cursor, int ind) {
			this.C = cursor;
			this.ind = ind;
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

		@Override
		public void getChars(int st, int len, char[] dst, int off) {
			D.subChars(st, len, dst, off);
		}

		@Override
		public DocumentAccessor clone() {
			return new Accessor(C, ind);
		}
	}
}
