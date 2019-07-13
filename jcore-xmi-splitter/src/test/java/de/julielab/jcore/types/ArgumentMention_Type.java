
/* First created by JCasGen Sat Jul 13 15:41:00 CEST 2019 */
package de.julielab.jcore.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;

/** 
 * Updated by JCasGen Sat Jul 13 15:41:00 CEST 2019
 * @generated */
public class ArgumentMention_Type extends Annotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = ArgumentMention.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.julielab.jcore.types.ArgumentMention");
 
  /** @generated */
  final Feature casFeat_ref;
  /** @generated */
  final int     casFeatCode_ref;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getRef(int addr) {
        if (featOkTst && casFeat_ref == null)
      jcas.throwFeatMissing("ref", "de.julielab.jcore.types.ArgumentMention");
    return ll_cas.ll_getRefValue(addr, casFeatCode_ref);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setRef(int addr, int v) {
        if (featOkTst && casFeat_ref == null)
      jcas.throwFeatMissing("ref", "de.julielab.jcore.types.ArgumentMention");
    ll_cas.ll_setRefValue(addr, casFeatCode_ref, v);}
    
  
 
  /** @generated */
  final Feature casFeat_role;
  /** @generated */
  final int     casFeatCode_role;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getRole(int addr) {
        if (featOkTst && casFeat_role == null)
      jcas.throwFeatMissing("role", "de.julielab.jcore.types.ArgumentMention");
    return ll_cas.ll_getStringValue(addr, casFeatCode_role);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setRole(int addr, String v) {
        if (featOkTst && casFeat_role == null)
      jcas.throwFeatMissing("role", "de.julielab.jcore.types.ArgumentMention");
    ll_cas.ll_setStringValue(addr, casFeatCode_role, v);}
    
  
 
  /** @generated */
  final Feature casFeat_relationString;
  /** @generated */
  final int     casFeatCode_relationString;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getRelationString(int addr) {
        if (featOkTst && casFeat_relationString == null)
      jcas.throwFeatMissing("relationString", "de.julielab.jcore.types.ArgumentMention");
    return ll_cas.ll_getRefValue(addr, casFeatCode_relationString);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setRelationString(int addr, int v) {
        if (featOkTst && casFeat_relationString == null)
      jcas.throwFeatMissing("relationString", "de.julielab.jcore.types.ArgumentMention");
    ll_cas.ll_setRefValue(addr, casFeatCode_relationString, v);}
    
   /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public String getRelationString(int addr, int i) {
        if (featOkTst && casFeat_relationString == null)
      jcas.throwFeatMissing("relationString", "de.julielab.jcore.types.ArgumentMention");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_relationString), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_relationString), i);
	return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_relationString), i);
  }
   
  /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setRelationString(int addr, int i, String v) {
        if (featOkTst && casFeat_relationString == null)
      jcas.throwFeatMissing("relationString", "de.julielab.jcore.types.ArgumentMention");
    if (lowLevelTypeChecks)
      ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_relationString), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_relationString), i);
    ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_relationString), i, v);
  }
 



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public ArgumentMention_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_ref = jcas.getRequiredFeatureDE(casType, "ref", "de.julielab.jcore.types.Annotation", featOkTst);
    casFeatCode_ref  = (null == casFeat_ref) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_ref).getCode();

 
    casFeat_role = jcas.getRequiredFeatureDE(casType, "role", "uima.cas.String", featOkTst);
    casFeatCode_role  = (null == casFeat_role) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_role).getCode();

 
    casFeat_relationString = jcas.getRequiredFeatureDE(casType, "relationString", "uima.cas.StringArray", featOkTst);
    casFeatCode_relationString  = (null == casFeat_relationString) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_relationString).getCode();

  }
}



    