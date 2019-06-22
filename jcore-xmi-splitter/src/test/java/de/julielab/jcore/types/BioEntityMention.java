

/* First created by JCasGen Sat Jun 22 14:44:29 CEST 2019 */
package de.julielab.jcore.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.StringArray;


/** Biomedical Entity (e.g. gene, organism, cell type)
 * Updated by JCasGen Sat Jun 22 14:44:29 CEST 2019
 * XML source: /Users/faessler/Coding/git/jcore-dependencies/jcore-xmi-splitter/src/test/resources/de/julielab/jcore/types/all-test-types.xml
 * @generated */
public class BioEntityMention extends EntityMention {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(BioEntityMention.class);
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
  protected BioEntityMention() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public BioEntityMention(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public BioEntityMention(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public BioEntityMention(JCas jcas, int begin, int end) {
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
  //* Feature: species

  /** getter for species - gets The species this BioEntityMentions refers to, if known.
   * @generated
   * @return value of the feature 
   */
  public StringArray getSpecies() {
    if (BioEntityMention_Type.featOkTst && ((BioEntityMention_Type)jcasType).casFeat_species == null)
      jcasType.jcas.throwFeatMissing("species", "de.julielab.jcore.types.BioEntityMention");
    return (StringArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((BioEntityMention_Type)jcasType).casFeatCode_species)));}
    
  /** setter for species - sets The species this BioEntityMentions refers to, if known. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setSpecies(StringArray v) {
    if (BioEntityMention_Type.featOkTst && ((BioEntityMention_Type)jcasType).casFeat_species == null)
      jcasType.jcas.throwFeatMissing("species", "de.julielab.jcore.types.BioEntityMention");
    jcasType.ll_cas.ll_setRefValue(addr, ((BioEntityMention_Type)jcasType).casFeatCode_species, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for species - gets an indexed value - The species this BioEntityMentions refers to, if known.
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public String getSpecies(int i) {
    if (BioEntityMention_Type.featOkTst && ((BioEntityMention_Type)jcasType).casFeat_species == null)
      jcasType.jcas.throwFeatMissing("species", "de.julielab.jcore.types.BioEntityMention");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((BioEntityMention_Type)jcasType).casFeatCode_species), i);
    return jcasType.ll_cas.ll_getStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((BioEntityMention_Type)jcasType).casFeatCode_species), i);}

  /** indexed setter for species - sets an indexed value - The species this BioEntityMentions refers to, if known.
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setSpecies(int i, String v) { 
    if (BioEntityMention_Type.featOkTst && ((BioEntityMention_Type)jcasType).casFeat_species == null)
      jcasType.jcas.throwFeatMissing("species", "de.julielab.jcore.types.BioEntityMention");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((BioEntityMention_Type)jcasType).casFeatCode_species), i);
    jcasType.ll_cas.ll_setStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((BioEntityMention_Type)jcasType).casFeatCode_species), i, v);}
  }

    