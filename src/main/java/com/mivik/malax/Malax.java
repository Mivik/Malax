package com.mivik.malax;

import com.mivik.mlexer.MLexer;
import com.mivik.mlexer.NullLexer;
import com.mivik.mlexer.RangeSelection;

import java.nio.CharBuffer;
import java.util.Arrays;

public class Malax extends Editable<Malax.Cursor> {
	public static final int LINE_BUFFER_SIZE = 8, COLUMN_BUFFER_SIZE = 8;

	protected final SplayTree L = new SplayTree();
	protected char[][] S = new char[LINE_BUFFER_SIZE][];
	protected MLexer M;

	public Malax() {
		this(null, 0, 0);
	}

	public Malax(char[] cs) {
		this(cs, 0, cs.length);
	}

	public Malax(char[] cs, int off, int len) {
		clear();
		setLexer(new NullLexer());
		if (cs != null) insert(getBeginCursor(), cs, off, len);
	}

	public Malax(CharSequence cs) {
		this(Editable.CharSequence2CharArray(cs, 0, cs.length()), 0, cs.length());
	}

	public void ensureParsed() {
		M.ensureParsed();
	}

	public void setLexer(MLexer lexer) {
		M = lexer;
		M.setDocument(this);
	}

	public MLexer getLexer() {
		return M;
	}

	public SplayTree getLineTree() {
		return L;
	}

	public char[][] getLines() {
		return S;
	}

	public int getLineStart(int line) {
		if (line <= 0 || L.empty()) return 0;
		if (line >= L.size()) return L.sum();
		return L.getPrefixSum(line - 1);
	}

	public int getLineEnd(int line) {
		if (line <= 0 || L.empty()) return 0;
		if (line >= L.size()) return L.sum();
		return L.getPrefixSum(line);
	}

	public int getLineCount() {
		return L.size();
	}

	public RangeSelection<Cursor> getLineSelection(int line) {
		return new RangeSelection<>(new Cursor(line, 0), new Cursor(line, L.get(line)));
	}

	public char[] getLineChars(int line) {
		char[] ret = new char[L.get(line)];
		System.arraycopy(S[line], 0, ret, 0, ret.length);
		return ret;
	}

	public String getLine(int line) {
		return new String(S[line], 0, L.get(line));
	}

	@Override
	public char charAt(Cursor cursor) {
		return S[cursor.line][cursor.column];
	}

	@Override
	public StringBuilder subStringBuilder(RangeSelection<Cursor> range) {
		ensureRange(range);
		final Cursor st = range.begin;
		final Cursor en = range.end;
		StringBuilder ret = new StringBuilder();
		if (st.line == en.line) ret.append(S[st.line], st.column, en.column - st.column);
		else {
			SplayTree.SplayNode cur = L.getKth(st.line);
			ret.append(S[st.line], st.column, cur.val - st.column);
			cur = cur.successor();
			for (int i = st.line + 1; i < en.line; i++) {
				ret.append(S[i], 0, cur.val);
				cur = cur.successor();
			}
			ret.append(S[en.line], 0, en.column);
		}
		return ret;
	}

	@Override
	public int moveBack(Cursor cursor, int dis) {
		return L.moveBack(cursor, dis);
	}

	@Override
	public int moveForward(Cursor cursor, int dis) {
		return L.moveForward(cursor, dis);
	}

	@Override
	public boolean moveBack(Cursor cursor) {
		return L.moveBack(cursor);
	}

	@Override
	public boolean moveForward(Cursor cursor) {
		return L.moveForward(cursor);
	}

	@Override
	public Cursor getBeginCursor() {
		return L.getBeginCursor();
	}

	@Override
	public Cursor getEndCursor() {
		return L.getEndCursor();
	}

	@Override
	public int length() {
		return L.sum();
	}

	@Override
	public void clear() {
		L.clear();
		L.append(0);
		S = new char[LINE_BUFFER_SIZE][];
		S[0] = new char[0];
	}

	@Override
	public void delete(Cursor x) {
		if (x.line == 0 && x.column == 0) return;
		ensureCursor(x);
		moveBack(x);
		final int line = x.line;
		int oriLen = L.get(line);
		final int col = Math.min(x.column, oriLen);
		if (S[line][col] == '\n') {
			int nxtLen = L.get(line + 1);
			L.set(line, oriLen + nxtLen - 1);
			L.remove(line + 1);
			ensureStringCapture(line, col + nxtLen);
			System.arraycopy(S[line + 1], 0, S[line], col, nxtLen);
			for (int i = line + 1; i < L.size(); i++) S[i] = S[i + 1];
		} else {
			L.set(line, oriLen - 1);
			char[] s = S[line];
			for (int i = col + 1; i < oriLen; i++) s[i - 1] = s[i];
		}
		M.onTextReferenceUpdate();
		M.onDeleteChars(Cursor2Index(x), 1);
	}

