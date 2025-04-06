package com.citi.reghub.rhoo.cust;

import com.citi.reghub.core.context.ContextBase;
import com.citi.reghub.core.context.ContextInterface;
import com.citi.reghub.core.fact.Fact;
import com.citi.reghub.core.fact.FactContainer;
import com.citi.reghub.core.metadata.local.MetaDataLoaderUtility;
import com.citi.reghub.core.pipeline.pipe.PipeFactory;
import com.citi.reghub.core.pipeline.pipe.PipeInterface;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class RhooCustIdTest {

    private static final String METADATA_DIRS = "../reghub-olympus-core/seed/metadata;./seed";
    private static final String CACHE_NAME = "file_system";

    private static final String FACT_ID = "cust_fact";
    private static final String CONTEXT_KEY = "fact.container.current";

    private ContextInterface context;
    private FactContainer factContainer;
    private Fact fact;

    @BeforeClass
    public static void loadMetadata() {
        MetaDataLoaderUtility.loadFileSystemCache(CACHE_NAME, METADATA_DIRS);
    }

    @Before
    public void setup() {
        fact = new Fact(FACT_ID);
        factContainer = new FactContainer();
        factContainer.setFact(fact);

        context = new ContextBase();
        context.setContext(CONTEXT_KEY, factContainer);
    }

    /**
     * Tests that 'id' field is enriched using 'custId' + original 'id' via pipe: rhoo_test_pipe_id
     * Expected: "123-Test0123" or per enrichment logic (e.g., if uppercased: "123-TEST0123")
     */
    @Test
    public void testCustFactIdEnrichment() {
        fact.set("id", "Test0123");
        fact.set("custId", "123");

        try {
            PipeInterface pipe = PipeFactory.getPipeFactory().getPipe("rhoo_test_pipe_id");
            pipe.execute(context);

            String enrichedId = getFactValue("id");
            System.out.println("Enriched ID: " + enrichedId);

            assertEquals("123-TEST0123", enrichedId);  // ← match your actual logic here

        } catch (Exception e) {
            e.printStackTrace();
            fail("Pipe execution failed for ID enrichment: " + e.getMessage());
        }
    }

    /**
     * Tests that 'custId' is enriched (normalized/uppercased) via pipe: rhoo_test_pipe_custId
     * Expected: "TEST0123"
     */
    @Test
    public void testCustIdEnrichment() {
        fact.set("custId", "Test0123");

        try {
            PipeInterface pipe = PipeFactory.getPipeFactory().getPipe("rhoo_test_pipe_custId");
            pipe.execute(context);

            String enrichedCustId = getFactValue("custId");
            System.out.println("Enriched custId: " + enrichedCustId);

            assertEquals("TEST0123", enrichedCustId);  // ← match your enrichment outcome

        } catch (Exception e) {
            e.printStackTrace();
            fail("Pipe execution failed for custId enrichment: " + e.getMessage());
        }
    }

    /**
     * Helper to get the field value from the current fact container.
     */
    private String getFactValue(String key) {
        FactContainer container = (FactContainer) context.getContext(CONTEXT_KEY);
        return container.getFact(FACT_ID).getString(key);
    }
}
