/* Copyright (C) 2003 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */
package edu.umass.cs.mallet.projects.seg_plus_coref.ie;

import bsh.EvalError;
import edu.umass.cs.mallet.base.fst.*;
import edu.umass.cs.mallet.base.pipe.*;
import edu.umass.cs.mallet.base.pipe.iterator.AbstractPipeInputIterator;
import edu.umass.cs.mallet.base.pipe.iterator.LineGroupIterator;
import edu.umass.cs.mallet.base.pipe.tsf.LexiconMembership;
import edu.umass.cs.mallet.base.pipe.tsf.OffsetConjunctions;
import edu.umass.cs.mallet.base.pipe.tsf.RegexMatches;
import edu.umass.cs.mallet.base.pipe.tsf.TokenText;
import edu.umass.cs.mallet.base.types.*;
import edu.umass.cs.mallet.base.util.CharSequenceLexer;
import edu.umass.cs.mallet.base.util.CommandOption;
import edu.umass.cs.mallet.base.util.MalletLogger;
import edu.umass.cs.mallet.projects.seg_plus_coref.BaseTUICRF;
import edu.umass.cs.mallet.projects.seg_plus_coref.clustering.LineGroupIterator2;
import edu.umass.cs.mallet.projects.seg_plus_coref.clustering.SGML2FieldsPipe;
import gnu.trove.TIntArrayList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;


/**
 * Created: May 22, 2004
 * 
 * @author <A HREF="mailto:casutton@cs.umass.edu>casutton@cs.umass.edu</A>
 * @version $Id: TUI_CorefIE.java,v 1.14 2004/06/15 14:39:58 casutton Exp $
 */
public class TUI_CorefIE extends BaseTUICRF {

  private static String[] SEPARATORS = new String[]{"<NEW_HEADER>", "<NEWREFERENCE>"};
//	private static String[] SEPERATOR = new String[] {"<NEW_HEADER>", "^$"};

  private static final Logger logger = MalletLogger.getLogger(TUI_CorefIE.class.getName());

  static CommandOption.File crfInputFileOption = new CommandOption.File
          (TUI_CorefIE.class, "crf-input-file", "FILENAME", true, null,
                  "The name of the file to read the trained CRF for testing.", null);

  static CommandOption.File inputFileOption = new CommandOption.File
          (TUI_CorefIE.class, "input-file", "FILENAME", true, null,
                  "The name of the file containing the testing data.", null);

  static CommandOption.Integer headOrRefOption = new CommandOption.Integer
          (TUI_CorefIE.class, "head-or-ref", "INTEGER", true, 0,
                  "0 for header, 1 for reference", null);

  static CommandOption.Integer nBestChoice = new CommandOption.Integer
          (TUI_CorefIE.class, "nbestchoice", "INTEGER", true, 1,
                  "N for N-best", null);

  static CommandOption.Boolean includeBibtexLexicons = new CommandOption.Boolean
          (TUI_CorefIE.class, "include-bibtex-lexicons", "INTEGER", true, false,
                  "Whether to use BibTeX lexicons from Fuchun.", null);

  static CommandOption.Boolean excludingSingletons = new CommandOption.Boolean
          (TUI_CorefIE.class, "exclude-singletons", "boolean", true, true,
                  "excluding singletons.", null);

  static CommandOption.Boolean useClusterFeatures = new CommandOption.Boolean
          (TUI_CorefIE.class, "use-cluster-features", "boolean", true, true,
                  "excluding singletons.", null);

  static CommandOption.Boolean useNegativeClusterFeatures = new CommandOption.Boolean
          (TUI_CorefIE.class, "use-negative-cluster-features", "boolean", true, false,
                  "Whether to use features that say words AREN'T tagged in cluster.", null);

  static CommandOption.Boolean useNumClusterOccurences = new CommandOption.Boolean
          (TUI_CorefIE.class, "use-cluster-occurrences", "boolean", true, false,
                  "Whether to use number of tagged word occurrences in cluster as features.", null);

  static CommandOption.Boolean useBogusClusterFeatures = new CommandOption.Boolean
          (TUI_CorefIE.class, "use-bogus-cluster-features", "boolean", true, false,
                  "If true, use features from the instance's true segmentation.", null);

