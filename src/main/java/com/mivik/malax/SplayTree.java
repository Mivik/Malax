package com.mivik.malax;

import java.util.Arrays;
import java.util.Stack;

public class SplayTree {
	private SplayNode root = null;

	public SplayTree() {
	}

	public SplayTree(int[] a) {
		this(a, 0, a.length);
	}

	public SplayTree(int[] a, int off) {
		this(a, off, a.length - off);
	}

	public SplayTree(int[] a, int off, int len) {
		root = SplayNode.build(a, off, len);
	}

	public int moveBack(Malax.Cursor cursor, int dis) {
		if (dis == 0) return 0;
		if (cursor.column == 0 && cursor.line == 0) return 0;
		int odis = dis;
		while (dis > cursor.column) {
			dis -= cursor.column + 1;
			if (cursor.line == 0) {
				cursor.column = 0;
				return odis - dis;
			}
			cursor.column = get(--cursor.line) - 1;
		}
		cursor.column -= dis;
		return odis;
	}

	public int moveForward(Malax.Cursor cursor, int dis) {
		if (dis == 0) return 0;
		int len = get(cursor.line);
		if (cursor.line == size() - 1 && cursor.column == len) return 0;
		int odis = dis;
		while (dis >= len - cursor.column) {
			dis -= len - cursor.column;
			if (cursor.line == size() - 1) {
				cursor.column = len;
				return dis - odis;
			}
			len = get(++cursor.line);
			cursor.column = 0;
		}
		cursor.column += dis;
		return odis;
	}

	public boolean moveBack(Malax.Cursor cursor) {
		if (cursor.column == 0) {
			if (cursor.line == 0) return false;
			else cursor.column = get(--cursor.line) - 1;
		} else cursor.column--;
		return true;
	}

	public boolean moveForward(Malax.Cursor cursor) {
		if (cursor.line == size() - 1) {
			if (cursor.column == get(cursor.line)) return false;
			else ++cursor.column;
		} else {
			if (cursor.column == get(cursor.line) - 1) {
				++cursor.line;
				cursor.column = 0;
			} else ++cursor.column;
		}
		return true;
	}

	public Malax.Cursor getBeginCursor() {
		return new Malax.Cursor(0, 0);
	}

	public Malax.Cursor getEndCursor() {
		if (root == null) return new Malax.Cursor(0, 0);
		return new Malax.Cursor(root.size - 1, end().val);
	}

	public SplayNode end() {
		return root == null ? null : root.maximum();
	}

	public SplayNode begin() {
		return root == null ? null : root.minimum();
	}

	public int sum() {
		return root == null ? 0 : root.sum;
	}

	public int size() {
		return root == null ? 0 : root.size;
	}

	public boolean empty() {
		return root == null;
	}

	public void clear() {
		root = null;
	}

	public int indexOf(SplayNode x) {
		splay(x);
		return x.son[0] == null ? 0 : x.son[0].size;
	}

	public SplayNode find(int x) {
		if (root == null) return null;
		++x;
		if (x <= 0) return null;
		if (x > root.sum) return end();
		SplayNode cur = root;
		while (true) {
			int lef = 0;
			if (cur.son[0] != null) lef = cur.son[0].sum;
			if (x <= lef) cur = cur.son[0];
			else {
				x -= lef;
				if (x <= cur.val) return cur;
				x -= cur.val;
				cur = cur.son[1];
			}
		}
	}

	public Malax.Cursor Index2Cursor(int x) {
		SplayNode ret = find(x);
		if (ret == null) return new Malax.Cursor(0, 0);
		splay(ret);
		int lsiz = 0, lsum = 0;
		SplayNode lef = ret.son[0];
		if (lef != null) {
			lsiz = lef.size;
			lsum = lef.sum;
		}
		return new Malax.Cursor(lsiz, x - lsum);
	}

