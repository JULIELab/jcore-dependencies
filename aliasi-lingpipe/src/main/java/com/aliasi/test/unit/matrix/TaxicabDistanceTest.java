package com.aliasi.test.unit.matrix;

import com.aliasi.matrix.DenseVector;
import com.aliasi.matrix.SparseFloatVector;
import com.aliasi.matrix.TaxicabDistance;
import com.aliasi.matrix.Vector;
import com.aliasi.util.AbstractExternalizable;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.aliasi.test.unit.Asserts.succeed;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

public class TaxicabDistanceTest  {

    @Test
    public void testOne() throws IOException, ClassNotFoundException {
        Vector v1 = new DenseVector(new double[] { 1, 2 });
        Vector v2 = new DenseVector(new double[] { 3, 1 });

        Map<Integer,Double> map1 = new HashMap<Integer,Double>();
        map1.put(0,1.0);
        map1.put(1,2.0);
        Map<Integer,Double> map2 = new HashMap<Integer,Double>();
        map2.put(0,3.0);
        map2.put(1,1.0);
        SparseFloatVector sv1 = new SparseFloatVector(map1);
        SparseFloatVector sv2 = new SparseFloatVector(map2);

        assertEquals(3.0,
                     TaxicabDistance.DISTANCE.distance(v1,v2),
                     0.0001);
        assertEquals(3.0,
                     TaxicabDistance.DISTANCE.distance(sv1,sv2),
                     0.0001);
        assertEquals(3.0,
                     TaxicabDistance.DISTANCE.distance(v1,sv2),
                     0.0001);
        assertEquals(3.0,
                     TaxicabDistance.DISTANCE.distance(sv1,v2),
                     0.0001);

        TaxicabDistance d2
            = (TaxicabDistance)
            AbstractExternalizable
            .serializeDeserialize(TaxicabDistance.DISTANCE);

        assertEquals(3.0,
                     d2.distance(v1,v2),
                     0.0001);
        assertEquals(3.0,
                     d2.distance(v1,sv2),
                     0.0001);
        assertEquals(3.0,
                     d2.distance(sv1,v2),
                     0.0001);
        assertEquals(3.0,
                     d2.distance(sv1,sv2),
                     0.0001);

        Map<Integer,Double> map3 = new HashMap<Integer,Double>();
        map3.put(1,-1.0);
        SparseFloatVector vA = new SparseFloatVector(map3,5);

        Map<Integer,Double> map4 = new HashMap<Integer,Double>();
        map4.put(2,1.0);
        SparseFloatVector vB = new SparseFloatVector(map4,5);

        assertEquals(2.0,
                     d2.distance(vA,vB),
                     0.0001);
        assertEquals(2.0,
                     d2.distance(vB,vA),
                     0.0001);

        Vector vC = new DenseVector(new double[] { 0, 1, 0, 0, 0 });
        Vector vD = new DenseVector(new double[] { 0, 0, 1, 0, 0 });

        assertEquals(2.0,
                     d2.distance(vA,vD),
                     0.0001);
        assertEquals(2.0,
                     d2.distance(vD,vA),
                     0.0001);
        assertEquals(2.0,
                     d2.distance(vC,vB),
                     0.0001);
        assertEquals(2.0,
                     d2.distance(vB,vC),
                     0.0001);
    }



    @Test
    public void testExs() {
        Vector v1 = new DenseVector(new double[] { 0.0, 1.0 });
        Vector v2 = new DenseVector(new double[] { 2.0 });
        try {
            TaxicabDistance.DISTANCE.distance(v1,v2);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }
    }

}
