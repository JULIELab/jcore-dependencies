package martin.common;

import java.util.Random;

public class Selector<E extends Comparable<E>> {
	private static Random random = new Random();

	@SuppressWarnings("unchecked")
	public E select(E[] arr, int rank){
		if (rank >= arr.length)
			throw new IllegalStateException("rank > arr.length");

		int smaller=0, greater=0, equal=0;
		E randomObject = arr[random.nextInt(arr.length)];

		for (int i = 0; i < arr.length; i++){
			int compare = randomObject.compareTo(arr[i]);

			if (compare > 0)
				smaller++;
			else if (compare < 0)
				greater++;
			else
				equal++;
		}

		if (smaller > rank){
			E[] next = (E[]) new Comparable[smaller];
			int counter = 0;

			for (int i = 0; i < arr.length; i++)
				if (randomObject.compareTo(arr[i]) > 0)
					next[counter++] = arr[i];

			return select(next, rank);
		}

		if (smaller + equal > rank){
			return randomObject;
		}

		if (smaller + equal + greater > rank){
			E[] next = (E[]) new Comparable[greater];
			int counter = 0;

			for (int i = 0; i < arr.length; i++)
				if (randomObject.compareTo(arr[i]) < 0)
					next[counter++] = arr[i];

			return select(next, rank - smaller - equal);
		}

		return null;
	}
}
