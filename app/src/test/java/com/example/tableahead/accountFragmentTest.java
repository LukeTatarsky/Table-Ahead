package com.example.tableahead;

import org.junit.Assert;
import org.junit.Test;

public class accountFragmentTest {

    @Test
    public void testOne() {
        accountFragment tester = new accountFragment();
        Assert.assertNull(tester.mAuth);
    }
}