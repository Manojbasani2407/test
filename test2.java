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
     * Tests enrichment of 'id' using both 'custId' and 'id' via pipe 'rhoo_test_pipe_id'
     * Expected: "123-TEST0123"
     */
    @Test
    public void testCustFactIdEnrichment() {
        fact.set("id", "Test0123");
        fact.set("custId", "123");

        try {
            PipeInterface pipe = PipeFactory.getPipeFactory().getPipe("rhoo_test_pipe_id");
            pipe.execute(context);

            String actualId = getFactValue("id");
            System.out.println("Enriched ID: " + actualId);

            assertEquals("123-TEST0123", actualId);

        } catch (Exception e) {
            e.printStackTrace();
            fail("Pipe execution failed for cust_fact:id enrichment: " + e.getMessage());
        }
    }

    /**
     * Tests normalization of 'custId' via pipe 'rhoo_test_pipe_custId'
     * Expected: "TEST0123"
     */
    @Test
    public void testCustIdEnrichment() {
        fact.set("custId", "Test0123");

        try {
            PipeInterface pipe = PipeFactory.getPipeFactory().getPipe("rhoo_test_pipe_custId");
            pipe.execute(context);

            String actualCustId = getFactValue("custId");
            System.out.println("Enriched custId: " + actualCustId);

            assertEquals("TEST0123", actualCustId);

        } catch (Exception e) {
            e.printStackTrace();
            fail("Pipe execution failed for cust_fact:custId enrichment: " + e.getMessage());
        }
    }

    /**
     * Utility to extract a field value from the fact container.
     */
    private String getFactValue(String key) {
        FactContainer container = (FactContainer) context.getContext(CONTEXT_KEY);
        return container.getFact(FACT_ID).getString(key);
    }
}
