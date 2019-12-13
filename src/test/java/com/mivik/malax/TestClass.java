package com.mivik.malax;

import java.util.Random;

public class TestClass {
	public static void main(String[] args) {
		UndoableDocument doc = new UndoableDocument("qw\n012345\ned");
		doc.deleteChars(5, 4);
		System.out.println(doc);
		System.out.println("=================");
		doc.undo();
		System.out.println(doc);
	}

	private static void DocumentBenchmark() {
		final int count = 1024 * 100;
		long st = System.currentTimeMillis();
		Document doc = new Document("".toCharArray());
		Random random = new Random();
		for (int i = 0; i < count; i++) {
			int ind = random.nextInt(doc.length() + 1);
			if (random.nextInt(8) == 0) doc.insertChar(ind, '\n');
			else doc.insertChar(ind, (char) (random.nextInt(95) + 32));
		}
		st = System.currentTimeMillis() - st;
		System.out.println("插入" + count + "次耗时: " + st + "ms");
		System.out.println("平均单次插入耗时: " + ((double) st / count) + "ms");
	}

	private static void SplayBenchmark() {
		int i;
		final int size = 1000000; //序列长度
		final int count = 1000000; //查询次数
		SplayTree tree = new SplayTree();
		Random random = new Random();
		//插入{size}个随机数
		for (i = 0; i < size; i++) tree.append(random.nextInt(200));

		//查询{count}次
		long st = System.currentTimeMillis();
		for (i = 0; i < count; i++) tree.getPrefixSum(random.nextInt(size));
		st = System.currentTimeMillis() - st;
		System.out.println("序列长度: " + size);
		System.out.println("查询" + count + "次前缀和总耗时: " + st + "ms");
		System.out.println("平均单次查询前缀和耗时: " + ((double) st / count) + "ms");
	}
}