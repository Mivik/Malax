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

	public abstract T insert(T st, char c);

	public abstract T insert(T st, char[] cs, int off, int len);

	public final T insert(T st, char[] cs) {
		return insert(st, cs, 0, cs.length);
	}

	public final T insert(T st, String s) {
		return insert(st, s.toCharArray(), 0, s.length());
	}

	public abstract void delete(T en);

	public abstract void delete(RangeSelection<T> sel);

	public final void delete(T en, int len) {
		delete(fromEnd(en, len));
	}

	public final T replace(RangeSelection<T> sel, char c) {
		return replace(sel, new char[]{c}, 0, 1);
	}

	public final T replace(RangeSelection<T> sel, char[] cs) {
		return replace(sel, cs, 0, cs.length);
	}

	public final T replace(RangeSelection<T> sel, String s) {
		return replace(sel, s.toCharArray(), 0, s.length());
	}

	public T replace(RangeSelection<T> sel, char[] cs, int off, int len) {
		delete(sel);
		return insert(sel.begin, cs, off, len);
	}

	public final void setText(String s) {
		setText(s.toCharArray(), 0, s.length());
	}

	public final void setText(char[] cs) {
		setText(cs, 0, cs.length);
	}

	public void setText(char[] cs, int off, int len) {
		clear();
		insert(getBeginCursor(), cs, off, len);
	}
}