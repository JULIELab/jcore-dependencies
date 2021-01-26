package com.aliasi.test.unit.util;

import com.aliasi.util.Sort;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;



import java.util.Arrays;
import java.util.Random;

public class SortTest {

    @Test
    public void testSort() {
        Random random = new Random();
        for (int i = 0; i < 129; ++i) {
            int[] xs = new int[i];
            int[] ys = new int[i];
            int[] zs = new int[i];
            for (int numTests = 1; numTests < 10; ++numTests) {
                randomFill(xs,random);
                copy(xs,ys);
                copy(xs,zs);
                Arrays.sort(xs);
                Sort.qsort(ys, Sort.NATURAL_INT_COMPARE);
                Sort.isort(zs, Sort.NATURAL_INT_COMPARE);
                assertArrayEquals(xs,ys);
                assertArrayEquals(xs,zs);
            }
        }
    }

    static void randomFill(int[] xs, Random random) {
        for (int i = 0; i < xs.length; ++i)
            xs[i] = random.nextInt();
    }

    static void copy(int[] xs, int[] ys) {
        for (int i = 0; i < xs.length; ++i)
            xs[i] = ys[i];
    }



}