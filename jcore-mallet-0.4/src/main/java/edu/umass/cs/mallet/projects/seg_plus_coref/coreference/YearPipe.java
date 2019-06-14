package edu.umass.cs.mallet.projects.seg_plus_coref.coreference;

import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.types.Instance;

public class YearPipe extends Pipe
{
    public YearPipe() {}

    public Instance pipe (Instance carrier)
    {
			/*
        NodePair pair = (NodePair)carrier.getData();
        Citation c1 = (Citation)((Node)pair.getObject1()).getObject();
        Citation c2 = (Citation)((Node)pair.getObject2()).getObject();
        Integer s1 = (Integer)c1.getField("year");
        Integer s2 = (Integer)c2.getField("year");

        if(s1.intValue() != -1 && s2.intValue() != -1){
            int year1 = s1.intValue();
            int year2 = s2.intValue();
            int diff = Math.abs(year1 - year2);
            pair.setFeatureValue("YearDifference", -diff);
        }
			*/

        return carrier;
    }


}
