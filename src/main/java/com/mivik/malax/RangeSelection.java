package com.mivik.malax;

public class RangeSelection implements Cloneable {
	Cursor begin, end;

	public RangeSelection(Cursor cursor) {
		this.begin = cursor.clone();
		this.end = cursor.clone();
	}

	public RangeSelection(Cursor begin, Cursor end) {
		this.begin = begin.clone();
		this.end = end.clone();
	}

	public RangeSelection(RangeSelection ori) {
		this.begin = ori.begin.clone();
		this.end = ori.end.clone();
	}

	@Override
	public RangeSelection clone() {
		return new RangeSelection(this);
	}

	@Override
	public int hashCode() {
		return begin.hashCode() ^ end.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof RangeSelection)) return false;
		RangeSelection t = (RangeSelection) obj;
		return begin.equals(t.begin) && end.equals(t.end);
	}

	@Override
	public String toString() {
		return "[" + begin + " ~ " + end + ']';
	}
}