

/* First created by JCasGen Tue Sep 03 12:35:27 CEST 2019 */
package de.julielab.jcore.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** This encodes a complex noun phrase (CNP) which is usually an extension of a ChunkNP (see, e.g., jcore-cnp-extractor-ae). A CNP is currently defined to be the top-modes noun phrase of a constituency parse tree (excluding some special phrases such as appositions and SBARs).
 * Updated by JCasGen Tue Sep 03 12:35:27 CEST 2019
 * XML source: arrayAndListHolderTestType.xml
 * @generated */
public class ChunkComplexNP extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(ChunkComplexNP.class);
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
  protected ChunkComplexNP() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public ChunkComplexNP(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public ChunkComplexNP(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public ChunkComplexNP(JCas jcas, int begin, int end) {
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
  //* Feature: chunkID

  /** getter for chunkID - gets number of this complex NP in the given sentence
   * @generated
   * @return value of the feature 
   */
  public String getChunkID() {
    if (ChunkComplexNP_Type.featOkTst && ((ChunkComplexNP_Type)jcasType).casFeat_chunkID == null)
      jcasType.jcas.throwFeatMissing("chunkID", "de.julielab.jcore.types.ChunkComplexNP");
    return jcasType.ll_cas.ll_getStringValue(addr, ((ChunkComplexNP_Type)jcasType).casFeatCode_chunkID);}
    
  /** setter for chunkID - sets number of this complex NP in the given sentence 
   * @generated
   * @param v value to set into the feature 
   */
  public void setChunkID(String v) {
    if (ChunkComplexNP_Type.featOkTst && ((ChunkComplexNP_Type)jcasType).casFeat_chunkID == null)
      jcasType.jcas.throwFeatMissing("chunkID", "de.julielab.jcore.types.ChunkComplexNP");
    jcasType.ll_cas.ll_setStringValue(addr, ((ChunkComplexNP_Type)jcasType).casFeatCode_chunkID, v);}    
   
    
  //*--------------*
  //* Feature: sentenceID

  /** getter for sentenceID - gets the number of the sentence in which this complex NP is
   * @generated
   * @return value of the feature 
   */
  public String getSentenceID() {
    if (ChunkComplexNP_Type.featOkTst && ((ChunkComplexNP_Type)jcasType).casFeat_sentenceID == null)
      jcasType.jcas.throwFeatMissing("sentenceID", "de.julielab.jcore.types.ChunkComplexNP");
    return jcasType.ll_cas.ll_getStringValue(addr, ((ChunkComplexNP_Type)jcasType).casFeatCode_sentenceID);}
    
  /** setter for sentenceID - sets the number of the sentence in which this complex NP is 
   * @generated
   * @param v value to set into the feature 
   */
  public void setSentenceID(String v) {
    if (ChunkComplexNP_Type.featOkTst && ((ChunkComplexNP_Type)jcasType).casFeat_sentenceID == null)
      jcasType.jcas.throwFeatMissing("sentenceID", "de.julielab.jcore.types.ChunkComplexNP");
    jcasType.ll_cas.ll_setStringValue(addr, ((ChunkComplexNP_Type)jcasType).casFeatCode_sentenceID, v);}    
  }

    