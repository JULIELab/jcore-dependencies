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

package com.aliasi.test.unit.util;

import com.aliasi.util.Pair;
import com.aliasi.util.ScoredObject;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static com.aliasi.test.unit.Asserts.assertFullEquals;


public class ScoredObjectTest  {

    @Test
    public void test1() {
    	ScoredObject<String> so1 = new ScoredObject<String>("foo",1.0);
    	ScoredObject<CharSequence> so2 = new ScoredObject<CharSequence>("foo",1.0);
    	assertFullEquals(so1,so2);
    	
    	ScoredObject<Integer> so3 = new ScoredObject<Integer>(1, 1.0);
    	assertFalse(so3.equals(so1));
    	assertFalse(so1.equals(so3));

    	ScoredObject<String> so4 = new ScoredObject<String> ("foo",1.0);
    	assertEquals(Double.valueOf(1.0),so4.score());
    	assertEquals("foo",so4.getObject());
    }

    

}
