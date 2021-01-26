/*
 * LingPipe v. 4.1.0
 * Copyright (C) 2003-2011 Alias-i
 *
 * This program is licensed under the Alias-i Royalty Free License
 * Version 1 WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the Alias-i
 * Royalty Free License Version 1 for more details.
 *
 * You should have received a copy of the Alias-i Royalty Free License
 * Version 1 along with this program; if not, visit
 * http://alias-i.com/lingpipe/licenses/lingpipe-license-1.txt or contact
 * Alias-i, Inc. at 181 North 11th Street, Suite 401, Brooklyn, NY 11211,
 * +1 (718) 290-9170.
 */

package com.aliasi.tag;

import com.aliasi.classify.BaseClassifier;
import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;

import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ObjectHandler;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Compilable;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A {@code ClassifierTagger} implements the first-best tagger
 * interface with a classifier that operates left-to-right
 * over the tokens, classifying one token at a time.  
 *
 * <p>The current state of the tagging up to the current
 * position being tagged is represented using the static
 * nested class {@code ClassifierTagger.State}.  The state
 * contains all of the input tokens, an integer input position,
 * and the tags for all of the tokens earlier in the sequence.
 *
 * <h3>Advantages of the Classifier Tagger</h3>
 * 
 * <p>An advantage of the classifier tagger over a more
 * complex tagger such as conditional random fields (CRF) is that
 * it is able to use longer-distance information about tags
 * that have already been assigned.  Another advantage is
 * that the classifier tagger will use much less memory and
 * tag much more quickly.  Depending on the base classifier
 * used, a classifier tagger will likely be more efficient
 * to train in terms of time and space than a CRF.  
 *
 * <h3>Implementation</h3>
 * 
 * <p>The implementation of the decoder is the obvious one.
 * It walks along the input string, constructing a state
 * of the position so far, then feeds the state into the
 * classifier, the output classification of which determines
 * the next state.
 *
 * <h3>Training the Underlying Classifier</h3>
 *
 * The static utility method {@code toClassifierCorpus()} converts
 * a tagging corpus to a classifier corpus, which may then be
 * used to train a classifier.  The resulting trained classifier
 * may then be plugged into a classifier tagger, which may be
 * serialized or compiled, depending on the serializability and
 * compilability of the underlying classifier.
 *
 * <h3>Serialization and Compilation</h3>
 *
 * A classifier tagger will be serializable if the underlying
 * classifier is serializable.  The deserialized classifier tagger
 * will be an instance of {@code ClassifierTagger<E>} with the
 * deserialized classifier as its base classifier.
 *
 * <p>A classifier tagger is compilable if the underlying
 * classifier is compilable.  The deserialized classifier tagger
 * will be an instance of {@code ClassifierTagger<E>} with the
 * deserialized compiled classifier as its base classifier.
 *
 * <h3>Thread Safety</h3>
 *
 * A classifier tagger is thread safe if the underlying classifier
 * is thread safe.  
 *
 * @author Bob Carpenter
 * @version 4.1.1
 * @since LingPipe 4.1.1
 * @param <E> Type of the tokens being tagged.
 */
