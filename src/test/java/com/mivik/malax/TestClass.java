package com.mivik.malax;

import com.mivik.mlexer.JSONLexer;
import com.mivik.mlexer.MLexer;

import java.util.Random;

public class TestClass {
	public static void main(String[] args) {
		Malax malax = new Malax(new BaseMalax("d\n12213"));
		malax.setContentChangeListener(new BaseMalax.ContentChangeListener() {
			@Override
			public void onExpand(int st, int en) {
				System.out.println("expand: " + st + " " + en);
			}

			@Override
			public void onMerge(int st, int en) {
				System.out.println("merge: " + st + " " + en);
			}

			@Override
			public void onLineUpdated(int x) {
				System.out.println("update: " + x);
			}
		});
		LineManager line = malax.getLineManager();
		malax.delete(malax.Index2Cursor(3), 2);
		malax.insert(malax.Index2Cursor(1), "\n\n");
		malax.delete(malax.Index2Cursor(3), 2);
		System.out.println(malax);
	}

	private static void printState(BaseMalax doc) {
		MLexer lexer = doc.getLexer();
		if (lexer == null) {
			System.out.println("[Null Lexer]");
			return;
		}
		for (int i = 1; i <= lexer.DS[0]; i++)
			System.out.println(lexer.getTypeName(lexer.D[i]) + ":" + lexer.getTrimmedPartText(i));
		System.out.println("============");
	}

	private static void MalaxBenchmark() {
		final int count = 1024 * 20;
		long st = System.currentTimeMillis();
		BaseMalax doc = new BaseMalax("");
		doc.setLexer(new JSONLexer());
		Random random = new Random();
		for (int i = 0; i < count; i++) {
			int ind = random.nextInt(doc.length() + 1);
			BaseMalax.Cursor cursor = doc.Index2Cursor(ind);
			if (random.nextInt(8) == 0) doc.insert(cursor, '\n');
			else if (random.nextInt(4) == 0) doc.delete(cursor);
			else doc.insert(cursor, (char) (random.nextInt(95) + 32));
		}
		st = System.currentTimeMillis() - st;
		System.out.println("插入" + count + "次耗时: " + st + "ms");
		System.out.println("平均单次插入耗时: " + ((double) st / count) + "ms");
//		printState(doc);
	}
}