package com.mivik.malax;

import com.mivik.mlexer.Cursor;
import com.mivik.mlexer.RangeSelection;

public class UndoableEditable<T extends Cursor> extends Editable<T> {

	public static final int MERGE_ACTIONS_INTERVAL = 250;

	protected Editable<T> E;
	protected EditActionStack A = new EditActionStack();

	public UndoableEditable(Editable<T> editable) {
		this.E = editable;
	}

	public boolean undo() {
		return A.undo();
	}

	public boolean redo() {
		return A.redo();
	}

	public EditAction getLastAction() {
		return A.getLastAction();
	}

	public void setMaxUndoSize(int size) {
		A.setMaxSize(size);
	}

	public int getMaxUndoSize() {
		return A.getMaxSize();
	}

	@Override
	public void clear() {
		A.addAction(new ClearAction());
	}

	@Override
	public void insert(T st, char c) {
		A.addAction(new InsertCharAction(st, c));
	}

	@Override
	public void insert(T st, char[] cs, int off, int len) {
		A.addAction(new InsertCharsAction(st, cs, off, len));
	}

	@Override
	public void delete(T en) {
		A.addAction(new DeleteCharAction(en));
	}

	@Override
	public void delete(RangeSelection<T> sel) {
		A.addAction(new DeleteCharsAction(sel));
	}

	@Override
	public void replace(RangeSelection<T> sel, char[] cs, int off, int len) {
		A.addAction(new ReplaceAction(sel, cs, off, len));
	}

	@Override
	public T getBeginCursor() {
		return E.getBeginCursor();
	}

	@Override
	public T getEndCursor() {
		return E.getEndCursor();
	}

	@Override
	public char charAt(T x) {
		return E.charAt(x);
	}

	@Override
	public boolean moveBack(T x) {
		return E.moveBack(x);
	}

	@Override
	public boolean moveForward(T x) {
		return E.moveForward(x);
	}

	@Override
	public int moveBack(T x, int dis) {
		return E.moveBack(x, dis);
	}

	@Override
	public int moveForward(T x, int dis) {
		return E.moveForward(x, dis);
	}

	@Override
	public StringBuilder subStringBuilder(RangeSelection<T> sel) {
		return E.subStringBuilder(sel);
	}

	@Override
	public int length() {
		return E.length();
	}

	@Override
	public T Index2Cursor(int ind) {
		return E.Index2Cursor(ind);
	}

	@Override
	public int Cursor2Index(T x) {
		return E.Cursor2Index(x);
	}

	@Override
	public char[] toCharArray() {
		return E.toCharArray();
	}

	private MergedAction mergeActions(EditAction ori, EditAction ac) {
		if (ori instanceof MergedAction) {
			MergedAction ret = (MergedAction) ori;
			ret.append(ac);
			return ret;
		} else return new MergedAction(new EditAction[]{ori, ac});
	}

	public class ClearAction implements EditAction {
		private char[] cs;

		public ClearAction() {
			cs = E.toCharArray();
		}

		@Override
		public void undo() {
			E.insert(getBeginCursor(), cs, 0, cs.length);
		}

		@Override
		public void redo() {
			E.clear();
		}
	}

	public static class MergedAction implements EditAction {
		public static final int MERGE_BUFFER = 16;

		private EditAction[] actions;
		private int pos;

		public MergedAction(EditAction[] actions) {
			this.actions = actions;
			this.pos = actions.length;
		}

		public EditAction[] getActions() {
			return actions;
		}

		public void append(EditAction action) {
			if (pos == actions.length) {
				EditAction[] narr = new EditAction[actions.length + MERGE_BUFFER];
				System.arraycopy(actions, 0, narr, 0, actions.length);
				actions = narr;
			}
			actions[pos++] = action;
		}

		@Override
		public void redo() {
			for (int i = 0; i < pos; i++) actions[i].redo();
		}

		@Override
		public void undo() {
			for (int i = pos - 1; i >= 0; i--) actions[i].undo();
		}
	}

	public class InsertCharsAction implements EditAction {
		private T lef, rig;
		private char[] cs;

		public InsertCharsAction(T cursor, char[] cs, int off, int len) {
			this.lef = (T) cursor.clone();
			this.rig = (T) cursor.clone();
			E.moveForward(rig, len);
			this.cs = new char[len];
			System.arraycopy(cs, off, this.cs, 0, len);
		}

