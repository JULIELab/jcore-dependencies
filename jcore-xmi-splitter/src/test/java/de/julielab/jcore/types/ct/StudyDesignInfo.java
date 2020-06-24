

/* First created by JCasGen Tue Sep 03 12:34:17 CEST 2019 */
package de.julielab.jcore.types.ct;

import de.julielab.jcore.types.DocumentAnnotation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;


/** 
 * Updated by JCasGen Tue Sep 03 12:34:17 CEST 2019
 * XML source: all-test-types.xml
 * @generated */
public class StudyDesignInfo extends DocumentAnnotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(StudyDesignInfo.class);
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
  protected StudyDesignInfo() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public StudyDesignInfo(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public StudyDesignInfo(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public StudyDesignInfo(JCas jcas, int begin, int end) {
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
  //* Feature: interventionModel

  /** getter for interventionModel - gets 
   * @generated
   * @return value of the feature 
   */
  public String getInterventionModel() {
    if (StudyDesignInfo_Type.featOkTst && ((StudyDesignInfo_Type)jcasType).casFeat_interventionModel == null)
      jcasType.jcas.throwFeatMissing("interventionModel", "de.julielab.jcore.types.ct.StudyDesignInfo");
    return jcasType.ll_cas.ll_getStringValue(addr, ((StudyDesignInfo_Type)jcasType).casFeatCode_interventionModel);}
    
  /** setter for interventionModel - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setInterventionModel(String v) {
    if (StudyDesignInfo_Type.featOkTst && ((StudyDesignInfo_Type)jcasType).casFeat_interventionModel == null)
      jcasType.jcas.throwFeatMissing("interventionModel", "de.julielab.jcore.types.ct.StudyDesignInfo");
    jcasType.ll_cas.ll_setStringValue(addr, ((StudyDesignInfo_Type)jcasType).casFeatCode_interventionModel, v);}    
   
    
  //*--------------*
  //* Feature: primaryPurpose

  /** getter for primaryPurpose - gets 
   * @generated
   * @return value of the feature 
   */
  public String getPrimaryPurpose() {
    if (StudyDesignInfo_Type.featOkTst && ((StudyDesignInfo_Type)jcasType).casFeat_primaryPurpose == null)
      jcasType.jcas.throwFeatMissing("primaryPurpose", "de.julielab.jcore.types.ct.StudyDesignInfo");
    return jcasType.ll_cas.ll_getStringValue(addr, ((StudyDesignInfo_Type)jcasType).casFeatCode_primaryPurpose);}
    
  /** setter for primaryPurpose - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setPrimaryPurpose(String v) {
    if (StudyDesignInfo_Type.featOkTst && ((StudyDesignInfo_Type)jcasType).casFeat_primaryPurpose == null)
      jcasType.jcas.throwFeatMissing("primaryPurpose", "de.julielab.jcore.types.ct.StudyDesignInfo");
    jcasType.ll_cas.ll_setStringValue(addr, ((StudyDesignInfo_Type)jcasType).casFeatCode_primaryPurpose, v);}    
   
    
  //*--------------*
  //* Feature: masking

  /** getter for masking - gets 
   * @generated
   * @return value of the feature 
   */
  public String getMasking() {
    if (StudyDesignInfo_Type.featOkTst && ((StudyDesignInfo_Type)jcasType).casFeat_masking == null)
      jcasType.jcas.throwFeatMissing("masking", "de.julielab.jcore.types.ct.StudyDesignInfo");
    return jcasType.ll_cas.ll_getStringValue(addr, ((StudyDesignInfo_Type)jcasType).casFeatCode_masking);}
    
  /** setter for masking - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setMasking(String v) {
    if (StudyDesignInfo_Type.featOkTst && ((StudyDesignInfo_Type)jcasType).casFeat_masking == null)
      jcasType.jcas.throwFeatMissing("masking", "de.julielab.jcore.types.ct.StudyDesignInfo");
    jcasType.ll_cas.ll_setStringValue(addr, ((StudyDesignInfo_Type)jcasType).casFeatCode_masking, v);}    
  }

    