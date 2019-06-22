
/* First created by JCasGen Sat Jun 22 14:44:28 CEST 2019 */
package de.julielab.jcore.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;

/** An abbreviation is a letter or group of letters, taken from a word or words. For example, the word "abbreviation" can be abbreviated as "abbr." or "abbrev."
 * Updated by JCasGen Sat Jun 22 14:44:28 CEST 2019
 * @generated */
public class Abbreviation_Type extends Annotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Abbreviation.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.julielab.jcore.types.Abbreviation");
 
  /** @generated */
  final Feature casFeat_expan;
  /** @generated */
  final int     casFeatCode_expan;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getExpan(int addr) {
        if (featOkTst && casFeat_expan == null)
      jcas.throwFeatMissing("expan", "de.julielab.jcore.types.Abbreviation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_expan);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setExpan(int addr, String v) {
        if (featOkTst && casFeat_expan == null)
      jcas.throwFeatMissing("expan", "de.julielab.jcore.types.Abbreviation");
    ll_cas.ll_setStringValue(addr, casFeatCode_expan, v);}
    
  
 
  /** @generated */
  final Feature casFeat_textReference;
  /** @generated */
  final int     casFeatCode_textReference;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getTextReference(int addr) {
        if (featOkTst && casFeat_textReference == null)
      jcas.throwFeatMissing("textReference", "de.julielab.jcore.types.Abbreviation");
    return ll_cas.ll_getRefValue(addr, casFeatCode_textReference);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setTextReference(int addr, int v) {
        if (featOkTst && casFeat_textReference == null)
      jcas.throwFeatMissing("textReference", "de.julielab.jcore.types.Abbreviation");
    ll_cas.ll_setRefValue(addr, casFeatCode_textReference, v);}
    
  
 
  /** @generated */
  final Feature casFeat_definedHere;
  /** @generated */
  final int     casFeatCode_definedHere;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public boolean getDefinedHere(int addr) {
        if (featOkTst && casFeat_definedHere == null)
      jcas.throwFeatMissing("definedHere", "de.julielab.jcore.types.Abbreviation");
    return ll_cas.ll_getBooleanValue(addr, casFeatCode_definedHere);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setDefinedHere(int addr, boolean v) {
        if (featOkTst && casFeat_definedHere == null)
      jcas.throwFeatMissing("definedHere", "de.julielab.jcore.types.Abbreviation");
    ll_cas.ll_setBooleanValue(addr, casFeatCode_definedHere, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public Abbreviation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_expan = jcas.getRequiredFeatureDE(casType, "expan", "uima.cas.String", featOkTst);
    casFeatCode_expan  = (null == casFeat_expan) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_expan).getCode();

 
    casFeat_textReference = jcas.getRequiredFeatureDE(casType, "textReference", "de.julielab.jcore.types.AbbreviationLongform", featOkTst);
    casFeatCode_textReference  = (null == casFeat_textReference) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_textReference).getCode();

 
    casFeat_definedHere = jcas.getRequiredFeatureDE(casType, "definedHere", "uima.cas.Boolean", featOkTst);
    casFeatCode_definedHere  = (null == casFeat_definedHere) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_definedHere).getCode();

  }
}



    