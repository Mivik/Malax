package com.mivik.malax;

public class UndoableMalax extends UndoableEditable<Malax.Cursor> {
	public UndoableMalax(Malax malax) {
		super(malax);
	}

	private UndoableMalax(Editable<Malax.Cursor> editable) {
		super(editable);
	}
}