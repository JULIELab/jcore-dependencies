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


package com.aliasi.dict;

import com.aliasi.chunk.*;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Scored;
import com.aliasi.util.ScoredObject;
import com.aliasi.util.Strings;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.io.NotSerializableException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * An exact dictionary chunker extracts chunks based on exact matches
 * of tokenized dictionary entries.
 *
 * <p>All dictionary entry categories are converted to strings from
 * generic objects using {@link Object#toString()}.
 *
 * <p>An exact dicitonary chunker may be configured either to
 * extract all matching chunks, or to restrict the results to a
 * consistent set of non-overlapping chunks.  These non-overlapping
 * chunks are taken to be the left-most, longest-matching,
 * highest-scoring, or alphabetically preceding in type according
 * to the following definitions. A chunk with span
 * <code>(start1,end1)</code> overlaps a chunk with span
 * <code>(start2,end2)</code> if and only if either end
 * points of the second chunk lie within the first chunk:
 * <ul>
 * <li> <code>start1 <= start2 < end1</code>, or
 * <li> <code>start1 < end2 <= end1</code>.
 * </ul>
 *
 * For instance, <code>(0,1)</code> and <code>(1,3)</code> do
 * not overlap, but
 * <code>(0,1)</code> overlaps <code>(0,2)</code>,
 * <code>(1,2)</code> overlaps <code>(0,2)</code>, and
 * <code>(1,7)</code> overlaps <code>(2,3)</code>.
 *
 * <p>A chunk <code>chunk1=(start1,end1):type1@score1</code> dominates
 * another chunk <code>chunk2=(start2,end2):type2@score2</code> if and
 * only if the chunks overlap and:
 *
 * <ul>
 * <li> <code>start1 &lt; start2</code> (leftmost), or
 * <li> <code>start1 == start2</code>
 *      and <code>end1 &gt; end2</code> (longest), or
 * <li> <code>start1 == start2</code>, <code>end1 == end2</code>
 *      and <code>score1 &gt; score2</code> (highest scoring), or
 * <li> <code>start1 == start2</code>, <code>end1 == end2</code>,
 *      <code>score1 == score2</code> and
 *      <code>type1 &lt; type2</code> (alphabetical).
 * </ul>
 *
 * To construct a non-overlapping result, all dominated chunks are
 * removed.
 *
 * <p>If the chunker is specified to be case sensitive, the exact
 * dictionary entries must match.  If it is not case sensitive, all
 * matching will be done after applying string normalization using
 * {@link java.lang.String#toLowerCase()}.
 *
 * <p>Matching ignores whitespace.  The tokenizer factory should
 * provide accurate start and end token positions as these will be
 * used to determine the chunks.
 *
 * <p>Chunking is thread safe, and may be run concurrently.  Changing
 * the return-all-matches flag with {@link
 * #setReturnAllMatches(boolean)} should not be called while chunking
 * is running, as it may affect the behavior of the running example
 * with respect to whether it returns all chunkings.  Once
 * constructed, the tokenizer's behavior should not change.
 *
 * <p><i>Implementation Note:</i> This class is implemented using the
 * Aho-Corasick algorithm, a generalization of the Knuth-Morris-Pratt
 * string-matching algorithm to sets of strings.  Aho-Corasick is
 * linear in the number of tokens in the input plus the number of
 * output chunks.  Memory requirements are only an array of integers
 * as long as the longest phrase (a circular queue for holding start
 * points of potential chunks) and the memory required by the chunking
 * implementation for the result (which may be as large as quadratic
 * in the size of the input, or may be very small if there are not
 * many matches).  Compilation of the Aho-Corasick tree is done in the
 * constructor and is linear in number of dictionary entries with a
 * constant factor as high as the maximum phrase length; this can be
 * improved to a constant factor using suffix-tree like speedups, but
 * it didn't seem worth the complexity here when the dictionaries
 * would be long-lived.
 *
 * <h3>Serialization</h3>
 *
 * <p>An exact dictionary chunker can be serialized if its
 * tokenizer factory is serializable.  The deserialized version
 * of the exact dictionary chunker is an instance of this class
 * which is behaviorally identical to the chunker that was
 * serialized.
 *
 * <ul>
 *
 * <li>Aho, Alfred V. and Margaret J. Corasick. 1975. Efficient string
 * matching: an aid to bibliographic search, <i>CACM</i>,
 * <b>18</b>(6):333-340.
 *
 * <li><a href="http://en.wikipedia.org/wiki/Aho-Corasick_algorithm"
 *      >Wikipedia: Aho-Corasick Algorithm</a>
 * <br /><small>[Entry sketchy at the time of writing, but good links.]</small>
 * <li>Gusfield, Dan. 1997. <i>Algorithms on Strings, Trees and Sequences.</i>
 * Cambridge University Press.
 * <br /><small>[Best book on string algorithms out there.  Great explanation
 * of Aho-Corasick.</small>]
 * </ul>
 *
 * @author Bob Carpenter
 * @version 4.0.2
 * @since   LingPipe2.3.1
 */
public class ExactDictionaryChunker 
    implements Chunker, Serializable {

    static final long serialVersionUID = -4380886361370971305L;

    final TrieNode mTrieRootNode;
    final TokenizerFactory mTokenizerFactory;
    final boolean mCaseSensitive;
    boolean mReturnAllMatches;
    int mMaxPhraseLength = 0;

    

    /**
     * Construct an exact dictionary chunker from the specified
     * dictionary and tokenizer factory which is case sensitive and
     * returns all matches.  See the class documentation above for
     * more information on chunking and requirements for tokenizer
     * factories.
     *
     * <p>After construction, this class does not use the dictionary
     * and will not be sensitive to changes in the underlying
     * dictionary.
     *
     * @param dict Dictionary forming the basis of the chunker.
     * @param factory Tokenizer factory underlying chunker.
     */
    public ExactDictionaryChunker(Dictionary<String> dict, 
                                  TokenizerFactory factory) {
        this(dict,factory,true,true);
    }

    /**
     * Construct an exact dictionary chunker from the specified
     * dictionary and tokenizer factory, returning all matches or not
     * as specified.  See the class documentation above for more
     * information on chunking.
     *
     * <p>After construction, this class does not use the dictionary
     * and will not be sensitive to changes in the underlying
     * dictionary.
     *
     * <p>Case sensitivity is defined using {@link
     * java.util.Locale#ENGLISH}.  For other languages, underlying case
     * sensitivity must be defined externally by passing in
     * case-normalized text.
     *
     * @param dict Dictionary forming the basis of the chunker.
     * @param factory Tokenizer factory underlying chunker.
     * @param returnAllMatches <code>true</code> if chunker should return
     * all matches.
     * @param caseSensitive <code>true</code> if chunker is case
     * sensitive.
     */
    public ExactDictionaryChunker(Dictionary<String> dict,
                                  TokenizerFactory factory,
                                  boolean returnAllMatches,
                                  boolean caseSensitive) {
        mTokenizerFactory = factory;
        mReturnAllMatches = returnAllMatches;
        mCaseSensitive = caseSensitive;
        mTrieRootNode = compileTrie(dict);
    }


    // called by Serializer for deserialization
    private ExactDictionaryChunker(TrieNode rootNode,
                                   TokenizerFactory tokenizerFactory,
                                   boolean caseSensitive,
                                   boolean returnAllMatches,
                                   int maxPhraseLength) {
        mTrieRootNode = rootNode;
        mTokenizerFactory = tokenizerFactory;
        mCaseSensitive = caseSensitive;
        mReturnAllMatches = returnAllMatches;
        mMaxPhraseLength = maxPhraseLength;
    }


    /**
     * Returns the tokenizer factory underlying this chunker.  Once
     * set in the constructor, the tokenizer factory may not be
     * changed.  If the tokenizer factory allows dynamic
     * reconfiguration, it should not be reconfigured or inconsistent
     * results may be returned.
     *
     * @return The tokenizer factory for this chunker.
     */
    public TokenizerFactory tokenizerFactory() {
        return mTokenizerFactory;
    }

    /**
     * Returns <code>true</code> if this dictionary chunker is
     * case sensitive.  Case sensitivity must be defined at
     * construction time and may not be reset.
     *
     * @return Whether this chunker is case sensitive.
     */
    public boolean caseSensitive() {
        return mCaseSensitive;
    }

    /**
     * Returns <code>true</code> if this chunker returns all matches.
     *
     * @return Whether this chunker returns all matches.
     */
    public boolean returnAllMatches() {
        return mReturnAllMatches;
    }

    /**
     * Set whether to return all matches to the specified condition.
     *
     * <p>Note that setting this while running a chunking in another
     * thread may affect that chunking.
     *
     * @param returnAllMatches <code>true</code> if all matches should
     * be returned.
     */
    public void setReturnAllMatches(boolean returnAllMatches) {
        mReturnAllMatches = returnAllMatches;
    }

    /**
     * Returns the chunking for the specified character sequence.
     * Whether all matching chunks are returned depends on whether
     * this chunker is configured to return all matches or not.
     * See the class documentation above for more information.
     *
     * @param cSeq Character sequence to chunk.
     * @return The chunking for the specified character sequence.
     */
    public Chunking chunk(CharSequence cSeq) {
        char[] cs = Strings.toCharArray(cSeq);
        return chunk(cs,0,cs.length);
    }

    // awkward conversion from start/end to start/length
    final Tokenizer tokenizer(char[] cs, int start, int end) {
        TokenizerFactory factory
            = mCaseSensitive
            ? mTokenizerFactory
            : new LowerCaseTokenizerFactory(mTokenizerFactory);
        return factory.tokenizer(cs,start,end-start);
    }

    /**
     * Returns the chunking for the specified character slice.
     * Whether all matching chunks are returned depends on whether
     * this chunker is configured to return all matches or not.
     * See the class documentation above for more information.
     *
     * @param cs Underlying array of characters.
     * @param start Index of first character in slice.
     * @param end One past the index of the last character in the slice.
     * @return The chunking for the specified character slice.
     */
    public Chunking chunk(char[] cs, int start, int end) {
        ChunkingImpl chunking = new ChunkingImpl(cs,start,end);
        if (mMaxPhraseLength == 0)
            return chunking; // no dict entries
        CircularQueueInt queue = new CircularQueueInt(mMaxPhraseLength);
        Tokenizer tokenizer = tokenizer(cs,start,end); // adjusts end to length
        TrieNode node = mTrieRootNode;
        String token;
        while ((token = tokenizer.nextToken()) != null) {
            int tokenStartPos = tokenizer.lastTokenStartPosition();
            int tokenEndPos = tokenizer.lastTokenEndPosition();
            queue.enqueue(tokenStartPos);
            while (true) {
                TrieNode daughterNode = node.getDaughter(token);
                if (daughterNode != null) {
                    node = daughterNode;
                    break;
                }
                if (node.mSuffixNode == null) {
                    node = mTrieRootNode.getDaughter(token);
                    if (node == null)
                        node = mTrieRootNode;
                    break;
                }
                node = node.mSuffixNode;
            }
            emit(node,queue,tokenEndPos,chunking);
            for (TrieNode suffixNode = node.mSuffixNodeWithCategory;
                 suffixNode != null;
                 suffixNode = suffixNode.mSuffixNodeWithCategory) {
                emit(suffixNode,queue,tokenEndPos,chunking);
            }
        }
        return mReturnAllMatches ? chunking : restrictToLongest(chunking);
    }

    /**
     * Returns a string-based representation of this chunker.
     * The string includes the tokenizer factory's class name, whether
     * or not it returns all matches, whether or not it is case
     * sensitive, and also includes the entire trie underlying the
     * matcher, which is quite large for large dictionaries (multiple
     * lines per dictionary entry).
     *
     * @return String-based representation of this chunker.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ExactDictionaryChunker\n");
        sb.append("Tokenizer factory=" + mTokenizerFactory.getClass() + "\n");
        sb.append("(toString) mCaseSensitive=" + mCaseSensitive + "\n");
        sb.append("Return all matches=" + mReturnAllMatches + "\n\n");
        mTrieRootNode.toString(sb,0);
        return sb.toString();
    }

    Object writeReplace() {
        return new Serializer(this);
    }

    void emit(TrieNode node, CircularQueueInt queue, int end,
              ChunkingImpl chunking) {
        ScoredCat[] scoredCats = node.mCategories;
        for (int i = 0; i < scoredCats.length; ++i) {
            int start = queue.get(node.depth());
            String type = scoredCats[i].mCat;
            double score = scoredCats[i].mScore;
            Chunk chunk = ChunkFactory.createChunk(start,end,type,score);
            chunking.add(chunk);
        }
    }

    final TrieNode compileTrie(Dictionary<String> dict) {
        TrieNode rootNode = new TrieNode(0);
        for (DictionaryEntry<String> entry : dict) {
            String phrase = entry.phrase();
            char[] cs = phrase.toCharArray();
            Tokenizer tokenizer = tokenizer(cs,0,cs.length);
            int length = rootNode.add(tokenizer,entry);
            if (length > mMaxPhraseLength)
                mMaxPhraseLength = length;
        }
        computeSuffixes(rootNode,rootNode,new String[mMaxPhraseLength],0);
        return rootNode;
    }

    final static void computeSuffixes(TrieNode node, TrieNode rootNode,
                         String[] tokens, int length) {
        for (int i = 1; i < length; ++i) {
            TrieNode suffixNode = rootNode.getDaughter(tokens,i,length);
            if (suffixNode == null) continue;
            node.mSuffixNode = suffixNode;
            break;
        }

        // second loop could start where first left off
        for (int i = 1; i < length; ++i) {
            TrieNode suffixNode = rootNode.getDaughter(tokens,i,length);
            if (suffixNode == null) continue;
            if (suffixNode.mCategories.length == 0) continue;
            node.mSuffixNodeWithCategory = suffixNode;
            break;
        }
        if (node.mDaughterMap == null) return;
        for (Map.Entry<String,TrieNode> entry : node.mDaughterMap.entrySet()) {
            tokens[length] = entry.getKey().toString();
            TrieNode dtrNode = entry.getValue();
            computeSuffixes(dtrNode,rootNode,tokens,length+1);
        }
    }

    static Chunking restrictToLongest(Chunking chunking) {
        ChunkingImpl result = new ChunkingImpl(chunking.charSequence());
        Set<Chunk> chunkSet = chunking.chunkSet();
        if (chunkSet.size() == 0) return chunking;
        Chunk[] chunks = chunkSet.<Chunk>toArray(EMPTY_CHUNK_ARRAY);
        Arrays.<Chunk>sort(chunks,Chunk.LONGEST_MATCH_ORDER_COMPARATOR);
        int lastEnd = -1;
        for (int i = 0; i < chunks.length; ++i) {
            if (chunks[i].start() >= lastEnd) {
                result.add(chunks[i]);
                lastEnd = chunks[i].end();
            }
        }
        return result;
    }

    static class ScoredCat implements Scored {
        String mCat;
        double mScore;
        ScoredCat(String cat, double score) {
            mCat = cat;
            mScore = score;
        }
        public double score() {
            return mScore;
        }
        @Override
        public String toString() {
            return mCat + ":" + mScore;
        }
    }

    static final ScoredCat[] EMPTY_SCORED_CATS = new ScoredCat[0];

    static class TrieNode implements Serializable {

        static final long serialVersionUID = -6412834366677374806L;

        int mDepth;

        Map<String,TrieNode> mDaughterMap = null;

        ScoredCat[] mCategories = EMPTY_SCORED_CATS;

        TrieNode mSuffixNode;
        TrieNode mSuffixNodeWithCategory;

        TrieNode(int depth) {
            mDepth = depth;
        }

        public int depth() {
            return mDepth;
        }

        public void addEntry(DictionaryEntry<String> entry) {
            // should just do this with a merge; tighter with parallel arrays
            ScoredCat[] newCats = new ScoredCat[mCategories.length+1];
            System.arraycopy(mCategories,0,newCats,0,mCategories.length);
            newCats[newCats.length-1]
                = new ScoredCat(entry.category().toString(),entry.score());
            Arrays.sort(newCats,ScoredObject.reverseComparator());
            mCategories = newCats;
        }

        public TrieNode getDaughter(String[] tokens, int start, int end) {
            TrieNode node = this;
            for (int i = start; i < end && node != null; ++i)
                node = node.getDaughter(tokens[i]);
            return node;
        }

        public TrieNode getDaughter(String token) {
            return mDaughterMap == null
                ? null
                :  mDaughterMap.get(token);
        }
        public TrieNode getOrCreateDaughter(String token) {
            TrieNode existingDaughter = getDaughter(token);
            if (existingDaughter != null) return existingDaughter;
            TrieNode newDaughter = new TrieNode(depth()+1);
            if (mDaughterMap == null)
                mDaughterMap = new HashMap<String,TrieNode>(2);
            mDaughterMap.put(token,newDaughter);
            return newDaughter;
        }

        public int add(Tokenizer tokenizer, DictionaryEntry<String> entry) {
            TrieNode node = this;
            String token;
            while ((token = tokenizer.nextToken()) != null)
                node = node.getOrCreateDaughter(token);
            node.addEntry(entry);
            return node.depth();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            toString(sb,0);
            return sb.toString();
        }
        String id() {
            return mDepth + ":" + Integer.toHexString(hashCode());
        }
        void toString(StringBuilder sb, int depth) {
            indent(sb,depth);
            sb.append(id());
            for (int i = 0; i < mCategories.length; ++i) {
                indent(sb,depth);
                sb.append("cat " + i + "=" + mCategories[i]);
            }
            if (mSuffixNode != null) {
                indent(sb,depth);
                sb.append("suffixNode=");
                sb.append(mSuffixNode.id());
            }
            if (mSuffixNodeWithCategory != null) {
                indent(sb,depth);
                sb.append("suffixNodeWithCat=");
                sb.append(mSuffixNodeWithCategory.id());
            }
            if (mDaughterMap == null) return;
            for (String token : mDaughterMap.keySet()) {
                indent(sb,depth);
                sb.append(token);
                getDaughter(token).toString(sb,depth+1);
            }
        }
        
        Object writeReplace() {
            return new Serializer(this);
        }

        static void indent(StringBuilder sb, int depth) {
            sb.append("\n");
            for (int i = 0; i < depth; ++i)
                sb.append("  ");
        }

        static class Serializer extends AbstractExternalizable {
            static final long serialVersionUID = 4017335190312081213L;
            private final TrieNode mNode;
            public Serializer(TrieNode node) {
                mNode = node;
            }
            public Serializer() {
                this(null);
            }
            public Object read(ObjectInput in) 
                throws IOException, ClassNotFoundException {
                TrieNode rootNode = new TrieNode(0);
                int maxPhraseLength = 0;
                while (true) {
                    int numToks = in.readInt();
                    if (numToks == -1) break; // end signal
                    if (numToks > maxPhraseLength)
                        maxPhraseLength = numToks;
                    TrieNode node = rootNode;
                    for (int i = 0; i < numToks; ++i) {
                        String tok = in.readUTF();
                        node = node.getOrCreateDaughter(tok);
                    }
                    List<ScoredCat> scoredCatList = new ArrayList<ScoredCat>();
                    int numCats = in.readInt();
                    for (int i = 0; i < numCats; ++i) {
                        String cat = in.readUTF();
                        double score = in.readDouble();
                        scoredCatList.add(new ScoredCat(cat,score));
                    }
                    ScoredCat[] scoredCats = scoredCatList.toArray(new ScoredCat[0]);
                    Arrays.sort(scoredCats,ScoredObject.reverseComparator());
                    node.mCategories = scoredCats;
                }
                computeSuffixes(rootNode,rootNode,new String[maxPhraseLength],0);
                return rootNode;
            }
            public void writeExternal(ObjectOutput out) throws IOException {
                List<String> tokenList = new ArrayList<String>();
                writeAll(mNode,tokenList,0,out);
                out.writeInt(-1);
            }
            void writeAll(TrieNode node, List<String> tokens, 
                          int pos, ObjectOutput out)
                throws IOException {

                if (node.mCategories.length > 0)
                    writeNode(node.mCategories,tokens,pos,out);
                if (node.mDaughterMap != null) {
                    for (Map.Entry<String,TrieNode> entry 
                             : node.mDaughterMap.entrySet()) {
                        if (tokens.size() <= pos)
                            tokens.add(entry.getKey());
                        else
                            tokens.set(pos,entry.getKey());
                        writeAll(entry.getValue(), tokens, pos + 1, out);
                    }
                }
            }
            void writeNode(ScoredCat[] cats, List<String> tokens, int numTokens, ObjectOutput out) 
                throws IOException {
                out.writeInt(numTokens);
                for (int i = 0; i < numTokens; ++i) 
                    out.writeUTF(tokens.get(i));
                out.writeInt(cats.length);
                for (int i = 0; i < cats.length; ++i) {
                    out.writeUTF(cats[i].mCat);
                    out.writeDouble(cats[i].mScore);
                }
            }
        }

    }

    static class CircularQueueInt {
        final int[] mQueue;
        int mNextPos = 0;
        public CircularQueueInt(int size) {
            mQueue = new int[size];
            Arrays.fill(mQueue,0);
        }
        public void enqueue(int val) {
            mQueue[mNextPos] = val;
            if (++mNextPos == mQueue.length)
                mNextPos = 0;
        }
        public int get(int offset) {
            // should have: -mQueue.length < offset <= 0
            int pos = mNextPos - offset;
            if (pos < 0)
                pos += mQueue.length;
            return mQueue[pos];
        }
    }

    static final Chunk[] EMPTY_CHUNK_ARRAY = new Chunk[0];

    static class Serializer extends AbstractExternalizable {
        static final long serialVersionUID = 5411870376342457513L;
        private final ExactDictionaryChunker mChunker;
        public Serializer() {
            this(null);
        }
        public Serializer(ExactDictionaryChunker chunker) {
            mChunker = chunker;
        }
        public Object read(ObjectInput in) 
            throws IOException, ClassNotFoundException {
            TrieNode rootNode = (TrieNode) in.readObject();
            TokenizerFactory tokenizerFactory = (TokenizerFactory) in.readObject();
            boolean caseSensitive = in.readBoolean();
            boolean returnAllMatches = in.readBoolean();
            int maxPhraseLength = in.readInt();
            return new ExactDictionaryChunker(rootNode,tokenizerFactory,
                                              caseSensitive,returnAllMatches,maxPhraseLength);
        }
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(mChunker.mTrieRootNode);
            out.writeObject(mChunker.mTokenizerFactory);
            out.writeBoolean(mChunker.mCaseSensitive);
            out.writeBoolean(mChunker.mReturnAllMatches);
            out.writeInt(mChunker.mMaxPhraseLength);
        }
    }

}
