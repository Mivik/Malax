package com.mivik.malax;

import static com.mivik.malax.BaseMalax.Cursor;

public class LineManager {
	public static final int EXPAND_SIZE = 16;

	public int S;
	public int[] E = new int[EXPAND_SIZE];
	private LineChangeListener listener;

	public LineManager() {
		clear();
	}

	public LineChangeListener getLineChangeListener() {
		return listener;
	}

	public void setLineChangeListener(LineChangeListener listener) {
		this.listener = listener;
	}

	public void insertAll(Cursor x, int[] a, int tot, int len) {
		if (tot == 0) {
			increase(x.line, len);
			return;
		}
		int[] dst;
		if (S + tot >= E.length) {
			dst = new int[newSize(S + tot + 1)];
			System.arraycopy(E, 0, dst, 0, x.line + 1);
		} else dst = E;
		for (int i = S; i > x.line; i--) dst[i + tot] = E[i] + len;
		final int base = E[x.line] + x.column + 1;
		for (int i = 0; i < tot; i++) dst[x.line + i + 1] = base + a[i];
		E = dst;
		updateSize(S + tot);
	}

	public Cursor getBeginCursor() {
		return new Cursor(0, 0);
	}

	public Cursor getEndCursor() {
		if (S == 0) return new Cursor(0, 0);
		return new Cursor(S - 1, get(S - 1));
	}

	public boolean moveBack(Cursor x) {
		if (x.column == 0) {
			if (x.line == 0) return false;
			x.column = get(--x.line) - 1;
		} else --x.column;
		return true;
	}

	public boolean moveForward(Cursor x) {
		if (x.line == S - 1) {
			if (x.column == get(x.line)) return false;
			else ++x.column;
		} else {
			if (x.column == get(x.line) - 1) {
				++x.line;
				x.column = 0;
			} else ++x.column;
		}
		return true;
	}

	public int moveBack(Cursor x, int dis) {
		if (dis == 0) return 0;
		if (x.column == 0 && x.line == 0) return 0;
		int odis = dis;
		while (dis > x.column) {
			dis -= x.column;
			if (x.line == 0) {
				x.column = 0;
				return odis - dis;
			}
			--dis;
			x.column = get(--x.line) - 1;
		}
		x.column -= dis;
		return odis;
	}

	public int moveForward(Cursor x, int dis) {
		if (dis == 0) return 0;
		int len = get(x.line);
		if (x.line == S - 1 && x.column == len) return 0;
		int odis = dis;
		while (dis >= len - x.column) {
			dis -= len - x.column;
			if (x.line == S - 1) {
				x.column = len;
				return odis - dis;
			}
			len = get(++x.line);
			x.column = 0;
		}
		x.column += dis;
		return odis;
	}

	public int findLine(int x) {
		if (x < 0) return 0;
		if (x >= E[S]) return S - 1;
		int l = 0, r = S - 1, mid, ret = 0;
		while (l <= r) {
			mid = (l + r) >> 1;
			if (x >= E[mid]) {
				l = mid + 1;
				ret = mid;
			} else r = mid - 1;
		}
		return ret;
	}

	public Cursor Index2Cursor(int x) {
		if (S == 0 || x < 0) return new Cursor(0, 0);
		if (x >= E[S]) return new Cursor(S - 1, get(S - 1));
		Cursor ret = new Cursor(findLine(x), 0);
		ret.column = x - E[ret.line];
		return ret;
	}

	public int Cursor2Index(Cursor x) {
		return E[x.line] + x.column;
	}

	public int length() {
		return E[S];
	}

	public int size() {
		return S;
	}

	public int getLineStart(int x) {
		checkBounds(x);
		return E[x];
	}

	public int getLineEnd(int x) {
		checkBounds(x);
		return E[x + 1];
	}

	public int get(int x) {
		checkBounds(x);
		return E[x + 1] - E[x];
	}

	public int getTrimmed(int x) {
		checkBounds(x);
		if (x == S - 1) return E[x + 1] - E[x];
		return E[x + 1] - E[x] - 1;
	}

	public void clear() {
		updateSize(0);
		E[0] = 0;
	}

	public boolean empty() {
		return S == 0;
	}

	public void merge(int l, int r, int tar) {
		if (l == r) return;
		if (l > r) throw new IllegalArgumentException("l > r (" + l + ", " + r + ")");
		checkBounds(l);
		checkBounds(r);
		final int len = r - l;
		tar = E[r + 1] - E[l] - tar;
		updateSize(S - len);
		for (int i = l + 1; i <= S; i++) E[i] = E[i + len] - tar;
	}

	public void increase(int x, int len) {
		checkBounds(x);
		for (int i = x + 1; i <= S; i++) E[i] += len;
	}

	public void set(int x, int len) {
		increase(x, len - get(x));
	}

	public void insert(int x, int len) {
		checkBounds(x);
		int[] dst;
		if (S + 1 >= E.length) {
			dst = new int[E.length + EXPAND_SIZE];
			System.arraycopy(E, 0, dst, 0, x + 1);
		} else dst = E;
		updateSize(S + 1);
		for (int i = S; i > x; i--) dst[i] = E[i - 1] + len;
		E = dst;
	}

	public void append(int len) {
		insert(S, len);
	}

	public void remove(int x) {
		checkBounds(x);
		final int len = E[x + 1] - E[x];
		for (int i = x + 1; i <= S; i++) E[i] = E[i + 1] - len;
		updateSize(S - 1);
	}

	public void removeRange(int l, int r) {
		if (l > r) throw new IllegalArgumentException("l > r (" + l + ", " + r + ")");
		checkBounds(l);
		checkBounds(r);
		final int len = E[r + 1] - E[l];
		final int tar = r - l + 1;
		updateSize(S - (r - l + 1));
		for (int i = l; i <= S + 1; i++) E[i] = E[i + tar] - len;
	}

	public void trim() {
		if (E.length == S + 1) return;
		int[] ori = E;
		E = new int[S + 1];
		System.arraycopy(ori, 0, E, 0, S + 1);
	}

	private int newSize(int size) {
		return (size + EXPAND_SIZE - 1) / EXPAND_SIZE * EXPAND_SIZE;
	}

	private void checkBounds(int x) {
		if (x < 0 || x > S) throw new ArrayIndexOutOfBoundsException(x);
	}

	private void updateSize(int tar) {
		if (S == tar) return;
		int ori = S;
		S = tar;
		if (listener != null) listener.onLineChanged(this, ori, tar);
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder("[");
		for (int i = 0; i < S; i++) ret.append(E[i]).append(", ");
		ret.append(E[S]).append(']');
		return ret.toString();
	}

	public interface LineChangeListener {
		void onLineChanged(LineManager manager, int ori, int cur);
	}
}