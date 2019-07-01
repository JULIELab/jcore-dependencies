

/* First created by JCasGen Mon Jul 01 14:16:43 CEST 2019 */
package de.julielab.jcore.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.StringArray;


/** Token annotation marks the span of a token and takes all additional annotations that are on the token level, including Part-of-Speech information, lemma, stemmed form,  grammatical features such as gender, number and orthographical information; furthemore, Token includes the information about dependency relations to other tokens (see correspondent annotation types for further infromation).
 * Updated by JCasGen Mon Jul 01 14:16:43 CEST 2019
 * XML source: arrayAndListHolderTestType.xml
 * @generated */
public class Token extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Token.class);
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
  protected Token() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public Token(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Token(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public Token(JCas jcas, int begin, int end) {
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
  //* Feature: lemma

  /** getter for lemma - gets the lemma information, O
   * @generated
   * @return value of the feature 
   */
  public Lemma getLemma() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_lemma == null)
      jcasType.jcas.throwFeatMissing("lemma", "de.julielab.jcore.types.Token");
    return (Lemma)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_lemma)));}
    
  /** setter for lemma - sets the lemma information, O 
   * @generated
   * @param v value to set into the feature 
   */
  public void setLemma(Lemma v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_lemma == null)
      jcasType.jcas.throwFeatMissing("lemma", "de.julielab.jcore.types.Token");
    jcasType.ll_cas.ll_setRefValue(addr, ((Token_Type)jcasType).casFeatCode_lemma, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: posTag

  /** getter for posTag - gets List contains part-of-speech tags of different part-of-speech tagsets (see also POSTag and subtypes), O
   * @generated
   * @return value of the feature 
   */
  public FSArray getPosTag() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_posTag == null)
      jcasType.jcas.throwFeatMissing("posTag", "de.julielab.jcore.types.Token");
    return (FSArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_posTag)));}
    
  /** setter for posTag - sets List contains part-of-speech tags of different part-of-speech tagsets (see also POSTag and subtypes), O 
   * @generated
   * @param v value to set into the feature 
   */
  public void setPosTag(FSArray v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_posTag == null)
      jcasType.jcas.throwFeatMissing("posTag", "de.julielab.jcore.types.Token");
    jcasType.ll_cas.ll_setRefValue(addr, ((Token_Type)jcasType).casFeatCode_posTag, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for posTag - gets an indexed value - List contains part-of-speech tags of different part-of-speech tagsets (see also POSTag and subtypes), O
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public POSTag getPosTag(int i) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_posTag == null)
      jcasType.jcas.throwFeatMissing("posTag", "de.julielab.jcore.types.Token");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_posTag), i);
    return (POSTag)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_posTag), i)));}

  /** indexed setter for posTag - sets an indexed value - List contains part-of-speech tags of different part-of-speech tagsets (see also POSTag and subtypes), O
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setPosTag(int i, POSTag v) { 
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_posTag == null)
      jcasType.jcas.throwFeatMissing("posTag", "de.julielab.jcore.types.Token");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_posTag), i);
    jcasType.ll_cas.ll_setRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_posTag), i, jcasType.ll_cas.ll_getFSRef(v));}
   
    
  //*--------------*
  //* Feature: stemmedForm

  /** getter for stemmedForm - gets Contains the stemmed form of token (see StemmedForm), O
   * @generated
   * @return value of the feature 
   */
  public StemmedForm getStemmedForm() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_stemmedForm == null)
      jcasType.jcas.throwFeatMissing("stemmedForm", "de.julielab.jcore.types.Token");
    return (StemmedForm)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_stemmedForm)));}
    
  /** setter for stemmedForm - sets Contains the stemmed form of token (see StemmedForm), O 
   * @generated
   * @param v value to set into the feature 
   */
  public void setStemmedForm(StemmedForm v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_stemmedForm == null)
      jcasType.jcas.throwFeatMissing("stemmedForm", "de.julielab.jcore.types.Token");
    jcasType.ll_cas.ll_setRefValue(addr, ((Token_Type)jcasType).casFeatCode_stemmedForm, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: feats

  /** getter for feats - gets Contains grammatical features correspondent to the part-of-speech tag of current token (see GrammaticalFeats and subtypes), O
   * @generated
   * @return value of the feature 
   */
  public GrammaticalFeats getFeats() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_feats == null)
      jcasType.jcas.throwFeatMissing("feats", "de.julielab.jcore.types.Token");
    return (GrammaticalFeats)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_feats)));}
    
  /** setter for feats - sets Contains grammatical features correspondent to the part-of-speech tag of current token (see GrammaticalFeats and subtypes), O 
   * @generated
   * @param v value to set into the feature 
   */
  public void setFeats(GrammaticalFeats v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_feats == null)
      jcasType.jcas.throwFeatMissing("feats", "de.julielab.jcore.types.Token");
    jcasType.ll_cas.ll_setRefValue(addr, ((Token_Type)jcasType).casFeatCode_feats, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: orthogr

  /** getter for orthogr - gets see de.julielab.jcore.types.Orthogrpahy
   * @generated
   * @return value of the feature 
   */
  public String getOrthogr() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_orthogr == null)
      jcasType.jcas.throwFeatMissing("orthogr", "de.julielab.jcore.types.Token");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Token_Type)jcasType).casFeatCode_orthogr);}
    
  /** setter for orthogr - sets see de.julielab.jcore.types.Orthogrpahy 
   * @generated
   * @param v value to set into the feature 
   */
  public void setOrthogr(String v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_orthogr == null)
      jcasType.jcas.throwFeatMissing("orthogr", "de.julielab.jcore.types.Token");
    jcasType.ll_cas.ll_setStringValue(addr, ((Token_Type)jcasType).casFeatCode_orthogr, v);}    
   
    
  //*--------------*
  //* Feature: depRel

  /** getter for depRel - gets Contains a list of syntactical dependencies, see DependencyRelation, O
   * @generated
   * @return value of the feature 
   */
  public FSArray getDepRel() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_depRel == null)
      jcasType.jcas.throwFeatMissing("depRel", "de.julielab.jcore.types.Token");
    return (FSArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_depRel)));}
    
  /** setter for depRel - sets Contains a list of syntactical dependencies, see DependencyRelation, O 
   * @generated
   * @param v value to set into the feature 
   */
  public void setDepRel(FSArray v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_depRel == null)
      jcasType.jcas.throwFeatMissing("depRel", "de.julielab.jcore.types.Token");
    jcasType.ll_cas.ll_setRefValue(addr, ((Token_Type)jcasType).casFeatCode_depRel, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for depRel - gets an indexed value - Contains a list of syntactical dependencies, see DependencyRelation, O
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public DependencyRelation getDepRel(int i) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_depRel == null)
      jcasType.jcas.throwFeatMissing("depRel", "de.julielab.jcore.types.Token");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_depRel), i);
    return (DependencyRelation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_depRel), i)));}

  /** indexed setter for depRel - sets an indexed value - Contains a list of syntactical dependencies, see DependencyRelation, O
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setDepRel(int i, DependencyRelation v) { 
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_depRel == null)
      jcasType.jcas.throwFeatMissing("depRel", "de.julielab.jcore.types.Token");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_depRel), i);
    jcasType.ll_cas.ll_setRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_depRel), i, jcasType.ll_cas.ll_getFSRef(v));}
   
    
  //*--------------*
  //* Feature: synonyms

  /** getter for synonyms - gets 
   * @generated
   * @return value of the feature 
   */
  public StringArray getSynonyms() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_synonyms == null)
      jcasType.jcas.throwFeatMissing("synonyms", "de.julielab.jcore.types.Token");
    return (StringArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_synonyms)));}
    
  /** setter for synonyms - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setSynonyms(StringArray v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_synonyms == null)
      jcasType.jcas.throwFeatMissing("synonyms", "de.julielab.jcore.types.Token");
    jcasType.ll_cas.ll_setRefValue(addr, ((Token_Type)jcasType).casFeatCode_synonyms, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for synonyms - gets an indexed value - 
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public String getSynonyms(int i) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_synonyms == null)
      jcasType.jcas.throwFeatMissing("synonyms", "de.julielab.jcore.types.Token");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_synonyms), i);
    return jcasType.ll_cas.ll_getStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_synonyms), i);}

  /** indexed setter for synonyms - sets an indexed value - 
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setSynonyms(int i, String v) { 
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_synonyms == null)
      jcasType.jcas.throwFeatMissing("synonyms", "de.julielab.jcore.types.Token");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_synonyms), i);
    jcasType.ll_cas.ll_setStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_synonyms), i, v);}
   
    
  //*--------------*
  //* Feature: hypernyms

  /** getter for hypernyms - gets 
   * @generated
   * @return value of the feature 
   */
  public StringArray getHypernyms() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_hypernyms == null)
      jcasType.jcas.throwFeatMissing("hypernyms", "de.julielab.jcore.types.Token");
    return (StringArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_hypernyms)));}
    
  /** setter for hypernyms - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setHypernyms(StringArray v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_hypernyms == null)
      jcasType.jcas.throwFeatMissing("hypernyms", "de.julielab.jcore.types.Token");
    jcasType.ll_cas.ll_setRefValue(addr, ((Token_Type)jcasType).casFeatCode_hypernyms, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for hypernyms - gets an indexed value - 
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public String getHypernyms(int i) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_hypernyms == null)
      jcasType.jcas.throwFeatMissing("hypernyms", "de.julielab.jcore.types.Token");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_hypernyms), i);
    return jcasType.ll_cas.ll_getStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_hypernyms), i);}

  /** indexed setter for hypernyms - sets an indexed value - 
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setHypernyms(int i, String v) { 
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_hypernyms == null)
      jcasType.jcas.throwFeatMissing("hypernyms", "de.julielab.jcore.types.Token");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_hypernyms), i);
    jcasType.ll_cas.ll_setStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_hypernyms), i, v);}
   
    
  //*--------------*
  //* Feature: embeddingVectors

  /** getter for embeddingVectors - gets An array of embedding vectors for this token.
   * @generated
   * @return value of the feature 
   */
  public FSArray getEmbeddingVectors() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_embeddingVectors == null)
      jcasType.jcas.throwFeatMissing("embeddingVectors", "de.julielab.jcore.types.Token");
    return (FSArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_embeddingVectors)));}
    
  /** setter for embeddingVectors - sets An array of embedding vectors for this token. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setEmbeddingVectors(FSArray v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_embeddingVectors == null)
      jcasType.jcas.throwFeatMissing("embeddingVectors", "de.julielab.jcore.types.Token");
    jcasType.ll_cas.ll_setRefValue(addr, ((Token_Type)jcasType).casFeatCode_embeddingVectors, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for embeddingVectors - gets an indexed value - An array of embedding vectors for this token.
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public EmbeddingVector getEmbeddingVectors(int i) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_embeddingVectors == null)
      jcasType.jcas.throwFeatMissing("embeddingVectors", "de.julielab.jcore.types.Token");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_embeddingVectors), i);
    return (EmbeddingVector)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_embeddingVectors), i)));}

  /** indexed setter for embeddingVectors - sets an indexed value - An array of embedding vectors for this token.
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setEmbeddingVectors(int i, EmbeddingVector v) { 
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_embeddingVectors == null)
      jcasType.jcas.throwFeatMissing("embeddingVectors", "de.julielab.jcore.types.Token");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_embeddingVectors), i);
    jcasType.ll_cas.ll_setRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_embeddingVectors), i, jcasType.ll_cas.ll_getFSRef(v));}
  }

    