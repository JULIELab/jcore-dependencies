
/* First created by JCasGen Wed Aug 08 13:36:50 CEST 2018 */
package de.julielab.jcore.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;

/** Token annotation marks the span of a token and takes all additional annotations that are on the token level, including Part-of-Speech information, lemma, stemmed form,  grammatical features such as gender, number and orthographical information; furthemore, Token includes the information about dependency relations to other tokens (see correspondent annotation types for further infromation).
 * Updated by JCasGen Wed Aug 08 13:36:50 CEST 2018
 * @generated */
public class Token_Type extends Annotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Token.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.julielab.jcore.types.Token");
 
  /** @generated */
  final Feature casFeat_lemma;
  /** @generated */
  final int     casFeatCode_lemma;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getLemma(int addr) {
        if (featOkTst && casFeat_lemma == null)
      jcas.throwFeatMissing("lemma", "de.julielab.jcore.types.Token");
    return ll_cas.ll_getRefValue(addr, casFeatCode_lemma);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setLemma(int addr, int v) {
        if (featOkTst && casFeat_lemma == null)
      jcas.throwFeatMissing("lemma", "de.julielab.jcore.types.Token");
    ll_cas.ll_setRefValue(addr, casFeatCode_lemma, v);}
    
  
 
  /** @generated */
  final Feature casFeat_posTag;
  /** @generated */
  final int     casFeatCode_posTag;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getPosTag(int addr) {
        if (featOkTst && casFeat_posTag == null)
      jcas.throwFeatMissing("posTag", "de.julielab.jcore.types.Token");
    return ll_cas.ll_getRefValue(addr, casFeatCode_posTag);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setPosTag(int addr, int v) {
        if (featOkTst && casFeat_posTag == null)
      jcas.throwFeatMissing("posTag", "de.julielab.jcore.types.Token");
    ll_cas.ll_setRefValue(addr, casFeatCode_posTag, v);}
    
   /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public int getPosTag(int addr, int i) {
        if (featOkTst && casFeat_posTag == null)
      jcas.throwFeatMissing("posTag", "de.julielab.jcore.types.Token");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_posTag), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_posTag), i);
	return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_posTag), i);
  }
   
  /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setPosTag(int addr, int i, int v) {
        if (featOkTst && casFeat_posTag == null)
      jcas.throwFeatMissing("posTag", "de.julielab.jcore.types.Token");
    if (lowLevelTypeChecks)
      ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_posTag), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_posTag), i);
    ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_posTag), i, v);
  }
 
 
  /** @generated */
  final Feature casFeat_stemmedForm;
  /** @generated */
  final int     casFeatCode_stemmedForm;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getStemmedForm(int addr) {
        if (featOkTst && casFeat_stemmedForm == null)
      jcas.throwFeatMissing("stemmedForm", "de.julielab.jcore.types.Token");
    return ll_cas.ll_getRefValue(addr, casFeatCode_stemmedForm);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setStemmedForm(int addr, int v) {
        if (featOkTst && casFeat_stemmedForm == null)
      jcas.throwFeatMissing("stemmedForm", "de.julielab.jcore.types.Token");
    ll_cas.ll_setRefValue(addr, casFeatCode_stemmedForm, v);}
    
  
 
  /** @generated */
  final Feature casFeat_feats;
  /** @generated */
  final int     casFeatCode_feats;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getFeats(int addr) {
        if (featOkTst && casFeat_feats == null)
      jcas.throwFeatMissing("feats", "de.julielab.jcore.types.Token");
    return ll_cas.ll_getRefValue(addr, casFeatCode_feats);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setFeats(int addr, int v) {
        if (featOkTst && casFeat_feats == null)
      jcas.throwFeatMissing("feats", "de.julielab.jcore.types.Token");
    ll_cas.ll_setRefValue(addr, casFeatCode_feats, v);}
    
  
 
  /** @generated */
  final Feature casFeat_orthogr;
  /** @generated */
  final int     casFeatCode_orthogr;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getOrthogr(int addr) {
        if (featOkTst && casFeat_orthogr == null)
      jcas.throwFeatMissing("orthogr", "de.julielab.jcore.types.Token");
    return ll_cas.ll_getStringValue(addr, casFeatCode_orthogr);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setOrthogr(int addr, String v) {
        if (featOkTst && casFeat_orthogr == null)
      jcas.throwFeatMissing("orthogr", "de.julielab.jcore.types.Token");
    ll_cas.ll_setStringValue(addr, casFeatCode_orthogr, v);}
    
  
 
  /** @generated */
  final Feature casFeat_depRel;
  /** @generated */
  final int     casFeatCode_depRel;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getDepRel(int addr) {
        if (featOkTst && casFeat_depRel == null)
      jcas.throwFeatMissing("depRel", "de.julielab.jcore.types.Token");
    return ll_cas.ll_getRefValue(addr, casFeatCode_depRel);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setDepRel(int addr, int v) {
        if (featOkTst && casFeat_depRel == null)
      jcas.throwFeatMissing("depRel", "de.julielab.jcore.types.Token");
    ll_cas.ll_setRefValue(addr, casFeatCode_depRel, v);}
    
   /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public int getDepRel(int addr, int i) {
        if (featOkTst && casFeat_depRel == null)
      jcas.throwFeatMissing("depRel", "de.julielab.jcore.types.Token");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_depRel), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_depRel), i);
	return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_depRel), i);
  }
   
  /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setDepRel(int addr, int i, int v) {
        if (featOkTst && casFeat_depRel == null)
      jcas.throwFeatMissing("depRel", "de.julielab.jcore.types.Token");
    if (lowLevelTypeChecks)
      ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_depRel), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_depRel), i);
    ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_depRel), i, v);
  }
 
 
  /** @generated */
  final Feature casFeat_synonyms;
  /** @generated */
  final int     casFeatCode_synonyms;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getSynonyms(int addr) {
        if (featOkTst && casFeat_synonyms == null)
      jcas.throwFeatMissing("synonyms", "de.julielab.jcore.types.Token");
    return ll_cas.ll_getRefValue(addr, casFeatCode_synonyms);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setSynonyms(int addr, int v) {
        if (featOkTst && casFeat_synonyms == null)
      jcas.throwFeatMissing("synonyms", "de.julielab.jcore.types.Token");
    ll_cas.ll_setRefValue(addr, casFeatCode_synonyms, v);}
    
   /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public String getSynonyms(int addr, int i) {
        if (featOkTst && casFeat_synonyms == null)
      jcas.throwFeatMissing("synonyms", "de.julielab.jcore.types.Token");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_synonyms), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_synonyms), i);
	return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_synonyms), i);
  }
   
  /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setSynonyms(int addr, int i, String v) {
        if (featOkTst && casFeat_synonyms == null)
      jcas.throwFeatMissing("synonyms", "de.julielab.jcore.types.Token");
    if (lowLevelTypeChecks)
      ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_synonyms), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_synonyms), i);
    ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_synonyms), i, v);
  }
 
 
  /** @generated */
  final Feature casFeat_hypernyms;
  /** @generated */
  final int     casFeatCode_hypernyms;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getHypernyms(int addr) {
        if (featOkTst && casFeat_hypernyms == null)
      jcas.throwFeatMissing("hypernyms", "de.julielab.jcore.types.Token");
    return ll_cas.ll_getRefValue(addr, casFeatCode_hypernyms);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setHypernyms(int addr, int v) {
        if (featOkTst && casFeat_hypernyms == null)
      jcas.throwFeatMissing("hypernyms", "de.julielab.jcore.types.Token");
    ll_cas.ll_setRefValue(addr, casFeatCode_hypernyms, v);}
    
   /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public String getHypernyms(int addr, int i) {
        if (featOkTst && casFeat_hypernyms == null)
      jcas.throwFeatMissing("hypernyms", "de.julielab.jcore.types.Token");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_hypernyms), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_hypernyms), i);
	return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_hypernyms), i);
  }
   
  /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setHypernyms(int addr, int i, String v) {
        if (featOkTst && casFeat_hypernyms == null)
      jcas.throwFeatMissing("hypernyms", "de.julielab.jcore.types.Token");
    if (lowLevelTypeChecks)
      ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_hypernyms), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_hypernyms), i);
    ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_hypernyms), i, v);
  }
 



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public Token_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_lemma = jcas.getRequiredFeatureDE(casType, "lemma", "de.julielab.jcore.types.Lemma", featOkTst);
    casFeatCode_lemma  = (null == casFeat_lemma) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_lemma).getCode();

 
    casFeat_posTag = jcas.getRequiredFeatureDE(casType, "posTag", "uima.cas.FSArray", featOkTst);
    casFeatCode_posTag  = (null == casFeat_posTag) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_posTag).getCode();

 
    casFeat_stemmedForm = jcas.getRequiredFeatureDE(casType, "stemmedForm", "de.julielab.jcore.types.StemmedForm", featOkTst);
    casFeatCode_stemmedForm  = (null == casFeat_stemmedForm) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_stemmedForm).getCode();

 
    casFeat_feats = jcas.getRequiredFeatureDE(casType, "feats", "de.julielab.jcore.types.GrammaticalFeats", featOkTst);
    casFeatCode_feats  = (null == casFeat_feats) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_feats).getCode();

 
    casFeat_orthogr = jcas.getRequiredFeatureDE(casType, "orthogr", "de.julielab.jcore.types.Orthography", featOkTst);
    casFeatCode_orthogr  = (null == casFeat_orthogr) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_orthogr).getCode();

 
    casFeat_depRel = jcas.getRequiredFeatureDE(casType, "depRel", "uima.cas.FSArray", featOkTst);
    casFeatCode_depRel  = (null == casFeat_depRel) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_depRel).getCode();

 
    casFeat_synonyms = jcas.getRequiredFeatureDE(casType, "synonyms", "uima.cas.StringArray", featOkTst);
    casFeatCode_synonyms  = (null == casFeat_synonyms) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_synonyms).getCode();

 
    casFeat_hypernyms = jcas.getRequiredFeatureDE(casType, "hypernyms", "uima.cas.StringArray", featOkTst);
    casFeatCode_hypernyms  = (null == casFeat_hypernyms) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_hypernyms).getCode();

  }
}



    