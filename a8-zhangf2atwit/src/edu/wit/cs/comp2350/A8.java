package edu.wit.cs.comp2350;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/** Provides a solution to the 0-1 knapsack problem 
 * 
 * Wentworth Institute of Technology
 * COMP 2350
 * Assignment 8
 * 
 */

/**
 * FindDynamic uses 2D array of m by n size. best_price keeps track of the best value 
 * ItemIncluded array keeps track of whether or not to include a weight
 * when Figuring out which indices make up the best value. 
 * FindDynamic returning an array with the included items update the class variable best price 
 * with the optimal price in a solution
 * Source of video for Dynamic programming: https://www.youtube.com/watch?v=nLmhmB6NzcM
 * Main founction for knapsack problem:  P[v,w] = max {v[ i -1][w], v[i-1][w-w[i]+p[i]}
 * @param table  table of Items with a weight and value
 * @param capacity is the max weight allowed in the knapsack
 * @return array of indices of items that make the best value
 */

public class A8 {

	// TODO: document this method
	public static Item[] FindDynamic(Item[] table, int capacity) {
		// TODO: implement this method

		// since theyre accessed by i-1 below, +1 to include all values in table
		int m = table.length + 1;
		int n = capacity + 1;
		int[][] P = new int[m][n];

		// bool itemIncluded keeps track of whether or not to include a weight when
		// figuring out
		Boolean[][] itemIncluded = new Boolean[m][n]; // keep update the T or F values when number at index updates

		// Default all the value of first row and first cloumn for table
		for (int i = 0; i < m; i++) {
			P[i][0] = 0;
		}
		for (int j = 0; j < n; j++) {
			P[0][j] = 0;
		}

		/*If the weight of the current Item is less than the curr w value (meaning more
		 * can fit in the knapsack), then P[i][w] is calculated by finding the max of
		 * the last value, and value at the w val corresponding to w-w[i] (which
		 * represents the exact amount of weight that can still fit in the knapsack).
		 */
		for (int i = 1; i < m; i++) {
			for (int w = 1; w < n; w++) {
				
				if (table[i - 1].weight <= w) {
					P[i][w] = Math.max(P[i - 1][w], table[i - 1].price + P[i - 1][w - table[i - 1].weight]);

					if (P[i - 1][w] < P[i][w])                   // if the 2nd max arg was the one accepted
						itemIncluded[i][w] = true;
					else                                        // if prev val was max or equal to (no val update)
						itemIncluded[i][w] = false;
				} else {
					P[i][w] = P[i - 1][w];
					itemIncluded[i][w] = false;
				}
			}
		}

		best_price = P[m - 1][n - 1];                            // last index of P (also table)

		ArrayList<Item> result = new ArrayList<Item>();          // ArrayList used so indices arent needed in findItems
		findItems(table, P, itemIncluded, result, m - 1, n - 1); // start with final index
		Item[] res = result.toArray(new Item[result.size()]);    // update the class variable best price with the optimal price in a solution
		
		return res; // return an array of items to include in knapsack
	}

	private static void findItems(Item[] table, int[][] P, Boolean[][] b, ArrayList<Item> result, int i, int w) {	
		// when there are no more items can be added
		if (i == 0 || w == 0) 
			return;
		
		if (b[i][w]) {
			result.add(table[i - 1]);
			
			// go to index of last best value to go into knapsack with current index
			findItems(table, P, b, result, i - 1, w - table[i - 1].weight); 
		} else {
			findItems(table, P, b, result, i - 1, w);
		}	
		
	// TODO: set best_price to the sum of the optimal items' prices
	// return null;                                            
	}


	/********************************************
	 * 
	 * You shouldn't modify anything past here
	 * 
	 ********************************************/

	// set by calls to Find* methods
	private static int best_price = -1;

	// enumerates all subsets of items to find maximum price that fits in knapsack
	public static Item[] FindEnumerate(Item[] table, int capacity) {

		if (table.length > 31) {	// bitshift fails for larger sizes
			System.err.println("Problem size too large. Exiting");
			System.exit(0);
		}

		int nCr = 1 << table.length; // bitmask for included items
		int bestSum = -1;
		boolean[] bestUsed = {}; 
		boolean[] used = new boolean[table.length];

		for (int i = 0; i < nCr; i++) {	// test all combinations
			int temp = i;

			for (int j = 0; j < table.length; j++) {
				used[j] = (temp % 2 == 1);
				temp = temp >> 1;
			}

			if (TotalWeight(table, used) <= capacity) {
				if (TotalPrice(table, used) > bestSum) {
					bestUsed = Arrays.copyOf(used, used.length);
					bestSum = TotalPrice(table, used);
				}
			}
		}

		int itemCount = 0;	// count number of items in best result
		for (int i = 0; i < bestUsed.length; i++)
			if (bestUsed[i])
				itemCount++;

		Item[] ret = new Item[itemCount];
		int retIndex = 0;

		for (int i = 0; i < bestUsed.length; i++) {	// construct item list
			if (bestUsed[i]) {
				ret[retIndex] = table[i];
				retIndex++;
			}
		}
		best_price = bestSum;
		return ret;

	}