	public int Cursor2Index(Malax.Cursor x) {
		if (root == null) return 0;
		if (x.line < 0) return 0;
		if (x.line >= root.size) return root.sum - 1;
		int ret = x.column;
		if (x.line != 0) ret += getPrefixSum(x.line - 1);
		return ret;
	}

	public int get(int k) {
		if (root == null || (k >= root.size || k < 0)) throw new ArrayIndexOutOfBoundsException(k);
		return getKth(k).val;
	}

	public void set(int k, int val) {
		if (root == null || (k >= root.size || k < 0)) throw new ArrayIndexOutOfBoundsException(k);
		set(getKth(k), val);
	}

	public void set(SplayNode x, int val) {
		x.val = val;
		splay(x);
	}

	public int getPrefixSum(int k) {
		if (root == null || (k >= root.size || k < 0)) throw new ArrayIndexOutOfBoundsException(k);
		if (k == root.size - 1) return root.sum;
		splay(getKth(k + 1));
		return root.son[0].sum;
	}

	public int getSuffixSum(int k) {
		if (root == null || (k >= root.size || k < 0)) throw new ArrayIndexOutOfBoundsException(k);
		if (k == 0) return root.sum;
		splay(getKth(k - 1));
		return root.son[1].sum;
	}

	public SplayNode append(int val) {
		if (root == null) return root = new SplayNode(null, val);
		return splay(root.maximum().setSon(1, val));
	}

	public SplayNode appendAll(int[] a) {
		return appendAll(a, 0, a.length);
	}

	public SplayNode appendAll(int[] a, int off) {
		return appendAll(a, off, a.length - off);
	}

	public SplayNode appendAll(int[] a, int off, int len) {
		if (root == null) return root = SplayNode.build(a);
		SplayNode cur = root.maximum();
		SplayNode ret = cur.son[1] = SplayNode.build(a, off, len);
		ret.fa = cur;
		return ret;
	}

	public void removeSons(SplayNode x) {
		x.son[0] = x.son[1] = null;
		splay(x);
	}

	public void remove(int k) {
		if (root == null || (k >= root.size || k < 0)) throw new ArrayIndexOutOfBoundsException(k);
		if (root.size == 1) {
			root = null;
			return;
		}
		if (k == 0) {
			splay(getKth(k + 1));
			root.son[0] = null;
			root.pushUp();
			return;
		}
		if (k == root.size - 1) {
			splay(getKth(k - 1));
			root.son[1] = null;
			root.pushUp();
			return;
		}
		splay(getKth(k - 1));
		splay(getKth(k + 1), root);
		root.son[1].son[0] = null;
		root.son[1].pushUp();
		root.pushUp();
	}

	public SplayNode getRange(int l, int r) {
		if (root == null || (l < 0 || l >= root.size)) throw new ArrayIndexOutOfBoundsException(l);
		if (r < 0 || r >= root.size) throw new ArrayIndexOutOfBoundsException(r);
		if (l > r) throw new IllegalArgumentException("l cannot be greater then r");
		if (l == 0) {
			if (r == root.size - 1) return root;
			return splay(getKth(r + 1)).son[0];
		}
		if (r == root.size - 1) return splay(getKth(l - 1)).son[1];
		splay(getKth(l - 1));
		splay(getKth(r + 1), root);
		return root.son[1].son[0];
	}

	public void removeRange(int l, int r) {
		if (root == null || (l < 0 || l >= root.size)) throw new ArrayIndexOutOfBoundsException(l);
		if (r < 0 || r >= root.size) throw new ArrayIndexOutOfBoundsException(r);
		if (l > r) throw new IllegalArgumentException("l cannot be greater then r");
		if (l == 0) {
			splay(getKth(r + 1));
			root.son[0] = null;
			root.pushUp();
			return;
		}
		if (r == root.size - 1) {
			splay(getKth(l - 1));
			root.son[1] = null;
			root.pushUp();
			return;
		}
		splay(getKth(l - 1));
		splay(getKth(r + 1), root);
		root.son[1].son[0] = null;
		root.son[1].pushUp();
		root.pushUp();
	}

