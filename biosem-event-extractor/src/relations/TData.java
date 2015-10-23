package relations;

/**
 *
 * @author Chinh
 * @Date: Oct 28, 2010
 */
public class TData implements Comparable {

        String PID; // pubmed ID
        String tid ;// Sub id ; from annotated data
        String type ; // theme / trigger (type: Gene_expression, Transcription,...)
        int list[];// Position in the text
        String name; // protein name ; trigger value ;
        String new_name="";
        TData(String pid, String id, String Type, int[] pos, String pname) {
            PID = pid;
            tid = id;
            list = pos;
            name = pname;
            type = Type ;
        }

    @Override
        public int compareTo(Object o) {

            if (list[0] == ((TData) o).list[0]) {
                if (list.length == 2) {
                    return list[1]-((TData) o).list[1] ;
                } else {
                    return  list[3] -((TData) o).list[3];
                }
            } else {
                return list[0] -((TData) o).list[0]  ;
            }
        }
    }