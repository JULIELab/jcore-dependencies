
/* First created by JCasGen Tue Sep 03 12:34:17 CEST 2019 */
package de.julielab.jcore.types;

import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;

/** 
 * Updated by JCasGen Tue Sep 03 12:34:17 CEST 2019
 * @generated */
public class ConceptMention_Type extends Annotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = ConceptMention.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.julielab.jcore.types.ConceptMention");
 
  /** @generated */
  final Feature casFeat_specificType;
  /** @generated */
  final int     casFeatCode_specificType;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getSpecificType(int addr) {
        if (featOkTst && casFeat_specificType == null)
      jcas.throwFeatMissing("specificType", "de.julielab.jcore.types.ConceptMention");
    return ll_cas.ll_getStringValue(addr, casFeatCode_specificType);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setSpecificType(int addr, String v) {
        if (featOkTst && casFeat_specificType == null)
      jcas.throwFeatMissing("specificType", "de.julielab.jcore.types.ConceptMention");
    ll_cas.ll_setStringValue(addr, casFeatCode_specificType, v);}
    
  
 
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
      jcas.throwFeatMissing("ref", "de.julielab.jcore.types.ConceptMention");
    return ll_cas.ll_getRefValue(addr, casFeatCode_ref);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setRef(int addr, int v) {
        if (featOkTst && casFeat_ref == null)
      jcas.throwFeatMissing("ref", "de.julielab.jcore.types.ConceptMention");
    ll_cas.ll_setRefValue(addr, casFeatCode_ref, v);}
    
  
 
  /** @generated */
  final Feature casFeat_resourceEntryList;
  /** @generated */
  final int     casFeatCode_resourceEntryList;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getResourceEntryList(int addr) {
        if (featOkTst && casFeat_resourceEntryList == null)
      jcas.throwFeatMissing("resourceEntryList", "de.julielab.jcore.types.ConceptMention");
    return ll_cas.ll_getRefValue(addr, casFeatCode_resourceEntryList);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setResourceEntryList(int addr, int v) {
        if (featOkTst && casFeat_resourceEntryList == null)
      jcas.throwFeatMissing("resourceEntryList", "de.julielab.jcore.types.ConceptMention");
    ll_cas.ll_setRefValue(addr, casFeatCode_resourceEntryList, v);}
    
   /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public int getResourceEntryList(int addr, int i) {
        if (featOkTst && casFeat_resourceEntryList == null)
      jcas.throwFeatMissing("resourceEntryList", "de.julielab.jcore.types.ConceptMention");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_resourceEntryList), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_resourceEntryList), i);
	return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_resourceEntryList), i);
  }
   
  /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setResourceEntryList(int addr, int i, int v) {
        if (featOkTst && casFeat_resourceEntryList == null)
      jcas.throwFeatMissing("resourceEntryList", "de.julielab.jcore.types.ConceptMention");
    if (lowLevelTypeChecks)
      ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_resourceEntryList), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_resourceEntryList), i);
    ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_resourceEntryList), i, v);
  }
 
 
  /** @generated */
  final Feature casFeat_textualRepresentation;
  /** @generated */
  final int     casFeatCode_textualRepresentation;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getTextualRepresentation(int addr) {
        if (featOkTst && casFeat_textualRepresentation == null)
      jcas.throwFeatMissing("textualRepresentation", "de.julielab.jcore.types.ConceptMention");
    return ll_cas.ll_getStringValue(addr, casFeatCode_textualRepresentation);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setTextualRepresentation(int addr, String v) {
        if (featOkTst && casFeat_textualRepresentation == null)
      jcas.throwFeatMissing("textualRepresentation", "de.julielab.jcore.types.ConceptMention");
    ll_cas.ll_setStringValue(addr, casFeatCode_textualRepresentation, v);}
    
  
 
  /** @generated */
  final Feature casFeat_likelihood;
  /** @generated */
  final int     casFeatCode_likelihood;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getLikelihood(int addr) {
        if (featOkTst && casFeat_likelihood == null)
      jcas.throwFeatMissing("likelihood", "de.julielab.jcore.types.ConceptMention");
    return ll_cas.ll_getRefValue(addr, casFeatCode_likelihood);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setLikelihood(int addr, int v) {
        if (featOkTst && casFeat_likelihood == null)
      jcas.throwFeatMissing("likelihood", "de.julielab.jcore.types.ConceptMention");
    ll_cas.ll_setRefValue(addr, casFeatCode_likelihood, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public ConceptMention_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_specificType = jcas.getRequiredFeatureDE(casType, "specificType", "uima.cas.String", featOkTst);
    casFeatCode_specificType  = (null == casFeat_specificType) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_specificType).getCode();

 
    casFeat_ref = jcas.getRequiredFeatureDE(casType, "ref", "uima.cas.TOP", featOkTst);
    casFeatCode_ref  = (null == casFeat_ref) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_ref).getCode();

 
    casFeat_resourceEntryList = jcas.getRequiredFeatureDE(casType, "resourceEntryList", "uima.cas.FSArray", featOkTst);
    casFeatCode_resourceEntryList  = (null == casFeat_resourceEntryList) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_resourceEntryList).getCode();

 
    casFeat_textualRepresentation = jcas.getRequiredFeatureDE(casType, "textualRepresentation", "uima.cas.String", featOkTst);
    casFeatCode_textualRepresentation  = (null == casFeat_textualRepresentation) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_textualRepresentation).getCode();

 
    casFeat_likelihood = jcas.getRequiredFeatureDE(casType, "likelihood", "de.julielab.jcore.types.LikelihoodIndicator", featOkTst);
    casFeatCode_likelihood  = (null == casFeat_likelihood) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_likelihood).getCode();

  }
}



    