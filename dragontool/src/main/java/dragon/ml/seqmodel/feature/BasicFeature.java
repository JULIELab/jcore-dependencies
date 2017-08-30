package dragon.ml.seqmodel.feature;


/**
 * <p>Basic data structure for feature data </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */


public class BasicFeature implements Feature{
    private FeatureIdentifier id;
    private int index;
    private int ystart, yend;
    private double val;

    public BasicFeature() {
        index=-1;
        ystart=-1;
        yend=-1;
        id=null;
    }

    public BasicFeature(String id, int label, double val){
        this(id,-1,label,val);
    }

    public BasicFeature(FeatureIdentifier id, int label, double val){
        this(id,-1,label,val);
    }

    public BasicFeature(String id, int prevLabel, int label, double val){
        this(new FeatureIdentifier(id),prevLabel,label,val);
    }

    public BasicFeature(FeatureIdentifier id, int prevLabel, int label, double val){
        this.id=id;
        this.yend=label;
        this.val =val;
        this.ystart=prevLabel;
        this.index=-1;
    }

    public BasicFeature(BasicFeature f) {
        copyFrom(f);
    }

    public void copyFrom(BasicFeature f) {
        index = f.getIndex();
        ystart = f.getPrevLabel();
        yend =f.getLabel();
        val = f.getValue();
        id=f.getID().copy();
    }

    public Feature copy(){
        return new BasicFeature(this);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index){
        this.index=index;
    }

    public int getLabel() {
        return yend;
    }

    public void setLabel(int label){
        this.yend=label;
    }

    public int getPrevLabel() {
        return ystart;
    }

    public void setPrevLabel(int prevLabel){
        this.ystart =prevLabel;
    }

    public double getValue() {
        return val;
    }

    public void setValue(double val){
        this.val =val;
    }

    public String toString() {
        return id + " " + val;
    }

    public FeatureIdentifier getID() {
        return id;
    }

    public void setID(FeatureIdentifier id){
        this.id=id;
    }
};

