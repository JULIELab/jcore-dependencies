

/* First created by JCasGen Sat Jun 22 14:44:29 CEST 2019 */
package de.julielab.jcore.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** Describes a word structure, default grammatical features of  pronouns
 * Updated by JCasGen Sat Jun 22 14:44:29 CEST 2019
 * XML source: /Users/faessler/Coding/git/jcore-dependencies/jcore-xmi-splitter/src/test/resources/de/julielab/jcore/types/all-test-types.xml
 * @generated */
public class PronounFeats extends GrammaticalFeats {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(PronounFeats.class);
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
  protected PronounFeats() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public PronounFeats(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public PronounFeats(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public PronounFeats(JCas jcas, int begin, int end) {
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
  //* Feature: gender

  /** getter for gender - gets Gender
   * @generated
   * @return value of the feature 
   */
  public String getGender() {
    if (PronounFeats_Type.featOkTst && ((PronounFeats_Type)jcasType).casFeat_gender == null)
      jcasType.jcas.throwFeatMissing("gender", "de.julielab.jcore.types.PronounFeats");
    return jcasType.ll_cas.ll_getStringValue(addr, ((PronounFeats_Type)jcasType).casFeatCode_gender);}
    
  /** setter for gender - sets Gender 
   * @generated
   * @param v value to set into the feature 
   */
  public void setGender(String v) {
    if (PronounFeats_Type.featOkTst && ((PronounFeats_Type)jcasType).casFeat_gender == null)
      jcasType.jcas.throwFeatMissing("gender", "de.julielab.jcore.types.PronounFeats");
    jcasType.ll_cas.ll_setStringValue(addr, ((PronounFeats_Type)jcasType).casFeatCode_gender, v);}    
   
    
  //*--------------*
  //* Feature: case

  /** getter for case - gets Case
   * @generated
   * @return value of the feature 
   */
  public String getCase() {
    if (PronounFeats_Type.featOkTst && ((PronounFeats_Type)jcasType).casFeat_case == null)
      jcasType.jcas.throwFeatMissing("case", "de.julielab.jcore.types.PronounFeats");
    return jcasType.ll_cas.ll_getStringValue(addr, ((PronounFeats_Type)jcasType).casFeatCode_case);}
    
  /** setter for case - sets Case 
   * @generated
   * @param v value to set into the feature 
   */
  public void setCase(String v) {
    if (PronounFeats_Type.featOkTst && ((PronounFeats_Type)jcasType).casFeat_case == null)
      jcasType.jcas.throwFeatMissing("case", "de.julielab.jcore.types.PronounFeats");
    jcasType.ll_cas.ll_setStringValue(addr, ((PronounFeats_Type)jcasType).casFeatCode_case, v);}    
   
    
  //*--------------*
  //* Feature: number

  /** getter for number - gets Number
   * @generated
   * @return value of the feature 
   */
  public String getNumber() {
    if (PronounFeats_Type.featOkTst && ((PronounFeats_Type)jcasType).casFeat_number == null)
      jcasType.jcas.throwFeatMissing("number", "de.julielab.jcore.types.PronounFeats");
    return jcasType.ll_cas.ll_getStringValue(addr, ((PronounFeats_Type)jcasType).casFeatCode_number);}
    
  /** setter for number - sets Number 
   * @generated
   * @param v value to set into the feature 
   */
  public void setNumber(String v) {
    if (PronounFeats_Type.featOkTst && ((PronounFeats_Type)jcasType).casFeat_number == null)
      jcasType.jcas.throwFeatMissing("number", "de.julielab.jcore.types.PronounFeats");
    jcasType.ll_cas.ll_setStringValue(addr, ((PronounFeats_Type)jcasType).casFeatCode_number, v);}    
   
    
  //*--------------*
  //* Feature: person

  /** getter for person - gets Person
   * @generated
   * @return value of the feature 
   */
  public String getPerson() {
    if (PronounFeats_Type.featOkTst && ((PronounFeats_Type)jcasType).casFeat_person == null)
      jcasType.jcas.throwFeatMissing("person", "de.julielab.jcore.types.PronounFeats");
    return jcasType.ll_cas.ll_getStringValue(addr, ((PronounFeats_Type)jcasType).casFeatCode_person);}
    
  /** setter for person - sets Person 
   * @generated
   * @param v value to set into the feature 
   */
  public void setPerson(String v) {
    if (PronounFeats_Type.featOkTst && ((PronounFeats_Type)jcasType).casFeat_person == null)
      jcasType.jcas.throwFeatMissing("person", "de.julielab.jcore.types.PronounFeats");
    jcasType.ll_cas.ll_setStringValue(addr, ((PronounFeats_Type)jcasType).casFeatCode_person, v);}    
  }

    