  static CommandOption.Boolean useSparseWeights = new CommandOption.Boolean
          (TUI_CorefIE.class, "use-sparse-weights", "boolean", true, false,
                  "If true, use only input features that appear in training set.", null);

  static CommandOption.Integer clusterFeatureMinimum = new CommandOption.Integer
          (TUI_CorefIE.class, "cluster-feature-minimum", "INTEGER", true, 2,
                  "Minimum number of coreferent citations that need to agree to create a cluster feature.", null);

  static CommandOption.Integer clusterSizeLimit = new CommandOption.Integer
          (TUI_CorefIE.class, "cluster-size-limit", "INTEGER", true, 10000,
                  "cluster Size Limit", null);

  static CommandOption.Integer methodChoice = new CommandOption.Integer
          (TUI_CorefIE.class, "methodchoice", "INTEGER", true, 1,
                  "method for canonical citation creation", null);

  static CommandOption.Integer markovOrder = new CommandOption.Integer
          (TUI_CorefIE.class, "markov-order", "INTEGER", true, 0,
                  "0 = states for all transitions, 1 = half labels, 2 = three-quarter labels", null);

  static CommandOption.Integer numRepsOption = new CommandOption.Integer
          (TUI_CorefIE.class, "num-reps", "INTEGER", true, 5,
                  "Number of random test-training splits to try.", null);



  static String refNoMeta = "reference_no=";
  static String clusterNoMeta = "cluster_no=";

  static String[] FIELD_NAMES = new String[]
  {"author", "title", "date", "publisher", "location", "pages",
   "institution", "editor", "volume", "note", "booktitle", "tech", "journal"};

  static String[] startTags = new String[]
  {"<author>", "<title>", "<date>", "<publisher>", "<location>", "<pages>",
   "<institution>", "<editor>", "<volume>", "<note>", "<booktitle>", "<tech>", "<journal>"};

  static String[] endTags = new String[]
  {"</author>", "</title>", "</date>", "</publisher>", "</location>", "</pages>",
   "</institution>", "</editor>", "</volume>", "</note>", "</booktitle>", "</tech>", "</journal>"};

  static double[] tagWeight = new double[]{1.0, 1.0, 0.5, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0};

  static int NumFields = 10;
  private static String separator;


