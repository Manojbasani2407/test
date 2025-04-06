package com.citi.reghub.rhoo.cust;

import com.citi.reghub.core.context.ContextBase;
import com.citi.reghub.core.context.ContextInterface;
import com.citi.reghub.core.fact.Fact;
import com.citi.reghub.core.fact.FactContainer;
import com.citi.reghub.core.metadata.local.MetaDataLoaderUtility;
import com.citi.reghub.core.pipeline.pipe.PipeFactory;
import com.citi.reghub.core.pipeline.pipe.PipeInterface;
import com.citi.reghub.core.decision.DecisionFactory;
import com.citi.reghub.core.decision.DecisionInterface;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RhooStatusTest {

    private static final String METADATA_DIRS = "../reghub-olympus-core/seed/metadata;../reghub-olympus-tech-core/seed/metadata/";
    private static final String CACHE_NAME = "file_system";
    private static final String FACT_ID = "cust_fact";
    private static final String CONTEXT_KEY = "fact.container.current";

    private static final String ID_KEY = "id";
    private static final String CUST_KEY = "custId";
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
        factContainer = new FactContainer();
        factContainer.setFact(testfact);

        contextInterface = new ContextBase();
        contextInterface.setContext(CONTEXT_KEY, factContainer);
    }

    @Test
    public void testCustFactStatusReportableDecision() {
        testfact.set(ID_KEY, "5678888");
        testfact.set(CUST_KEY, "hdfc#");
        testfact.set(ITEM_KEY, "trade");
        executePipeAndAssert("rhoo_test_decision_status_reportable", "rhoo_test_pipe_status_reportable", "REPORTABLE");
    }

    @Test
    public void testCustFactIDL10GT6StatusExceptionDecision() {
        testfact.set(ID_KEY, "8");
        testfact.set(CUST_KEY, "abcdef");
        testfact.set(ITEM_KEY, "non-trade");
        executePipeAndAssert("rhoo_test_decision_status_exception", "rhoo_test_pipe_status_exception", "EXCEPTION");
    }

    @Test
    public void testCustFactStatusExceptionIDGT10Decision() {
        testfact.set(ID_KEY, "11");
        testfact.set(CUST_KEY, "abcdef");
        testfact.set(ITEM_KEY, "non-trade");
        executePipeAndAssert("rhoo_test_decision_status_exception", "rhoo_test_pipe_status_exception", "EXCEPTION");
    }

    @Test
    public void testCustFactStatusExceptionIDLTL10LT6Decision() {
        testfact.set(ID_KEY, "5");
        testfact.set(CUST_KEY, "abcdef");
        testfact.set(ITEM_KEY, "BOND");
        executePipeAndAssert("rhoo_test_decision_status_exception", "rhoo_test_pipe_status_exception", "EXCEPTION");
    }

    @Test
    public void testCustFactStatusIDNull() {
        testfact.set(ID_KEY, "null");
        executePipeAndAssert("rhoo_test_decision_status_exception", "rhoo_test_pipe_status_exception", "EXCEPTION");
    }

    private void executePipeAndAssert(String decisionId, String pipeId, String expectedStatus) {
        try {
            DecisionInterface decision = DecisionFactory.getDecisionFactory().getDecision(decisionId);
            decision.applies(contextInterface);

            PipeInterface pipe = PipeFactory.getPipeFactory().getPipe(pipeId);
            pipe.execute(contextInterface);

            String status = testfact.getString("status");
            System.out.println("status = " + status);
            assertThat(status).isEqualTo(expectedStatus);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AssertionError("Test failed for decision=" + decisionId, e);
        }
    }
}
