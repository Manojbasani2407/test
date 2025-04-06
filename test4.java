package com.citi.reghub.rhoo.cust;

import com.citi.reghub.core.context.ContextBase;
import com.citi.reghub.core.context.ContextInterface;
import com.citi.reghub.core.decision.DecisionFactory;
import com.citi.reghub.core.decision.DecisionInterface;
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
    private static final String FACT_CONTEXT = "fact.container.current";
    private static final String ITEM_KEY = "item";

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
        contextInterface = new ContextBase();
        factContainer = new FactContainer();
        factContainer.setFact(testfact);
        contextInterface.setContext(FACT_CONTEXT, factContainer);
    }

    @Test
    public void testCustFactTradeDecision() {
        testfact.set(ITEM_KEY, "trade");
        executeDecisionAndPipeAndAssertPrice("rhoo_test_decision_check_price_5", "rhoo_test_pipe_price_as_5", 5.0);
    }

    @Test
    public void testCustFactNonTradeDecision() {
        testfact.set(ITEM_KEY, "non-trade");
        executeDecisionAndPipeAndAssertPrice("rhoo_test_decision_price_1", "rhoo_test_pipe_price_as_1", 1.0);
    }

    @Test
    public void testCustFactEnquiryDecision() {
        testfact.set(ITEM_KEY, "enquiry");
        executeDecisionAndPipeAndAssertPrice("rhoo_test_decision_price_0", "rhoo_test_pipe_price_as_0", 0.0);
    }

    @Test
    public void testCustFactDefaultDecision() {
        testfact.set(ITEM_KEY, "other");
        executeDecisionAndPipeAndAssertPrice("rhoo_test_decision_price_1", "rhoo_test_pipe_price_as_1", 1.0);
    }

    /**
     * Core test logic: Apply decision, execute pipe, assert price value
     */
    private void executeDecisionAndPipeAndAssertPrice(String decisionID, String pipeID, double expectedPrice) {
        try {
            // Apply decision
            DecisionInterface decision = DecisionFactory.getDecisionFactory().getDecision(decisionID);
            boolean applies = decision.applies(contextInterface);
            System.out.println("Decision applied: " + applies);

            // Execute enrichment via pipe
            PipeInterface pipe = PipeFactory.getPipeFactory().getPipe(pipeID);
            pipe.execute(contextInterface);

            // Assert final price
            Double actualPrice = testfact.getDouble("price");
            System.out.printf("Final price after pipe '%s': %.2f%n", pipeID, actualPrice);

            assertThat(actualPrice)
                    .as("Price enrichment should match expected value")
                    .isEqualTo(expectedPrice);

        } catch (Exception e) {
            e.printStackTrace();
            throw new AssertionError("Test failed for decision: " + decisionID + " and pipe: " + pipeID, e);
        }
    }
}
