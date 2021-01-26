package de.julielab.xml;

public class StreamParserElementNode {
    private int offset;
    private int depth;

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public StreamParserElementNode(int offset, int depth) {

        this.offset = offset;
        this.depth = depth;
    }
}