	@Override
	public void delete(final RangeSelection<Cursor> sel) {
		final Cursor st = sel.begin;
		final Cursor en = sel.end.clone();
		moveBack(en);
		int enLen = L.get(en.line);
		int len;
		if (st.line == en.line) {
			len = en.column - st.column;
			L.set(st.line, enLen - len);
			char[] s = S[st.line];
			for (int i = en.column + 1; i < enLen; i++) s[i - len] = s[i];
		} else {
			final int nl = st.column + enLen - en.column - 1;
			SplayTree.SplayNode node = L.getRange(st.line, en.line);
			len = node.sum - st.column - enLen + en.column + 1;
			SplayTree.SplayNode lef = node.minimum();
			L.splay(lef, node.fa);
			L.removeSons(lef);
			L.set(lef, nl);
			ensureStringCapture(st.line, nl);
			System.arraycopy(S[en.line], en.column + 1, S[st.line], st.column, enLen - en.column - 1);
			int off = en.line - st.line;
			for (int i = st.line + 1; i < L.size(); i++) S[i] = S[i + off];
		}
		M.onTextReferenceUpdate();
		M.onDeleteChars(Cursor2Index(en), len);
	}

	@Override
	public void insert(Cursor x, char c) {
		ensureCursor(x);
		int len = L.get(x.line);
		int col = Math.min(x.column, len);
		if (c == '\n') {
			ensureLineCapture(L.size() + 1);
			L.set(x.line, col + 1);
			int cur = x.line + 1;
			int nl = len - col;
			L.insert(cur, nl);
			for (int i = L.size() - 1; i > cur; i--) S[i] = S[i - 1];
			S[cur] = Arrays.copyOfRange(S[x.line], col, len);
			ensureStringCapture(x.line, col + 1);
			S[x.line][col] = '\n';
		} else {
			L.set(x.line, ++len);
			ensureStringCapture(x.line, len);
			char[] s = S[x.line];
			for (int i = len - 1; i > col; i--) s[i] = s[i - 1];
			s[col] = c;
		}
		M.onTextReferenceUpdate();
		M.onInsertChars(Cursor2Index(x), 1);
	}

	@Override
	public void insert(Cursor x, final char[] cs, final int off, final int len) {
		if (len == 0) return;
		if (len == 1) {
			insert(x, cs[0]);
			return;
		}
		ensureCursor(x);
		int i;
		final int line = x.line;
		SplayTree.SplayNode cur = null;
		cur = L.getKth(line);
		final int oriLen = cur.val;
		final int col = Math.min(x.column, oriLen);
		final int rig = oriLen - col;
		int lst = -col;
		int curLine = x.line;
		int tot = 0;
		int[] d = new int[LINE_BUFFER_SIZE];
		for (i = 0; i < len; ) {
			if (cs[off + i] == '\n') {
				d = ensureArrayCapture(d, tot + 1);
				d[tot++] = i++;
				cur.val = i - lst;
				L.splay(cur);
				cur = L.insert(++curLine, 0);
				lst = i;
			} else ++i;
		}
		cur.val = i - lst + rig;
		L.splay(cur);
		if (tot == 0) {
			ensureStringCapture(line, oriLen + len);
			char[] s = S[line];
			for (i = oriLen + len - 1; i > col + len - 1; i--) s[i] = s[i - len];
			System.arraycopy(cs, 0, s, col, len);
		} else {
			ensureLineCapture(L.size()); // 现在的L是已经插入好了的，所以size是新的size
			for (i = L.size() - 1; i > line + tot; i--) S[i] = S[i - tot];
			// 第一行和最后一行要特殊对待ww
			int lstLen = len - d[tot - 1] - 1;
			S[line + tot] = new char[rig + lstLen];
			System.arraycopy(cs, d[tot - 1] + 1, S[line + tot], 0, lstLen);
			System.arraycopy(S[line], col, S[line + tot], lstLen, rig);
			int fstLen = d[0] + 1;
			ensureStringCapture(line, col + fstLen);
			System.arraycopy(cs, 0, S[line], col, fstLen);
			for (i = 1; i < tot; i++) {
				S[line + i] = new char[d[i] - d[i - 1]];
				System.arraycopy(cs, d[i - 1] + 1, S[line + i], 0, d[i] - d[i - 1]);
			}
		}
		M.onTextReferenceUpdate();
		M.onInsertChars(Cursor2Index(x), len);
	}