	public SplayNode insertAll(int k, int[] a) {
		return insertAll(k, a, 0, a.length);
	}

	public SplayNode insertAll(int k, int[] a, int off) {
		return insertAll(k, a, off, a.length - off);
	}

	public SplayNode insertAll(int k, int[] a, int off, int len) {
		if (root == null) {
			if (k != 0) throw new ArrayIndexOutOfBoundsException(k);
			return root = SplayNode.build(a, off, len);
		}
		if (k > root.size || k < 0) throw new ArrayIndexOutOfBoundsException(k);
		SplayNode ret = SplayNode.build(a, off, len);
		if (k == 0) {
			SplayNode cur = root.minimum();
			cur.son[0] = ret;
			ret.fa = cur;
			return splay(ret);
		}
		if (k == root.size) {
			SplayNode cur = root.maximum();
			cur.son[1] = ret;
			ret.fa = cur;
			return splay(ret);
		}
		splay(getKth(k - 1));
		if (k == root.size - 1) {
			root.son[1] = ret;
			ret.fa = root;
			root.pushUp();
			return ret;
		}
		splay(getKth(k), root);
		root.son[1].son[0] = ret;
		ret.fa = root.son[1];
		root.son[1].pushUp();
		root.pushUp();
		return ret;
	}

	public SplayNode insert(int k, int val) {
		if (root == null) {
			if (k != 0) throw new ArrayIndexOutOfBoundsException(k);
			return root = new SplayNode(null, val);
		}
		if (k > root.size || k < 0) throw new ArrayIndexOutOfBoundsException(k);
		if (k == 0) return splay(root.minimum().setSon(0, val));
		if (k == root.size) return splay(root.maximum().setSon(1, val));
		splay(getKth(k - 1));
		splay(getKth(k), root);
		SplayNode ret = root.son[1].setSon(0, val);
		root.son[1].pushUp();
		root.pushUp();
		return ret;
	}

	public SplayNode getKth(int k) {
		if (root == null || (k >= root.size || k < 0)) throw new ArrayIndexOutOfBoundsException(k);
		++k;
		SplayNode cur = root;
		while (true) {
			int siz = 0;
			if (cur.son[0] != null) siz = cur.son[0].size;
			if (k <= siz) cur = cur.son[0];
			else {
				k -= siz + 1;
				if (k == 0) return cur;
				cur = cur.son[1];
			}
		}
	}

	public SplayNode splay(SplayNode x) {
		return splay(x, null);
	}

	public SplayNode splay(SplayNode x, SplayNode tar) {
		SplayNode y;
		while ((y = x.fa) != tar) {
			if (y.fa != tar) (x.side() == y.side() ? y : x).rotate();
			x.rotate();
		}
		if (tar == null) root = x;
		x.pushUp();
		return x;
	}

	public void dump(int[] a) {
		if (root == null) return;
		root.dump(a);
	}

	public void dump(int[] a, int off) {
		if (root == null) return;
		root.dump(a, off);
	}

	public int[] toArray() {
		if (root == null) return new int[0];
		return root.toArray();
	}

	public void rebuild() {
		if (root == null) return;
		root.rebuild();
	}

	@Override
	public String toString() {
		return Arrays.toString(toArray());
	}

	public static class SplayNode {
		SplayNode fa;
		SplayNode[] son = new SplayNode[2];
		int val;
		int sum;
		int size;

		public static SplayNode build(int[] a) {
			return build(a, 0, a.length);
		}

		public static SplayNode build(int[] a, int off) {
			return build(a, off, a.length - off);
		}

		public static SplayNode build(int[] a, int off, int len) {
			if (len == 0) return null;
			int mid = len >> 1;
			SplayNode x = new SplayNode(null, a[off + mid]);
			x.son[0] = build(a, off, mid);
			if (x.son[0] != null) x.son[0].fa = x;
			x.son[1] = build(a, off + mid + 1, len - mid - 1);
			if (x.son[1] != null) x.son[1].fa = x;
			x.pushUp();
			return x;
		}

