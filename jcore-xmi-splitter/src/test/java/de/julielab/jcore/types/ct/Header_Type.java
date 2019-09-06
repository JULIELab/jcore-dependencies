
/* First created by JCasGen Tue Sep 03 12:34:17 CEST 2019 */
package de.julielab.jcore.types.ct;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;

/** The special Header for PubMed (http://www.pubmed.org)
                documents
 * Updated by JCasGen Tue Sep 03 12:34:17 CEST 2019
 * @generated */
public class Header_Type extends de.julielab.jcore.types.pubmed.Header_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Header.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.julielab.jcore.types.ct.Header");
 
  /** @generated */
  final Feature casFeat_studyType;
  /** @generated */
  final int     casFeatCode_studyType;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getStudyType(int addr) {
        if (featOkTst && casFeat_studyType == null)
      jcas.throwFeatMissing("studyType", "de.julielab.jcore.types.ct.Header");
    return ll_cas.ll_getStringValue(addr, casFeatCode_studyType);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setStudyType(int addr, String v) {
        if (featOkTst && casFeat_studyType == null)
      jcas.throwFeatMissing("studyType", "de.julielab.jcore.types.ct.Header");
    ll_cas.ll_setStringValue(addr, casFeatCode_studyType, v);}
    
  
 
  /** @generated */
  final Feature casFeat_studyDesignInfo;
  /** @generated */
  final int     casFeatCode_studyDesignInfo;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getStudyDesignInfo(int addr) {
        if (featOkTst && casFeat_studyDesignInfo == null)
      jcas.throwFeatMissing("studyDesignInfo", "de.julielab.jcore.types.ct.Header");
    return ll_cas.ll_getRefValue(addr, casFeatCode_studyDesignInfo);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setStudyDesignInfo(int addr, int v) {
        if (featOkTst && casFeat_studyDesignInfo == null)
      jcas.throwFeatMissing("studyDesignInfo", "de.julielab.jcore.types.ct.Header");
    ll_cas.ll_setRefValue(addr, casFeatCode_studyDesignInfo, v);}
    
  
 
  /** @generated */
  final Feature casFeat_minimumAge;
  /** @generated */
  final int     casFeatCode_minimumAge;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getMinimumAge(int addr) {
        if (featOkTst && casFeat_minimumAge == null)
      jcas.throwFeatMissing("minimumAge", "de.julielab.jcore.types.ct.Header");
    return ll_cas.ll_getIntValue(addr, casFeatCode_minimumAge);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setMinimumAge(int addr, int v) {
        if (featOkTst && casFeat_minimumAge == null)
      jcas.throwFeatMissing("minimumAge", "de.julielab.jcore.types.ct.Header");
    ll_cas.ll_setIntValue(addr, casFeatCode_minimumAge, v);}
    
  
 
  /** @generated */
  final Feature casFeat_maximumAge;
  /** @generated */
  final int     casFeatCode_maximumAge;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getMaximumAge(int addr) {
        if (featOkTst && casFeat_maximumAge == null)
      jcas.throwFeatMissing("maximumAge", "de.julielab.jcore.types.ct.Header");
    return ll_cas.ll_getIntValue(addr, casFeatCode_maximumAge);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setMaximumAge(int addr, int v) {
        if (featOkTst && casFeat_maximumAge == null)
      jcas.throwFeatMissing("maximumAge", "de.julielab.jcore.types.ct.Header");
    ll_cas.ll_setIntValue(addr, casFeatCode_maximumAge, v);}
    
  
 
  /** @generated */
  final Feature casFeat_gender;
  /** @generated */
  final int     casFeatCode_gender;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getGender(int addr) {
        if (featOkTst && casFeat_gender == null)
      jcas.throwFeatMissing("gender", "de.julielab.jcore.types.ct.Header");
    return ll_cas.ll_getRefValue(addr, casFeatCode_gender);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setGender(int addr, int v) {
        if (featOkTst && casFeat_gender == null)
      jcas.throwFeatMissing("gender", "de.julielab.jcore.types.ct.Header");
    ll_cas.ll_setRefValue(addr, casFeatCode_gender, v);}
    
   /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public String getGender(int addr, int i) {
        if (featOkTst && casFeat_gender == null)
      jcas.throwFeatMissing("gender", "de.julielab.jcore.types.ct.Header");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_gender), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_gender), i);
	return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_gender), i);
  }
   
  /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setGender(int addr, int i, String v) {
        if (featOkTst && casFeat_gender == null)
      jcas.throwFeatMissing("gender", "de.julielab.jcore.types.ct.Header");
    if (lowLevelTypeChecks)
      ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_gender), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_gender), i);
    ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_gender), i, v);
  }
 



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public Header_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_studyType = jcas.getRequiredFeatureDE(casType, "studyType", "uima.cas.String", featOkTst);
    casFeatCode_studyType  = (null == casFeat_studyType) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_studyType).getCode();

 
    casFeat_studyDesignInfo = jcas.getRequiredFeatureDE(casType, "studyDesignInfo", "de.julielab.jcore.types.ct.StudyDesignInfo", featOkTst);
    casFeatCode_studyDesignInfo  = (null == casFeat_studyDesignInfo) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_studyDesignInfo).getCode();

 
    casFeat_minimumAge = jcas.getRequiredFeatureDE(casType, "minimumAge", "uima.cas.Integer", featOkTst);
    casFeatCode_minimumAge  = (null == casFeat_minimumAge) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_minimumAge).getCode();

 
    casFeat_maximumAge = jcas.getRequiredFeatureDE(casType, "maximumAge", "uima.cas.Integer", featOkTst);
    casFeatCode_maximumAge  = (null == casFeat_maximumAge) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_maximumAge).getCode();

 
    casFeat_gender = jcas.getRequiredFeatureDE(casType, "gender", "uima.cas.StringArray", featOkTst);
    casFeatCode_gender  = (null == casFeat_gender) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_gender).getCode();

  }
}



    