package com.mivik.malax;

import com.mivik.mlexer.MLexer;
import com.mivik.mlexer.NullLexer;

import java.nio.CharBuffer;
import java.util.Arrays;

public class Document {
	public static final int LINE_BUFFER_SIZE = 8, COLUMN_BUFFER_SIZE = 8;

	protected SplayTree L = new SplayTree();
	protected char[][] S = new char[LINE_BUFFER_SIZE][];
	protected MLexer M;

	public static char[] CharSequence2CharArray(CharSequence cs) {
		char[] ret = new char[cs.length()];
		for (int i = 0; i < cs.length(); i++) ret[i] = cs.charAt(i);
		return ret;
	}

	public Document() {
		this(null, 0, 0);
	}

	public Document(CharSequence cs) {
		this(CharSequence2CharArray(cs), 0, cs.length());
	}

	public Document(char[] cs) {
		this(cs, 0, cs.length);
	}

	public Document(char[] cs, int off) {
		this(cs, off, cs.length - off);
	}

	public Document(char[] cs, int off, int len) {
		_clear();
		setLexer(new NullLexer());
		if (cs != null) _insertChars(getBeginCursor(), cs, off, len);
	}

	public void ensureParsed() {
		M.ensureParsed();
	}

	public void setLexer(MLexer lexer) {
		M = lexer;
		M.setText(new DocumentStringProvider(this));
	}

	public MLexer getLexer() {
		return M;
	}

	public char charAt(int x) {
		return charAt(Index2Cursor(x));
	}

	public char charAt(Cursor cursor) {
		return S[cursor.line][cursor.column];
	}

	private StringBuilder subStringBuilder(Cursor st, int len) {
		ensureCursor(st);
		StringBuilder ret = new StringBuilder();
		int line = st.line;
		int col = st.column;
		SplayTree.SplayNode cur = L.getKth(line);
		while (len > cur.val - col) {
			len -= cur.val - col;
			ret.append(S[line], col, cur.val - col);
			cur = cur.successor();
			++line;
			if (line == L.size()) return ret;
			col = 0;
		}
		ret.append(S[line], col, len);
		return ret;
	}

	public char[] subChars(int st, int len) {
		return subChars(Index2Cursor(st), len);
	}

	public char[] subChars(Cursor st, int len) {
		StringBuilder r = subStringBuilder(st, len);
		char[] ret = new char[r.length()];
		r.getChars(0, r.length(), ret, 0);
		return ret;
	}

	public String substring(int st, int len) {
		return substring(Index2Cursor(st), len);
	}

	public String substring(Cursor st, int len) {
		return subStringBuilder(st, len).toString();
	}

	public String substring(RangeSelection range) {
		ensureRange(range);
		final Cursor st = range.begin;
		final Cursor en = range.end;
		if (st.line == en.line) return new String(S[st.line], st.column, en.column - st.column);
		else {
			StringBuilder ret = new StringBuilder();
			SplayTree.SplayNode cur = L.getKth(st.line);
			ret.append(S[st.line], st.column, cur.val - st.column);
			cur = cur.successor();
			for (int i = st.line + 1; i < en.line; i++) {
				ret.append(S[i], 0, cur.val);
				cur = cur.successor();
			}
			ret.append(S[en.line], 0, en.column);
			return ret.toString();
		}
	}

	public SplayTree getLineTree() {
		return L;
	}

	public char[][] getLines() {
		return S;
	}

	public boolean moveLeft(Cursor cursor, int dis) {
		return L.moveLeft(cursor, dis);
	}

	public boolean moveRight(Cursor cursor, int dis) {
		return L.moveRight(cursor, dis);
	}

	public boolean moveLeft(Cursor cursor) {
		return L.moveLeft(cursor);
	}

	public boolean moveRight(Cursor cursor) {
		return L.moveRight(cursor);
	}

	public Cursor getBeginCursor() {
		return L.getBeginCursor();
	}

	public Cursor getEndCursor() {
		return L.getEndCursor();
	}

