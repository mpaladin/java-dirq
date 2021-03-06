package ch.cern.dirq;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;

import ch.cern.mig.utils.StringUtils;

/**
 * Unit tests for {@link ch.cern.dirq.StringUtils}.
 *
 * @author Lionel Cons &lt;lionel.cons@cern.ch&gt;
 * @author Massimo Paladin &lt;massimo.paladin@gmail.com&gt;
 * Copyright (C) CERN 2012-2013
 */

public class StringUtilsTest {

    /**
     * Test join.
     */
    @Test
    public void join() {
        assertEquals("", StringUtils.join(new ArrayList<Object>(), ","));
        assertEquals("", StringUtils.join(new String[]{}, ", "));
        assertEquals("hello, world",
                StringUtils.join(new String[]{"hello", "world"}, ", "));
        assertEquals("hello, magic, world", StringUtils.join(new String[]{
                "hello", "magic", "world"}, ", "));
    }

}