public class ClassifierTagger<E> 
    implements Tagger<E>,
               Compilable,
               Serializable {

    static final long serialVersionUID = -3999881235621306119L;

    private final BaseClassifier<State<E>> mClassifier;

    /**
     * Construct a classifier tagger based on the specified
     * base classifier over states.
     *
     * @param classifier Base classifier over tagging partial results.
     */
    public ClassifierTagger(BaseClassifier<State<E>> classifier) {
        mClassifier = classifier;
    }

    /**
     * Returns the underlying classifier for this classifier tagger.
     *
     * @return Underlying classifier.
     */
    public BaseClassifier<State<E>> classifier() {
        return mClassifier;
    }

    /**
     * Return the tagging for the specified list of tokens.
     *
     * @param tokens Input sequence of tokens.
     * @return Tagging for the specified tokens.
     */
    public Tagging<E> tag(List<E> tokens) {
        List<String> tags = new ArrayList<String>(tokens.size());
        for (int i = 0; i < tokens.size(); ++i) {
            State<E> state = new State<E>(tokens,tags,i);
            String tag = mClassifier.classify(state).bestCategory();
            tags.add(tag);
        }
        return new Tagging<E>(tokens,tags);
    }

    /**
     * Compile this classifier tagger to the specified
     * object output stream.
     *
     * @param out Object output stream to which this classifier tagger
     * is compiled.
     * @throws NotSerializableException If the base classifier is
     * not compilable.
     * @throws IOException If there is an underlying error during
     * I/O.
     */
    public void compileTo(ObjectOutput out) throws IOException {
        if (!(mClassifier instanceof Compilable)) {
            String msg = "Base classifier is not compilable."
                + " Found base classifier class=" + mClassifier.getClass();
            throw new NotSerializableException(msg);
        }
        out.writeObject(new Compiler<E>(this));
    }

    Object writeReplace() {
        return new Serializer<E>(this);
    }

    /**
     * Return a corpus consisting of classified tagger states derived
     * from the specified corpus of taggings.  The returned corpus
     * will respect the same test versus train split as the underlying
     * corpus.
     *
     * <p>The resulting corpus is implemented as a lightweight wrapper
     * around the tagging corpus.  This makes it slightly slower than
     * explicitly converting the corpus, but is much smaller in memory.
     *
     * <p>The returned corpus will be serializable if the specified
     * corpus is serializable.
     *
     * @param taggingCorpus Corpus of taggings.
     * @return Corpus of classified states.
     * @param <F> Type of the tokens being tagged.
     */
    public static <F> Corpus<ObjectHandler<Classified<State<F>>>>
        toClassifiedCorpus(Corpus<ObjectHandler<Tagging<F>>> taggingCorpus) {
        
        return new StateCorpus<F>(taggingCorpus);
    }

    /**
     * A {@code ClassifierTagger.State} represents the full list
     * of input tokens and a list of the tags assigned so far.
     *
     * @author Bob Carpenter
     * @version 4.1.1
     * @since LingPipe 4.1.1
     */
    public static class State<F> {
        private final List<F> mTokens;
        private final List<String> mTags;
        private final int mPosition;
        State(List<F> tokens,
              List<String> tags,
              int position) {
            mTokens = Collections.unmodifiableList(tokens);
            mTags = Collections.unmodifiableList(tags);
            mPosition = position;
        }
        /**
         * Returns the number of tokens in the input.
         * This is the size of the returned tokens list.
         *
         * @return Number of tokens in the input.
         */
        public int numTokens() {
            return mTokens.size();
        }
        /**
         * Returns the current position in the input.
         * Tags up to this point have already been assigned,
         * so it is one greater than the size of the list
         * of tags.
         *
         * @return The current input position.
         */
        public int position() {
            return mPosition;
        }
        /**
         * Return an unmodifiable view of the underlying tokens.
         *
         * @return View of the underlying tokens.
         */
        public List<F> tokens() {
            return mTokens;
        }
        /**
         * Return an unmodifiable view of the underlying tags.
         *
         * @return View of the underlying tags.
         */
        public List<String> tags() {
            return mTags;
        }
        /**
         * Return the token at the specified position.
         *
         * @param pos Position of token.
         * @return Token at the specified position.
         * @throws IndexOutOfBoundsException if the position is out of range
         * (i.e., less than zero or greater than or equal to the number of
         * tokens).
         */
        public F token(int pos) {
            return mTokens.get(pos);
        }
        /**
         * Return the tag at the specified position.
         *
         * @param pos Position of tag.
         * @return Token at the specified position.
         * @throws IndexOutOfBoundsException if the position is out of
         * range for the list of tags (i.e., less than 0 or greater
         * than the position minus one).
         */
        public String tag(int pos) {
            return mTags.get(pos);
        }
    }

    static class Serializer<F> extends AbstractExternalizable {
        static final long serialVersionUID = -6902579802924987108L;
        private final ClassifierTagger<F> mTagger;
        public Serializer() { 
            this(null); 
        }
        public Serializer(ClassifierTagger<F> tagger) {
            mTagger = tagger;
        }
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(mTagger.mClassifier);
        }
        public Object read(ObjectInput in)
            throws IOException, ClassNotFoundException {

            @SuppressWarnings("unchecked")
            BaseClassifier<State<F>> classifier
                = (BaseClassifier<State<F>>) in.readObject();
            return new ClassifierTagger<F>(classifier);
        }
    }

    static class Compiler<F> extends AbstractExternalizable {
        static final long serialVersionUID = -1204364907641769096L;
        private final ClassifierTagger<F> mTagger;
        public Compiler() {
            this(null);
        }
        public Compiler(ClassifierTagger<F> tagger) {
            mTagger = tagger;
        }
        public void writeExternal(ObjectOutput out)
            throws IOException {
            
            ((Compilable) mTagger.mClassifier).compileTo(out);

        }
        public Object read(ObjectInput in)
            throws IOException, ClassNotFoundException {
            
            @SuppressWarnings("unchecked")
            BaseClassifier<State<F>> classifier
                = (BaseClassifier<State<F>>) in.readObject();
            return new ClassifierTagger<F>(classifier);
        }
    }

    static class StateCorpus<F> 
        extends Corpus<ObjectHandler<Classified<State<F>>>>
        implements Serializable {

        static final long serialVersionUID = -8545927933382605392L;

        private final Corpus<ObjectHandler<Tagging<F>>> mTaggingCorpus;
        public StateCorpus(Corpus<ObjectHandler<Tagging<F>>> taggingCorpus) {
            mTaggingCorpus = taggingCorpus;
        }
        public void visitTrain(ObjectHandler<Classified<State<F>>> handler)
            throws IOException {
            mTaggingCorpus.visitTrain(new HandlerAdapter<F>(handler));
        }
        public void visitTest(ObjectHandler<Classified<State<F>>> handler) 
            throws IOException {
            mTaggingCorpus.visitTest(new HandlerAdapter<F>(handler));
            
        }
        Object writeReplace() {
            return new ScSerializer<F>(this);
        }
    }

    
    static class HandlerAdapter<F> implements ObjectHandler<Tagging<F>> {
        final ObjectHandler<Classified<State<F>>> mHandler;
        HandlerAdapter(ObjectHandler<Classified<State<F>>> handler) {
            mHandler = handler;
        }
        public void handle(Tagging<F> tagging) {
            List<F> tokens = tagging.tokens();
            List<String> tags = tagging.tags();
            for (int i = 0; i < tagging.size(); ++i) {
                State<F> state = new State<F>(tokens,tags.subList(0,i),i);
                Classification c = new Classification(tags.get(i));
                Classified<State<F>> classified = new Classified<State<F>>(state,c);
                mHandler.handle(classified);
            }
        }
    }

    static class ScSerializer<F> extends AbstractExternalizable {
        static final long serialVersionUID = -4849217701670674035L;
        final StateCorpus<F> mCorpus;
        public ScSerializer() {
            this(null);
        }
        ScSerializer(StateCorpus<F> corpus) {
            mCorpus = corpus;
        }
        public void writeExternal(ObjectOutput out) 
            throws IOException {
            out.writeObject(mCorpus.mTaggingCorpus);
        }
        public Object read(ObjectInput in) 
            throws IOException, ClassNotFoundException {
            @SuppressWarnings("unchecked")
                Corpus<ObjectHandler<Tagging<F>>> taggingCorpus
                = (Corpus<ObjectHandler<Tagging<F>>>)
                in.readObject();
            return new StateCorpus<F>(taggingCorpus);
        }
    }



}
               