	public int length() {
		return L.sum();
	}

	public int getLineCount() {
		return L.size();
	}

	public int getLineLength(int x) {
		return L.get(x);
	}

	public String getLine(int x) {
		return new String(S[x], 0, getLineLength(x));
	}

	public char[] getLineChars(int x) {
		return S[x];
	}

	public void clear() {
		_clear();
	}

	public void deleteChars(int x, int len) {
		_deleteChars(Index2Cursor(x), len);
	}

	public void deleteChar(int x) {
		_deleteChar(Index2Cursor(x));
	}

	public void append(CharSequence s) {
		_insertChars(getEndCursor(), CharSequence2CharArray(s), 0, s.length());
	}

	public void appendChar(char c) {
		_insertChar(getEndCursor(), c);
	}

	public void appendChars(char[] cs) {
		_insertChars(getEndCursor(), cs, 0, cs.length);
	}

	public void appendChars(char[] cs, int off) {
		_insertChars(getEndCursor(), cs, off, cs.length - off);
	}

	public void appendChars(char[] cs, int off, int len) {
		_insertChars(getEndCursor(), cs, off, len);
	}

	public void insert(int x, CharSequence s) {
		_insertChars(Index2Cursor(x), CharSequence2CharArray(s), 0, s.length());
	}

	public void insertChars(int x, char[] cs) {
		_insertChars(Index2Cursor(x), cs, 0, cs.length);
	}

	public void insertChars(int x, char[] cs, int off) {
		_insertChars(Index2Cursor(x), cs, off, cs.length - off);
	}

	public void insertChars(int x, char[] cs, int off, int len) {
		_insertChars(Index2Cursor(x), cs, off, len);
	}

	public void insert(Cursor x, String s) {
		_insertChars(x, s.toCharArray(), 0, s.length());
	}

	public void insertChars(Cursor x, char[] cs) {
		_insertChars(x, cs, 0, cs.length);
	}

	public void insertChars(Cursor x, char[] cs, int off) {
		_insertChars(x, cs, off, cs.length - off);
	}

	public void insertChar(int x, char c) {
		_insertChar(Index2Cursor(x), c);
	}

	public void _clear() {
		L.clear();
		L.append(0);
		S = new char[LINE_BUFFER_SIZE][];
		S[0] = new char[0];
	}

	protected void _deleteChar(Cursor x) {
		if (x.line == 0 && x.column == 0) return;
		ensureCursor(x);
		moveLeft(x);
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

	protected void _deleteChars(Cursor en, int len) {
		if (en.line == 0 && en.column == 0) return;
		ensureCursor(en);
		moveLeft(en);
		len = Math.min(len, Cursor2Index(en) + 1);
		Cursor st = en.clone();
		moveLeft(st, len - 1);
		int enLen = L.get(en.line);
		if (st.line == en.line) {
			L.set(st.line, enLen - len);
			char[] s = S[st.line];
			for (int i = en.column + 1; i < enLen; i++) s[i - len] = s[i];
		} else {
			int nl = st.column + enLen - en.column - 1;
			L.set(st.line, nl);
			L.removeRange(st.line + 1, en.line);
			ensureStringCapture(st.line, nl);
			System.arraycopy(S[en.line], en.column + 1, S[st.line], st.column, enLen - en.column - 1);
			int off = en.line - st.line;
			for (int i = st.line + 1; i < L.size(); i++) S[i] = S[i + off];
		}
		M.onTextReferenceUpdate();
		M.onDeleteChars(Cursor2Index(en), len);
	}

	protected void _insertChar(Cursor x, char c) {
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

	protected void _insertChars(Cursor x, final char[] cs, final int off, final int len) {
		if (len == 0) return;
		if (len == 1) {
			_insertChar(x, cs[0]);
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

	public Cursor Index2Cursor(int x) {
		return L.Index2Cursor(x);
	}

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

	protected void ensureRange(RangeSelection range) {
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
}