

/* First created by JCasGen Tue Sep 03 12:35:27 CEST 2019 */
package de.julielab.jcore.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** This annotation marks the span of a sentence.
 * Updated by JCasGen Tue Sep 03 12:35:27 CEST 2019
 * XML source: arrayAndListHolderTestType.xml
 * @generated */
public class Sentence extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Sentence.class);
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
  protected Sentence() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public Sentence(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Sentence(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public Sentence(JCas jcas, int begin, int end) {
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
  //* Feature: segment

  /** getter for segment - gets 
   * @generated
   * @return value of the feature 
   */
  public Segment getSegment() {
    if (Sentence_Type.featOkTst && ((Sentence_Type)jcasType).casFeat_segment == null)
      jcasType.jcas.throwFeatMissing("segment", "de.julielab.jcore.types.Sentence");
    return (Segment)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Sentence_Type)jcasType).casFeatCode_segment)));}
    
  /** setter for segment - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setSegment(Segment v) {
    if (Sentence_Type.featOkTst && ((Sentence_Type)jcasType).casFeat_segment == null)
      jcasType.jcas.throwFeatMissing("segment", "de.julielab.jcore.types.Sentence");
    jcasType.ll_cas.ll_setRefValue(addr, ((Sentence_Type)jcasType).casFeatCode_segment, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    