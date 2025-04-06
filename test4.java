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
        executePipeAndAssertPrice(5.0);
    }

    @Test
    public void testCustFactNonTradeDecision() {
        testfact.set("item", "non-trade");
        executePipeAndAssertPrice(1.0);
    }

    @Test
    public void testCustFactEnquiryDecision() {
        testfact.set("item", "enquiry");
        executePipeAndAssertPrice(0.0);
    }

    @Test
    public void testCustFactDefaultDecision() {
        testfact.set("item", "other");
        executePipeAndAssertPrice(1.0);
    }

    private void executePipeAndAssertPrice(double expectedPrice) {
        try {
            PipeInterface pipe = PipeFactory.getPipeFactory().getPipe("rhoo_test_pipe_cust_fact_price_as_1");
            pipe.execute(contextInterface);

            // Workaround in case price is still set as String
            Object priceObj = testfact.get("price");
            Double price = priceObj instanceof Double
                ? (Double) priceObj
                : Double.valueOf(priceObj.toString());

            System.out.println("price = " + price);
            assertThat(price).isEqualTo(expectedPrice);

        } catch (Exception e) {
            e.printStackTrace();
            throw new AssertionError("Failed to execute pipe and assert price", e);
        }
    }
}
