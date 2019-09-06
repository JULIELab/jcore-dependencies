
/* First created by JCasGen Tue Sep 03 12:34:17 CEST 2019 */
package de.julielab.jcore.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;

/** A specific version of the resource entry for Gene entries.
 * Updated by JCasGen Tue Sep 03 12:34:17 CEST 2019
 * @generated */
public class GeneResourceEntry_Type extends ResourceEntry_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = GeneResourceEntry.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.julielab.jcore.types.GeneResourceEntry");
 
  /** @generated */
  final Feature casFeat_synonym;
  /** @generated */
  final int     casFeatCode_synonym;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getSynonym(int addr) {
        if (featOkTst && casFeat_synonym == null)
      jcas.throwFeatMissing("synonym", "de.julielab.jcore.types.GeneResourceEntry");
    return ll_cas.ll_getStringValue(addr, casFeatCode_synonym);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setSynonym(int addr, String v) {
        if (featOkTst && casFeat_synonym == null)
      jcas.throwFeatMissing("synonym", "de.julielab.jcore.types.GeneResourceEntry");
    ll_cas.ll_setStringValue(addr, casFeatCode_synonym, v);}
    
  
 
  /** @generated */
  final Feature casFeat_confidenceMention;
  /** @generated */
  final int     casFeatCode_confidenceMention;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public double getConfidenceMention(int addr) {
        if (featOkTst && casFeat_confidenceMention == null)
      jcas.throwFeatMissing("confidenceMention", "de.julielab.jcore.types.GeneResourceEntry");
    return ll_cas.ll_getDoubleValue(addr, casFeatCode_confidenceMention);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setConfidenceMention(int addr, double v) {
        if (featOkTst && casFeat_confidenceMention == null)
      jcas.throwFeatMissing("confidenceMention", "de.julielab.jcore.types.GeneResourceEntry");
    ll_cas.ll_setDoubleValue(addr, casFeatCode_confidenceMention, v);}
    
  
 
  /** @generated */
  final Feature casFeat_confidenceSemantic;
  /** @generated */
  final int     casFeatCode_confidenceSemantic;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public double getConfidenceSemantic(int addr) {
        if (featOkTst && casFeat_confidenceSemantic == null)
      jcas.throwFeatMissing("confidenceSemantic", "de.julielab.jcore.types.GeneResourceEntry");
    return ll_cas.ll_getDoubleValue(addr, casFeatCode_confidenceSemantic);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setConfidenceSemantic(int addr, double v) {
        if (featOkTst && casFeat_confidenceSemantic == null)
      jcas.throwFeatMissing("confidenceSemantic", "de.julielab.jcore.types.GeneResourceEntry");
    ll_cas.ll_setDoubleValue(addr, casFeatCode_confidenceSemantic, v);}
    
  
 
  /** @generated */
  final Feature casFeat_taxonomyId;
  /** @generated */
  final int     casFeatCode_taxonomyId;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getTaxonomyId(int addr) {
        if (featOkTst && casFeat_taxonomyId == null)
      jcas.throwFeatMissing("taxonomyId", "de.julielab.jcore.types.GeneResourceEntry");
    return ll_cas.ll_getStringValue(addr, casFeatCode_taxonomyId);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setTaxonomyId(int addr, String v) {
        if (featOkTst && casFeat_taxonomyId == null)
      jcas.throwFeatMissing("taxonomyId", "de.julielab.jcore.types.GeneResourceEntry");
    ll_cas.ll_setStringValue(addr, casFeatCode_taxonomyId, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public GeneResourceEntry_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_synonym = jcas.getRequiredFeatureDE(casType, "synonym", "uima.cas.String", featOkTst);
    casFeatCode_synonym  = (null == casFeat_synonym) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_synonym).getCode();

 
    casFeat_confidenceMention = jcas.getRequiredFeatureDE(casType, "confidenceMention", "uima.cas.Double", featOkTst);
    casFeatCode_confidenceMention  = (null == casFeat_confidenceMention) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_confidenceMention).getCode();

 
    casFeat_confidenceSemantic = jcas.getRequiredFeatureDE(casType, "confidenceSemantic", "uima.cas.Double", featOkTst);
    casFeatCode_confidenceSemantic  = (null == casFeat_confidenceSemantic) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_confidenceSemantic).getCode();

 
    casFeat_taxonomyId = jcas.getRequiredFeatureDE(casType, "taxonomyId", "uima.cas.String", featOkTst);
    casFeatCode_taxonomyId  = (null == casFeat_taxonomyId) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_taxonomyId).getCode();

  }
}



    