  public static void main (String[] args) throws Exception
  {
    CommandOption.List options = new CommandOption.List("Segmenting references based on coreference information.",
            new CommandOption[0]);
    options.add(TUI_CorefIE.class);
    options.add(BaseTUICRF.class);
    options.process(args);

    initOutputDirectory ();
    options.logOptions(logger);

    long timeStart = System.currentTimeMillis();

    separator = SEPARATORS[headOrRefOption.value()];

    Random r = new Random (randomSeedOption.value);

    for (int rep = 0; rep < numRepsOption.value; rep++) {

      logger.info ("REPETITION "+rep);

      // This is convoluted because the instances need to be split by cluster
      InstanceList rawList = new InstanceList(new Alphabet(), new Alphabet());
      rawList.add(new LineGroupIterator(new FileReader(inputFileOption.value), Pattern.compile(separator), true));

      InstanceList rawClusters = getInstanceListClusters(rawList, inputFileOption.value);

      InstanceList[] trainTest = rawClusters.split(r, new double[] {
          trainingPct.value,
          1 - trainingPct.value, });
      InstanceList trainClusters = trainTest[0];
      InstanceList testClusters = trainTest[1];

      logger.info("Num train clusters = " + trainClusters.size());
      logger.info("Num test clusters = " + testClusters.size());

//    System.out.println("Train clusters: ");
//    printClusterList (trainClusters);
//    System.out.println("\n\n\nTest clusters: ");
//    printClusterList (testClusters);

      System.out.println("Creating allclustersegmentation");
      Pipe basePipe = makeBasePipe();
      AllClusterSegmentation allseg = new AllClusterSegmentation(rawClusters, basePipe);

//    System.out.println("ALL SEGMENTATION: ");
//    allseg.print ();

      //TODO: Add loop, coreferent segmentation aware pipe

      Pipe segmentationsPipe = makeSegmentationsPipe (allseg, useClusterFeatures.value,
                  useNumClusterOccurences.value, useBogusClusterFeatures.value);

      Pipe thePipe = new SerialPipes(new Pipe[]{
        makeBasePipe(),
        segmentationsPipe,
        new TokenSequence2FeatureVectorSequence(),
//        new PrintInputAndTarget(),
      });

      InstanceList training = new InstanceList(thePipe);
      training.add(new ClusterListIterator(trainClusters));

      InstanceList testing = new InstanceList(thePipe);
      testing.add(new ClusterListIterator(testClusters));

      logger.info ("Number of training instances = "+training.size ());
      logger.info ("Number of testing instances = "+testing.size ());

      CRF4 crf = new CRF4(thePipe, null);
      crf.setUseSparseWeights(useSparseWeights.value);
      switch (markovOrder.value) {
        case 0:
          crf.addStatesForLabelsConnectedAsIn(training); break;

        case 1:
          crf.addStatesForHalfLabelsConnectedAsIn(training); break;

        case 2:
          crf.addStatesForThreeQuarterLabelsConnectedAsIn(training); break;

        default:
          System.err.println("Unknown markov-order "+markovOrder.value);
          System.exit (1);
      }

      InstanceList nontrivialTest = getNonTrivialTesting (thePipe, testClusters);

      TransducerEvaluator eval = new TokenAccuracyEvaluator();
      eval.setNumIterationsToWait(10);
      eval.setNumIterationsToSkip(5);

      crf.train(training, null, testing, eval);
      TransducerEvaluator fieldEval = new FieldF1Evaluator(FIELD_NAMES);
      fieldEval.test(crf, training, "Training", null);
      fieldEval.test(crf, testing, "Testing", null);
      fieldEval.test(crf, nontrivialTest, "Clusters>=3", null);

      TransducerEvaluator perClassEval = new PerClassAccuracyEvaluator();
      perClassEval.test(crf, training, "Training", null);
      perClassEval.test(crf, testing, "Testing", null);
      perClassEval.test(crf, nontrivialTest, "Clusters>=3", null);

      TransducerEvaluator instanceEval = new InstanceAccuracyEvaluator();
      instanceEval.test(crf, training, "Training", null);
      instanceEval.test(crf, testing, "Testing", null);
      instanceEval.test(crf, nontrivialTest, "Clusters>=3", null);

      writeOutput(crf, training, "-train-" + rep);
      writeOutput(crf, testing, "-test-" + rep);
      writeOutput(crf, nontrivialTest, "test-gt-3-" + rep); 

      writeCrf(crf, "-" + rep);
//      saveCrf(crf, "-" + rep);
    }
    /*
    Sequence[] oldPaths = null;
    Sequence[] paths = null;

    do {

      oldPaths = null;
      paths = getViterbiPaths ()
    } while ((oldPaths == null) || !(pathsEqual (oldPaths, paths)) );
      */

    /*
      CorefIE corefIE = new CorefIE(crfInputFileOption.value,
              refNoMeta, clusterNoMeta, startTags, endTags, tagWeight, NumFields);

      corefIE.loadCRF();
      corefIE.canonicalInstanceMaker();
      corefIE.setClusterSizeLimit(clusterSizeLimit.value());

      int N = nBestChoice.value();//1;
      corefIE.setMethod(methodChoice.value);// 1, 2, 3

      System.out.println("N=" + N);
      System.out.println("Canonical citation creation method: " + methodChoice.value());
      System.out.println("Excluding singletons: " + excludingSingletons.value());
      System.out.println("clusterSizeLimit: " + clusterSizeLimit.value());

      corefIE.viterbiCRF_SingleFile(ttestFileOption.value, true, SEPERATOR[headOrRefOption.value()], N,
              excludingSingletons.value());

    */

    long timeEnd = System.currentTimeMillis();
    double timeElapse = (timeEnd - timeStart) / (1000.000);
    System.out.println("Time elapses " + timeElapse + " seconds for testing.");
  }

  private static InstanceList getNonTrivialTesting (Pipe pipe, InstanceList clusterList)
  {
    InstanceList goodClusters = new InstanceList (new Alphabet(), new Alphabet());
    for (InstanceList.Iterator it = clusterList.iterator(); it.hasNext();) {
      Instance instance = (Instance) it.next();
      InstanceList cluster = (InstanceList) instance.getData ();
      if (cluster.size() > 2) {
        goodClusters.add (instance);
      }
    }

    InstanceList goodInstances = new InstanceList (pipe);
    goodInstances.add (new ClusterListIterator(goodClusters));

    return goodInstances;
  }


