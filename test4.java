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

import static org.assertj.core.api.Assertions.assertThat;

public class RhooPriceTest {

    private static final String METADATA_DIRS = "../reghub-olympus-core/seed/metadata;../reghub-olympus-tech-core/seed/metadata/";
    private static final String CACHE_NAME = "file_system";
    private static final String FACT_ID = "cust_fact";
    private static final String CONTEXT_KEY = "fact.container.current";

    private ContextInterface contextInterface;
    private FactContainer factContainer;
    private Fact testfact;

    @BeforeClass
    public static void setUpMetadata() {
        MetaDataLoaderUtility.loadFileSystemCache(CACHE_NAME, METADATA_DIRS);
    }

    @Before
    public void setup() {
        testfact = new Fact(FACT_ID);
        factContainer = new FactContainer();
        factContainer.setFact(testfact);

        contextInterface = new ContextBase();
        contextInterface.setContext(CONTEXT_KEY, factContainer);
    }

    @Test
    public void testCustFactTradeDecision() {
        testfact.set("item", "trade");
        executePipeAndAssertPrice("rhoo_test_pipe_price_as_5", 5.0);
    }

    @Test
    public void testCustFactNonTradeDecision() {
        testfact.set("item", "non-trade");
        executePipeAndAssertPrice("rhoo_test_pipe_price_as_1", 1.0);
    }

    @Test
    public void testCustFactEnquiryDecision() {
        testfact.set("item", "enquiry");
        executePipeAndAssertPrice("rhoo_test_pipe_price_as_0", 0.0);
    }

    @Test
    public void testCustFactDefaultDecision() {
        testfact.set("item", "other");
        executePipeAndAssertPrice("rhoo_test_pipe_price_as_1", 1.0);
    }

    /**
     * Utility method to execute pipe and assert price outcome.
     */
    private void executePipeAndAssertPrice(String pipeID, double expectedPrice) {
        try {
            PipeInterface pipe = PipeFactory.getPipeFactory().getPipe(pipeID);
            pipe.execute(contextInterface);

            Object priceObj = testfact.get("price");
            Double price = priceObj instanceof Double
                    ? (Double) priceObj
                    : Double.valueOf(priceObj.toString());

            System.out.printf("price = %.2f (via pipe: %s)%n", price, pipeID);
            assertThat(price).isEqualTo(expectedPrice);

        } catch (Exception e) {
            e.printStackTrace();
            throw new AssertionError("Pipe execution failed for " + pipeID, e);
        }
    }
}
