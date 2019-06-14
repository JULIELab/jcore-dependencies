
/* First created by JCasGen Wed Aug 08 13:36:49 CEST 2018 */
package de.julielab.jcore.types;

import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;

/** This type was introduced during the participation of the BioNLP Shared Task 2009 by Ekaterina Buyko. Therefore, it mainly satisfies the requirements of the event type of this challenge. This includes the use of an "event trigger" which is not necessary for all relation types. In practice, the type is used to capture predicate-argument relations (such as binding) as well as eventive propositional relations (such as phosphorylation or up- and down-regulation). The latter category also includes events where one of the event arguments is an event itself.
 * Updated by JCasGen Wed Aug 08 13:36:49 CEST 2018
 * @generated */
public class EventMention_Type extends GeneralEventMention_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = EventMention.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.julielab.jcore.types.EventMention");
 
  /** @generated */
  final Feature casFeat_genericity;
  /** @generated */
  final int     casFeatCode_genericity;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getGenericity(int addr) {
        if (featOkTst && casFeat_genericity == null)
      jcas.throwFeatMissing("genericity", "de.julielab.jcore.types.EventMention");
    return ll_cas.ll_getStringValue(addr, casFeatCode_genericity);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setGenericity(int addr, String v) {
        if (featOkTst && casFeat_genericity == null)
      jcas.throwFeatMissing("genericity", "de.julielab.jcore.types.EventMention");
    ll_cas.ll_setStringValue(addr, casFeatCode_genericity, v);}
    
  
 
  /** @generated */
  final Feature casFeat_trigger;
  /** @generated */
  final int     casFeatCode_trigger;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getTrigger(int addr) {
        if (featOkTst && casFeat_trigger == null)
      jcas.throwFeatMissing("trigger", "de.julielab.jcore.types.EventMention");
    return ll_cas.ll_getRefValue(addr, casFeatCode_trigger);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setTrigger(int addr, int v) {
        if (featOkTst && casFeat_trigger == null)
      jcas.throwFeatMissing("trigger", "de.julielab.jcore.types.EventMention");
    ll_cas.ll_setRefValue(addr, casFeatCode_trigger, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public EventMention_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_genericity = jcas.getRequiredFeatureDE(casType, "genericity", "uima.cas.String", featOkTst);
    casFeatCode_genericity  = (null == casFeat_genericity) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_genericity).getCode();

 
    casFeat_trigger = jcas.getRequiredFeatureDE(casType, "trigger", "de.julielab.jcore.types.EventTrigger", featOkTst);
    casFeatCode_trigger  = (null == casFeat_trigger) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_trigger).getCode();

  }
}



    