  private static String CAPS = "[A-Zçêòéñì]";
  private static String ALPHA = "[A-Zçêòéñìa-z]";
  private static String ALPHANUM = "[A-Zçêòéñìa-z0-9]";
  private static String PUNT = "[,\\.;:?!()]";

  private static String bibtexLexDir = "/usr/col/tmp1/casutton/resources/fuchun/";

  private static Pipe makeBasePipe () throws EvalError, FileNotFoundException
  {

    Pipe bibtexLexiconsPipe = includeBibtexLexicons.value ?
            new SerialPipes(new Pipe[]{
              new LexiconMembership("BIBTEX_AUTHOR", new File(bibtexLexDir, "lexicon_author"), true),
              new LexiconMembership("BIBTEX_DATE", new File(bibtexLexDir, "lexicon_date"), true),
              new LexiconMembership("NOTES", new File(bibtexLexDir, "lexicon_note"), true),
              new LexiconMembership("DEGREE", new File(bibtexLexDir, "lexicon_degree"), true),
              new LexiconMembership("AFFILIATION", new File(bibtexLexDir, "lexicon_affiliation"), true),
            })
            :
            (Pipe) new Noop();

    Pipe pipe = new SerialPipes(new Pipe[]{
      new AddClusterPropertyPipe(),
      new Input2CharSequence(),
      new SGML2TokenSequence(new CharSequenceLexer(CharSequenceLexer.LEX_NONWHITESPACE_CLASSES), "O"),
//      new SGML2FieldsPipe (new CharSequenceLexer(CharSequenceLexer.LEX_NONWHITESPACE_CLASSES), "O",
//              refNoMeta, clusterNoMeta, startTags, endTags, tagWeight),
      new TokenText("W="),
      new RegexMatches("INITCAP", Pattern.compile(CAPS + ".*")),
      new RegexMatches("ALLDIGITS", Pattern.compile("[0-9]*")),
      new RegexMatches("ALLCAPS", Pattern.compile(CAPS + "+")),
      new RegexMatches("CONTAINSDIGITS", Pattern.compile(".*[0-9].*")),
      new RegexMatches("ALLDIGITS", Pattern.compile("[0-9]+")),
      new RegexMatches("PHONEORZIP", Pattern.compile("[0-9]+-[0-9]+")),
      new RegexMatches("CONTAINSDOTS", Pattern.compile("[^\\.]*\\..*")),
      new RegexMatches("CONTAINSDASH", Pattern.compile(ALPHANUM + "+-" + ALPHANUM + "*")),
      new RegexMatches("ACRO", Pattern.compile("[A-Z][A-Z\\.]*\\.[A-Z\\.]*")),
      new RegexMatches("LONELYINITIAL", Pattern.compile(CAPS + "\\.")),
      new RegexMatches("SINGLECHAR", Pattern.compile(ALPHA)),
      new RegexMatches("CAPLETTER", Pattern.compile(CAPS)),
      new RegexMatches("PUNC", Pattern.compile(PUNT)),
      new RegexMatches("URL", Pattern.compile("www\\..*|http://.*|ftp\\..*")),
      new RegexMatches("EMAIL", Pattern.compile("\\S+@\\S+|e-mail.*|email.*|Email.*")), //added

      new OffsetConjunctions(true, getOffsets()),

      bibtexLexiconsPipe,

      new Target2LabelSequence(),
//        new TokenSequence2FeatureVectorSequence(),
//        new PrintInputAndTarget(),
    });

    return pipe;
  }

  // Makes the part of the pipeline that depends on the segmentation.
  private static Pipe makeSegmentationsPipe (AllClusterSegmentation segs, boolean useInAny, boolean useNumOccurences,
                                             boolean useBogusFeatures)
  {
    return new SerialPipes(new Pipe[]{
      useInAny ? (Pipe) new WordAppearsInAnyClusterPipe(segs) : new Noop (),
      useNumOccurences ? (Pipe) new NumAppearancesInClusterPipe(segs) : new Noop (),
      useBogusFeatures ? (Pipe) new BogusClusterPipe (segs) : new Noop (),
      clusterFeatureMinimum.wasInvoked() ? (Pipe) new WordOftenAppearsAsPipe(segs) : new Noop (),
      useNegativeClusterFeatures.value ? (Pipe) new NegativeClusterFeaturePipe(segs) : new Noop (),
    });
  }

