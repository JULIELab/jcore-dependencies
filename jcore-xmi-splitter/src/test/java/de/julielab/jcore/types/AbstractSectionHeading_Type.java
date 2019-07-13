
/* First created by JCasGen Sat Jul 13 15:41:00 CEST 2019 */
package de.julielab.jcore.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;

/** The heading of a section of a structured abstract as
				used by MEDLINE and PubMed.
				The TitleType feature value should always be 'abstractSection'.
 * Updated by JCasGen Sat Jul 13 15:41:00 CEST 2019
 * @generated */
public class AbstractSectionHeading_Type extends Title_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = AbstractSectionHeading.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.julielab.jcore.types.AbstractSectionHeading");
 
  /** @generated */
  final Feature casFeat_label;
  /** @generated */
  final int     casFeatCode_label;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getLabel(int addr) {
        if (featOkTst && casFeat_label == null)
      jcas.throwFeatMissing("label", "de.julielab.jcore.types.AbstractSectionHeading");
    return ll_cas.ll_getStringValue(addr, casFeatCode_label);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setLabel(int addr, String v) {
        if (featOkTst && casFeat_label == null)
      jcas.throwFeatMissing("label", "de.julielab.jcore.types.AbstractSectionHeading");
    ll_cas.ll_setStringValue(addr, casFeatCode_label, v);}
    
  
 
  /** @generated */
  final Feature casFeat_nlmCategory;
  /** @generated */
  final int     casFeatCode_nlmCategory;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getNlmCategory(int addr) {
        if (featOkTst && casFeat_nlmCategory == null)
      jcas.throwFeatMissing("nlmCategory", "de.julielab.jcore.types.AbstractSectionHeading");
    return ll_cas.ll_getStringValue(addr, casFeatCode_nlmCategory);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setNlmCategory(int addr, String v) {
        if (featOkTst && casFeat_nlmCategory == null)
      jcas.throwFeatMissing("nlmCategory", "de.julielab.jcore.types.AbstractSectionHeading");
    ll_cas.ll_setStringValue(addr, casFeatCode_nlmCategory, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public AbstractSectionHeading_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_label = jcas.getRequiredFeatureDE(casType, "label", "uima.cas.String", featOkTst);
    casFeatCode_label  = (null == casFeat_label) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_label).getCode();

 
    casFeat_nlmCategory = jcas.getRequiredFeatureDE(casType, "nlmCategory", "uima.cas.String", featOkTst);
    casFeatCode_nlmCategory  = (null == casFeat_nlmCategory) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_nlmCategory).getCode();

  }
}



    