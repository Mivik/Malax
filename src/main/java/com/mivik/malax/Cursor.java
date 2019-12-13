package com.mivik.malax;

public class Cursor implements Comparable<Cursor>, Cloneable {
	int line, column;

	public Cursor() {
	}

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