/*
 * LingPipe v. 4.1.0
 * Copyright (C) 2003-2011 Alias-i
 *
 * This program is licensed under the Alias-i Royalty Free License
 * Version 1 WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the Alias-i
 * Royalty Free License Version 1 for more details.
 *
 * You should have received a copy of the Alias-i Royalty Free License
 * Version 1 along with this program; if not, visit
 * http://alias-i.com/lingpipe/licenses/lingpipe-license-1.txt or contact
 * Alias-i, Inc. at 181 North 11th Street, Suite 401, Brooklyn, NY 11211,
 * +1 (718) 290-9170.
 */

package com.aliasi.util;

import java.util.Random;

/**
 * The {@code Sort} class contains static utilities for sorting
 * arrays of primitive values with respect to primitive comparators.
 *
 * <h3>References</h3>
 *
 * The algorithms were adapted from Jon Bentley's excellent
 * book on code design and tuning, which also inspired
 * Java's implementations:
 *
 * <ul>
 * <li>Bentley, Jon.  2000. <i>Programming Pearls</i>, 2nd Edition.  Addison-Wesley.
 * <li>Bloch, Joshua. 2006.  <a href="http://googleresearch.blogspot.com/2006/06/extra-extra-read-all-about-it-nearly.html">Extra, Extra - Read All About It: Nearly All Binary Searches and Mergesorts are Broken</a>.  Google Research Blog.
 * </ul>
 *
 * @author Bob Carpenter
 * @version 4.1.1
 * @since LingPipe 4.1.1
 */
public class Sort {

    private Sort() { /* no instances */ }

    /**
     * Sort the specified array of integers according to the
     * specified comparator using insertion sort.  
     *
     * @param x Array of ints to sort.
     * @param compare Comparator to use for sorting.
     */
    public static void isort(int[] x, CompareInt compare) {
        for (int i = 0; i < x.length; ++i) {
            int t = x[i];
            int j = i;
            for ( ; j > 0 && compare.lessThan(t,x[j-1]); --j)
                x[j] = x[j-1];
            x[j] = t;
        }
    }


    /**
     * Sort the specified array of integers according to the
     * specified comparator using quicksort.
     *
     * @param x Array of ints to sort.
     * @param compare Comparator to use for sorting.
     */
    public static void qsort(int[] x, CompareInt compare) {
        Random random = new Random();
        qsortPartial(x,0,x.length-1,compare,random);
        isort(x,compare);
    }


    static void qsortPartial(int[] x, int lower, int upper,
                             CompareInt compare,
                             Random random) {
        if (upper - lower < MIN_QSORT_SIZE)
            return;
        swap(x, lower, lower + random.nextInt(upper-lower+1));
        int t = x[lower];
        int i = lower;
        int j = upper + 1;
        while (true) {
            do {
                ++i;
            } while (i <= upper && compare.lessThan(x[i],t));
            do {
                --j;
            } while (compare.lessThan(t,x[j]));
            if (i > j)
                break;
            swap(x,i,j);
        }
        
    }

    // tested and working, but no longer used
    static void qsort2(int[] x, int lower, int upper,
                             CompareInt compare) {
        if (lower > upper) return;
        int mid = lower;
        for (int i = lower + 1; i <= upper; ++i) {
            if (compare.lessThan(x[i], x[lower]))
                swap(x,++mid, i);
        }
        swap(x,lower,mid);
        qsort2(x,lower,mid-1,compare);
        qsort2(x,mid+1,upper,compare);
    }

    /**
     * Swap the values for the array at the specified indexes.
     *
     * @param xs Array of elements.
     * @param i Index of first element in array.
     * @param j Index of second element in array.
     * @throws IndexOutOfBoundsException If either index is out
     * of bounds for the array
     */
    public static void swap(int[] xs, int i, int j) {
        int temp = xs[i];
        xs[i] = xs[j];
        xs[j] = temp;
    }

    // tuning parameter, below this size use insertion sort
    static int MIN_QSORT_SIZE = 7;

    /**
     * The {@code CompareInt} interface is for comparing
     * pairs of integers.
     *
     * @author Bob Carpenter
     * @version 4.1.1
     * @since LingPipe 4.1.1
     */
    public interface CompareInt {
        
        /**
         * Return {@code true} if the first argument is strictly
         * less than the secon under this comparator's sorting.
         *
         * @param a First integer.
         * @param b Second integer.
         * @return {@code true} if the first integer is less than the second.
         */
        public boolean lessThan(int a, int b);
    }

    /**
     * This constant implements the {@code CompareInt} interface using
     * the natural order of integers.
     */
    public static final CompareInt NATURAL_INT_COMPARE
        = new CompareInt() {
                public boolean lessThan(int a, int b) {
                    return a < b;
                }
            };

}