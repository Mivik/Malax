package com.mivik.malax;

import com.mivik.mlexer.JSONLexer;
import com.mivik.mlexer.MLexer;

import java.util.Random;

public class TestClass {
	public static void main(String[] args) {
		Malax malax = new Malax("123\n456");
		malax.insert(malax.Index2Cursor(1), "78\na23\nb");
		System.out.println(malax);
	}

	private static void printState(Malax doc) {
		MLexer lexer = doc.getLexer();
		for (int i = 1; i <= lexer.DS[0]; i++)
			System.out.println(lexer.getTypeName(lexer.D[i]) + ":" + lexer.getTrimmedPartText(i));
		System.out.println("============");
	}

	private static void MalaxBenchmark() {
		final int count = 1024 * 20;
		long st = System.currentTimeMillis();
		Malax doc = new Malax("");
		doc.setLexer(new JSONLexer());
		Random random = new Random();
		for (int i = 0; i < count; i++) {
			int ind = random.nextInt(doc.length() + 1);
			Malax.Cursor cursor = doc.Index2Cursor(ind);
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