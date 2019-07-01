

/* First created by JCasGen Mon Jul 01 14:16:43 CEST 2019 */
package de.julielab.jcore.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.DoubleArray;


/** 
 * Updated by JCasGen Mon Jul 01 14:16:43 CEST 2019
 * XML source: arrayAndListHolderTestType.xml
 * @generated */
public class EmbeddingVector extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(EmbeddingVector.class);
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
  protected EmbeddingVector() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public EmbeddingVector(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public EmbeddingVector(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public EmbeddingVector(JCas jcas, int begin, int end) {
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
  //* Feature: source

  /** getter for source - gets The source of the embedding, i.e. some identifier of the method that produces the embedding.
   * @generated
   * @return value of the feature 
   */
  public String getSource() {
    if (EmbeddingVector_Type.featOkTst && ((EmbeddingVector_Type)jcasType).casFeat_source == null)
      jcasType.jcas.throwFeatMissing("source", "de.julielab.jcore.types.EmbeddingVector");
    return jcasType.ll_cas.ll_getStringValue(addr, ((EmbeddingVector_Type)jcasType).casFeatCode_source);}
    
  /** setter for source - sets The source of the embedding, i.e. some identifier of the method that produces the embedding. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setSource(String v) {
    if (EmbeddingVector_Type.featOkTst && ((EmbeddingVector_Type)jcasType).casFeat_source == null)
      jcasType.jcas.throwFeatMissing("source", "de.julielab.jcore.types.EmbeddingVector");
    jcasType.ll_cas.ll_setStringValue(addr, ((EmbeddingVector_Type)jcasType).casFeatCode_source, v);}    
   
    
  //*--------------*
  //* Feature: vector

  /** getter for vector - gets The actual embedding vector.
   * @generated
   * @return value of the feature 
   */
  public DoubleArray getVector() {
    if (EmbeddingVector_Type.featOkTst && ((EmbeddingVector_Type)jcasType).casFeat_vector == null)
      jcasType.jcas.throwFeatMissing("vector", "de.julielab.jcore.types.EmbeddingVector");
    return (DoubleArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((EmbeddingVector_Type)jcasType).casFeatCode_vector)));}
    
  /** setter for vector - sets The actual embedding vector. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setVector(DoubleArray v) {
    if (EmbeddingVector_Type.featOkTst && ((EmbeddingVector_Type)jcasType).casFeat_vector == null)
      jcasType.jcas.throwFeatMissing("vector", "de.julielab.jcore.types.EmbeddingVector");
    jcasType.ll_cas.ll_setRefValue(addr, ((EmbeddingVector_Type)jcasType).casFeatCode_vector, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for vector - gets an indexed value - The actual embedding vector.
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public double getVector(int i) {
    if (EmbeddingVector_Type.featOkTst && ((EmbeddingVector_Type)jcasType).casFeat_vector == null)
      jcasType.jcas.throwFeatMissing("vector", "de.julielab.jcore.types.EmbeddingVector");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((EmbeddingVector_Type)jcasType).casFeatCode_vector), i);
    return jcasType.ll_cas.ll_getDoubleArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((EmbeddingVector_Type)jcasType).casFeatCode_vector), i);}

  /** indexed setter for vector - sets an indexed value - The actual embedding vector.
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setVector(int i, double v) { 
    if (EmbeddingVector_Type.featOkTst && ((EmbeddingVector_Type)jcasType).casFeat_vector == null)
      jcasType.jcas.throwFeatMissing("vector", "de.julielab.jcore.types.EmbeddingVector");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((EmbeddingVector_Type)jcasType).casFeatCode_vector), i);
    jcasType.ll_cas.ll_setDoubleArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((EmbeddingVector_Type)jcasType).casFeatCode_vector), i, v);}
  }

    