package relations;

import java.util.Map;

/**
 *
 * @author Chinh
 * @Date: Nov 1, 2010
 */
public class EData {

    String pmid;
    String eid;
    String trigID;
    String theme1, theme2 = null;
    String type;
    String cause = null;
    boolean initialized = false;
    Object data1 = null; // Data / EventData/ theme1
    TData trgdata =null, data2 =null; // triggger / theme2 <-> binding
    Object ecause = null; // Data/ EventData (use for regulation)
    public EData(String id, String e_id, String tp, String tg, String th1, String th2, String cs) {
        pmid = id;
        eid = e_id;
        trigID = tg; // trigger id
        theme1 = th1; // protein1 id / event1 id
        theme2 = th2; // protein2 id / event2 id
        type = tp; // type of event
        cause = cs; // cause of event
    }

    public void init(Map<String, TData> plist, Map<String, TData> tlist, Map<String, EData> elist) {
        // Regulation (positive/negative)
        if (theme1.startsWith("E")) { // event
            data1 = elist.get(theme1); // event
        } else { // protein -> Binding / simple events
            data1 = plist.get(theme1); // protein
        }
        if (cause.startsWith("E")) {
            ecause = elist.get(cause);
        } else if (cause.startsWith("T")) {
            ecause = plist.get(cause);
        }
        if(theme2.length()>0){ // Binding event
            data2 = (TData) plist.get(theme2);
        }
        trgdata = tlist.get(trigID); // trigger
        initialized = true;
    }

    public String getTxt(int idx){
        String txt = type+": "+trgdata.name ;
        if(data1 instanceof TData){
            txt+=" pro1-"+idx+": "+((TData)data1).new_name ;
        }else {
            txt+=" event"+idx+": ["+((EData)data1).getTxt(idx+1)+"] ";
        }
        if(data2!=null){
            txt+=" pro2-"+idx+": "+((TData)data2).new_name ;
        }
        if(ecause instanceof TData){
            txt+=" cause-"+idx+": "+((TData)ecause).new_name ;
        }else if(ecause instanceof EData){
            txt+=" cause-"+idx+" : ["+((EData)ecause).getTxt(idx+1)+"] " ;
        }
        return txt ;
    }

    public String getTxt(){
        String txt = type+": "+trgdata.name ;
        if(data1 instanceof TData){
            txt+=" Pro1: "+((TData)data1).new_name ;
        }else {
            txt+=" Pro1: "+((EData)data1).getPro().new_name;
        }
        if(data2!=null){
            txt+=" | Pro2: "+((TData)data2).new_name ;
        }
        if(ecause instanceof TData){
            txt+=" | Pro2: "+((TData)ecause).new_name ;
        }else if(ecause instanceof EData){
            txt+=" | Pro2: "+((EData)ecause).getPro().new_name;
        }
        return txt ;
    }
    
    public int getLevel(int idx){
        int v1,v2=idx;
        if(data1 instanceof TData){
            v1=idx ;
        }else {
            v1 = ((EData)data1).getLevel(idx+1);
        }
        if(ecause instanceof EData){
            v2 =((EData)ecause).getLevel(idx+1) ;
        }
        return Math.max(v1, v2) ;
    }
    
    
    /**
     * Get protein closest to the trigger in order to map trigger location into sentence and vice vesa
     * @return Data (protein)
     */
    public TData getPro(){
        if(data1 instanceof TData){
            return (TData) data1 ;
        }else if (data1 instanceof EData){
            return ((EData)data1).getPro();
        }
        return null ;
    }

    public TData getTrigger() {
        return trgdata;
    }

    public String getType() {
        return type;
    }

    public Object getCause() {
        return ecause;
    }

    String toTxt=" ";
    @Override
    public String toString(){
        return toTxt ;
    }
}
