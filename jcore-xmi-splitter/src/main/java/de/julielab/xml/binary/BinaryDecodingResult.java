package de.julielab.xml.binary;

import com.google.common.collect.Multimap;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class BinaryDecodingResult {
    private ByteArrayOutputStream xmiData;
    /**
     * A map of the form sofaID -> {XMI IDs of elements belonging to the Sofa}.
     */
    private Multimap<Integer, Integer> sofaElements;
    private List<DataRange> xmiPortionsToModify;
    private boolean shrinkArraysAndListsIfReferenceNotLoaded;

    public BinaryDecodingResult(ByteArrayOutputStream xmiData, Multimap<Integer, Integer> sofaElements, boolean shrinkArraysAndListsIfReferenceNotLoaded) {

        this.xmiData = xmiData;
        this.sofaElements = sofaElements;
        this.shrinkArraysAndListsIfReferenceNotLoaded = shrinkArraysAndListsIfReferenceNotLoaded;
    }

    public boolean isShrinkArraysAndListsIfReferenceNotLoaded() {
        return shrinkArraysAndListsIfReferenceNotLoaded;
    }

    public List<DataRange> getXmiPortionsToModify() {
        return xmiPortionsToModify;
    }

    public void setXmiPortionsToModify(List<DataRange> xmiPortionsToModify) {
        this.xmiPortionsToModify = xmiPortionsToModify;
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
