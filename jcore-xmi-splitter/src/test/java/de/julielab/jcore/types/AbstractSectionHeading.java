

/* First created by JCasGen Wed Aug 08 13:36:49 CEST 2018 */
package de.julielab.jcore.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** The heading of a section of a structured abstract as
				used by MEDLINE and PubMed.
				The TitleType feature value should always be 'abstractSection'.
 * Updated by JCasGen Wed Aug 08 13:36:49 CEST 2018
 * XML source: /Volumes/OUTERSPACE/Coding/git/jcore-dependencies/jcore-xmi-splitter/src/test/resources/test-types/all-test-types.xml
 * @generated */
public class AbstractSectionHeading extends Title {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(AbstractSectionHeading.class);
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
  protected AbstractSectionHeading() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public AbstractSectionHeading(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public AbstractSectionHeading(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public AbstractSectionHeading(JCas jcas, int begin, int end) {
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
  //* Feature: label

  /** getter for label - gets The author-given label to the abstract section this heading belongs to.
   * @generated
   * @return value of the feature 
   */
  public String getLabel() {
    if (AbstractSectionHeading_Type.featOkTst && ((AbstractSectionHeading_Type)jcasType).casFeat_label == null)
      jcasType.jcas.throwFeatMissing("label", "de.julielab.jcore.types.AbstractSectionHeading");
    return jcasType.ll_cas.ll_getStringValue(addr, ((AbstractSectionHeading_Type)jcasType).casFeatCode_label);}
    
  /** setter for label - sets The author-given label to the abstract section this heading belongs to. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setLabel(String v) {
    if (AbstractSectionHeading_Type.featOkTst && ((AbstractSectionHeading_Type)jcasType).casFeat_label == null)
      jcasType.jcas.throwFeatMissing("label", "de.julielab.jcore.types.AbstractSectionHeading");
    jcasType.ll_cas.ll_setStringValue(addr, ((AbstractSectionHeading_Type)jcasType).casFeatCode_label, v);}    
   
    
  //*--------------*
  //* Feature: nlmCategory

  /** getter for nlmCategory - gets The NLM category associated with the section label given by the authors (see feature 'label').
   * @generated
   * @return value of the feature 
   */
  public String getNlmCategory() {
    if (AbstractSectionHeading_Type.featOkTst && ((AbstractSectionHeading_Type)jcasType).casFeat_nlmCategory == null)
      jcasType.jcas.throwFeatMissing("nlmCategory", "de.julielab.jcore.types.AbstractSectionHeading");
    return jcasType.ll_cas.ll_getStringValue(addr, ((AbstractSectionHeading_Type)jcasType).casFeatCode_nlmCategory);}
    
  /** setter for nlmCategory - sets The NLM category associated with the section label given by the authors (see feature 'label'). 
   * @generated
   * @param v value to set into the feature 
   */
  public void setNlmCategory(String v) {
    if (AbstractSectionHeading_Type.featOkTst && ((AbstractSectionHeading_Type)jcasType).casFeat_nlmCategory == null)
      jcasType.jcas.throwFeatMissing("nlmCategory", "de.julielab.jcore.types.AbstractSectionHeading");
    jcasType.ll_cas.ll_setStringValue(addr, ((AbstractSectionHeading_Type)jcasType).casFeatCode_nlmCategory, v);}    
  }

    