

/* First created by JCasGen Sat Jul 13 15:41:00 CEST 2019 */
package de.julielab.jcore.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.cas.DoubleArray;
import org.apache.uima.jcas.cas.IntegerArray;


/** Topics label documents with vectors weights for their semantically most prominent words
 * Updated by JCasGen Sat Jul 13 15:41:00 CEST 2019
 * XML source: all-test-types.xml
 * @generated */
public class DocumentTopics extends DocumentAnnotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(DocumentTopics.class);
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
  protected DocumentTopics() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public DocumentTopics(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public DocumentTopics(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public DocumentTopics(JCas jcas, int begin, int end) {
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
  //* Feature: Weights

  /** getter for Weights - gets Vector of weights denoting the semantical descriptivity of one word for the respective topic
   * @generated
   * @return value of the feature 
   */
  public DoubleArray getWeights() {
    if (DocumentTopics_Type.featOkTst && ((DocumentTopics_Type)jcasType).casFeat_Weights == null)
      jcasType.jcas.throwFeatMissing("Weights", "de.julielab.jcore.types.DocumentTopics");
    return (DoubleArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((DocumentTopics_Type)jcasType).casFeatCode_Weights)));}
    
  /** setter for Weights - sets Vector of weights denoting the semantical descriptivity of one word for the respective topic 
   * @generated
   * @param v value to set into the feature 
   */
  public void setWeights(DoubleArray v) {
    if (DocumentTopics_Type.featOkTst && ((DocumentTopics_Type)jcasType).casFeat_Weights == null)
      jcasType.jcas.throwFeatMissing("Weights", "de.julielab.jcore.types.DocumentTopics");
    jcasType.ll_cas.ll_setRefValue(addr, ((DocumentTopics_Type)jcasType).casFeatCode_Weights, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for Weights - gets an indexed value - Vector of weights denoting the semantical descriptivity of one word for the respective topic
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public double getWeights(int i) {
    if (DocumentTopics_Type.featOkTst && ((DocumentTopics_Type)jcasType).casFeat_Weights == null)
      jcasType.jcas.throwFeatMissing("Weights", "de.julielab.jcore.types.DocumentTopics");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((DocumentTopics_Type)jcasType).casFeatCode_Weights), i);
    return jcasType.ll_cas.ll_getDoubleArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((DocumentTopics_Type)jcasType).casFeatCode_Weights), i);}

  /** indexed setter for Weights - sets an indexed value - Vector of weights denoting the semantical descriptivity of one word for the respective topic
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setWeights(int i, double v) { 
    if (DocumentTopics_Type.featOkTst && ((DocumentTopics_Type)jcasType).casFeat_Weights == null)
      jcasType.jcas.throwFeatMissing("Weights", "de.julielab.jcore.types.DocumentTopics");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((DocumentTopics_Type)jcasType).casFeatCode_Weights), i);
    jcasType.ll_cas.ll_setDoubleArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((DocumentTopics_Type)jcasType).casFeatCode_Weights), i, v);}
   
    
  //*--------------*
  //* Feature: IDs

  /** getter for IDs - gets IDs for topics determined by the modeling implementation
   * @generated
   * @return value of the feature 
   */
  public IntegerArray getIDs() {
    if (DocumentTopics_Type.featOkTst && ((DocumentTopics_Type)jcasType).casFeat_IDs == null)
      jcasType.jcas.throwFeatMissing("IDs", "de.julielab.jcore.types.DocumentTopics");
    return (IntegerArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((DocumentTopics_Type)jcasType).casFeatCode_IDs)));}
    
  /** setter for IDs - sets IDs for topics determined by the modeling implementation 
   * @generated
   * @param v value to set into the feature 
   */
  public void setIDs(IntegerArray v) {
    if (DocumentTopics_Type.featOkTst && ((DocumentTopics_Type)jcasType).casFeat_IDs == null)
      jcasType.jcas.throwFeatMissing("IDs", "de.julielab.jcore.types.DocumentTopics");
    jcasType.ll_cas.ll_setRefValue(addr, ((DocumentTopics_Type)jcasType).casFeatCode_IDs, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for IDs - gets an indexed value - IDs for topics determined by the modeling implementation
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public int getIDs(int i) {
    if (DocumentTopics_Type.featOkTst && ((DocumentTopics_Type)jcasType).casFeat_IDs == null)
      jcasType.jcas.throwFeatMissing("IDs", "de.julielab.jcore.types.DocumentTopics");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((DocumentTopics_Type)jcasType).casFeatCode_IDs), i);
    return jcasType.ll_cas.ll_getIntArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((DocumentTopics_Type)jcasType).casFeatCode_IDs), i);}

  /** indexed setter for IDs - sets an indexed value - IDs for topics determined by the modeling implementation
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setIDs(int i, int v) { 
    if (DocumentTopics_Type.featOkTst && ((DocumentTopics_Type)jcasType).casFeat_IDs == null)
      jcasType.jcas.throwFeatMissing("IDs", "de.julielab.jcore.types.DocumentTopics");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((DocumentTopics_Type)jcasType).casFeatCode_IDs), i);
    jcasType.ll_cas.ll_setIntArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((DocumentTopics_Type)jcasType).casFeatCode_IDs), i, v);}
   
    
  //*--------------*
  //* Feature: ModelID

  /** getter for ModelID - gets ID identifying the model holding the topics to be labeled
   * @generated
   * @return value of the feature 
   */
  public String getModelID() {
    if (DocumentTopics_Type.featOkTst && ((DocumentTopics_Type)jcasType).casFeat_ModelID == null)
      jcasType.jcas.throwFeatMissing("ModelID", "de.julielab.jcore.types.DocumentTopics");
    return jcasType.ll_cas.ll_getStringValue(addr, ((DocumentTopics_Type)jcasType).casFeatCode_ModelID);}
    
  /** setter for ModelID - sets ID identifying the model holding the topics to be labeled 
   * @generated
   * @param v value to set into the feature 
   */
  public void setModelID(String v) {
    if (DocumentTopics_Type.featOkTst && ((DocumentTopics_Type)jcasType).casFeat_ModelID == null)
      jcasType.jcas.throwFeatMissing("ModelID", "de.julielab.jcore.types.DocumentTopics");
    jcasType.ll_cas.ll_setStringValue(addr, ((DocumentTopics_Type)jcasType).casFeatCode_ModelID, v);}    
   
    
  //*--------------*
  //* Feature: TopicWords

  /** getter for TopicWords - gets The top words for the respective topic
   * @generated
   * @return value of the feature 
   */
  public StringArray getTopicWords() {
    if (DocumentTopics_Type.featOkTst && ((DocumentTopics_Type)jcasType).casFeat_TopicWords == null)
      jcasType.jcas.throwFeatMissing("TopicWords", "de.julielab.jcore.types.DocumentTopics");
    return (StringArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((DocumentTopics_Type)jcasType).casFeatCode_TopicWords)));}
    
  /** setter for TopicWords - sets The top words for the respective topic 
   * @generated
   * @param v value to set into the feature 
   */
  public void setTopicWords(StringArray v) {
    if (DocumentTopics_Type.featOkTst && ((DocumentTopics_Type)jcasType).casFeat_TopicWords == null)
      jcasType.jcas.throwFeatMissing("TopicWords", "de.julielab.jcore.types.DocumentTopics");
    jcasType.ll_cas.ll_setRefValue(addr, ((DocumentTopics_Type)jcasType).casFeatCode_TopicWords, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for TopicWords - gets an indexed value - The top words for the respective topic
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public String getTopicWords(int i) {
    if (DocumentTopics_Type.featOkTst && ((DocumentTopics_Type)jcasType).casFeat_TopicWords == null)
      jcasType.jcas.throwFeatMissing("TopicWords", "de.julielab.jcore.types.DocumentTopics");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((DocumentTopics_Type)jcasType).casFeatCode_TopicWords), i);
    return jcasType.ll_cas.ll_getStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((DocumentTopics_Type)jcasType).casFeatCode_TopicWords), i);}

  /** indexed setter for TopicWords - sets an indexed value - The top words for the respective topic
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setTopicWords(int i, String v) { 
    if (DocumentTopics_Type.featOkTst && ((DocumentTopics_Type)jcasType).casFeat_TopicWords == null)
      jcasType.jcas.throwFeatMissing("TopicWords", "de.julielab.jcore.types.DocumentTopics");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((DocumentTopics_Type)jcasType).casFeatCode_TopicWords), i);
    jcasType.ll_cas.ll_setStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((DocumentTopics_Type)jcasType).casFeatCode_TopicWords), i, v);}
   
    
  //*--------------*
  //* Feature: ModelVersion

  /** getter for ModelVersion - gets Version of the model holding the topics
   * @generated
   * @return value of the feature 
   */
  public String getModelVersion() {
    if (DocumentTopics_Type.featOkTst && ((DocumentTopics_Type)jcasType).casFeat_ModelVersion == null)
      jcasType.jcas.throwFeatMissing("ModelVersion", "de.julielab.jcore.types.DocumentTopics");
    return jcasType.ll_cas.ll_getStringValue(addr, ((DocumentTopics_Type)jcasType).casFeatCode_ModelVersion);}
    
  /** setter for ModelVersion - sets Version of the model holding the topics 
   * @generated
   * @param v value to set into the feature 
   */
  public void setModelVersion(String v) {
    if (DocumentTopics_Type.featOkTst && ((DocumentTopics_Type)jcasType).casFeat_ModelVersion == null)
      jcasType.jcas.throwFeatMissing("ModelVersion", "de.julielab.jcore.types.DocumentTopics");
    jcasType.ll_cas.ll_setStringValue(addr, ((DocumentTopics_Type)jcasType).casFeatCode_ModelVersion, v);}    
  }

    