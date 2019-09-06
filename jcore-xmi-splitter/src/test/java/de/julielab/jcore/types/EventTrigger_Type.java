
/* First created by JCasGen Tue Sep 03 12:34:17 CEST 2019 */
package de.julielab.jcore.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;

/** 
 * Updated by JCasGen Tue Sep 03 12:34:17 CEST 2019
 * @generated */
public class EventTrigger_Type extends ConceptMention_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = EventTrigger.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.julielab.jcore.types.EventTrigger");
 
  /** @generated */
  final Feature casFeat_probability;
  /** @generated */
  final int     casFeatCode_probability;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getProbability(int addr) {
        if (featOkTst && casFeat_probability == null)
      jcas.throwFeatMissing("probability", "de.julielab.jcore.types.EventTrigger");
    return ll_cas.ll_getStringValue(addr, casFeatCode_probability);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setProbability(int addr, String v) {
        if (featOkTst && casFeat_probability == null)
      jcas.throwFeatMissing("probability", "de.julielab.jcore.types.EventTrigger");
    ll_cas.ll_setStringValue(addr, casFeatCode_probability, v);}
    
  
 
  /** @generated */
  final Feature casFeat_specifity;
  /** @generated */
  final int     casFeatCode_specifity;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getSpecifity(int addr) {
        if (featOkTst && casFeat_specifity == null)
      jcas.throwFeatMissing("specifity", "de.julielab.jcore.types.EventTrigger");
    return ll_cas.ll_getStringValue(addr, casFeatCode_specifity);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setSpecifity(int addr, String v) {
        if (featOkTst && casFeat_specifity == null)
      jcas.throwFeatMissing("specifity", "de.julielab.jcore.types.EventTrigger");
    ll_cas.ll_setStringValue(addr, casFeatCode_specifity, v);}
    
  
 
  /** @generated */
  final Feature casFeat_importance;
  /** @generated */
  final int     casFeatCode_importance;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getImportance(int addr) {
        if (featOkTst && casFeat_importance == null)
      jcas.throwFeatMissing("importance", "de.julielab.jcore.types.EventTrigger");
    return ll_cas.ll_getStringValue(addr, casFeatCode_importance);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setImportance(int addr, String v) {
        if (featOkTst && casFeat_importance == null)
      jcas.throwFeatMissing("importance", "de.julielab.jcore.types.EventTrigger");
    ll_cas.ll_setStringValue(addr, casFeatCode_importance, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public EventTrigger_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_probability = jcas.getRequiredFeatureDE(casType, "probability", "uima.cas.String", featOkTst);
    casFeatCode_probability  = (null == casFeat_probability) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_probability).getCode();

 
    casFeat_specifity = jcas.getRequiredFeatureDE(casType, "specifity", "uima.cas.String", featOkTst);
    casFeatCode_specifity  = (null == casFeat_specifity) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_specifity).getCode();

 
    casFeat_importance = jcas.getRequiredFeatureDE(casType, "importance", "uima.cas.String", featOkTst);
    casFeatCode_importance  = (null == casFeat_importance) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_importance).getCode();

  }
}



    