

/* First created by JCasGen Sat Jul 13 15:41:00 CEST 2019 */
package de.julielab.jcore.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.StringArray;


/** A super type for all event mentions. Under this type, mentions for predicate-argument relations and eventive propositional relations are found. This terminology follows section 3.1 and more especially 3.1.3 of Ekaterina Buyko's dissertation "Event Extraction from Biomedical Texts Using
Trimmed Dependency Graphs" (http://www.db-thueringen.de/servlets/DerivateServlet/Derivate-26400/Diss2/thesis_buyko.pdf). The type system should completely follow the thesis' notion but does not due to historical reasons.
 * Updated by JCasGen Sat Jul 13 15:41:00 CEST 2019
 * XML source: all-test-types.xml
 * @generated */
public class GeneralEventMention extends ConceptMention {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(GeneralEventMention.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected GeneralEventMention() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public GeneralEventMention(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public GeneralEventMention(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public GeneralEventMention(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** 
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   * @generated modifiable 
   */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: tense

  /** getter for tense - gets 
   * @generated
   * @return value of the feature 
   */
  public String getTense() {
    if (GeneralEventMention_Type.featOkTst && ((GeneralEventMention_Type)jcasType).casFeat_tense == null)
      jcasType.jcas.throwFeatMissing("tense", "de.julielab.jcore.types.GeneralEventMention");
    return jcasType.ll_cas.ll_getStringValue(addr, ((GeneralEventMention_Type)jcasType).casFeatCode_tense);}
    
  /** setter for tense - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setTense(String v) {
    if (GeneralEventMention_Type.featOkTst && ((GeneralEventMention_Type)jcasType).casFeat_tense == null)
      jcasType.jcas.throwFeatMissing("tense", "de.julielab.jcore.types.GeneralEventMention");
    jcasType.ll_cas.ll_setStringValue(addr, ((GeneralEventMention_Type)jcasType).casFeatCode_tense, v);}    
   
    
  //*--------------*
  //* Feature: modality

  /** getter for modality - gets 
   * @generated
   * @return value of the feature 
   */
  public String getModality() {
    if (GeneralEventMention_Type.featOkTst && ((GeneralEventMention_Type)jcasType).casFeat_modality == null)
      jcasType.jcas.throwFeatMissing("modality", "de.julielab.jcore.types.GeneralEventMention");
    return jcasType.ll_cas.ll_getStringValue(addr, ((GeneralEventMention_Type)jcasType).casFeatCode_modality);}
    
  /** setter for modality - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setModality(String v) {
    if (GeneralEventMention_Type.featOkTst && ((GeneralEventMention_Type)jcasType).casFeat_modality == null)
      jcasType.jcas.throwFeatMissing("modality", "de.julielab.jcore.types.GeneralEventMention");
    jcasType.ll_cas.ll_setStringValue(addr, ((GeneralEventMention_Type)jcasType).casFeatCode_modality, v);}    
   
    
  //*--------------*
  //* Feature: arguments

  /** getter for arguments - gets 
   * @generated
   * @return value of the feature 
   */
  public FSArray getArguments() {
    if (GeneralEventMention_Type.featOkTst && ((GeneralEventMention_Type)jcasType).casFeat_arguments == null)
      jcasType.jcas.throwFeatMissing("arguments", "de.julielab.jcore.types.GeneralEventMention");
    return (FSArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((GeneralEventMention_Type)jcasType).casFeatCode_arguments)));}
    
  /** setter for arguments - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setArguments(FSArray v) {
    if (GeneralEventMention_Type.featOkTst && ((GeneralEventMention_Type)jcasType).casFeat_arguments == null)
      jcasType.jcas.throwFeatMissing("arguments", "de.julielab.jcore.types.GeneralEventMention");
    jcasType.ll_cas.ll_setRefValue(addr, ((GeneralEventMention_Type)jcasType).casFeatCode_arguments, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for arguments - gets an indexed value - 
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public ArgumentMention getArguments(int i) {
    if (GeneralEventMention_Type.featOkTst && ((GeneralEventMention_Type)jcasType).casFeat_arguments == null)
      jcasType.jcas.throwFeatMissing("arguments", "de.julielab.jcore.types.GeneralEventMention");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((GeneralEventMention_Type)jcasType).casFeatCode_arguments), i);
    return (ArgumentMention)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((GeneralEventMention_Type)jcasType).casFeatCode_arguments), i)));}

  /** indexed setter for arguments - sets an indexed value - 
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setArguments(int i, ArgumentMention v) { 
    if (GeneralEventMention_Type.featOkTst && ((GeneralEventMention_Type)jcasType).casFeat_arguments == null)
      jcasType.jcas.throwFeatMissing("arguments", "de.julielab.jcore.types.GeneralEventMention");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((GeneralEventMention_Type)jcasType).casFeatCode_arguments), i);
    jcasType.ll_cas.ll_setRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((GeneralEventMention_Type)jcasType).casFeatCode_arguments), i, jcasType.ll_cas.ll_getFSRef(v));}
   
    
  //*--------------*
  //* Feature: reorderedArguments

  /** getter for reorderedArguments - gets 
   * @generated
   * @return value of the feature 
   */
  public FSArray getReorderedArguments() {
    if (GeneralEventMention_Type.featOkTst && ((GeneralEventMention_Type)jcasType).casFeat_reorderedArguments == null)
      jcasType.jcas.throwFeatMissing("reorderedArguments", "de.julielab.jcore.types.GeneralEventMention");
    return (FSArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((GeneralEventMention_Type)jcasType).casFeatCode_reorderedArguments)));}
    
