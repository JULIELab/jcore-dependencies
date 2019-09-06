

/* First created by JCasGen Tue Sep 03 12:34:17 CEST 2019 */
package de.julielab.jcore.types.pubmed;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** Annotation of the complete abstract.
 * Updated by JCasGen Tue Sep 03 12:34:17 CEST 2019
 * XML source: all-test-types.xml
 * @generated */
public class AbstractText extends de.julielab.jcore.types.AbstractText {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(AbstractText.class);
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
  protected AbstractText() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public AbstractText(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public AbstractText(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public AbstractText(JCas jcas, int begin, int end) {
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
  //* Feature: abstractType

  /** getter for abstractType - gets A string specifying what kind of abstract this is.
   * @generated
   * @return value of the feature 
   */
  public String getAbstractType() {
    if (AbstractText_Type.featOkTst && ((AbstractText_Type)jcasType).casFeat_abstractType == null)
      jcasType.jcas.throwFeatMissing("abstractType", "de.julielab.jcore.types.pubmed.AbstractText");
    return jcasType.ll_cas.ll_getStringValue(addr, ((AbstractText_Type)jcasType).casFeatCode_abstractType);}
    
  /** setter for abstractType - sets A string specifying what kind of abstract this is. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setAbstractType(String v) {
    if (AbstractText_Type.featOkTst && ((AbstractText_Type)jcasType).casFeat_abstractType == null)
      jcasType.jcas.throwFeatMissing("abstractType", "de.julielab.jcore.types.pubmed.AbstractText");
    jcasType.ll_cas.ll_setStringValue(addr, ((AbstractText_Type)jcasType).casFeatCode_abstractType, v);}    
  }

    