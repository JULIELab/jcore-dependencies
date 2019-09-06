
/* First created by JCasGen Tue Sep 03 12:34:17 CEST 2019 */
package de.julielab.jcore.types.ct;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import de.julielab.jcore.types.DocumentAnnotation_Type;

/** 
 * Updated by JCasGen Tue Sep 03 12:34:17 CEST 2019
 * @generated */
public class StudyDesignInfo_Type extends DocumentAnnotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = StudyDesignInfo.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.julielab.jcore.types.ct.StudyDesignInfo");
 
  /** @generated */
  final Feature casFeat_interventionModel;
  /** @generated */
  final int     casFeatCode_interventionModel;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getInterventionModel(int addr) {
        if (featOkTst && casFeat_interventionModel == null)
      jcas.throwFeatMissing("interventionModel", "de.julielab.jcore.types.ct.StudyDesignInfo");
    return ll_cas.ll_getStringValue(addr, casFeatCode_interventionModel);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setInterventionModel(int addr, String v) {
        if (featOkTst && casFeat_interventionModel == null)
      jcas.throwFeatMissing("interventionModel", "de.julielab.jcore.types.ct.StudyDesignInfo");
    ll_cas.ll_setStringValue(addr, casFeatCode_interventionModel, v);}
    
  
 
  /** @generated */
  final Feature casFeat_primaryPurpose;
  /** @generated */
  final int     casFeatCode_primaryPurpose;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getPrimaryPurpose(int addr) {
        if (featOkTst && casFeat_primaryPurpose == null)
      jcas.throwFeatMissing("primaryPurpose", "de.julielab.jcore.types.ct.StudyDesignInfo");
    return ll_cas.ll_getStringValue(addr, casFeatCode_primaryPurpose);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setPrimaryPurpose(int addr, String v) {
        if (featOkTst && casFeat_primaryPurpose == null)
      jcas.throwFeatMissing("primaryPurpose", "de.julielab.jcore.types.ct.StudyDesignInfo");
    ll_cas.ll_setStringValue(addr, casFeatCode_primaryPurpose, v);}
    
  
 
  /** @generated */
  final Feature casFeat_masking;
  /** @generated */
  final int     casFeatCode_masking;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getMasking(int addr) {
        if (featOkTst && casFeat_masking == null)
      jcas.throwFeatMissing("masking", "de.julielab.jcore.types.ct.StudyDesignInfo");
    return ll_cas.ll_getStringValue(addr, casFeatCode_masking);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setMasking(int addr, String v) {
        if (featOkTst && casFeat_masking == null)
      jcas.throwFeatMissing("masking", "de.julielab.jcore.types.ct.StudyDesignInfo");
    ll_cas.ll_setStringValue(addr, casFeatCode_masking, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public StudyDesignInfo_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_interventionModel = jcas.getRequiredFeatureDE(casType, "interventionModel", "uima.cas.String", featOkTst);
    casFeatCode_interventionModel  = (null == casFeat_interventionModel) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_interventionModel).getCode();

 
    casFeat_primaryPurpose = jcas.getRequiredFeatureDE(casType, "primaryPurpose", "uima.cas.String", featOkTst);
    casFeatCode_primaryPurpose  = (null == casFeat_primaryPurpose) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_primaryPurpose).getCode();

 
    casFeat_masking = jcas.getRequiredFeatureDE(casType, "masking", "uima.cas.String", featOkTst);
    casFeatCode_masking  = (null == casFeat_masking) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_masking).getCode();

  }
}



    