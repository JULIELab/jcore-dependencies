

/* First created by JCasGen Sat Jul 13 15:41:00 CEST 2019 */
package de.julielab.jcore.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** A specific version of the resource entry for Gene entries.
 * Updated by JCasGen Sat Jul 13 15:41:00 CEST 2019
 * XML source: all-test-types.xml
 * @generated */
public class GeneResourceEntry extends ResourceEntry {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(GeneResourceEntry.class);
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
  protected GeneResourceEntry() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public GeneResourceEntry(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public GeneResourceEntry(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public GeneResourceEntry(JCas jcas, int begin, int end) {
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
  //* Feature: synonym

  /** getter for synonym - gets The synonym for which this resource entry was chosen.
   * @generated
   * @return value of the feature 
   */
  public String getSynonym() {
    if (GeneResourceEntry_Type.featOkTst && ((GeneResourceEntry_Type)jcasType).casFeat_synonym == null)
      jcasType.jcas.throwFeatMissing("synonym", "de.julielab.jcore.types.GeneResourceEntry");
    return jcasType.ll_cas.ll_getStringValue(addr, ((GeneResourceEntry_Type)jcasType).casFeatCode_synonym);}
    
  /** setter for synonym - sets The synonym for which this resource entry was chosen. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setSynonym(String v) {
    if (GeneResourceEntry_Type.featOkTst && ((GeneResourceEntry_Type)jcasType).casFeat_synonym == null)
      jcasType.jcas.throwFeatMissing("synonym", "de.julielab.jcore.types.GeneResourceEntry");
    jcasType.ll_cas.ll_setStringValue(addr, ((GeneResourceEntry_Type)jcasType).casFeatCode_synonym, v);}    
   
    
  //*--------------*
  //* Feature: confidenceMention

  /** getter for confidenceMention - gets The confidence that the textual found mention fits to the selected synonyms.
   * @generated
   * @return value of the feature 
   */
  public double getConfidenceMention() {
    if (GeneResourceEntry_Type.featOkTst && ((GeneResourceEntry_Type)jcasType).casFeat_confidenceMention == null)
      jcasType.jcas.throwFeatMissing("confidenceMention", "de.julielab.jcore.types.GeneResourceEntry");
    return jcasType.ll_cas.ll_getDoubleValue(addr, ((GeneResourceEntry_Type)jcasType).casFeatCode_confidenceMention);}
    
  /** setter for confidenceMention - sets The confidence that the textual found mention fits to the selected synonyms. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setConfidenceMention(double v) {
    if (GeneResourceEntry_Type.featOkTst && ((GeneResourceEntry_Type)jcasType).casFeat_confidenceMention == null)
      jcasType.jcas.throwFeatMissing("confidenceMention", "de.julielab.jcore.types.GeneResourceEntry");
    jcasType.ll_cas.ll_setDoubleValue(addr, ((GeneResourceEntry_Type)jcasType).casFeatCode_confidenceMention, v);}    
   
    
  //*--------------*
  //* Feature: confidenceSemantic

  /** getter for confidenceSemantic - gets Confidence that this resource entry is indeed semantically fitting to the context.
   * @generated
   * @return value of the feature 
   */
  public double getConfidenceSemantic() {
    if (GeneResourceEntry_Type.featOkTst && ((GeneResourceEntry_Type)jcasType).casFeat_confidenceSemantic == null)
      jcasType.jcas.throwFeatMissing("confidenceSemantic", "de.julielab.jcore.types.GeneResourceEntry");
    return jcasType.ll_cas.ll_getDoubleValue(addr, ((GeneResourceEntry_Type)jcasType).casFeatCode_confidenceSemantic);}
    
  /** setter for confidenceSemantic - sets Confidence that this resource entry is indeed semantically fitting to the context. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setConfidenceSemantic(double v) {
    if (GeneResourceEntry_Type.featOkTst && ((GeneResourceEntry_Type)jcasType).casFeat_confidenceSemantic == null)
      jcasType.jcas.throwFeatMissing("confidenceSemantic", "de.julielab.jcore.types.GeneResourceEntry");
    jcasType.ll_cas.ll_setDoubleValue(addr, ((GeneResourceEntry_Type)jcasType).casFeatCode_confidenceSemantic, v);}    
   
    
  //*--------------*
  //* Feature: taxonomyId

  /** getter for taxonomyId - gets The NCBI taxonomy ID of this gene.
   * @generated
   * @return value of the feature 
   */
  public String getTaxonomyId() {
    if (GeneResourceEntry_Type.featOkTst && ((GeneResourceEntry_Type)jcasType).casFeat_taxonomyId == null)
      jcasType.jcas.throwFeatMissing("taxonomyId", "de.julielab.jcore.types.GeneResourceEntry");
    return jcasType.ll_cas.ll_getStringValue(addr, ((GeneResourceEntry_Type)jcasType).casFeatCode_taxonomyId);}
    
  /** setter for taxonomyId - sets The NCBI taxonomy ID of this gene. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setTaxonomyId(String v) {
    if (GeneResourceEntry_Type.featOkTst && ((GeneResourceEntry_Type)jcasType).casFeat_taxonomyId == null)
      jcasType.jcas.throwFeatMissing("taxonomyId", "de.julielab.jcore.types.GeneResourceEntry");
    jcasType.ll_cas.ll_setStringValue(addr, ((GeneResourceEntry_Type)jcasType).casFeatCode_taxonomyId, v);}    
  }

    