	@Override
	public Cursor Index2Cursor(int x) {
		return L.Index2Cursor(x);
	}

	@Override
	public int Cursor2Index(Cursor x) {
		return L.Cursor2Index(x);
	}

	protected void ensureCursor(Cursor cursor) {
		if (cursor.line < 0) throw new IllegalArgumentException("line number cannot be negative");
		if (cursor.line < L.size()) return;
		if (cursor.line == L.size() && cursor.column == 0) return;
		throw new IllegalArgumentException("line number exceeded the total count of lines");
	}

	protected static int[] ensureArrayCapture(int[] a, int len) {
		if (a.length >= len) return a;
		int[] ret = new int[((len - a.length - 1) / LINE_BUFFER_SIZE + 1) * LINE_BUFFER_SIZE + a.length];
		System.arraycopy(a, 0, ret, 0, a.length);
		return ret;
	}

	protected void ensureLineCapture(int len) {
		if (S.length >= len) return;
		int nl = ((len - S.length - 1) / COLUMN_BUFFER_SIZE + 1) * COLUMN_BUFFER_SIZE + S.length;
		char[][] ori = S;
		S = new char[nl][];
		System.arraycopy(ori, 0, S, 0, ori.length);
	}

	protected void ensureStringCapture(int line, int len) {
		if (S[line] == null) {
			S[line] = new char[len];
			return;
		}
		if (S[line].length >= len) return;
		int nl = ((len - S[line].length - 1) / COLUMN_BUFFER_SIZE + 1) * COLUMN_BUFFER_SIZE + S[line].length;
		char[] ori = S[line];
		S[line] = new char[nl];
		System.arraycopy(ori, 0, S[line], 0, ori.length);
	}

	protected void ensureRange(RangeSelection<Cursor> range) {
		final Cursor be = range.begin;
		final Cursor en = range.end;
		ensureCursor(be);
		ensureCursor(en);
		do {
			if (be.line > en.line) break;
			if (be.line != en.line) return;
			if (be.column >= en.column) break;
			return;
		} while (false);
		throw new IllegalArgumentException("The start cursor cannot be greater than the end");
	}

	public char[] toCharArray() {
		return toCharArray(null);
	}

	public char[] toCharArray(char[] cs) {
		if (cs == null) cs = new char[length()];
		if (L.empty()) return cs;
		CharBuffer buffer = CharBuffer.wrap(cs);
		int len = getLineCount();
		SplayTree.SplayNode x = L.begin();
		for (int i = 0; i < len; i++) {
			buffer.put(S[i], 0, x.val);
			x = x.successor();
		}
		return cs;
	}

	@Override
	public String toString() {
		if (L.empty()) return "";
		StringBuilder ret = new StringBuilder();
		int len = getLineCount();
		SplayTree.SplayNode x = L.begin();
		for (int i = 0; i < len; i++) {
			ret.append(S[i], 0, x.val);
			x = x.successor();
		}
		return ret.toString();
	}

	public static class Cursor extends com.mivik.mlexer.Cursor implements Comparable<Cursor> {
		int line, column;

		public Cursor(int line, int col) {
			this.line = line;
			this.column = col;
		}

		public Cursor(Cursor ori) {
			this.line = ori.line;
			this.column = ori.column;
		}

		public void setLine(int line) {
			this.line = line;
		}

		public void setColumn(int col) {
			this.column = col;
		}

		public int getLine() {
			return line;
		}

		public int getColumn() {
			return column;
		}

		@Override
		public Cursor clone() {
			return new Cursor(this);
		}

		@Override
		public int compareTo(Cursor t) {
			if (line != t.line) return Integer.compare(line, t.line);
			return Integer.compare(column, t.column);
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Cursor)) return false;
			Cursor t = (Cursor) obj;
			return line == t.line && column == t.column;
		}

		@Override
		public int hashCode() {
			return line ^ column;
		}

		@Override
		public String toString() {
			return "(" + line + ',' + column + ')';
		}
	}
}