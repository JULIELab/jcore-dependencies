package com.aliasi.test.unit.tag;

import com.aliasi.classify.BaseClassifier;
import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;

import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ListCorpus;
import com.aliasi.corpus.ObjectHandler;

import com.aliasi.tag.ClassifierTagger;
import com.aliasi.tag.Tagging;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Compilable;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertArrayEquals;


public class ClassifierTaggerTest {

    @Test
    public void testToClassified() throws IOException {
        List<Integer> toks1 = Arrays.asList(1,2,3);
        List<String> tags1 = Arrays.asList("a","b","c");
        Tagging<Integer> tagging1 = new Tagging<Integer>(toks1,tags1);

        List<Integer> toks2 = Arrays.asList(4,5);
        List<String> tags2 = Arrays.asList("d","e");
        Tagging<Integer> tagging2 = new Tagging<Integer>(toks2,tags2);

        List<Integer> toks3 = Arrays.asList(6);
        List<String> tags3 = Arrays.asList("f");
        Tagging<Integer> tagging3 = new Tagging<Integer>(toks3,tags3);
        
        ListCorpus<Tagging<Integer>> taggingCorpus
            = new ListCorpus<Tagging<Integer>>();
        taggingCorpus.addTrain(tagging1);
        taggingCorpus.addTrain(tagging2);
        taggingCorpus.addTest(tagging3);
        assertEquals(2,taggingCorpus.trainCases().size());
        assertEquals(1,taggingCorpus.testCases().size());

        Corpus<ObjectHandler<Classified<ClassifierTagger.State<Integer>>>> stateCorpus
            = ClassifierTagger.toClassifiedCorpus(taggingCorpus);

        CounterHandler handler = new CounterHandler();
        stateCorpus.visitTrain(handler);
        assertEquals(5,handler.mList.size());
        
        assertEquals("a",handler.mList.get(0).getClassification().bestCategory());
        assertEquals("b",handler.mList.get(1).getClassification().bestCategory());
        assertEquals("c",handler.mList.get(2).getClassification().bestCategory());
        assertEquals("d",handler.mList.get(3).getClassification().bestCategory());
        assertEquals("e",handler.mList.get(4).getClassification().bestCategory());

        assertEquals(0,handler.mList.get(0).getObject().position());
        assertEquals(1,handler.mList.get(1).getObject().position());
        assertEquals(2,handler.mList.get(2).getObject().position());
        assertEquals(0,handler.mList.get(3).getObject().position());
        assertEquals(1,handler.mList.get(4).getObject().position());

        assertEquals(Arrays.asList(1,2,3),handler.mList.get(0).getObject().tokens());
        assertEquals(Arrays.asList(1,2,3),handler.mList.get(1).getObject().tokens());
        assertEquals(Arrays.asList(1,2,3),handler.mList.get(2).getObject().tokens());
        assertEquals(Arrays.asList(4,5),handler.mList.get(3).getObject().tokens());
        assertEquals(Arrays.asList(4,5),handler.mList.get(4).getObject().tokens());

        assertEquals(Arrays.asList(),handler.mList.get(0).getObject().tags());
        assertEquals(Arrays.asList("a"),handler.mList.get(1).getObject().tags());
        assertEquals(Arrays.asList("a","b"),handler.mList.get(2).getObject().tags());
        assertEquals(Arrays.asList(),handler.mList.get(3).getObject().tags());
        assertEquals(Arrays.asList("d"),handler.mList.get(4).getObject().tags());


        CounterHandler handler2 = new CounterHandler();
        stateCorpus.visitTest(handler2);
        assertEquals(1,handler2.mList.size());
        assertEquals("f",handler2.mList.get(0).getClassification().bestCategory());
        assertEquals(0,handler2.mList.get(0).getObject().position());
        assertEquals(Arrays.asList(6),handler2.mList.get(0).getObject().tokens());
        assertEquals(Arrays.asList(),handler2.mList.get(0).getObject().tags());
    }

    @Test
    public void testClassify() {
        MockClassifier classifier = new MockClassifier();
        ClassifierTagger<Integer> tagger = new ClassifierTagger<Integer>(classifier);
        assertEquals(classifier,tagger.classifier());

        Tagging<Integer> tagging = tagger.tag(Arrays.<Integer>asList());
        assertEquals(0,tagging.size());
    }

    static class CounterHandler 
        implements ObjectHandler<Classified<ClassifierTagger.State<Integer>>> {
        List<Classified<ClassifierTagger.State<Integer>>> mList
            = new ArrayList<Classified<ClassifierTagger.State<Integer>>>();
        int mCount = 0;
        public void handle(Classified<ClassifierTagger.State<Integer>> c) {
            mList.add(c);
        }
    }

    static class MockClassifier implements BaseClassifier<ClassifierTagger.State<Integer>> {
        public Classification classify(ClassifierTagger.State<Integer> state) {
            StringBuilder sb = new StringBuilder();
            sb.append(state.position());
            for (int i = 0; i < state.numTokens(); ++i) {
                sb.append(state.tag(i));
            }
            for (int i = 0; i < state.position(); ++i)
                sb.append(state.token(i));
            return new Classification(sb.toString());
        }
    }

}