  private static void printClusterList (InstanceList clusters)
  {
    for (InstanceList.Iterator it = clusters.iterator(); it.hasNext();) {
      Instance instance = (Instance) it.next();
      InstanceList thisCluster = (InstanceList) instance.getData();
      System.out.println("\n\nCLUSTER *** " + instance.getName());
      for (InstanceList.Iterator it2 = thisCluster.iterator(); it2.hasNext();) {
        Instance inner = (Instance) it2.next();
        String targetString = "<null>";
        if (inner.getTarget() != null)
          targetString = inner.getTarget().toString();
        System.out.println("name: " + inner.getName() + "\ninput: " + inner.getData().toString() + "\ntarget: " + targetString);
      }
    }
  }

  /**
   * Reads an SGML file with instances already clustered.
   *
   * @param input SGML file with all citations in all clusters
   * @return Instance list where each instance's data is instance list for that cluster
   */
  // This could be done much neater, but I'm afraid it's not worth my time
  private static InstanceList getInstanceListClusters (InstanceList lst, File input)
  {
    // instance list parsed by another pipe to take in the reference_no and cluster_no
    Pipe pipeForMeta = new SerialPipes(new Pipe[]{
      new Input2CharSequence(),
      new SGML2FieldsPipe(refNoMeta, clusterNoMeta, startTags, endTags, tagWeight),
//      new NormalizationPipe(startTags, endTags),
    });

    InstanceList instancelist2 = new InstanceList(pipeForMeta);

    Reader reader;
    try {
      reader = new FileReader(input);
    } catch (Exception e) {
      throw new IllegalArgumentException("Can't read file " + input);
    }

    instancelist2.add(new LineGroupIterator2(reader, Pattern.compile(separator), true));

    // merge instancelist2 into instancelist
    if (lst.size() != instancelist2.size()) {
      throw new UnsupportedOperationException("size not equal");
    }

    int size = lst.size();
    for (int i = 0; i < size; i++) {
      Instance instance = lst.getInstance(i);
      Instance instance2 = instancelist2.getInstance(i);
      instance.setPropertyList(instance2.getPropertyList());
      instance.setName (instance2.getName ());
      lst.setInstance(i, instance);
    }

    //divide into different groups
    LinkedHashMap clustermap = new LinkedHashMap();
    for (int i = 0; i < size; i++) {
      Instance instance = lst.getInstance(i);
      Object clusterNo = instance.getProperty(clusterNoMeta);
      if (clustermap.containsKey(clusterNo)) {
        InstanceList templist = (InstanceList) clustermap.get(clusterNo);
        templist.add(instance);
        clustermap.put(clusterNo, templist);
      } else {
        InstanceList templist = new InstanceList();
        templist.add(instance);

        clustermap.put(clusterNo, templist);
      }
    }

    InstanceList clusters = new InstanceList(null);
    for (Iterator it = clustermap.entrySet().iterator(); it.hasNext();) {
      Map.Entry entry = (Map.Entry) it.next();
      String clusterNo = (String) entry.getKey();
      InstanceList data = (InstanceList) entry.getValue();
      Instance instance = new Instance(data, null, "Cluster " + clusterNo, null);
      clusters.add(instance);
    }

    return clusters;
  }

  public static class ClusterListIterator extends AbstractPipeInputIterator {
    // The PipeInputIterator interface

    private Iterator perClusterIterator;
    private Iterator withinClusterIterator;
    private InstanceList currentCluster;

    public ClusterListIterator (InstanceList instList)
    {
      perClusterIterator = instList.iterator();
      nextCluster();
    }

    public Instance nextInstance ()
    {
      while (!withinClusterIterator.hasNext()) {
        nextCluster();
      }
      Instance next = (Instance) withinClusterIterator.next();
      // Skunky use of source
      next.setSource(currentCluster);
//      System.out.println("CLI: Adding " + next);
//      System.out.println("  Input: " + next.getData());
//      System.out.println("  Output: " + next.getTarget());
//      System.out.println("  Cluster: " + currentCluster);
//      System.out.println("  Cluster data: " + currentCluster.size());
      return next;
    }