		@Override
		public void undo() {
			E.delete(rig, cs.length);
		}

		@Override
		public void redo() {
			E.insert(lef, cs, 0, cs.length);
		}
	}

	public class DeleteCharsAction implements EditAction {
		private RangeSelection<T> sel;
		private char[] cs;

		public DeleteCharsAction(RangeSelection<T> sel) {
			this.sel = sel.clone();
			this.cs = substring(sel).toCharArray();
		}

		@Override
		public void undo() {
			E.insert(sel.begin, cs, 0, cs.length);
		}

		@Override
		public void redo() {
			E.delete(sel);
		}
	}

	public class InsertCharAction implements EditAction {
		private T lef, rig;
		private char ch;

		public InsertCharAction(T cursor, char ch) {
			this.lef = (T) cursor.clone();
			this.rig = (T) cursor.clone();
			E.moveForward(rig);
			this.ch = ch;
		}

		@Override
		public void undo() {
			E.delete(rig);
		}

		@Override
		public void redo() {
			E.insert(lef, ch);
		}
	}

	public class DeleteCharAction implements EditAction {
		private T lef, rig;
		private char ch;

		public DeleteCharAction(T cursor) {
			this.lef = (T) cursor.clone();
			this.rig = (T) cursor.clone();
			E.moveBack(lef);
			this.ch = E.charAt(lef);
		}

		@Override
		public void undo() {
			E.insert(lef, ch);
		}

		@Override
		public void redo() {
			E.delete(rig);
		}
	}

	public class ReplaceAction implements EditAction {
		private RangeSelection<T> osel;
		private RangeSelection<T> nsel;
		private char[] ocs;
		private char[] ncs;

		public ReplaceAction(RangeSelection<T> sel, char[] cs, int off, int len) {
			this.osel = sel.clone();
			this.nsel = new RangeSelection<>(sel.begin, sel.begin);
			E.moveForward(nsel.end, len);
			this.ocs = E.subChars(osel);
			this.ncs = new char[len];
			System.arraycopy(cs, off, this.ncs, 0, len);
		}

		@Override
		public void undo() {
			E.delete(nsel);
			E.insert(osel.begin, ocs);
		}

		@Override
		public void redo() {
			E.delete(osel);
			E.insert(nsel.begin, ncs);
		}
	}

	public interface EditAction {
		void undo();

		void redo();
	}

	public class EditActionStack {
		private EditAction[] arr;
		private int tot;
		private int cnt;
		private long LastActionTime = 0;

		EditActionStack() {
			this(64);
		}

		EditActionStack(int maxSize) {
			setMaxSize(maxSize);
		}

		public void setMaxSize(int size) {
			arr = new EditAction[size];
			tot = cnt = 0;
		}

		public void clear() {
			tot = cnt = 0;
		}

		public int getMaxSize() {
			return arr.length;
		}

		public void addAction(EditAction action) {
			long t = System.currentTimeMillis();
			long cur = t - LastActionTime;
			LastActionTime = t;
			if (cur <= MERGE_ACTIONS_INTERVAL) {
				EditAction lac = getLastAction();
				if (lac != null) {
					setLastAction(mergeActions(lac, action));
					action.redo();
					return;
				}
			}
			arr[tot++] = action;
			action.redo();
			if (tot == arr.length) tot = 0;
		}

		public boolean undo() {
			if (cnt >= arr.length) return false;
			if (tot == 0) tot = arr.length;
			tot--;
			if (arr[tot] == null) {
				if (++tot == arr.length) tot = 0;
				return false;
			}
			arr[tot].undo();
			cnt++;
			return true;
		}

		public boolean redo() {
			if (cnt == 0) return false;
			if (arr[tot] == null) return false;
			arr[tot].redo();
			if (++tot == arr.length) tot = 0;
			cnt--;
			return true;
		}

		public EditAction getLastAction() {
			if (cnt >= arr.length) return null;
			int pp = tot;
			if (pp == 0) pp = arr.length;
			return arr[pp - 1];
		}

		public void setLastAction(EditAction action) {
			if (cnt >= arr.length) return;
			int pp = tot;
			if (pp == 0) pp = arr.length;
			arr[pp - 1] = action;
		}
	}
}