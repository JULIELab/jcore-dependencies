package dragon.nlp.ontology.mesh;

/**
 * <p>MeSH node is designed for (Medical Subject Headings) MeSH ontology node </p>
 * <p>http://www.nlm.nih.gov/mesh/</p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Drexel University</p>
 * @author Xiaodan Zhang
 * @version 1.0
 */

public class MeshNode implements Comparable{
    private String path;
    private String name;
    private int freq,descendantNum;
    private double weight;

    public MeshNode(String name,String path) {
        this.name=name;
        this.path=path;
        this.freq=0;
        this.weight = -1;
        this.descendantNum=-1;
    }

    public MeshNode(String path) {
        this.name = null;
        this.path = path;
        this.freq = 0;
        this.weight = -1;
        this.descendantNum=-1;
    }

    public int compareTo(Object obj) {
        String objValue;

        objValue = ( (MeshNode) obj).getPath();
        return path.compareTo(objValue);
    }

    public String getName(){
        return name;
    }

    public String getPath(){
        return path;
    }

    public void setName(String name){
       this.name = name;
   }

   public void setPath(String path){
       this.path = path;
   }

    public String toString(){
        return path;
    }

    public void setDescendantNum(int num){
        this.descendantNum = num;
    }

    public int getDescendantNum(){
        return descendantNum;
    }

    public void addFrequency(int count){
        this.freq+=count;
    }

    public void setFrequency(int count){
        this.freq=count;
    }

    public int getFrequency(){
        return freq;
    }

    public void setWeight(double weight){
        this.weight=weight;
    }

    public double getWeight(){
        return weight;
    }
}