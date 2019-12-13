package com.mivik.malax;

public class UndoableDocument extends Document {

	private class InsertCharsAction implements EditAction {
		private Cursor cursor;
		private char[] cs;

		public InsertCharsAction(Cursor cursor, char[] cs, int off, int len) {
			this.cursor = cursor;
			this.cs = new char[len];
			System.arraycopy(cs, off, this.cs, 0, len);
		}

		@Override
		public void undo() {
			Cursor q = cursor.clone();
			moveRight(q, cs.length);
			UndoableDocument.super.deleteChars(q, cs.length);
		}

		@Override
		public void redo() {
			UndoableDocument.super.insertChars(cursor, cs, 0, cs.length);
		}
	}

	private class DeleteCharsAction implements EditAction {
		private Cursor cursor;
		private char[] cs;

		public DeleteCharsAction(Cursor cursor, int len) {
			this.cursor = cursor.clone();
		}

		@Override
		public void undo() {

		}

		@Override
		public void redo() {

		}
	}

	private class InsertCharAction implements EditAction {
		private Cursor cursor;
		private char ch;

		public InsertCharAction(Cursor cursor, char ch) {
			this.cursor = cursor.clone();
			this.ch = ch;
		}

		@Override
		public void undo() {
			Cursor q = cursor.clone();
			moveRight(q);
			UndoableDocument.super.deleteChar(q);
		}

		@Override
		public void redo() {
			UndoableDocument.super.insertChar(cursor, ch);
		}
	}

	private class DeleteCharAction implements EditAction {
		private Cursor cursor;
		private char ch;

		public DeleteCharAction(Cursor cursor) {
			this.cursor = cursor.clone();
			Cursor tmp = cursor.clone();
			moveLeft(tmp);
			this.ch = S[tmp.line][tmp.column];
		}

		@Override
		public void undo() {
			Cursor q = cursor.clone();
			moveLeft(q);
			UndoableDocument.super.insertChar(q, ch);
		}

		@Override
		public void redo() {
			UndoableDocument.super.deleteChar(cursor);
		}
	}

	public interface EditAction {
		void undo();

		void redo();
	}
}