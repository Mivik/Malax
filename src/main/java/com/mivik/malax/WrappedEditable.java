package com.mivik.malax;

import com.mivik.mlexer.Cursor;
import com.mivik.mlexer.RangeSelection;

import java.util.HashSet;
import java.util.Set;

public class WrappedEditable<T extends Cursor> extends Editable<T> {

	public static final int MERGE_ACTIONS_INTERVAL = 250;

	protected Editable<T> E;
	protected EditActionStack A = new EditActionStack();
	private CursorListener<T> _CursorListener;
	private boolean _Editable = true;

	public WrappedEditable(Editable<T> editable) {
		this.E = editable;
	}

	public CursorListener<T> getCursorListener() {
		return _CursorListener;
	}

	public void setCursorListener(CursorListener<T> listener) {
		this._CursorListener = listener;
	}

	public boolean addEditActionListener(EditActionListener listener) {
		return A.addEditActionListener(listener);
	}

	public boolean removeEditActionListener(EditActionListener listener) {
		return A.removeEditActionListener(listener);
	}

	public void clearEditActionListeners() {
		A.clearEditActionListeners();
	}

	public boolean isEditable() {
		return _Editable;
	}

	public void setEditable(boolean flag) {
		this._Editable = flag;
	}

	public boolean undo() {
		if (!_Editable) return false;
		return A.undo();
	}

	public boolean redo() {
		if (!_Editable) return false;
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
		if (!_Editable) return;
		A.addAction(new ClearAction());
	}

	@Override
	public T insert(T st, char c) {
		if (!_Editable) return (T) st.clone();
		return A.addAction(new InsertCharAction(st, c)).rig;
	}

	@Override
	public T insert(T st, char[] cs, int off, int len) {
		if (!_Editable) return (T) st.clone();
		if (len == 1) return insert(st, cs[off]);
		return A.addAction(new InsertCharsAction(st, cs, off, len)).rig;
	}

	@Override
	public void delete(T en) {
		if (!_Editable) return;
		A.addAction(new DeleteCharAction(en));
	}

	@Override
	public void delete(RangeSelection<T> sel) {
		if (!_Editable) return;
		A.addAction(new DeleteCharsAction(sel));
	}

	@Override
	public T replace(RangeSelection<T> sel, char[] cs, int off, int len) {
		if (!_Editable) return (T) sel.end.clone();
		return A.addAction(new ReplaceAction(sel, cs, off, len)).nsel.end;
	}

