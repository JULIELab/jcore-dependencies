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

package com.aliasi.chunk;

import com.aliasi.classify.PrecisionRecallEvaluation;
import com.aliasi.util.Strings;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A <code>ChunkingEvaluation</code> stores and reports the results of
 * evaluating response chunkings against reference chunkings.  Cases
 * to evaluate are supplied in the form of a reference and response
 * chunking through the method {@link #addCase(Chunking,Chunking)}.
 *
 * <P>The sets of true positive,
 * false positive and false negative chunks are available through the
 * methods {@link #truePositiveSet()}, {@link #falsePositiveSet()},
 * and {@link #falseNegativeSet()}.  True positives are chunks that
 * are in both the reference and response, false positives are chunks
 * in the response but not the reference, and false negatives are in
 * the reference, but not the response.  There is no notion of true
 * negative in this task, a fact that is reflected in the results of
 * the precision-recall evaluation.
 *
 * <P>The main method of reporting is through an instance of {@link
 * com.aliasi.classify.ScoredPrecisionRecallEvaluation} returned by
 * the method {@link #precisionRecallEvaluation()}.  The return result
 * provides an object capable of extensive reporting for scored
 * classification tasks such as chunking.  The instances of true and
 * false positive and negatives are described above; their scores are
 * derived from response scores.
 *
 * <P>This evaluator works solely on the basis of chunk offset and
 * exact match. There is no notion of alignment or mapping, as found,
 * for example, in the <a
 * href="http://www.itl.nist.gov/iaui/894.02/related_projects/muc/muc_sw/muc_sw_manual.html">MUC
 * Scoring Software User's Manual</a>, and its descendants such as the
 * <a
 * href="http://www.itl.nist.gov/iad/894.01/tests/ace/ace05/doc/ace05-evalplan.v2a.pdf">2005
 * ACE Evaluation Plan</a>.  In this regard, we follow the model of
 * <a href="http://acl.ldc.upenn.edu/W/W00/W00-0726.pdf">CoNLL 2000
 * Chunking Task</a>.
 *
 * <P>This evaluation is able to handle overlapping chunks with
 * results being reported in the same manner.  In particular, the
 * labeled precision and recall components of the approach that later
 * became known as <a
 * href="http://acl.ldc.upenn.edu/H/H91/H91-1060.pdf">PARSEVAL</a> can
 * be generated by using the <code>ChunkingEvaluation</code> class.
 *
 * @author  Bob Carpenter
 * @version 3.8
 * @since   LingPipe2.1
 */
public class ChunkingEvaluation {

    private final Set<Chunking[]> mCases = new HashSet<Chunking[]>();

    private final Set<ChunkAndCharSeq> mTruePositiveSet
        = new HashSet<ChunkAndCharSeq>();
    private final Set<ChunkAndCharSeq> mFalsePositiveSet
        = new HashSet<ChunkAndCharSeq>();
    private final Set<ChunkAndCharSeq> mFalseNegativeSet
        = new HashSet<ChunkAndCharSeq>();

    String mLastCase = null;

    /**
     * Construct a chunking evaluation.
     */
    public ChunkingEvaluation() {
        /* do nothing */
    }


    /**
     * Return the set of cases consisting of pairs of reference and
     * response chunkings.  The elements of the set returned are of
     * type <code>Chunking[]</code>, with the first element being the
     * reference chunk and the second element being the response
     * chunk.
     *
     * <P>The set returned is an unmodifiable view of the underlying
     * set of cases and will change as cases are added to this
     * evaluation.
     *
     * @return The set of cases.
     */
    public Set<Chunking[]> cases() {
        return Collections.<Chunking[]>unmodifiableSet(mCases);
    }

    /**
     * Returns a chunking evaluation which consists of the current
     * chunking evaluation restricted to the specified type.  A
     * new evaluation is constructed and populated with the same
     * cases as this evaluation, but with the reference and response
     * chunkings both restricted to only include answers of the
     * specified type.
     *
     * @param chunkType Type of chunk to be evaluated.
     * @return ChunkingEvaluation Evaluation for this type.
     */
    public ChunkingEvaluation perTypeEvaluation(String chunkType) {
        ChunkingEvaluation evaluation = new ChunkingEvaluation();
        for (Chunking[] testCase : cases()) {
            Chunking referenceChunking = testCase[0];
            Chunking responseChunking = testCase[1];
            Chunking referenceChunkingRestricted
                = restrictTo(referenceChunking,chunkType);
            Chunking responseChunkingRestricted
                = restrictTo(responseChunking,chunkType);
            evaluation.addCase(referenceChunkingRestricted,
                               responseChunkingRestricted);
        }
        return evaluation;
    }

    static Chunking restrictTo(Chunking chunking, String type) {
        CharSequence cs = chunking.charSequence();
        ChunkingImpl chunkingOut = new ChunkingImpl(cs);
        for (Chunk chunk : chunking.chunkSet())
            if (chunk.type().equals(type))
                chunkingOut.add(chunk);
        return chunkingOut;
    }


    static String formatChunks(Chunking chunking) {
        StringBuilder sb = new StringBuilder();
        int pos = 0;
        for (Chunk chunk : chunking.chunkSet()) {
            int start = chunk.start();
            int padLength = start-pos;
            for (int j = 0; j < padLength; ++j)
                sb.append(" ");
            int end = chunk.end();
            int chunkLength = end-start;
            char marker = chunk.type().length() > 0
                ? chunk.type().charAt(0)
                : '!';
            if (chunkLength > 0) sb.append(marker);
            for (int j = 1; j < chunkLength; ++j)
                sb.append(".");
            pos = end;
        }
        sb.append("\n");
        return sb.toString();
    }

    static String formatHeader(int indent, Chunking chunking) {
        String cs = chunking.charSequence().toString();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; ++i) sb.append(" ");
        sb.append("CHUNKS= ");
        for (Chunk chunk : chunking.chunkSet()) {
            sb.append("(" + chunk.start()
                      + "," + chunk.end()
                      + "):" + chunk.type() + "   ");
        }

        if (sb.charAt(sb.length()-1) != '\n') sb.append("\n");
        for (int i = 0; i < indent; ++i) sb.append(" ");
        sb.append(cs);
        sb.append("\n");
        int length = cs.length();
        printMods(1,length, sb,indent);
        printMods(10,length, sb,indent);
        printMods(100,length, sb,indent);
        if (sb.charAt(sb.length()-1) != '\n') sb.append("\n");
        return sb.toString();
    }

    static void printMods(int base, int length, StringBuilder sb, int indent) {
        if (length <= base) return;
        for (int i = 0; i < indent; ++i) sb.append(" ");
        for (int i = 0; i < length; ++i) {
            if (base == 1 || (i >= base && i % 10 == 0))
                sb.append(Integer.toString((i/base)%10));
            else
                sb.append(" ");
        }
        sb.append("\n");
    }


    /**
     * Add an evaluation case consisting of a reference chunk
     * set and a response chunk set.
     *
     * @param referenceChunking Chunking of reference chunks.
     * @param responseChunking Chunking of response chunks.
     * @throws IllegalArgumentException If the chunkings are not
     * over the same character sequence.
     */
    public void addCase(Chunking referenceChunking,
                        Chunking responseChunking) {
        StringBuilder sb = new StringBuilder();

        CharSequence cSeq = referenceChunking.charSequence();
        if (!Strings.equalCharSequence(cSeq,
                                       responseChunking.charSequence())) {
            String msg = "Char sequences must be same."
                + " Reference char seq=" + cSeq
                + " Response char seq=" + responseChunking.charSequence();
            throw new IllegalArgumentException(msg);
        }
        sb.append("\n");
        sb.append(formatHeader(5,referenceChunking)); // 5 is indent for " REF " and "RESP "
        sb.append("\n REF ");
        sb.append(formatChunks(referenceChunking));
        sb.append("RESP ");
        sb.append(formatChunks(responseChunking));
        sb.append("\n");
        mLastCase = sb.toString();

        mCases.add(new Chunking[] { referenceChunking, responseChunking });
        // need mutable sets, so wrap
        Set<Chunk> refSet = unscoredChunkSet(referenceChunking);
        Set<Chunk> respSet = unscoredChunkSet(responseChunking);
        for (Chunk respChunk : respSet) {
            boolean inRef = refSet.remove(respChunk);
            ChunkAndCharSeq ccs = new ChunkAndCharSeq(respChunk,cSeq);
            if (inRef) {
                mTruePositiveSet.add(ccs);
            } else {
                mFalsePositiveSet.add(ccs);
            }
        }
        for (Chunk refChunk : refSet) {
            mFalseNegativeSet.add(new ChunkAndCharSeq(refChunk,cSeq));
        }
    }

    static Set<Chunk> unscoredChunkSet(Chunking chunking) {
        Set<Chunk> result = new HashSet<Chunk>();
        for (Chunk chunk : chunking.chunkSet())
            result.add(ChunkFactory.createChunk(chunk.start(),
                                                chunk.end(),
                                                chunk.type()));
        return result;
    }

    /**
     * Returns the set of true positives.  True positives are chunks
     * that were in both a reference and response chunking case. The
     * set returned contains instances of {@link ChunkAndCharSeq},
     * which combine a chunk and a character sequence.
     *
     * <P> The set is unmodifiable, but tracks the changes in this
     * evaluator.
     *
     * @return The set of true positives.
     */
    public Set<ChunkAndCharSeq> truePositiveSet() {
        return Collections.<ChunkAndCharSeq>unmodifiableSet(mTruePositiveSet);
    }

    /**
     * Returns the set of false positives.  False positives are
     * response chunks that are not reference chunks. The set returned
     * contains instances of {@link ChunkAndCharSeq}, which combine a
     * chunk and a character sequence.
     *
     * <P> The set is unmodifiable, but tracks the changes in this
     * evaluator.
     *
     * @return The set of false positives.
     */
    public Set<ChunkAndCharSeq> falsePositiveSet() {
        return Collections.<ChunkAndCharSeq>unmodifiableSet(mFalsePositiveSet);
    }

    /**
     * Returns the set of false negatives.  False negatives are
     * reference chunks which are not response chunks.  The set
     * returned contains instances of {@link ChunkAndCharSeq}, which
     * combine a chunk and a character sequence.
     *
     * <P> The set is unmodifiable, but tracks the changes in this
     * evaluator.
     *
     * @return The set of false negatives.
     */
    public Set<ChunkAndCharSeq> falseNegativeSet() {
        return Collections.<ChunkAndCharSeq>unmodifiableSet(mFalseNegativeSet);
    }

    /**
     * Return the scored precision-recall evaluation for this chunker.
     * This is a copy of the precision-recall evaluation and changes to
     * it will not affect the results returned by this class.
     *
     * @return The precision-recall evaluation.
     */
    public PrecisionRecallEvaluation precisionRecallEvaluation() {
        int tp = truePositiveSet().size();
        int fn = falseNegativeSet().size();
        int fp = falsePositiveSet().size();
        return new PrecisionRecallEvaluation(tp,fn,fp,0);
    }

    /**
     * Returns the precision-recall evaluation for this chunking
     * as a string.
     *
     * @return This evaluation as a string.
     */
    @Override
    public String toString() {
        return precisionRecallEvaluation().toString();
    }

}
