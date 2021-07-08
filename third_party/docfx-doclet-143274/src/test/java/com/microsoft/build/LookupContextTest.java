package com.microsoft.build;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class LookupContextTest {

    private LookupContext lookupContext;
    private Map<String, String> globalLookup = new HashMap<>();
    private Map<String, String> localLookup = new LinkedHashMap<>();
    private String[] localKeys = {"local key 1", "local key 2"};
    private String[] localValues = {"local value 1", "local value 2"};
    private String globalKey = "global key";
    private String globalValue = "global value";
    private String unknownKey = "unknown key";

    @Before
    public void setUp() {
        localLookup.put(localKeys[0], localValues[0]);
        localLookup.put(localKeys[1], localValues[1]);
        globalLookup.put(globalKey, globalValue);
        globalLookup.putAll(localLookup);
        lookupContext = new LookupContext(globalLookup, localLookup);
    }

    @Test
    public void resolve() {
        assertEquals("Wrong value for global key", lookupContext.resolve(globalKey), globalValue);
        assertEquals("Wrong value for local key 1", lookupContext.resolve(localKeys[0]), localValues[0]);
        assertEquals("Wrong value for local key 2", lookupContext.resolve(localKeys[1]), localValues[1]);
        assertNull("Wrong value for unknown key", lookupContext.resolve(unknownKey));
    }

    @Test
    public void getOwnerUid() {
        assertEquals("Wrong ownerUid", lookupContext.getOwnerUid(), localKeys[0]);
    }

    @Test
    public void containsKey() {
        assertTrue("Wrong value for global key", lookupContext.containsKey(globalKey));
        assertTrue("Wrong value for local key 1", lookupContext.containsKey(localKeys[0]));
        assertTrue("Wrong value for local key 2", lookupContext.containsKey(localKeys[1]));
        assertFalse("Wrong value for unknown key", lookupContext.containsKey(unknownKey));
    }
}
