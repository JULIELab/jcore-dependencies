

/* First created by JCasGen Thu Mar 22 10:56:02 CET 2018 */
package de.julielab.jcore.types.test;

import de.julielab.jcore.types.Annotation;
import de.julielab.jcore.types.POSTag;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.TOP_Type;


/** 
 * Updated by JCasGen Thu Mar 22 10:56:02 CET 2018
 * XML source: /Users/faessler/Coding/workspace-semedico-nightly/delete/src/typeSystemDescriptor.xml
 * @generated */
public class OtherToken extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(OtherToken.class);
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
  protected OtherToken() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public OtherToken(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public OtherToken(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public OtherToken(JCas jcas, int begin, int end) {
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
  //* Feature: posTag

  /** getter for posTag - gets 
   * @generated
   * @return value of the feature 
   */
  public FSArray getPosTag() {
    if (OtherToken_Type.featOkTst && ((OtherToken_Type)jcasType).casFeat_posTag == null)
      jcasType.jcas.throwFeatMissing("posTag", "de.julielab.jcore.types.OtherToken");
    return (FSArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((OtherToken_Type)jcasType).casFeatCode_posTag)));}
    
  /** setter for posTag - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setPosTag(FSArray v) {
    if (OtherToken_Type.featOkTst && ((OtherToken_Type)jcasType).casFeat_posTag == null)
      jcasType.jcas.throwFeatMissing("posTag", "de.julielab.jcore.types.OtherToken");
    jcasType.ll_cas.ll_setRefValue(addr, ((OtherToken_Type)jcasType).casFeatCode_posTag, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for posTag - gets an indexed value - 
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public POSTag getPosTag(int i) {
    if (OtherToken_Type.featOkTst && ((OtherToken_Type)jcasType).casFeat_posTag == null)
      jcasType.jcas.throwFeatMissing("posTag", "de.julielab.jcore.types.OtherToken");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((OtherToken_Type)jcasType).casFeatCode_posTag), i);
    return (POSTag)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((OtherToken_Type)jcasType).casFeatCode_posTag), i)));}

  /** indexed setter for posTag - sets an indexed value - 
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setPosTag(int i, POSTag v) { 
    if (OtherToken_Type.featOkTst && ((OtherToken_Type)jcasType).casFeat_posTag == null)
      jcasType.jcas.throwFeatMissing("posTag", "de.julielab.jcore.types.OtherToken");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((OtherToken_Type)jcasType).casFeatCode_posTag), i);
    jcasType.ll_cas.ll_setRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((OtherToken_Type)jcasType).casFeatCode_posTag), i, jcasType.ll_cas.ll_getFSRef(v));}
  }

    