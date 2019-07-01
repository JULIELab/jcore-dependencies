package de.julielab.xml;

import com.google.common.collect.Multimap;

import java.io.ByteArrayOutputStream;

public class BinaryDecodingResult {
    private ByteArrayOutputStream xmiData;
    private Multimap<Integer, Integer> sofaElements;

    public BinaryDecodingResult(ByteArrayOutputStream xmiData, Multimap<Integer, Integer> sofaElements) {

        this.xmiData = xmiData;
        this.sofaElements = sofaElements;
    }

    public ByteArrayOutputStream getXmiData() {
        return xmiData;
    }

    public void setXmiData(ByteArrayOutputStream xmiData) {
        this.xmiData = xmiData;
    }

    /**
     * Returns a map from sofa ID to the XMI IDs of annotations contained in the sofa.
     *
     * @return
     */
    public Multimap<Integer, Integer> getSofaElements() {
        return sofaElements;
    }

    public void setSofaElements(Multimap<Integer, Integer> sofaElements) {
        this.sofaElements = sofaElements;
    }
}
