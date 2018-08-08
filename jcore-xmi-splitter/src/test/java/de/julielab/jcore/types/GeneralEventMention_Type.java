
/* First created by JCasGen Wed Aug 08 13:36:49 CEST 2018 */
package de.julielab.jcore.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;

/** A super type for all event mentions. Under this type, mentions for predicate-argument relations and eventive propositional relations are found. This terminology follows section 3.1 and more especially 3.1.3 of Ekaterina Buyko's dissertation "Event Extraction from Biomedical Texts Using
Trimmed Dependency Graphs" (http://www.db-thueringen.de/servlets/DerivateServlet/Derivate-26400/Diss2/thesis_buyko.pdf). The type system should completely follow the thesis' notion but does not due to historical reasons.
 * Updated by JCasGen Wed Aug 08 13:36:49 CEST 2018
 * @generated */
public class GeneralEventMention_Type extends ConceptMention_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = GeneralEventMention.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.julielab.jcore.types.GeneralEventMention");
 
  /** @generated */
  final Feature casFeat_tense;
  /** @generated */
  final int     casFeatCode_tense;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getTense(int addr) {
        if (featOkTst && casFeat_tense == null)
      jcas.throwFeatMissing("tense", "de.julielab.jcore.types.GeneralEventMention");
    return ll_cas.ll_getStringValue(addr, casFeatCode_tense);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setTense(int addr, String v) {
        if (featOkTst && casFeat_tense == null)
      jcas.throwFeatMissing("tense", "de.julielab.jcore.types.GeneralEventMention");
    ll_cas.ll_setStringValue(addr, casFeatCode_tense, v);}
    
  
 
  /** @generated */
  final Feature casFeat_modality;
  /** @generated */
  final int     casFeatCode_modality;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getModality(int addr) {
        if (featOkTst && casFeat_modality == null)
      jcas.throwFeatMissing("modality", "de.julielab.jcore.types.GeneralEventMention");
    return ll_cas.ll_getStringValue(addr, casFeatCode_modality);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setModality(int addr, String v) {
        if (featOkTst && casFeat_modality == null)
      jcas.throwFeatMissing("modality", "de.julielab.jcore.types.GeneralEventMention");
    ll_cas.ll_setStringValue(addr, casFeatCode_modality, v);}
    
  
 
  /** @generated */
  final Feature casFeat_arguments;
  /** @generated */
  final int     casFeatCode_arguments;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getArguments(int addr) {
        if (featOkTst && casFeat_arguments == null)
      jcas.throwFeatMissing("arguments", "de.julielab.jcore.types.GeneralEventMention");
    return ll_cas.ll_getRefValue(addr, casFeatCode_arguments);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setArguments(int addr, int v) {
        if (featOkTst && casFeat_arguments == null)
      jcas.throwFeatMissing("arguments", "de.julielab.jcore.types.GeneralEventMention");
    ll_cas.ll_setRefValue(addr, casFeatCode_arguments, v);}
    
   /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public int getArguments(int addr, int i) {
        if (featOkTst && casFeat_arguments == null)
      jcas.throwFeatMissing("arguments", "de.julielab.jcore.types.GeneralEventMention");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_arguments), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_arguments), i);
	return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_arguments), i);
  }
   
  /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setArguments(int addr, int i, int v) {
        if (featOkTst && casFeat_arguments == null)
      jcas.throwFeatMissing("arguments", "de.julielab.jcore.types.GeneralEventMention");
    if (lowLevelTypeChecks)
      ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_arguments), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_arguments), i);
    ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_arguments), i, v);
  }
 
 
  /** @generated */
  final Feature casFeat_reorderedArguments;
  /** @generated */
  final int     casFeatCode_reorderedArguments;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getReorderedArguments(int addr) {
        if (featOkTst && casFeat_reorderedArguments == null)
      jcas.throwFeatMissing("reorderedArguments", "de.julielab.jcore.types.GeneralEventMention");
    return ll_cas.ll_getRefValue(addr, casFeatCode_reorderedArguments);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setReorderedArguments(int addr, int v) {
        if (featOkTst && casFeat_reorderedArguments == null)
      jcas.throwFeatMissing("reorderedArguments", "de.julielab.jcore.types.GeneralEventMention");
    ll_cas.ll_setRefValue(addr, casFeatCode_reorderedArguments, v);}
    
   /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public int getReorderedArguments(int addr, int i) {
        if (featOkTst && casFeat_reorderedArguments == null)
      jcas.throwFeatMissing("reorderedArguments", "de.julielab.jcore.types.GeneralEventMention");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_reorderedArguments), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_reorderedArguments), i);
	return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_reorderedArguments), i);
  }
   
  /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setReorderedArguments(int addr, int i, int v) {
        if (featOkTst && casFeat_reorderedArguments == null)
      jcas.throwFeatMissing("reorderedArguments", "de.julielab.jcore.types.GeneralEventMention");
    if (lowLevelTypeChecks)
      ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_reorderedArguments), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_reorderedArguments), i);
    ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_reorderedArguments), i, v);
  }
 
 
  /** @generated */
  final Feature casFeat_polarity;
  /** @generated */
  final int     casFeatCode_polarity;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getPolarity(int addr) {
        if (featOkTst && casFeat_polarity == null)
      jcas.throwFeatMissing("polarity", "de.julielab.jcore.types.GeneralEventMention");
    return ll_cas.ll_getStringValue(addr, casFeatCode_polarity);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setPolarity(int addr, String v) {
        if (featOkTst && casFeat_polarity == null)
      jcas.throwFeatMissing("polarity", "de.julielab.jcore.types.GeneralEventMention");
    ll_cas.ll_setStringValue(addr, casFeatCode_polarity, v);}
    
  
 
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
      jcas.throwFeatMissing("relationString", "de.julielab.jcore.types.GeneralEventMention");
    return ll_cas.ll_getRefValue(addr, casFeatCode_relationString);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setRelationString(int addr, int v) {
        if (featOkTst && casFeat_relationString == null)
      jcas.throwFeatMissing("relationString", "de.julielab.jcore.types.GeneralEventMention");
    ll_cas.ll_setRefValue(addr, casFeatCode_relationString, v);}
    
   /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public String getRelationString(int addr, int i) {
        if (featOkTst && casFeat_relationString == null)
      jcas.throwFeatMissing("relationString", "de.julielab.jcore.types.GeneralEventMention");
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
      jcas.throwFeatMissing("relationString", "de.julielab.jcore.types.GeneralEventMention");
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
  public GeneralEventMention_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_tense = jcas.getRequiredFeatureDE(casType, "tense", "uima.cas.String", featOkTst);
    casFeatCode_tense  = (null == casFeat_tense) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_tense).getCode();

 
    casFeat_modality = jcas.getRequiredFeatureDE(casType, "modality", "uima.cas.String", featOkTst);
    casFeatCode_modality  = (null == casFeat_modality) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_modality).getCode();

 
    casFeat_arguments = jcas.getRequiredFeatureDE(casType, "arguments", "uima.cas.FSArray", featOkTst);
    casFeatCode_arguments  = (null == casFeat_arguments) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_arguments).getCode();

 
    casFeat_reorderedArguments = jcas.getRequiredFeatureDE(casType, "reorderedArguments", "uima.cas.FSArray", featOkTst);
    casFeatCode_reorderedArguments  = (null == casFeat_reorderedArguments) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_reorderedArguments).getCode();

 
    casFeat_polarity = jcas.getRequiredFeatureDE(casType, "polarity", "uima.cas.String", featOkTst);
    casFeatCode_polarity  = (null == casFeat_polarity) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_polarity).getCode();

 
    casFeat_relationString = jcas.getRequiredFeatureDE(casType, "relationString", "uima.cas.StringArray", featOkTst);
    casFeatCode_relationString  = (null == casFeat_relationString) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_relationString).getCode();

  }
}



    