    private void nextCluster ()
    {
      Instance inst = (Instance) perClusterIterator.next();
      currentCluster = (InstanceList) inst.getData();
      withinClusterIterator = currentCluster.iterator();
    }

    public boolean hasNext ()
    {
      return (perClusterIterator.hasNext() || withinClusterIterator.hasNext());
    }

  }


  public static class WordAppearsInAnyClusterPipe extends Pipe {

//    private static Pattern EXCLUDE = Pattern.compile("..?.?");
    private static Pattern EXCLUDE = Pattern.compile("\\p{Punct}");

    // Maps cluster (i.e., InstanceList) ==> ClusterSegmentation
    private AllClusterSegmentation segmentation;

    WordAppearsInAnyClusterPipe (AllClusterSegmentation segmentation)
    {
      this.segmentation = segmentation;
    }

    public Instance pipe (Instance carrier)
    {
      TokenSequence toks = (TokenSequence) carrier.getData();
      InstanceList cluster = segmentation.getCluster(carrier);
      for (InstanceList.Iterator it = cluster.iterator(); it.hasNext();) {
        Instance other = (Instance) it.next();
        if (!other.getName().equals(carrier.getName())) {
          Segmentation otherSeg = segmentation.getSegmentation(other);
          for (int i = 0; i < toks.size(); i++) {
            Token token = toks.getToken(i);
            String text = token.getText();
            if (!EXCLUDE.matcher(text).matches()) {
              String[] fields = otherSeg.fieldNamesForWord(text);
              for (int j = 0; j < fields.length; j++) {
                String fieldName = fields[j];
                String featureName = ("TAGGED_AS_" + fieldName).intern();
                if (token.getFeatureValue(featureName) == 0.0) {
                  token.setFeatureValue(featureName, 1.0);
                }
              }
            }
          }
        }
      }
      return carrier;
    }

  }

  private static final int MIN_CLUSTER_SIZE = 3;

  public static class WordOftenAppearsAsPipe extends Pipe {

//    private static Pattern EXCLUDE = Pattern.compile("..?.?");
    private static Pattern EXCLUDE = Pattern.compile("\\p{Punct}");

    // Maps cluster (i.e., InstanceList) ==> ClusterSegmentation
    private AllClusterSegmentation segmentation;

    WordOftenAppearsAsPipe (AllClusterSegmentation segmentation)
    {
      this.segmentation = segmentation;
    }

    public Instance pipe (Instance carrier)
    {
      InstanceList cluster = segmentation.getCluster(carrier);
      TokenSequence toks = (TokenSequence) carrier.getData();

      // This will be unreliable if cutoff too small
      //  xxx should try this with cutoff of 2
      if (cluster.size() < MIN_CLUSTER_SIZE) return carrier;

      for (int i = 0; i < toks.size(); i++) {
        Token token = toks.getToken(i);
        String text = token.getText();
        if (!EXCLUDE.matcher(text).matches()) {
          // First get which fields were never tagged
          int[] tagCounts = new int [FIELD_NAMES.length];
          for (InstanceList.Iterator it = cluster.iterator(); it.hasNext();) {
            Instance other = (Instance) it.next();
            if (!other.getName().equals(carrier.getName())) {
              Segmentation otherSeg = segmentation.getSegmentation(other);
              int[] fieldIds = otherSeg.fieldIdsForWord(text);
              for (int j = 0; j < fieldIds.length; j++) {
                int id = fieldIds[j];
                tagCounts [id]++;
              }
            }
          }
          // Now add features for untagged words
          for (int j = 0; j < tagCounts.length; j++) {
            if (tagCounts[j] >= clusterFeatureMinimum.value) {
                String featureName = ("OFTEN_TAGGED_AS_" + FIELD_NAMES[j]).intern();
                if (token.getFeatureValue(featureName) == 0.0) {
                  token.setFeatureValue(featureName, 1.0);
                }
            }
          }
        }
      }
      return carrier;
    }

  }


  public static class NegativeClusterFeaturePipe extends Pipe {

//    private static Pattern EXCLUDE = Pattern.compile("..?.?");
    private static Pattern EXCLUDE = Pattern.compile("\\p{Punct}");

