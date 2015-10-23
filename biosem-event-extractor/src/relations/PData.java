package relations;

import java.util.List;

/**
 * @date Mar 10, 2011
 * @author Chinh
 * @Revision June 28, 2011
 */
public class PData implements Comparable{
    public Word getPro1() {
		return pro1;
	}

	public Word getPro2() {
		return pro2;
	}

	public PData getPdata1() {
		return pdata1;
	}

	public PData getPdata2() {
		return pdata2;
	}

	public String PID=null ;
    Word trg=null, pro1=null, pro2=null;
    PData pdata1 =null, pdata2=null;
    int ttype =0 ;
    public boolean skip =false ;
    
    public PData(Word tg, Object ob1, String ev_type) {
        trg = tg;
        evt_type = ev_type ;
        if(ob1 instanceof Word){
            pro1 = (Word)ob1 ;
        }else if (ob1 instanceof PData){
            pdata1 = (PData)ob1;
        }
        ttype = SenSimplifier.hashType.get(ev_type);
    }

    public String evt_type ="";
    public String trig_ID="";

    public void setPID(String pid){
        PID = pid ;
    }

    public Word getProtein(){
        if(pro1!=null){
            return pro1 ;
        }else {
            return pdata1.getProtein() ;
        }
    }
    
    public Word getTrigger() {
    	return trg;
    }
    
     public PData(Word tg, Object ob1, Object ob2, String ev_type){
        trg =tg ;
        if(ob1 instanceof Word){
            pro1 = (Word)ob1 ;
        }else if (ob1 instanceof PData){
            pdata1 = (PData)ob1;
        }
        if(ob2 instanceof Word){
            pro2 = (Word)ob2 ;
        }else if (ob2 instanceof PData){
            pdata2 = (PData)ob2;
        }
        evt_type = ev_type ;
        ttype = SenSimplifier.hashType.get(ev_type);
    }
     
    public String getText(){
        String txt=evt_type+": "+trg.word ;
        if(pro1!=null){
            txt+=" pro1: "+ pro1.word;
        }else if (pdata1!=null){
            txt+=" event 1: ["+pdata1.getText()+" ] ";
        }
        if(pro2!=null && ttype==5){
            txt+=" pro2: "+pro2.word ;
        }else if(pro2!=null && ttype>5){
            txt+=" cause: "+pro2.word ;
        }else if (pdata2!=null){
            txt+=" cause : [ "+pdata2.getText()+" ] ";
        }
        return txt ;
    }

    public String getWriteID(){
        String txt=trg.TID;
        if(pro1!=null){
            txt+=pro1.TID;
        }else if (pdata1!=null){
            txt+=pdata1.PID;
        }
        if(pro2!=null && ttype==5){
            txt+=pro2.TID ;
        }else if(pro2!=null && ttype>5){
            txt+=pro2.TID ;
        }else if (pdata2!=null){
            txt+=pdata2.PID ;
        }
       return txt; 
    }

    @Override
    public String toString() {
        //evt_type = trg.type ;
        StringBuilder sb = new StringBuilder();
        sb.append(PID);
        sb.append('\t');
        sb.append(evt_type);
        sb.append(":");
        sb.append(trg.TID);
        sb.append(' ');
        sb.append("Theme:");
        if(pro1!=null){
            sb.append(pro1.TID);
        }else if (pdata1!=null){
            sb.append(pdata1.PID);
        }
        if(pro2!=null && SenSimplifier.hashType.get(evt_type)==5){ // type 5 == binding
            sb.append(' ');
            sb.append("Theme2:");
            sb.append(pro2.TID);
        }else if(pro2!=null || pdata2!=null){
            sb.append(' ');
            sb.append("Cause:");
            if(pdata2!=null){
                sb.append(pdata2.PID);
            }else {
                sb.append(pro2.TID);
            }
        }
        sb.append('\n');
        return sb.toString();
    }
    
    private void append(StringBuilder sb, List<String> ls, int len){
        int idx = Math.min(ls.size(), len);
        for(int i=0; i<idx;i++){
            sb.append(ls.get(i));
            sb.append(',');
        }
        if(idx<len){
            for(int i=idx; i<len;i++){
                sb.append("null");
                sb.append(',');
            }
        }
    }
    @Override
    public int compareTo(Object o) {
        if(o!=null){
            return trg.pos- ((PData)o).trg.pos;
        }else {
            System.out.println("NULL pair ----------------------------------------------------------------------------------->");
        }
        return -1 ;
    }
}