		public SplayNode(SplayNode fa, int val) {
			this.fa = fa;
			this.sum = this.val = val;
			this.size = 1;
		}

		private void pushUp() {
			sum = val;
			size = 1;
			if (son[0] != null) {
				sum += son[0].sum;
				size += son[0].size;
			}
			if (son[1] != null) {
				sum += son[1].sum;
				size += son[1].size;
			}
		}

		public int size() {
			return size;
		}

		public void rotate() {
			SplayNode y = fa, z = y.fa;
			int d = side();
			if (z != null) z.son[y.side()] = this;
			fa = z;
			SplayNode rev = son[d ^ 1];
			y.son[d] = rev;
			if (rev != null) rev.fa = y;
			son[d ^ 1] = y;
			y.fa = this;
			y.pushUp();
		}

		public SplayNode minimum() {
			SplayNode ret = this;
			while (ret.son[0] != null) ret = ret.son[0];
			return ret;
		}

		public SplayNode maximum() {
			SplayNode ret = this;
			while (ret.son[1] != null) ret = ret.son[1];
			return ret;
		}

		public SplayNode predecessor() {
			if (son[0] != null) return son[0].maximum();
			SplayNode cur = this;
			while (true) {
				SplayNode fa = cur.fa;
				if (fa == null) return null;
				if (fa.son[1] == cur) return fa;
				cur = fa;
			}
		}

		public SplayNode successor() {
			if (son[1] != null) return son[1].minimum();
			SplayNode cur = this;
			while (true) {
				SplayNode fa = cur.fa;
				if (fa == null) return null;
				if (fa.son[0] == cur) return fa;
				cur = fa;
			}
		}

		public SplayNode setSon(int side, int val) {
			return son[side] = new SplayNode(this, val);
		}

		public int side() {
			return fa.son[1] == this ? 1 : 0;
		}

		public void dump(int[] a) {
			dump(a, 0);
		}

		public void dump(final int[] a, int off) {
			if (off + size > a.length) throw new IllegalArgumentException("The size of array is too small");
			applyIteratively(new DumpVisitor(a, off));
		}

		public int[] toArray() {
			int[] ret = new int[size];
			applyIteratively(new DumpVisitor(ret));
			return ret;
		}

		public boolean applyRecursively(NodeVisitor visitor) {
			if (son[0] != null) if (son[0].applyRecursively(visitor)) return true;
			if (visitor.visit(this)) return true;
			if (son[1] != null) if (son[1].applyRecursively(visitor)) return true;
			return false;
		}

		public void applyIteratively(NodeVisitor visitor) {
			Stack<SplayNode> stack = new Stack<>();
			SplayNode tmp;
			tmp = this;
			while (tmp != null) {
				stack.push(tmp);
				tmp = tmp.son[0];
			}
			while (!stack.empty()) {
				SplayNode cur = stack.pop();
				if (visitor.visit(cur)) return;
				tmp = cur.son[1];
				while (tmp != null) {
					stack.push(tmp);
					tmp = tmp.son[0];
				}
			}
		}

		public void rebuild() {
			int[] val = toArray();
			SplayNode cur = build(val);
			son[0] = cur.son[0];
			son[1] = cur.son[1];
			this.val = cur.val;
		}
	}

	public static class DumpVisitor implements NodeVisitor {
		private int[] a;
		private int tot;

		public DumpVisitor(int[] a) {
			this(a, 0);
		}

		public DumpVisitor(int[] a, int off) {
			this.a = a;
			this.tot = off;
		}

		@Override
		public boolean visit(SplayNode node) {
			a[tot++] = node.val;
			return false;
		}
	}

	public interface NodeVisitor {
		boolean visit(SplayNode node);
	}
}