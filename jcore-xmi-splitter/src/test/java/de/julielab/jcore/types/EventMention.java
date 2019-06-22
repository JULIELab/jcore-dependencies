

/* First created by JCasGen Sat Jun 22 14:44:29 CEST 2019 */
package de.julielab.jcore.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** This type was introduced during the participation of the BioNLP Shared Task 2009 by Ekaterina Buyko. Therefore, it mainly satisfies the requirements of the event type of this challenge. This includes the use of an "event trigger" which is not necessary for all relation types. In practice, the type is used to capture predicate-argument relations (such as binding) as well as eventive propositional relations (such as phosphorylation or up- and down-regulation). The latter category also includes events where one of the event arguments is an event itself.
 * Updated by JCasGen Sat Jun 22 14:44:29 CEST 2019
 * XML source: /Users/faessler/Coding/git/jcore-dependencies/jcore-xmi-splitter/src/test/resources/de/julielab/jcore/types/all-test-types.xml
 * @generated */
public class EventMention extends GeneralEventMention {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(EventMention.class);
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
  protected EventMention() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public EventMention(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public EventMention(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public EventMention(JCas jcas, int begin, int end) {
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
  //* Feature: genericity

  /** getter for genericity - gets 
   * @generated
   * @return value of the feature 
   */
  public String getGenericity() {
    if (EventMention_Type.featOkTst && ((EventMention_Type)jcasType).casFeat_genericity == null)
      jcasType.jcas.throwFeatMissing("genericity", "de.julielab.jcore.types.EventMention");
    return jcasType.ll_cas.ll_getStringValue(addr, ((EventMention_Type)jcasType).casFeatCode_genericity);}
    
  /** setter for genericity - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setGenericity(String v) {
    if (EventMention_Type.featOkTst && ((EventMention_Type)jcasType).casFeat_genericity == null)
      jcasType.jcas.throwFeatMissing("genericity", "de.julielab.jcore.types.EventMention");
    jcasType.ll_cas.ll_setStringValue(addr, ((EventMention_Type)jcasType).casFeatCode_genericity, v);}    
   
    
  //*--------------*
  //* Feature: trigger

  /** getter for trigger - gets 
   * @generated
   * @return value of the feature 
   */
  public EventTrigger getTrigger() {
    if (EventMention_Type.featOkTst && ((EventMention_Type)jcasType).casFeat_trigger == null)
      jcasType.jcas.throwFeatMissing("trigger", "de.julielab.jcore.types.EventMention");
    return (EventTrigger)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((EventMention_Type)jcasType).casFeatCode_trigger)));}
    
  /** setter for trigger - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setTrigger(EventTrigger v) {
    if (EventMention_Type.featOkTst && ((EventMention_Type)jcasType).casFeat_trigger == null)
      jcasType.jcas.throwFeatMissing("trigger", "de.julielab.jcore.types.EventMention");
    jcasType.ll_cas.ll_setRefValue(addr, ((EventMention_Type)jcasType).casFeatCode_trigger, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    