    // Maps cluster (i.e., InstanceList) ==> ClusterSegmentation
    private AllClusterSegmentation segmentation;

    NegativeClusterFeaturePipe (AllClusterSegmentation segmentation)
    {
      this.segmentation = segmentation;
    }

    public Instance pipe (Instance carrier)
    {
      InstanceList cluster = segmentation.getCluster(carrier);
      TokenSequence toks = (TokenSequence) carrier.getData();

      // This will be unreliable if cutoff too small
      //  xxx should try this with cutoff of 2
      if (cluster.size() < MIN_CLUSTER_SIZE) return carrier;

      for (int i = 0; i < toks.size(); i++) {
        Token token = toks.getToken(i);
        String text = token.getText();
        if (!EXCLUDE.matcher(text).matches()) {
          // First get which fields were never tagged
          boolean[] wasTaggedAs = new boolean [FIELD_NAMES.length];
          for (InstanceList.Iterator it = cluster.iterator(); it.hasNext();) {
            Instance other = (Instance) it.next();
            if (!other.getName().equals(carrier.getName())) {
              Segmentation otherSeg = segmentation.getSegmentation(other);
              int[] fieldIds = otherSeg.fieldIdsForWord(text);
              for (int j = 0; j < fieldIds.length; j++) {
                int id = fieldIds[j];
                wasTaggedAs[id] = true;
              }
            }
          }
          // Now add features for untagged words
          for (int j = 0; j < wasTaggedAs.length; j++) {
            boolean wasGood = wasTaggedAs[j];
            if (!wasGood) {
                String featureName = ("NEVER_TAGGED_AS_" + FIELD_NAMES[j]).intern();
                if (token.getFeatureValue(featureName) == 0.0) {
                  token.setFeatureValue(featureName, 1.0);
                }
            }
          }
        }
      }
      return carrier;
    }

  }


   public static class NumAppearancesInClusterPipe extends Pipe {

    private static Pattern EXCLUDE = Pattern.compile("\\p{Punct}");

    // Maps cluster (i.e., InstanceList) ==> ClusterSegmentation
    private AllClusterSegmentation segmentation;

    NumAppearancesInClusterPipe (AllClusterSegmentation segmentation)
    {
      this.segmentation = segmentation;
    }

    public Instance pipe (Instance carrier)
    {
      TokenSequence toks = (TokenSequence) carrier.getData();
      InstanceList cluster = segmentation.getCluster(carrier);
      for (InstanceList.Iterator it = cluster.iterator(); it.hasNext();) {
        Instance other = (Instance) it.next();
        if (!other.getName().equals(carrier.getName())) {
          Segmentation otherSeg = segmentation.getSegmentation(other);
          for (int i = 0; i < toks.size(); i++) {
            Token token = toks.getToken(i);
            String text = token.getText();
            if (!EXCLUDE.matcher(text).matches()) {
              String[] fields = otherSeg.fieldNamesForWord(text);
              for (int j = 0; j < fields.length; j++) {
                String fieldName = fields[j];
                String featureName = ("TAGGED_AS_" + fieldName).intern();
                token.setFeatureValue(featureName, 1.0);
              }
            }
          }
        }
      }
      return carrier;
    }

  }


  public static class BogusClusterPipe extends Pipe {

    private static Pattern EXCLUDE = Pattern.compile("\\p{Punct}");

    // Maps cluster (i.e., InstanceList) ==> ClusterSegmentation
    private AllClusterSegmentation segmentation;

    BogusClusterPipe (AllClusterSegmentation segmentation)
    {
      this.segmentation = segmentation;
    }

    public Instance pipe (Instance carrier)
    {
      TokenSequence toks = (TokenSequence) carrier.getData();
      InstanceList cluster = segmentation.getCluster(carrier);
      for (InstanceList.Iterator it = cluster.iterator(); it.hasNext();) {
        Instance other = (Instance) it.next();
        String prefix;
        if (other.getName().equals(carrier.getName())) {
          prefix = "I_AM_TAGGED_AS_";
        } else {
          prefix = "TAGGED_AS_";
        }
        Segmentation otherSeg = segmentation.getSegmentation(other);
        for (int i = 0; i < toks.size(); i++) {
          Token token = toks.getToken(i);
          String text = token.getText();
          if (!EXCLUDE.matcher(text).matches()) {
            String[] fields = otherSeg.fieldNamesForWord(text);
            for (int j = 0; j < fields.length; j++) {
              String fieldName = fields[j];
              String featureName = (prefix + fieldName).intern();
              if (token.getFeatureValue(featureName) == 0.0) {
                token.setFeatureValue(featureName, 1.0);
              }
            }
          }
        }
      }
      return carrier;
    }

  }

