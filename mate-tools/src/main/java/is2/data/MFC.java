package is2.data;


import is2.util.DB;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Map Features, do not map long to integer
 * 
 * @author Bernd Bohnet, 17.09.2011
 */

final public class MFC  implements IEncoderPlus {
 
	/** The features and its values */
	private final HashMap<String,HashMap<String,Integer>> m_featureSets = new HashMap<String,HashMap<String,Integer>>();

	/** The feature class and the number of values */
	private final HashMap<String,Integer> m_featureCounters = new HashMap<String,Integer>();

	/** The number of bits needed to encode a feature */
	final HashMap<String,Integer> m_featureBits = new HashMap<String,Integer>();
	
	/** Integer counter for long2int */
	private int count=0;
			
	
	public MFC () {}
	
	
	public  int size() {return count;}
	
	
	
	/**
	 * Register an attribute class, if it not exists and add a possible value
	 * @param type
	 * @param type2
	 */
	 final public int  register(String a, String v) {

			synchronized(m_featureCounters) {
		
		HashMap<String,Integer> fs = getFeatureSet().get(a);
		if (fs==null) {
			fs = new HashMap<String,Integer>();
			getFeatureSet().put(a, fs);
			fs.put(NONE, 0);
			getFeatureCounter().put(a, 1);
		}
		
		Integer i = fs.get(v);
		if (i==null) {
			Integer c = getFeatureCounter().get(a);
			fs.put(v, c);
			c++;
			getFeatureCounter().put(a,c);
			return c-1;
		} else return i;
			}
	}
	
	/**
	 * Calculates the number of bits needed to encode a feature
	 */
	 public void calculateBits() {
		
		int total=0;
		for(Entry<String,Integer> e : getFeatureCounter().entrySet() ){
			int bits =(int)Math.ceil((Math.log(e.getValue()+1)/Math.log(2)));
			m_featureBits.put(e.getKey(), bits);
			total+=bits;
		//	System.out.println(" "+e.getKey()+" bits "+bits+" number "+(e.getValue()+1));
		}
		
//		System.out.println("total number of needed bits "+total);
	}
	
	
	
	public  String toString() {
		
		StringBuffer content = new StringBuffer();
		for(Entry<String,Integer> e : getFeatureCounter().entrySet() ){
			 content.append(e.getKey()+" "+e.getValue());
			 content.append(':');
	//		 HashMap<String,Integer> vs = getFeatureSet().get(e.getKey());
			 content.append(getFeatureBits(e.getKey()));
			 
			 /*if (vs.size()<120)
			 for(Entry<String,Integer> e2 : vs.entrySet()) {
				 content.append(e2.getKey()+" ("+e2.getValue()+") ");
			 }*/
			 content.append('\n');
				
		}
		return content.toString();
	}
	
	
	
	final public short getFeatureBits(String a) {
		if(m_featureBits.get(a)==null) return 0;
		return (short)m_featureBits.get(a).intValue();
	}

	

	/**
	 * Get the integer place holder of the string value v of the type a
	 * 
	 * @param t the type
	 * @param v the value 
	 * @return the integer place holder of v
	 */
	 final public int getValue(String t, String v) {
		
		if (m_featureSets.get(t)==null) return -1;
		Integer vi = m_featureSets.get(t).get(v);
		if (vi==null) return -1; //stop && 
		return vi.intValue();
	}

	 /**
	  * Static version of getValue
	  * @see getValue
	  */
	final public int getValueS(String a, String v) {
			
			if (m_featureSets.get(a)==null) return -1;
			Integer vi = m_featureSets.get(a).get(v);
			if (vi==null) return -1; //stop && 
			return vi.intValue();
		}
	
	 public int hasValue(String a, String v) {
		
		Integer vi = m_featureSets.get(a).get(v);
		if (vi==null) return -1;
		return vi.intValue();
	}
	
	
	public static String printBits(int k) {
		StringBuffer s = new StringBuffer();
		for(int i =0;i<31;i++) {
			s.append((k&0x00000001)==1?'1':'0');
			k=k>>1;
			
		}
		s.reverse();
		return s.toString();
	}
	
	

   
   
   
   
	/** 
	 * Maps a long to a integer value. This is very useful to save memory for sparse data long values 
	 */
	static public  int misses = 0;
	static public  int good = 0;
		

	
	
	/**
	 * Write the data
	 * @param dos
	 * @throws IOException
	 */
    public void writeData(DataOutputStream dos) throws IOException {
        dos.writeInt(getFeatureSet().size());
       // DB.println("write"+getFeatureSet().size());
        for(Entry<String, HashMap<String,Integer>> e : getFeatureSet().entrySet()) {
            dos.writeUTF(e.getKey());
            dos.writeInt(e.getValue().size());
        
            for(Entry<String,Integer> e2 : e.getValue().entrySet()) {
  
            	if(e2.getKey()==null) DB.println("key "+e2.getKey()+" value "+e2.getValue()+" e -key "+e.getKey());
                dos.writeUTF(e2.getKey()); 
                dos.writeInt(e2.getValue());
            
             }          
                
        }
    }
    public void read(DataInputStream din) throws IOException {
		 
		int size = din.readInt();
		for(int i=0; i<size;i++) {
			String k = din.readUTF();
			int size2 = din.readInt();
			
			HashMap<String,Integer> h = new HashMap<String,Integer>();
			getFeatureSet().put(k,h);
			for(int j = 0;j<size2;j++) {
				h.put(din.readUTF(), din.readInt());
			}
			getFeatureCounter().put(k, size2);
		}

    	count =size;
	//	stop();
 		calculateBits();
	}

	
	/** 
	 * Clear the data
	 */
     public void clearData() {
      getFeatureSet().clear();
      m_featureBits.clear();
      getFeatureSet().clear();
    }

    public HashMap<String,Integer> getFeatureCounter() {
		return m_featureCounters;
	}

	 public HashMap<String,HashMap<String,Integer>> getFeatureSet() {
		return m_featureSets;
	}
	
	 public String[] reverse(HashMap<String,Integer> v){
		String[] set = new String[v.size()];
		for(Entry<String,Integer> e : v.entrySet()) {
			set[e.getValue()]=e.getKey();
		}
		return set;
	}


}
