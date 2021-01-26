package com.aliasi.test.unit.chunk;

import com.aliasi.chunk.AbstractCharLmRescoringChunker;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingImpl;
import com.aliasi.chunk.CharLmRescoringChunker;
import com.aliasi.chunk.ConfidenceChunker;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import org.junit.Test;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.ScoredObject;

import static com.aliasi.test.unit.chunk.CharLmHmmChunkerTest.assertChunkingCompile;

import org.junit.Test;

import java.io.IOException;

import java.util.Arrays;
import java.util.Iterator;

public class CharLmRescoringChunkerTest {

    @Test
    public void testIncompleteTrain() 
        throws IOException, ClassNotFoundException {

        TokenizerFactory factory
            = IndoEuropeanTokenizerFactory.INSTANCE;
        CharLmRescoringChunker chunkerEstimator
            = new CharLmRescoringChunker(factory,
                                         1024 * 1024,
                                         2, 128, 2, false);

        String text = "Washington is near John Smith.";
        //             0123456789012345678901234567890
        //             0         1         2         3
        ChunkingImpl chunking = new ChunkingImpl(text);
        Chunk chunk21 = ChunkFactory.createChunk(0,10,"LOC");
        Chunk chunk22 = ChunkFactory.createChunk(19,29,"PER");
        chunking.add(chunk21);
        chunking.add(chunk22);

        String text2 = "Wamptton is near Smyth.";
        //              0123456789012345678901234567890
        //              0         1         2         3
        ChunkingImpl chunking2 = new ChunkingImpl(text2);
        Chunk chunk212 = ChunkFactory.createChunk(0,8,"PER");
        Chunk chunk222 = ChunkFactory.createChunk(17,22,"LOC");
        chunking2.add(chunk212);
        chunking2.add(chunk222);

        String text3 = "Washtonia Vile lives in Smithers.";
        //              0123456789012345678901234567890123
        //              0         1         2         3
        ChunkingImpl chunking3 = new ChunkingImpl(text3);
        Chunk chunk21_3 = ChunkFactory.createChunk(0,14,"LOC");
        Chunk chunk22_3 = ChunkFactory.createChunk(24,32,"PER");
        chunking3.add(chunk21_3);
        chunking3.add(chunk22_3);

        for (int i = 0; i < 1; ++i) {
            chunkerEstimator.handle(chunking);
            // chunkerEstimator.handle(chunking2);
            // chunkerEstimator.handle(chunking3);
        }

        chunkerEstimator.trainDictionary("Warton","PER");
        chunkerEstimator.trainDictionary("Warton","LOC");
        chunkerEstimator.trainDictionary("Soo","PER");
        chunkerEstimator.trainDictionary("Soo","LOC");
        // chunkerEstimator.trainDictionary("Warton Soo","PER");
        // chunkerEstimator.trainDictionary("Warton Soo","LOC");
        chunkerEstimator.trainDictionary("Vile","LOC");
        chunkerEstimator.trainDictionary("Vile","PER");
        // chunkerEstimator.trainDictionary("into Vile","PER");
        // chunkerEstimator.trainDictionary("into Vile","LOC");

        AbstractCharLmRescoringChunker chunkerCompiled
            = (AbstractCharLmRescoringChunker) AbstractExternalizable.compile(chunkerEstimator);

        for (AbstractCharLmRescoringChunker chunker 
                 : Arrays.asList(chunkerEstimator, chunkerCompiled)) {

            String testText = "Warton Soo into Vile Smoth Vile.";
            //                 012345678901234567890123456789012
            //                 0         1         2
            char[] cs = testText.toCharArray();
            Iterator<Chunk> nBestChunks = chunker.nBestChunks(cs,0,cs.length,100);
            int n = 0;
            while (nBestChunks.hasNext()) {
                Chunk chunk = nBestChunks.next();
                ++n;
            }

            int maxNBest = 100;
            Iterator<ScoredObject<Chunking>> nBestIt
                = chunker.baseChunker().nBest(cs,0,cs.length,maxNBest);
            int m = 0;
            while (nBestIt.hasNext()) {
                ScoredObject<Chunking> scoredChunking = nBestIt.next();
                ++m;
            }
        }

    }

    @Test
    public void testChunkHandler() throws IOException, ClassNotFoundException {
        TokenizerFactory factory
            = IndoEuropeanTokenizerFactory.INSTANCE;

        CharLmRescoringChunker chunkerEstimator
            = new CharLmRescoringChunker(factory,8,
                                         5,128,5.0);

    
        String text1 = "John J. Smith lives in Washington.";
        //              0123456789012345678901234567890123
        //              0         1         2         3
        ChunkingImpl chunking1 = new ChunkingImpl(text1);
        Chunk chunk11 = ChunkFactory.createChunk(0,13,"PER");
        Chunk chunk12 = ChunkFactory.createChunk(23,33,"LOC");
        chunking1.add(chunk11);
        chunking1.add(chunk12);
    
        for (int i = 0; i < 10; ++i)
            chunkerEstimator.handle(chunking1);

        assertChunkingCompile(chunkerEstimator,chunking1);

        String text2 = "Washington is near John";
        //              01234567890123456789012
        //              0         1         2  
        ChunkingImpl chunking2 = new ChunkingImpl(text2);
        Chunk chunk21 = ChunkFactory.createChunk(0,10,"LOC");
        Chunk chunk22 = ChunkFactory.createChunk(19,23,"PER");
        chunking2.add(chunk21);
        chunking2.add(chunk22);
    
        for (int i = 0; i < 10; ++i)
            chunkerEstimator.handle(chunking2);
    
        assertChunkingCompile(chunkerEstimator,chunking2);
    
    
        String text3 = "Washington D.C. is near Frank Jones.";
        //              012345678901234567890123456789012345
        //              0         1         2         3
        ChunkingImpl chunking3 = new ChunkingImpl(text3);
        Chunk chunk31 = ChunkFactory.createChunk(0,15,"LOC");
        Chunk chunk32 = ChunkFactory.createChunk(24,36,"PER");
        chunking3.add(chunk31);
        chunking3.add(chunk32);

        for (int i = 0; i < 10; ++i)
            chunkerEstimator.handle(chunking3);

        assertChunkingCompile(chunkerEstimator,chunking3);

    }

}
