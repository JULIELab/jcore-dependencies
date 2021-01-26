

/* First created by JCasGen Thu Jun 29 12:11:09 CEST 2017 */
package de.julielab.jcore.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;


/** This type stores required information for the appropiate handling of consistent XMI deserialization in the XMI reader and serialization in the XMI consumer with respect to the separate storage of annotations.
 * Updated by JCasGen Fri Jun 30 13:54:20 CEST 2017
 * XML source: /Volumes/OUTERSPACE/Coding/workspace-semedico-nightly/jules-xmi-splitter/src/main/resources/de/julielab/jcore/types/jcore-xmi-splitter-types.xml
 * @generated */
public class XmiMetaData extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(XmiMetaData.class);
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
  protected XmiMetaData() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public XmiMetaData(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public XmiMetaData(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public XmiMetaData(JCas jcas, int begin, int end) {
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
  //* Feature: maxXmiId

  /** getter for maxXmiId - gets 
   * @generated
   * @return value of the feature 
   */
  public int getMaxXmiId() {
    if (XmiMetaData_Type.featOkTst && ((XmiMetaData_Type)jcasType).casFeat_maxXmiId == null)
      jcasType.jcas.throwFeatMissing("maxXmiId", "de.julielab.jcore.types.XmiMetaData");
    return jcasType.ll_cas.ll_getIntValue(addr, ((XmiMetaData_Type)jcasType).casFeatCode_maxXmiId);}
    
  /** setter for maxXmiId - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setMaxXmiId(int v) {
    if (XmiMetaData_Type.featOkTst && ((XmiMetaData_Type)jcasType).casFeat_maxXmiId == null)
      jcasType.jcas.throwFeatMissing("maxXmiId", "de.julielab.jcore.types.XmiMetaData");
    jcasType.ll_cas.ll_setIntValue(addr, ((XmiMetaData_Type)jcasType).casFeatCode_maxXmiId, v);}    
   
    
  //*--------------*
  //* Feature: sofaIdMappings

  /** getter for sofaIdMappings - gets Mappings from sofa xmi:id to the sofa name, i.e. the sofaID attribute in XMI format. A mapping has the form xmiId=sofaID.
   * @generated
   * @return value of the feature 
   */
  public StringArray getSofaIdMappings() {
    if (XmiMetaData_Type.featOkTst && ((XmiMetaData_Type)jcasType).casFeat_sofaIdMappings == null)
      jcasType.jcas.throwFeatMissing("sofaIdMappings", "de.julielab.jcore.types.XmiMetaData");
    return (StringArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((XmiMetaData_Type)jcasType).casFeatCode_sofaIdMappings)));}
    
  /** setter for sofaIdMappings - sets Mappings from sofa xmi:id to the sofa name, i.e. the sofaID attribute in XMI format. A mapping has the form xmiId=sofaID. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setSofaIdMappings(StringArray v) {
    if (XmiMetaData_Type.featOkTst && ((XmiMetaData_Type)jcasType).casFeat_sofaIdMappings == null)
      jcasType.jcas.throwFeatMissing("sofaIdMappings", "de.julielab.jcore.types.XmiMetaData");
    jcasType.ll_cas.ll_setRefValue(addr, ((XmiMetaData_Type)jcasType).casFeatCode_sofaIdMappings, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for sofaIdMappings - gets an indexed value - Mappings from sofa xmi:id to the sofa name, i.e. the sofaID attribute in XMI format. A mapping has the form xmiId=sofaID.
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public String getSofaIdMappings(int i) {
    if (XmiMetaData_Type.featOkTst && ((XmiMetaData_Type)jcasType).casFeat_sofaIdMappings == null)
      jcasType.jcas.throwFeatMissing("sofaIdMappings", "de.julielab.jcore.types.XmiMetaData");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((XmiMetaData_Type)jcasType).casFeatCode_sofaIdMappings), i);
    return jcasType.ll_cas.ll_getStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((XmiMetaData_Type)jcasType).casFeatCode_sofaIdMappings), i);}

  /** indexed setter for sofaIdMappings - sets an indexed value - Mappings from sofa xmi:id to the sofa name, i.e. the sofaID attribute in XMI format. A mapping has the form xmiId=sofaID.
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setSofaIdMappings(int i, String v) { 
    if (XmiMetaData_Type.featOkTst && ((XmiMetaData_Type)jcasType).casFeat_sofaIdMappings == null)
      jcasType.jcas.throwFeatMissing("sofaIdMappings", "de.julielab.jcore.types.XmiMetaData");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((XmiMetaData_Type)jcasType).casFeatCode_sofaIdMappings), i);
    jcasType.ll_cas.ll_setStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((XmiMetaData_Type)jcasType).casFeatCode_sofaIdMappings), i, v);}
  }

    