  /** setter for reorderedArguments - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setReorderedArguments(FSArray v) {
    if (GeneralEventMention_Type.featOkTst && ((GeneralEventMention_Type)jcasType).casFeat_reorderedArguments == null)
      jcasType.jcas.throwFeatMissing("reorderedArguments", "de.julielab.jcore.types.GeneralEventMention");
    jcasType.ll_cas.ll_setRefValue(addr, ((GeneralEventMention_Type)jcasType).casFeatCode_reorderedArguments, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for reorderedArguments - gets an indexed value - 
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public ArgumentMention getReorderedArguments(int i) {
    if (GeneralEventMention_Type.featOkTst && ((GeneralEventMention_Type)jcasType).casFeat_reorderedArguments == null)
      jcasType.jcas.throwFeatMissing("reorderedArguments", "de.julielab.jcore.types.GeneralEventMention");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((GeneralEventMention_Type)jcasType).casFeatCode_reorderedArguments), i);
    return (ArgumentMention)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((GeneralEventMention_Type)jcasType).casFeatCode_reorderedArguments), i)));}

  /** indexed setter for reorderedArguments - sets an indexed value - 
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setReorderedArguments(int i, ArgumentMention v) { 
    if (GeneralEventMention_Type.featOkTst && ((GeneralEventMention_Type)jcasType).casFeat_reorderedArguments == null)
      jcasType.jcas.throwFeatMissing("reorderedArguments", "de.julielab.jcore.types.GeneralEventMention");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((GeneralEventMention_Type)jcasType).casFeatCode_reorderedArguments), i);
    jcasType.ll_cas.ll_setRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((GeneralEventMention_Type)jcasType).casFeatCode_reorderedArguments), i, jcasType.ll_cas.ll_getFSRef(v));}
   
    
  //*--------------*
  //* Feature: polarity

  /** getter for polarity - gets 
   * @generated
   * @return value of the feature 
   */
  public String getPolarity() {
    if (GeneralEventMention_Type.featOkTst && ((GeneralEventMention_Type)jcasType).casFeat_polarity == null)
      jcasType.jcas.throwFeatMissing("polarity", "de.julielab.jcore.types.GeneralEventMention");
    return jcasType.ll_cas.ll_getStringValue(addr, ((GeneralEventMention_Type)jcasType).casFeatCode_polarity);}
    
  /** setter for polarity - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setPolarity(String v) {
    if (GeneralEventMention_Type.featOkTst && ((GeneralEventMention_Type)jcasType).casFeat_polarity == null)
      jcasType.jcas.throwFeatMissing("polarity", "de.julielab.jcore.types.GeneralEventMention");
    jcasType.ll_cas.ll_setStringValue(addr, ((GeneralEventMention_Type)jcasType).casFeatCode_polarity, v);}    
   
    
  //*--------------*
  //* Feature: relationString

  /** getter for relationString - gets used to make relations visible for LuCas
   * @generated
   * @return value of the feature 
   */
  public StringArray getRelationString() {
    if (GeneralEventMention_Type.featOkTst && ((GeneralEventMention_Type)jcasType).casFeat_relationString == null)
      jcasType.jcas.throwFeatMissing("relationString", "de.julielab.jcore.types.GeneralEventMention");
    return (StringArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((GeneralEventMention_Type)jcasType).casFeatCode_relationString)));}
    
  /** setter for relationString - sets used to make relations visible for LuCas 
   * @generated
   * @param v value to set into the feature 
   */
  public void setRelationString(StringArray v) {
    if (GeneralEventMention_Type.featOkTst && ((GeneralEventMention_Type)jcasType).casFeat_relationString == null)
      jcasType.jcas.throwFeatMissing("relationString", "de.julielab.jcore.types.GeneralEventMention");
    jcasType.ll_cas.ll_setRefValue(addr, ((GeneralEventMention_Type)jcasType).casFeatCode_relationString, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for relationString - gets an indexed value - used to make relations visible for LuCas
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public String getRelationString(int i) {
    if (GeneralEventMention_Type.featOkTst && ((GeneralEventMention_Type)jcasType).casFeat_relationString == null)
      jcasType.jcas.throwFeatMissing("relationString", "de.julielab.jcore.types.GeneralEventMention");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((GeneralEventMention_Type)jcasType).casFeatCode_relationString), i);
    return jcasType.ll_cas.ll_getStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((GeneralEventMention_Type)jcasType).casFeatCode_relationString), i);}

  /** indexed setter for relationString - sets an indexed value - used to make relations visible for LuCas
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setRelationString(int i, String v) { 
    if (GeneralEventMention_Type.featOkTst && ((GeneralEventMention_Type)jcasType).casFeat_relationString == null)
      jcasType.jcas.throwFeatMissing("relationString", "de.julielab.jcore.types.GeneralEventMention");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((GeneralEventMention_Type)jcasType).casFeatCode_relationString), i);
    jcasType.ll_cas.ll_setStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((GeneralEventMention_Type)jcasType).casFeatCode_relationString), i, v);}
  }

    