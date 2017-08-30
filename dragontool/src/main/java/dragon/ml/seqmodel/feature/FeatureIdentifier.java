package dragon.ml.seqmodel.feature;


/**
 * <p>Feature identifier </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */


public class FeatureIdentifier implements Comparable{
    private int id;
    private String name;
    private int state;

    public FeatureIdentifier(String name, int id, int state) {
        this.name = name;
        this.id = id;
        this.state=state;
    }

    public FeatureIdentifier(String strRep) {
        int start, end;

        start=strRep.indexOf(':');
        name=strRep.substring(0,start);
        end=strRep.lastIndexOf(':');
        id = Integer.parseInt(strRep.substring(start+1,end));
        state= Integer.parseInt(strRep.substring(end+1));
    }

    public FeatureIdentifier copy(){
        return new FeatureIdentifier(name,id,state);
    }

    public int hashCode() {
        return id;
    }

    public boolean equals(Object o) {
        return (id == ( (FeatureIdentifier) o).getId());
    }

    public String toString() {
        return name + ":" + id + ":" + state;
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id=id;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name =name;
    }

    public int getState(){
        return state;
    }

    public void setState(int state){
        this.state =state;
    }

    public int compareTo(Object o){
        int newId;

        newId=((FeatureIdentifier)o).getId();
        if(id>newId)
            return 1;
        else if(id==newId)
            return 0;
        else
            return -1;
    }
};
