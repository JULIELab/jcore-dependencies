

/* First created by JCasGen Wed Aug 08 13:36:49 CEST 2018 */
package de.julielab.jcore.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.StringArray;


/** 
 * Updated by JCasGen Wed Aug 08 13:36:49 CEST 2018
 * XML source: /Volumes/OUTERSPACE/Coding/git/jcore-dependencies/jcore-xmi-splitter/src/test/resources/test-types/all-test-types.xml
 * @generated */
public class GOMention extends ConceptMention {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(GOMention.class);
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
  protected GOMention() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public GOMention(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public GOMention(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public GOMention(JCas jcas, int begin, int end) {
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
  //* Feature: categories

  /** getter for categories - gets created for the shared task, here we add the group
   * @generated
   * @return value of the feature 
   */
  public StringArray getCategories() {
    if (GOMention_Type.featOkTst && ((GOMention_Type)jcasType).casFeat_categories == null)
      jcasType.jcas.throwFeatMissing("categories", "de.julielab.jcore.types.GOMention");
    return (StringArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((GOMention_Type)jcasType).casFeatCode_categories)));}
    
  /** setter for categories - sets created for the shared task, here we add the group 
   * @generated
   * @param v value to set into the feature 
   */
  public void setCategories(StringArray v) {
    if (GOMention_Type.featOkTst && ((GOMention_Type)jcasType).casFeat_categories == null)
      jcasType.jcas.throwFeatMissing("categories", "de.julielab.jcore.types.GOMention");
    jcasType.ll_cas.ll_setRefValue(addr, ((GOMention_Type)jcasType).casFeatCode_categories, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for categories - gets an indexed value - created for the shared task, here we add the group
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public String getCategories(int i) {
    if (GOMention_Type.featOkTst && ((GOMention_Type)jcasType).casFeat_categories == null)
      jcasType.jcas.throwFeatMissing("categories", "de.julielab.jcore.types.GOMention");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((GOMention_Type)jcasType).casFeatCode_categories), i);
    return jcasType.ll_cas.ll_getStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((GOMention_Type)jcasType).casFeatCode_categories), i);}

  /** indexed setter for categories - sets an indexed value - created for the shared task, here we add the group
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setCategories(int i, String v) { 
    if (GOMention_Type.featOkTst && ((GOMention_Type)jcasType).casFeat_categories == null)
      jcasType.jcas.throwFeatMissing("categories", "de.julielab.jcore.types.GOMention");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((GOMention_Type)jcasType).casFeatCode_categories), i);
    jcasType.ll_cas.ll_setStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((GOMention_Type)jcasType).casFeatCode_categories), i, v);}
   
    
  //*--------------*
  //* Feature: goID

  /** getter for goID - gets 
   * @generated
   * @return value of the feature 
   */
  public String getGoID() {
    if (GOMention_Type.featOkTst && ((GOMention_Type)jcasType).casFeat_goID == null)
      jcasType.jcas.throwFeatMissing("goID", "de.julielab.jcore.types.GOMention");
    return jcasType.ll_cas.ll_getStringValue(addr, ((GOMention_Type)jcasType).casFeatCode_goID);}
    
  /** setter for goID - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setGoID(String v) {
    if (GOMention_Type.featOkTst && ((GOMention_Type)jcasType).casFeat_goID == null)
      jcasType.jcas.throwFeatMissing("goID", "de.julielab.jcore.types.GOMention");
    jcasType.ll_cas.ll_setStringValue(addr, ((GOMention_Type)jcasType).casFeatCode_goID, v);}    
  }

    