	// returns total price of all items that are marked true in used array
	private static int TotalPrice(Item[] table, boolean[] used) {
		int ret = 0;
		for (int i = 0; i < table.length; i++)
			if (used[i])
				ret += table[i].price;

		return ret;
	}

	// returns total weight of all items that are marked true in used array
	private static int TotalWeight(Item[] table, boolean[] used) {
		int ret = 0;
		for (int i = 0; i < table.length; i++) {
			if (used[i])
				ret += table[i].weight;
		}

		return ret;
	}

	// adds items to the knapsack by picking the next item with the highest
	// price:weight ratio. This could use a max-heap of ratios to run faster, but
	// it runs in n^2 time wrt items because it has to scan every item each time
	// an item is added
	public static Item[] FindGreedy(Item[] table, int capacity) {
		boolean[] used = new boolean[table.length];
		int itemCount = 0;

		while (capacity > 0) {	// while the knapsack has space
			int bestIndex = GetGreedyBest(table, used, capacity);
			if (bestIndex < 0)
				break;
			capacity -= table[bestIndex].weight;
			best_price += table[bestIndex].price;
			used[bestIndex] = true;
			itemCount++;
		}

		Item[] ret = new Item[itemCount];
		int retIndex = 0;

		for (int i = 0; i < used.length; i++) { // construct item list
			if (used[i]) {
				ret[retIndex] = table[i];
				retIndex++;
			}
		}

		return ret;
	}

	// finds the available item with the best price:weight ratio that fits in
	// the knapsack
	private static int GetGreedyBest(Item[] table, boolean[] used, int capacity) {

		double bestVal = -1;
		int bestIndex = -1;
		for (int i = 0; i < table.length; i++) {
			double ratio = (table[i].price*1.0)/table[i].weight;
			if (!used[i] && (ratio > bestVal) && (capacity >= table[i].weight)) {
				bestVal = ratio;
				bestIndex = i;
			}
		}

		return bestIndex;
	}

	public static int getBest() {
		return best_price;
	}

	public static void main(String[] args) {
		Scanner s = new Scanner(System.in);
		String file1;
		int capacity = 0;
		System.out.printf(
				"Enter <objects file> <knapsack capacity> <algorithm>, ([d]ynamic programming, [e]numerate, [g]reedy).\n");
		System.out.printf("(e.g: objects/small 10 g)\n");
		file1 = s.next();
		capacity = s.nextInt();

		ArrayList<Item> tableList = new ArrayList<Item>();

		try (Scanner f = new Scanner(new File(file1))) {
			int i = 0;
			while (f.hasNextInt())
				tableList.add(new Item(f.nextInt(), f.nextInt(), i++));
		} catch (IOException e) {
			System.err.println("Cannot open file " + file1 + ". Exiting.");
			System.exit(0);
		}

		Item[] table = new Item[tableList.size()];
		for (int i = 0; i < tableList.size(); i++)
			table[i] = tableList.get(i);

		String algo = s.next();
		Item[] result = {};

		switch (algo.charAt(0)) {
		case 'd':
			result = FindDynamic(table, capacity);
			break;
		case 'e':
			result = FindEnumerate(table, capacity);
			break;
		case 'g':
			result = FindGreedy(table, capacity);
			break;
		default:
			System.out.println("Invalid algorithm");
			System.exit(0);
			break;
		}

		s.close();

		System.out.printf("Index of included items: ");
		for (int i = 0; i < result.length; i++)
			System.out.printf("%d ", result[i].index);
		System.out.printf("\nBest total price: %d\n", best_price);
	}

	public static class Item {
		public int weight;
		public int price;
		public int index;

		public Item(int w, int p, int i) {
			weight = w;
			price = p;
			index = i;
		}

		public String toString() {
			return "(" + weight + "#, $" + price + ")";
		}
	}
}