	@Override
	public void setText(char[] cs, int off, int len) {
		if (!_Editable) return;
		A.addAction(new SetTextAction(cs, off, len));
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

	public Editable<T> unwrap() {
		return E;
	}

	private MergedAction mergeActions(EditAction ori, EditAction ac) {
		if (ori instanceof MergedAction) {
			MergedAction ret = (MergedAction) ori;
			ret.append(ac);
			return ret;
		} else return new MergedAction(new EditAction[]{ori, ac});
	}

	public class ClearAction implements EditAction {
		public char[] cs;

		public ClearAction() {
			cs = E.toCharArray();
		}

		@Override
		public void undo() {
			E.insert(getBeginCursor(), cs, 0, cs.length);
			updateCursor(E.getEndCursor());
		}

		@Override
		public void redo() {
			E.clear();
			updateCursor(E.getEndCursor());
		}
	}

	public static class MergedAction implements EditAction {
		public static final int MERGE_BUFFER = 16;

		public EditAction[] actions;
		public int pos;

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

	public class SetTextAction implements EditAction {
		public char[] ocs, ncs;

		public SetTextAction(char[] cs, int off, int len) {
			ocs = E.toCharArray();
			ncs = new char[len];
			System.arraycopy(cs, off, ncs, 0, len);
		}

		@Override
		public void undo() {
			E.clear();
			E.insert(E.getBeginCursor(), ocs);
			updateCursor(E.getEndCursor());
		}

		@Override
		public void redo() {
			E.clear();
			E.insert(E.getBeginCursor(), ncs);
			updateCursor(E.getEndCursor());
		}
	}

	public class InsertCharsAction implements EditAction {
		public T lef, rig;
		public char[] cs;

		public InsertCharsAction(T cursor, char[] cs, int off, int len) {
			this.lef = (T) cursor.clone();
			this.rig = null;
			this.cs = new char[len];
			System.arraycopy(cs, off, this.cs, 0, len);
		}

		@Override
		public void undo() {
			E.delete(rig, cs.length);
			updateCursor(lef);
		}

		@Override
		public void redo() {
			rig = E.insert(lef, cs, 0, cs.length);
			updateCursor(rig);
		}
	}

	public class DeleteCharsAction implements EditAction {
		public RangeSelection<T> sel;
		public char[] cs;

		public DeleteCharsAction(RangeSelection<T> sel) {
			this.sel = sel.clone();
			this.cs = substring(sel).toCharArray();
		}

		@Override
		public void undo() {
			E.insert(sel.begin, cs, 0, cs.length);
			updateCursor(sel.end);
		}

		@Override
		public void redo() {
			E.delete(sel);
			updateCursor(sel.begin);
		}
	}

	public class InsertCharAction implements EditAction {
		public T lef, rig;
		public char ch;

		public InsertCharAction(T cursor, char ch) {
			this.lef = (T) cursor.clone();
			this.rig = null;
			this.ch = ch;
		}

		@Override
		public void undo() {
			E.delete(rig);
			updateCursor(lef);
		}

		@Override
		public void redo() {
			rig = E.insert(lef, ch);
			updateCursor(rig);
		}
	}

	public class DeleteCharAction implements EditAction {
		public T lef, rig;
		public char ch;

		public DeleteCharAction(T cursor) {
			this.lef = (T) cursor.clone();
			this.rig = (T) cursor.clone();
			E.moveBack(lef);
			this.ch = E.charAt(lef);
		}

		@Override
		public void undo() {
			E.insert(lef, ch);
			updateCursor(rig);
		}

		@Override
		public void redo() {
			E.delete(rig);
			updateCursor(lef);
		}
	}

	public class ReplaceAction implements EditAction {
		public RangeSelection<T> osel;
		public RangeSelection<T> nsel;
		public char[] ocs;
		public char[] ncs;

		public ReplaceAction(RangeSelection<T> sel, char[] cs, int off, int len) {
			this.osel = sel.clone();
			this.nsel = new RangeSelection<>(sel.begin, sel.begin);
			this.ocs = E.subChars(osel);
			this.ncs = new char[len];
			System.arraycopy(cs, off, this.ncs, 0, len);
		}

		@Override
		public void undo() {
			E.replace(nsel, ocs);
			updateCursor(osel.end);
		}

		@Override
		public void redo() {
			nsel.end = E.replace(osel, ncs);
			updateCursor(nsel.end);
		}
	}

	private void updateCursor(T x) {
		if (_CursorListener != null) _CursorListener.onCursorMoved(x);
	}

	public interface CursorListener<T extends Cursor> {
		void onCursorMoved(T cursor);
	}

	public interface EditActionListener {
		boolean beforeAction(WrappedEditable wrappedEditable, EditAction action);

		void afterAction(WrappedEditable wrappedEditable, EditAction action);
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
		private final Set<EditActionListener> listeners = new HashSet<>();

		EditActionStack() {
			this(64);
		}

		EditActionStack(int maxSize) {
			setMaxSize(maxSize);
		}

		public boolean addEditActionListener(EditActionListener listener) {
			synchronized (listeners) {
				return this.listeners.add(listener);
			}
		}

		public boolean removeEditActionListener(EditActionListener listener) {
			synchronized (listeners) {
				return this.listeners.remove(listener);
			}
		}

		public void clearEditActionListeners() {
			synchronized (listeners) {
				listeners.clear();
			}
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

		public <T extends EditAction> T addAction(T action) {
			synchronized (listeners) {
				boolean has = false;
				for (EditActionListener listener : listeners)
					has |= listener.beforeAction(WrappedEditable.this, action);
				if (has) return action;
			}
			action.redo();
			synchronized (listeners) {
				for (EditActionListener listener : listeners) listener.afterAction(WrappedEditable.this, action);
			}
			long t = System.currentTimeMillis();
			long cur = t - LastActionTime;
			LastActionTime = t;
			if (cur <= MERGE_ACTIONS_INTERVAL) {
				EditAction lac = getLastAction();
				if (lac != null) {
					setLastAction(mergeActions(lac, action));
					return action;
				}
			}
			arr[tot++] = action;
			if (tot == arr.length) tot = 0;
			return action;
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