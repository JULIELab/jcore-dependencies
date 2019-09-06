

/* First created by JCasGen Tue Sep 03 12:34:17 CEST 2019 */
package de.julielab.jcore.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** 
 * Updated by JCasGen Tue Sep 03 12:34:17 CEST 2019
 * XML source: all-test-types.xml
 * @generated */
public class EventTrigger extends ConceptMention {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(EventTrigger.class);
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
  protected EventTrigger() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public EventTrigger(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public EventTrigger(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public EventTrigger(JCas jcas, int begin, int end) {
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
  //* Feature: probability

  /** getter for probability - gets probability of this trigger to be an event or relation trigger
   * @generated
   * @return value of the feature 
   */
  public String getProbability() {
    if (EventTrigger_Type.featOkTst && ((EventTrigger_Type)jcasType).casFeat_probability == null)
      jcasType.jcas.throwFeatMissing("probability", "de.julielab.jcore.types.EventTrigger");
    return jcasType.ll_cas.ll_getStringValue(addr, ((EventTrigger_Type)jcasType).casFeatCode_probability);}
    
  /** setter for probability - sets probability of this trigger to be an event or relation trigger 
   * @generated
   * @param v value to set into the feature 
   */
  public void setProbability(String v) {
    if (EventTrigger_Type.featOkTst && ((EventTrigger_Type)jcasType).casFeat_probability == null)
      jcasType.jcas.throwFeatMissing("probability", "de.julielab.jcore.types.EventTrigger");
    jcasType.ll_cas.ll_setStringValue(addr, ((EventTrigger_Type)jcasType).casFeatCode_probability, v);}    
   
    
  //*--------------*
  //* Feature: specifity

  /** getter for specifity - gets specifity to be an event trigger, compare with using in other context
   * @generated
   * @return value of the feature 
   */
  public String getSpecifity() {
    if (EventTrigger_Type.featOkTst && ((EventTrigger_Type)jcasType).casFeat_specifity == null)
      jcasType.jcas.throwFeatMissing("specifity", "de.julielab.jcore.types.EventTrigger");
    return jcasType.ll_cas.ll_getStringValue(addr, ((EventTrigger_Type)jcasType).casFeatCode_specifity);}
    
  /** setter for specifity - sets specifity to be an event trigger, compare with using in other context 
   * @generated
   * @param v value to set into the feature 
   */
  public void setSpecifity(String v) {
    if (EventTrigger_Type.featOkTst && ((EventTrigger_Type)jcasType).casFeat_specifity == null)
      jcasType.jcas.throwFeatMissing("specifity", "de.julielab.jcore.types.EventTrigger");
    jcasType.ll_cas.ll_setStringValue(addr, ((EventTrigger_Type)jcasType).casFeatCode_specifity, v);}    
   
    
  //*--------------*
  //* Feature: importance

  /** getter for importance - gets how important is this trigger for the event
   * @generated
   * @return value of the feature 
   */
  public String getImportance() {
    if (EventTrigger_Type.featOkTst && ((EventTrigger_Type)jcasType).casFeat_importance == null)
      jcasType.jcas.throwFeatMissing("importance", "de.julielab.jcore.types.EventTrigger");
    return jcasType.ll_cas.ll_getStringValue(addr, ((EventTrigger_Type)jcasType).casFeatCode_importance);}
    
  /** setter for importance - sets how important is this trigger for the event 
   * @generated
   * @param v value to set into the feature 
   */
  public void setImportance(String v) {
    if (EventTrigger_Type.featOkTst && ((EventTrigger_Type)jcasType).casFeat_importance == null)
      jcasType.jcas.throwFeatMissing("importance", "de.julielab.jcore.types.EventTrigger");
    jcasType.ll_cas.ll_setStringValue(addr, ((EventTrigger_Type)jcasType).casFeatCode_importance, v);}    
  }

    