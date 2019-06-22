

/* First created by JCasGen Sat Jun 22 14:44:28 CEST 2019 */
package de.julielab.jcore.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.StringArray;


/** 
 * Updated by JCasGen Sat Jun 22 14:44:28 CEST 2019
 * XML source: /Users/faessler/Coding/git/jcore-dependencies/jcore-xmi-splitter/src/test/resources/de/julielab/jcore/types/all-test-types.xml
 * @generated */
public class ArgumentMention extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(ArgumentMention.class);
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
  protected ArgumentMention() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public ArgumentMention(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public ArgumentMention(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public ArgumentMention(JCas jcas, int begin, int end) {
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
  //* Feature: ref

  /** getter for ref - gets Reference to the Annotation Object that acts as a argument here
   * @generated
   * @return value of the feature 
   */
  public Annotation getRef() {
    if (ArgumentMention_Type.featOkTst && ((ArgumentMention_Type)jcasType).casFeat_ref == null)
      jcasType.jcas.throwFeatMissing("ref", "de.julielab.jcore.types.ArgumentMention");
    return (Annotation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((ArgumentMention_Type)jcasType).casFeatCode_ref)));}
    
  /** setter for ref - sets Reference to the Annotation Object that acts as a argument here 
   * @generated
   * @param v value to set into the feature 
   */
  public void setRef(Annotation v) {
    if (ArgumentMention_Type.featOkTst && ((ArgumentMention_Type)jcasType).casFeat_ref == null)
      jcasType.jcas.throwFeatMissing("ref", "de.julielab.jcore.types.ArgumentMention");
    jcasType.ll_cas.ll_setRefValue(addr, ((ArgumentMention_Type)jcasType).casFeatCode_ref, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: role

  /** getter for role - gets the role of the argument
   * @generated
   * @return value of the feature 
   */
  public String getRole() {
    if (ArgumentMention_Type.featOkTst && ((ArgumentMention_Type)jcasType).casFeat_role == null)
      jcasType.jcas.throwFeatMissing("role", "de.julielab.jcore.types.ArgumentMention");
    return jcasType.ll_cas.ll_getStringValue(addr, ((ArgumentMention_Type)jcasType).casFeatCode_role);}
    
  /** setter for role - sets the role of the argument 
   * @generated
   * @param v value to set into the feature 
   */
  public void setRole(String v) {
    if (ArgumentMention_Type.featOkTst && ((ArgumentMention_Type)jcasType).casFeat_role == null)
      jcasType.jcas.throwFeatMissing("role", "de.julielab.jcore.types.ArgumentMention");
    jcasType.ll_cas.ll_setStringValue(addr, ((ArgumentMention_Type)jcasType).casFeatCode_role, v);}    
   
    
  //*--------------*
  //* Feature: relationString

  /** getter for relationString - gets used to make relations visible for LuCas
   * @generated
   * @return value of the feature 
   */
  public StringArray getRelationString() {
    if (ArgumentMention_Type.featOkTst && ((ArgumentMention_Type)jcasType).casFeat_relationString == null)
      jcasType.jcas.throwFeatMissing("relationString", "de.julielab.jcore.types.ArgumentMention");
    return (StringArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((ArgumentMention_Type)jcasType).casFeatCode_relationString)));}
    
  /** setter for relationString - sets used to make relations visible for LuCas 
   * @generated
   * @param v value to set into the feature 
   */
  public void setRelationString(StringArray v) {
    if (ArgumentMention_Type.featOkTst && ((ArgumentMention_Type)jcasType).casFeat_relationString == null)
      jcasType.jcas.throwFeatMissing("relationString", "de.julielab.jcore.types.ArgumentMention");
    jcasType.ll_cas.ll_setRefValue(addr, ((ArgumentMention_Type)jcasType).casFeatCode_relationString, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for relationString - gets an indexed value - used to make relations visible for LuCas
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public String getRelationString(int i) {
    if (ArgumentMention_Type.featOkTst && ((ArgumentMention_Type)jcasType).casFeat_relationString == null)
      jcasType.jcas.throwFeatMissing("relationString", "de.julielab.jcore.types.ArgumentMention");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((ArgumentMention_Type)jcasType).casFeatCode_relationString), i);
    return jcasType.ll_cas.ll_getStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((ArgumentMention_Type)jcasType).casFeatCode_relationString), i);}

  /** indexed setter for relationString - sets an indexed value - used to make relations visible for LuCas
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setRelationString(int i, String v) { 
    if (ArgumentMention_Type.featOkTst && ((ArgumentMention_Type)jcasType).casFeat_relationString == null)
      jcasType.jcas.throwFeatMissing("relationString", "de.julielab.jcore.types.ArgumentMention");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((ArgumentMention_Type)jcasType).casFeatCode_relationString), i);
    jcasType.ll_cas.ll_setStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((ArgumentMention_Type)jcasType).casFeatCode_relationString), i, v);}
  }

    