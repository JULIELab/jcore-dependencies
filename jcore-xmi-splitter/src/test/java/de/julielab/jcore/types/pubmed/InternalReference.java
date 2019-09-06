

/* First created by JCasGen Tue Sep 03 12:34:17 CEST 2019 */
package de.julielab.jcore.types.pubmed;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import de.julielab.jcore.types.Annotation;


/** Internal references with a special feature for PMC related reference types. It would be a
                cleaner class hierarchy if this annotation type would be a subtype of
                de.julielab.jcore.types.InternalReference. However, this won't work because this general purpose type
                does not have a restriction on the possible reference types. For PubMed/PMC we want to restrict the
                reference type to the ones valid for PubMed and PMC. But we cannot overwrite the base feature. Thus we
                can't extend the base InternalReference type but create a new one.
 * Updated by JCasGen Tue Sep 03 12:34:17 CEST 2019
 * XML source: all-test-types.xml
 * @generated */
public class InternalReference extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(InternalReference.class);
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
  protected InternalReference() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public InternalReference(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public InternalReference(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public InternalReference(JCas jcas, int begin, int end) {
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
  //* Feature: reftype

  /** getter for reftype - gets The reference type: literature, figure, footnote etc.
   * @generated
   * @return value of the feature 
   */
  public String getReftype() {
    if (InternalReference_Type.featOkTst && ((InternalReference_Type)jcasType).casFeat_reftype == null)
      jcasType.jcas.throwFeatMissing("reftype", "de.julielab.jcore.types.pubmed.InternalReference");
    return jcasType.ll_cas.ll_getStringValue(addr, ((InternalReference_Type)jcasType).casFeatCode_reftype);}
    
  /** setter for reftype - sets The reference type: literature, figure, footnote etc. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setReftype(String v) {
    if (InternalReference_Type.featOkTst && ((InternalReference_Type)jcasType).casFeat_reftype == null)
      jcasType.jcas.throwFeatMissing("reftype", "de.julielab.jcore.types.pubmed.InternalReference");
    jcasType.ll_cas.ll_setStringValue(addr, ((InternalReference_Type)jcasType).casFeatCode_reftype, v);}    
   
    
  //*--------------*
  //* Feature: refid

  /** getter for refid - gets The ID of the referenced object.
   * @generated
   * @return value of the feature 
   */
  public String getRefid() {
    if (InternalReference_Type.featOkTst && ((InternalReference_Type)jcasType).casFeat_refid == null)
      jcasType.jcas.throwFeatMissing("refid", "de.julielab.jcore.types.pubmed.InternalReference");
    return jcasType.ll_cas.ll_getStringValue(addr, ((InternalReference_Type)jcasType).casFeatCode_refid);}
    
  /** setter for refid - sets The ID of the referenced object. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setRefid(String v) {
    if (InternalReference_Type.featOkTst && ((InternalReference_Type)jcasType).casFeat_refid == null)
      jcasType.jcas.throwFeatMissing("refid", "de.julielab.jcore.types.pubmed.InternalReference");
    jcasType.ll_cas.ll_setStringValue(addr, ((InternalReference_Type)jcasType).casFeatCode_refid, v);}    
  }

    