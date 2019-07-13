

/* First created by JCasGen Sat Jul 13 15:41:00 CEST 2019 */
package de.julielab.jcore.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** This type represents the span that some cue word like negation or hedging applies to.
 * Updated by JCasGen Sat Jul 13 15:41:00 CEST 2019
 * XML source: all-test-types.xml
 * @generated */
public class Scope extends Chunk {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Scope.class);
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
  protected Scope() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public Scope(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Scope(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public Scope(JCas jcas, int begin, int end) {
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
  //* Feature: cue

  /** getter for cue - gets The cue word or phrase the induces this scope.
   * @generated
   * @return value of the feature 
   */
  public Annotation getCue() {
    if (Scope_Type.featOkTst && ((Scope_Type)jcasType).casFeat_cue == null)
      jcasType.jcas.throwFeatMissing("cue", "de.julielab.jcore.types.Scope");
    return (Annotation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Scope_Type)jcasType).casFeatCode_cue)));}
    
  /** setter for cue - sets The cue word or phrase the induces this scope. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setCue(Annotation v) {
    if (Scope_Type.featOkTst && ((Scope_Type)jcasType).casFeat_cue == null)
      jcasType.jcas.throwFeatMissing("cue", "de.julielab.jcore.types.Scope");
    jcasType.ll_cas.ll_setRefValue(addr, ((Scope_Type)jcasType).casFeatCode_cue, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    