  public static class AllClusterSegmentation {

    // Maps instance name ==> Segmentation
    Map inst2segmentation = new HashMap();

    // Maps instance name ==> InstanceList
    Map inst2cluster = new HashMap();

    /**
     * make an AllClusterSegmentation from the true segmentation
     * of an instancelist
     *
     * @param clusterlist
     */
    public AllClusterSegmentation (InstanceList clusterlist, Pipe pipe)
    {
      InstanceList instances = new InstanceList(pipe);
      instances.add(new ClusterListIterator(clusterlist));
      for (InstanceList.Iterator it = instances.iterator(); it.hasNext();) {
        Instance instance = (Instance) it.next();
        TokenSequence input = (TokenSequence) instance.getData();
        Sequence output = (Sequence) instance.getTarget();
        Segmentation seg = new Segmentation(input, output);
        inst2segmentation.put(instance.getName(), seg);
//        System.out.println("Instance " + instance.getName() + "  source: " + instance.getSource());
        inst2cluster.put(instance.getName(), (InstanceList) instance.getProperty("CLUSTER"));
      }
    }

    public InstanceList getCluster (Instance inst)
    {
      return (InstanceList) inst2cluster.get(inst.getName());
    }

    public Segmentation getSegmentation (Instance inst)
    {
      return (Segmentation) inst2segmentation.get(inst.getName());
    }

    public void print ()
    {
      for (Iterator it = inst2cluster.keySet().iterator(); it.hasNext();) {
        Object o = it.next();
        Segmentation seg = (Segmentation) inst2segmentation.get(o);
        InstanceList cluster = (InstanceList) inst2cluster.get(o);
        System.out.println("Instance " + o + "\n  " + seg + "\n  " + cluster);
      }
    }
  }

  public static class Segmentation {

    private Sequence output;
    private TokenSequence input;

    public Segmentation (TokenSequence input, Sequence output)
    {
      this.input = input;
      this.output = output;
    }

    public String[] getFieldNames ()
    {
      return FIELD_NAMES;
    }

    public String[] fieldNamesForWord (String word)
    {
      HashSet allFields = new HashSet();
      assert input.size() == output.size();

      for (int t = 0; t < input.size(); t++) {
        if (input.getToken(t).getText().equals(word)) {
          allFields.add(output.get(t).toString());
        }
      }

      return (String[]) allFields.toArray(new String[allFields.size()]);
    }

    public String toString ()
    {
      return "SEGMENTATION\n  input:\n" + input + "\n  output:\n" + output;
    }

    static String background = "O";

    public int[] fieldIdsForWord (String text)
    {
      TIntArrayList idsFound = new TIntArrayList();
      String[] namesFound = fieldNamesForWord(text);
      List names = Arrays.asList(FIELD_NAMES);
      for (int i = 0; i < namesFound.length; i++) {
        String theName = namesFound[i];
        if (!theName.equals(background)) {
          int id = names.indexOf(theName);
          if (id == -1) {
            System.err.println("ERROR: Couldn't find " + theName + "\n");
          }
          idsFound.add(id);
        }
      }
      return idsFound.toNativeArray();
    }

  }


  /**
   * On each instance, sets a property called CLUSTER that is
   * the InstanceList which contains the other instances in
   * this guy's cluster.
   * <p/>
   * We get the cluster out of the source, because that's the only
   * way that ClusterListIterator can stick it in that won't get
   * mangled by the {@link InstanceList#add(edu.umass.cs.mallet.base.pipe.iterator.PipeInputIterator)}
   * method.  But we use this pipe to make it a property, because SGML2TokenSequence will blow away
   * the source.  How grotty is that?
   */
  private static class AddClusterPropertyPipe extends Pipe {
    public Instance pipe (Instance carrier)
    {
      carrier.setProperty("CLUSTER", carrier.getSource());
      return carrier;
    }

  }
}
