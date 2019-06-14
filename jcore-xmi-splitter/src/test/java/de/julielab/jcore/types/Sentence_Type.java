
/* First created by JCasGen Wed Aug 08 13:36:50 CEST 2018 */
package de.julielab.jcore.types;

import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;

/** This annotation marks the span of a sentence.
 * Updated by JCasGen Wed Aug 08 13:36:50 CEST 2018
 * @generated */
public class Sentence_Type extends Annotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Sentence.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.julielab.jcore.types.Sentence");
 
  /** @generated */
  final Feature casFeat_segment;
  /** @generated */
  final int     casFeatCode_segment;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getSegment(int addr) {
        if (featOkTst && casFeat_segment == null)
      jcas.throwFeatMissing("segment", "de.julielab.jcore.types.Sentence");
    return ll_cas.ll_getRefValue(addr, casFeatCode_segment);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setSegment(int addr, int v) {
        if (featOkTst && casFeat_segment == null)
      jcas.throwFeatMissing("segment", "de.julielab.jcore.types.Sentence");
    ll_cas.ll_setRefValue(addr, casFeatCode_segment, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public Sentence_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_segment = jcas.getRequiredFeatureDE(casType, "segment", "de.julielab.jcore.types.Segment", featOkTst);
    casFeatCode_segment  = (null == casFeat_segment) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_segment).getCode();

  }
}



    