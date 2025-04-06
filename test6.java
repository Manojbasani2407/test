package com.citi.reghub.rhoo.cust;

import com.citi.reghub.core.context.ContextBase;
import com.citi.reghub.core.context.ContextInterface;
import com.citi.reghub.core.fact.Fact;
import com.citi.reghub.core.fact.FactContainer;
import com.citi.reghub.core.pipeline.pipe.PipeFactory;
import com.citi.reghub.core.pipeline.pipe.PipeInterface;
import com.citi.reghub.core.decision.DecisionFactory;
import com.citi.reghub.core.decision.DecisionInterface;
import com.citi.reghub.core.metadata.local.MetaDataLoaderUtility;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RhooTotalAmtTest {

    private static final String METADATA_DIRS = "../reghub-olympus-core/seed/metadata;../reghub-olympus-tech-core/seed/metadata/";
    private static final String CACHE_NAME = "file_system";
    private static final String FACT_ID = "cust_fact";
    private static final String DECISION_ID = "rhoo_test_amt_decision_recordId";

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
        contextInterface.setContext("fact.container.current", factContainer);
    }

    @Test
    public void testCustFactNonTradeDecision() {
        testfact.set("item", "non-trade");
        testfact.set("txId", 15);
        testfact.set("recordId", "12345");

        executePipeAndAssertStrId("12345");
    }

    private void executePipeAndAssertStrId(String expectedId) {
        try {
            DecisionInterface d = DecisionFactory.getDecisionFactory().getDecision(DECISION_ID);
            d.applies(contextInterface);

            PipeInterface p = PipeFactory.getPipeFactory().getPipe("rhoo_test_pipe_total_amt_recordId");
            p.execute(contextInterface);

            String recordId = testfact.getString("recordId");
            System.out.println("recordId = " + recordId);
            assertThat(recordId).isEqualTo(expectedId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AssertionError("Test failed for recordId assertion", e);
        }
    }
}
