package com.mivik.malax;

public class UndoableMalax extends Malax {

	public static final int DOUBLE_CLICK_INTERVAL = 300, MERGE_ACTIONS_INTERVAL = 250;

	private EditActionStack A = new EditActionStack();

	public UndoableMalax() {
		_clear();
	}

	public UndoableMalax(CharSequence cs) {
		this(CharSequence2CharArray(cs), 0, cs.length());
	}

	public UndoableMalax(char[] cs) {
		this(cs, 0, cs.length);
	}

	public UndoableMalax(char[] cs, int off) {
		this(cs, off, cs.length - off);
	}

	public UndoableMalax(char[] cs, int off, int len) {
		super(cs, off, len);
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
	public void deleteChars(int x, int len) {
		A.addAction(new DeleteCharsAction(Index2Cursor(x), len));
	}

	@Override
	public void deleteChar(int x) {
		A.addAction(new DeleteCharAction(Index2Cursor(x)));
	}

	@Override
	public void append(CharSequence s) {
		A.addAction(new InsertCharsAction(getEndCursor(), CharSequence2CharArray(s), 0, s.length()));
	}

	@Override
	public void appendChar(char c) {
		A.addAction(new InsertCharAction(getEndCursor(), c));
	}

	@Override
	public void appendChars(char[] cs) {
		A.addAction(new InsertCharsAction(getEndCursor(), cs, 0, cs.length));
	}

	@Override
	public void appendChars(char[] cs, int off) {
		A.addAction(new InsertCharsAction(getEndCursor(), cs, off, cs.length - off));
	}

	@Override
	public void appendChars(char[] cs, int off, int len) {
		A.addAction(new InsertCharsAction(getEndCursor(), cs, off, len));
	}

	@Override
	public void insert(int x, CharSequence s) {
		A.addAction(new InsertCharsAction(Index2Cursor(x), CharSequence2CharArray(s), 0, s.length()));
	}

	@Override
	public void insertChars(int x, char[] cs) {
		A.addAction(new InsertCharsAction(Index2Cursor(x), cs, 0, cs.length));
	}

	@Override
	public void insertChars(int x, char[] cs, int off) {
		A.addAction(new InsertCharsAction(Index2Cursor(x), cs, off, cs.length - off));
	}

	@Override
	public void insertChars(int x, char[] cs, int off, int len) {
		A.addAction(new InsertCharsAction(Index2Cursor(x), cs, off, len));
	}

	@Override
	public void insert(Cursor x, String s) {
		A.addAction(new InsertCharsAction(x, s.toCharArray(), 0, s.length()));
	}

	@Override
	public void insertChars(Cursor x, char[] cs) {
		A.addAction(new InsertCharsAction(x, cs, 0, cs.length));
	}

	@Override
	public void insertChars(Cursor x, char[] cs, int off) {
		A.addAction(new InsertCharsAction(x, cs, off, cs.length - off));
	}

	@Override
	public void insertChar(int x, char c) {
		A.addAction(new InsertCharAction(Index2Cursor(x), c));
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
			cs = toCharArray();
		}

		@Override
		public void undo() {
			_insertChars(getBeginCursor(), cs, 0, cs.length);
		}

		@Override
		public void redo() {
			_clear();
		}
	}

	public class MergedAction implements EditAction {
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
				narr = null;
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
		private Cursor lef, rig;
		private char[] cs;

		public InsertCharsAction(Cursor cursor, char[] cs, int off, int len) {
			this.lef = cursor.clone();
			this.rig = cursor.clone();
			moveForward(rig, len);
			this.cs = new char[len];
			System.arraycopy(cs, off, this.cs, 0, len);
		}

		@Override
		public void undo() {
			_deleteChars(rig, cs.length);
		}

		@Override
		public void redo() {
			_insertChars(lef, cs, 0, cs.length);
		}
	}

	public class DeleteCharsAction implements EditAction {
		private Cursor lef, rig;
		private char[] cs;

		public DeleteCharsAction(Cursor cursor, int len) {
			this.lef = cursor.clone();
			this.rig = cursor.clone();
			moveBack(lef, len);
			this.cs = substring(lef, len).toCharArray();
		}

		@Override
		public void undo() {
			_insertChars(lef, cs, 0, cs.length);
		}

		@Override
		public void redo() {
			_deleteChars(rig, cs.length);
		}
	}

	public class InsertCharAction implements EditAction {
		private Cursor lef, rig;
		private char ch;

		public InsertCharAction(Cursor cursor, char ch) {
			this.lef = cursor.clone();
			this.rig = cursor.clone();
			moveForward(rig);
			this.ch = ch;
		}

		@Override
		public void undo() {
			_deleteChar(rig);
		}

		@Override
		public void redo() {
			_insertChar(lef, ch);
		}
	}

	public class DeleteCharAction implements EditAction {
		private Cursor lef, rig;
		private char ch;

		public DeleteCharAction(Cursor cursor) {
			this.lef = cursor.clone();
			this.rig = cursor.clone();
			moveBack(lef);
			this.ch = S[lef.line][lef.column];
		}

		@Override
		public void undo() {
			_insertChar(lef, ch);
		}

		@Override
		public void redo() {
			_deleteChar(rig);
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