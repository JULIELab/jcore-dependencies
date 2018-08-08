

/* First created by JCasGen Wed Aug 08 13:36:49 CEST 2018 */
package de.julielab.jcore.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** Describes a word structure, default grammatical features of a adjective
 * Updated by JCasGen Wed Aug 08 13:36:49 CEST 2018
 * XML source: /Volumes/OUTERSPACE/Coding/git/jcore-dependencies/jcore-xmi-splitter/src/test/resources/test-types/all-test-types.xml
 * @generated */
public class AdjectiveFeats extends GrammaticalFeats {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(AdjectiveFeats.class);
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
  protected AdjectiveFeats() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public AdjectiveFeats(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public AdjectiveFeats(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public AdjectiveFeats(JCas jcas, int begin, int end) {
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
  //* Feature: degree

  /** getter for degree - gets Degree
   * @generated
   * @return value of the feature 
   */
  public String getDegree() {
    if (AdjectiveFeats_Type.featOkTst && ((AdjectiveFeats_Type)jcasType).casFeat_degree == null)
      jcasType.jcas.throwFeatMissing("degree", "de.julielab.jcore.types.AdjectiveFeats");
    return jcasType.ll_cas.ll_getStringValue(addr, ((AdjectiveFeats_Type)jcasType).casFeatCode_degree);}
    
  /** setter for degree - sets Degree 
   * @generated
   * @param v value to set into the feature 
   */
  public void setDegree(String v) {
    if (AdjectiveFeats_Type.featOkTst && ((AdjectiveFeats_Type)jcasType).casFeat_degree == null)
      jcasType.jcas.throwFeatMissing("degree", "de.julielab.jcore.types.AdjectiveFeats");
    jcasType.ll_cas.ll_setStringValue(addr, ((AdjectiveFeats_Type)jcasType).casFeatCode_degree, v);}    
  }

    