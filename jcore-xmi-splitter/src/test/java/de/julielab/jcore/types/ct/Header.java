

/* First created by JCasGen Tue Sep 03 12:34:17 CEST 2019 */
package de.julielab.jcore.types.ct;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.cas.TOP_Type;


/** The special Header for PubMed (http://www.pubmed.org)
                documents
 * Updated by JCasGen Tue Sep 03 12:34:17 CEST 2019
 * XML source: all-test-types.xml
 * @generated */
public class Header extends de.julielab.jcore.types.pubmed.Header {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Header.class);
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
  protected Header() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public Header(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Header(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public Header(JCas jcas, int begin, int end) {
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
  //* Feature: studyType

  /** getter for studyType - gets 
   * @generated
   * @return value of the feature 
   */
  public String getStudyType() {
    if (Header_Type.featOkTst && ((Header_Type)jcasType).casFeat_studyType == null)
      jcasType.jcas.throwFeatMissing("studyType", "de.julielab.jcore.types.ct.Header");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Header_Type)jcasType).casFeatCode_studyType);}
    
  /** setter for studyType - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setStudyType(String v) {
    if (Header_Type.featOkTst && ((Header_Type)jcasType).casFeat_studyType == null)
      jcasType.jcas.throwFeatMissing("studyType", "de.julielab.jcore.types.ct.Header");
    jcasType.ll_cas.ll_setStringValue(addr, ((Header_Type)jcasType).casFeatCode_studyType, v);}    
   
    
  //*--------------*
  //* Feature: studyDesignInfo

  /** getter for studyDesignInfo - gets 
   * @generated
   * @return value of the feature 
   */
  public StudyDesignInfo getStudyDesignInfo() {
    if (Header_Type.featOkTst && ((Header_Type)jcasType).casFeat_studyDesignInfo == null)
      jcasType.jcas.throwFeatMissing("studyDesignInfo", "de.julielab.jcore.types.ct.Header");
    return (StudyDesignInfo)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Header_Type)jcasType).casFeatCode_studyDesignInfo)));}
    
  /** setter for studyDesignInfo - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setStudyDesignInfo(StudyDesignInfo v) {
    if (Header_Type.featOkTst && ((Header_Type)jcasType).casFeat_studyDesignInfo == null)
      jcasType.jcas.throwFeatMissing("studyDesignInfo", "de.julielab.jcore.types.ct.Header");
    jcasType.ll_cas.ll_setRefValue(addr, ((Header_Type)jcasType).casFeatCode_studyDesignInfo, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: minimumAge

  /** getter for minimumAge - gets 
   * @generated
   * @return value of the feature 
   */
  public int getMinimumAge() {
    if (Header_Type.featOkTst && ((Header_Type)jcasType).casFeat_minimumAge == null)
      jcasType.jcas.throwFeatMissing("minimumAge", "de.julielab.jcore.types.ct.Header");
    return jcasType.ll_cas.ll_getIntValue(addr, ((Header_Type)jcasType).casFeatCode_minimumAge);}
    
  /** setter for minimumAge - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setMinimumAge(int v) {
    if (Header_Type.featOkTst && ((Header_Type)jcasType).casFeat_minimumAge == null)
      jcasType.jcas.throwFeatMissing("minimumAge", "de.julielab.jcore.types.ct.Header");
    jcasType.ll_cas.ll_setIntValue(addr, ((Header_Type)jcasType).casFeatCode_minimumAge, v);}    
   
    
  //*--------------*
  //* Feature: maximumAge

  /** getter for maximumAge - gets 
   * @generated
   * @return value of the feature 
   */
  public int getMaximumAge() {
    if (Header_Type.featOkTst && ((Header_Type)jcasType).casFeat_maximumAge == null)
      jcasType.jcas.throwFeatMissing("maximumAge", "de.julielab.jcore.types.ct.Header");
    return jcasType.ll_cas.ll_getIntValue(addr, ((Header_Type)jcasType).casFeatCode_maximumAge);}
    
  /** setter for maximumAge - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setMaximumAge(int v) {
    if (Header_Type.featOkTst && ((Header_Type)jcasType).casFeat_maximumAge == null)
      jcasType.jcas.throwFeatMissing("maximumAge", "de.julielab.jcore.types.ct.Header");
    jcasType.ll_cas.ll_setIntValue(addr, ((Header_Type)jcasType).casFeatCode_maximumAge, v);}    
   
    
  //*--------------*
  //* Feature: gender

  /** getter for gender - gets 
   * @generated
   * @return value of the feature 
   */
  public StringArray getGender() {
    if (Header_Type.featOkTst && ((Header_Type)jcasType).casFeat_gender == null)
      jcasType.jcas.throwFeatMissing("gender", "de.julielab.jcore.types.ct.Header");
    return (StringArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Header_Type)jcasType).casFeatCode_gender)));}
    
  /** setter for gender - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setGender(StringArray v) {
    if (Header_Type.featOkTst && ((Header_Type)jcasType).casFeat_gender == null)
      jcasType.jcas.throwFeatMissing("gender", "de.julielab.jcore.types.ct.Header");
    jcasType.ll_cas.ll_setRefValue(addr, ((Header_Type)jcasType).casFeatCode_gender, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for gender - gets an indexed value - 
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public String getGender(int i) {
    if (Header_Type.featOkTst && ((Header_Type)jcasType).casFeat_gender == null)
      jcasType.jcas.throwFeatMissing("gender", "de.julielab.jcore.types.ct.Header");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Header_Type)jcasType).casFeatCode_gender), i);
    return jcasType.ll_cas.ll_getStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Header_Type)jcasType).casFeatCode_gender), i);}

  /** indexed setter for gender - sets an indexed value - 
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setGender(int i, String v) { 
    if (Header_Type.featOkTst && ((Header_Type)jcasType).casFeat_gender == null)
      jcasType.jcas.throwFeatMissing("gender", "de.julielab.jcore.types.ct.Header");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Header_Type)jcasType).casFeatCode_gender), i);
    jcasType.ll_cas.ll_setStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Header_Type)jcasType).casFeatCode_gender), i, v);}
  }

    