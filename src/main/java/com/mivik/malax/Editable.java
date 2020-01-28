package com.mivik.malax;

import com.mivik.mlexer.Cursor;
import com.mivik.mlexer.Document;
import com.mivik.mlexer.RangeSelection;

public abstract class Editable<T extends Cursor> extends Document<T> {
	public static char[] CharSequence2CharArray(CharSequence cs, int off, int len) {
		char[] ret = new char[len];
		for (int i = 0; i < len; i++) ret[i] = cs.charAt(off + i);
		return ret;
	}

	public abstract void clear();

	public abstract void insert(T st, char c);

	public abstract void insert(T st, char[] cs, int off, int len);

	public final void insert(T st, char[] cs) {
		insert(st, cs, 0, cs.length);
	}

	public final void insert(T st, String s) {
		insert(st, s.toCharArray(), 0, s.length());
	}

	public abstract void delete(T en);

	public abstract void delete(RangeSelection<T> sel);

	public final void delete(T en, int len) {
		delete(fromEnd(en, len));
	}

	public final void replace(RangeSelection<T> sel, char c) {
		replace(sel, new char[]{c}, 0, 1);
	}

	public final void replace(RangeSelection<T> sel, char[] cs) {
		replace(sel, cs, 0, cs.length);
	}

	public final void replace(RangeSelection<T> sel, String s) {
		replace(sel, s.toCharArray(), 0, s.length());
	}

	public void replace(RangeSelection<T> sel, char[] cs, int off, int len) {
		delete(sel);
		insert(sel.begin, cs, off, len);
	}
}