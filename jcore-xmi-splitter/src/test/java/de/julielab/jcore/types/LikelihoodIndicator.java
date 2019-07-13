

/* First created by JCasGen Sat Jul 13 15:41:00 CEST 2019 */
package de.julielab.jcore.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.StringArray;


/** This type refers to the likelihood aspect of epistemic modality. The annotation marks epistemic modal expressions used to linguistically modify the likelihood of an event or of a relation that an entity might be in. The employed likelihood scale also includes negation (0% likelihood) and assertion (100% likelihood), the latter being the default case where no explicit likelihood modifier is present in the text.
 * Updated by JCasGen Sat Jul 13 15:41:00 CEST 2019
 * XML source: all-test-types.xml
 * @generated */
public class LikelihoodIndicator extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(LikelihoodIndicator.class);
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
  protected LikelihoodIndicator() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public LikelihoodIndicator(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public LikelihoodIndicator(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public LikelihoodIndicator(JCas jcas, int begin, int end) {
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
  //* Feature: likelihood

  /** getter for likelihood - gets 
   * @generated
   * @return value of the feature 
   */
  public String getLikelihood() {
    if (LikelihoodIndicator_Type.featOkTst && ((LikelihoodIndicator_Type)jcasType).casFeat_likelihood == null)
      jcasType.jcas.throwFeatMissing("likelihood", "de.julielab.jcore.types.LikelihoodIndicator");
    return jcasType.ll_cas.ll_getStringValue(addr, ((LikelihoodIndicator_Type)jcasType).casFeatCode_likelihood);}
    
  /** setter for likelihood - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setLikelihood(String v) {
    if (LikelihoodIndicator_Type.featOkTst && ((LikelihoodIndicator_Type)jcasType).casFeat_likelihood == null)
      jcasType.jcas.throwFeatMissing("likelihood", "de.julielab.jcore.types.LikelihoodIndicator");
    jcasType.ll_cas.ll_setStringValue(addr, ((LikelihoodIndicator_Type)jcasType).casFeatCode_likelihood, v);}    
   
    
  //*--------------*
  //* Feature: entityAndRelationString

  /** getter for entityAndRelationString - gets 
   * @generated
   * @return value of the feature 
   */
  public StringArray getEntityAndRelationString() {
    if (LikelihoodIndicator_Type.featOkTst && ((LikelihoodIndicator_Type)jcasType).casFeat_entityAndRelationString == null)
      jcasType.jcas.throwFeatMissing("entityAndRelationString", "de.julielab.jcore.types.LikelihoodIndicator");
    return (StringArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((LikelihoodIndicator_Type)jcasType).casFeatCode_entityAndRelationString)));}
    
  /** setter for entityAndRelationString - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setEntityAndRelationString(StringArray v) {
    if (LikelihoodIndicator_Type.featOkTst && ((LikelihoodIndicator_Type)jcasType).casFeat_entityAndRelationString == null)
      jcasType.jcas.throwFeatMissing("entityAndRelationString", "de.julielab.jcore.types.LikelihoodIndicator");
    jcasType.ll_cas.ll_setRefValue(addr, ((LikelihoodIndicator_Type)jcasType).casFeatCode_entityAndRelationString, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for entityAndRelationString - gets an indexed value - 
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public String getEntityAndRelationString(int i) {
    if (LikelihoodIndicator_Type.featOkTst && ((LikelihoodIndicator_Type)jcasType).casFeat_entityAndRelationString == null)
      jcasType.jcas.throwFeatMissing("entityAndRelationString", "de.julielab.jcore.types.LikelihoodIndicator");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((LikelihoodIndicator_Type)jcasType).casFeatCode_entityAndRelationString), i);
    return jcasType.ll_cas.ll_getStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((LikelihoodIndicator_Type)jcasType).casFeatCode_entityAndRelationString), i);}

  /** indexed setter for entityAndRelationString - sets an indexed value - 
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setEntityAndRelationString(int i, String v) { 
    if (LikelihoodIndicator_Type.featOkTst && ((LikelihoodIndicator_Type)jcasType).casFeat_entityAndRelationString == null)
      jcasType.jcas.throwFeatMissing("entityAndRelationString", "de.julielab.jcore.types.LikelihoodIndicator");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((LikelihoodIndicator_Type)jcasType).casFeatCode_entityAndRelationString), i);
    jcasType.ll_cas.ll_setStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((LikelihoodIndicator_Type)jcasType).casFeatCode_entityAndRelationString), i, v);}
  }

    