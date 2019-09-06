
/* First created by JCasGen Tue Sep 03 12:34:17 CEST 2019 */
package de.julielab.jcore.types.ct;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import de.julielab.jcore.types.Zone_Type;

/** This should actually be a list where each list item consists of the elements 'measure', 'time frame' and 'description'. But at time of creation of type, these information were not parsed in the required granularity.
 * Updated by JCasGen Tue Sep 03 12:34:17 CEST 2019
 * @generated */
public class OutcomeMeasure_Type extends Zone_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = OutcomeMeasure.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.julielab.jcore.types.ct.OutcomeMeasure");



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public OutcomeMeasure_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

  }
}



    