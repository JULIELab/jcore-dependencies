
/* First created by JCasGen Tue Sep 03 12:34:17 CEST 2019 */
package de.julielab.jcore.types;

import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;

/** This type refers to the likelihood aspect of epistemic modality. The annotation marks epistemic modal expressions used to linguistically modify the likelihood of an event or of a relation that an entity might be in. The employed likelihood scale also includes negation (0% likelihood) and assertion (100% likelihood), the latter being the default case where no explicit likelihood modifier is present in the text.
 * Updated by JCasGen Tue Sep 03 12:34:17 CEST 2019
 * @generated */
public class LikelihoodIndicator_Type extends Annotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = LikelihoodIndicator.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.julielab.jcore.types.LikelihoodIndicator");
 
  /** @generated */
  final Feature casFeat_likelihood;
  /** @generated */
  final int     casFeatCode_likelihood;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getLikelihood(int addr) {
        if (featOkTst && casFeat_likelihood == null)
      jcas.throwFeatMissing("likelihood", "de.julielab.jcore.types.LikelihoodIndicator");
    return ll_cas.ll_getStringValue(addr, casFeatCode_likelihood);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setLikelihood(int addr, String v) {
        if (featOkTst && casFeat_likelihood == null)
      jcas.throwFeatMissing("likelihood", "de.julielab.jcore.types.LikelihoodIndicator");
    ll_cas.ll_setStringValue(addr, casFeatCode_likelihood, v);}
    
  
 
  /** @generated */
  final Feature casFeat_entityAndRelationString;
  /** @generated */
  final int     casFeatCode_entityAndRelationString;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getEntityAndRelationString(int addr) {
        if (featOkTst && casFeat_entityAndRelationString == null)
      jcas.throwFeatMissing("entityAndRelationString", "de.julielab.jcore.types.LikelihoodIndicator");
    return ll_cas.ll_getRefValue(addr, casFeatCode_entityAndRelationString);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setEntityAndRelationString(int addr, int v) {
        if (featOkTst && casFeat_entityAndRelationString == null)
      jcas.throwFeatMissing("entityAndRelationString", "de.julielab.jcore.types.LikelihoodIndicator");
    ll_cas.ll_setRefValue(addr, casFeatCode_entityAndRelationString, v);}
    
   /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public String getEntityAndRelationString(int addr, int i) {
        if (featOkTst && casFeat_entityAndRelationString == null)
      jcas.throwFeatMissing("entityAndRelationString", "de.julielab.jcore.types.LikelihoodIndicator");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_entityAndRelationString), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_entityAndRelationString), i);
	return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_entityAndRelationString), i);
  }
   
  /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setEntityAndRelationString(int addr, int i, String v) {
        if (featOkTst && casFeat_entityAndRelationString == null)
      jcas.throwFeatMissing("entityAndRelationString", "de.julielab.jcore.types.LikelihoodIndicator");
    if (lowLevelTypeChecks)
      ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_entityAndRelationString), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_entityAndRelationString), i);
    ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_entityAndRelationString), i, v);
  }
 



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public LikelihoodIndicator_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_likelihood = jcas.getRequiredFeatureDE(casType, "likelihood", "de.julielab.jcore.types.Likelihood", featOkTst);
    casFeatCode_likelihood  = (null == casFeat_likelihood) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_likelihood).getCode();

 
    casFeat_entityAndRelationString = jcas.getRequiredFeatureDE(casType, "entityAndRelationString", "uima.cas.StringArray", featOkTst);
    casFeatCode_entityAndRelationString  = (null == casFeat_entityAndRelationString) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_entityAndRelationString).getCode();

  }
}



    