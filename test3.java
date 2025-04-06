package com.citi.reghub.rhoo.cust;

import com.citi.reghub.core.context.ContextBase;
import com.citi.reghub.core.context.ContextInterface;
import com.citi.reghub.core.decision.DecisionFactory;
import com.citi.reghub.core.decision.DecisionInterface;
import com.citi.reghub.core.fact.Fact;
import com.citi.reghub.core.fact.FactContainer;
import com.citi.reghub.core.fact.FactFactory;
import com.citi.reghub.core.fact.FactInterface;
import com.citi.reghub.core.metadata.local.MetaDataLoaderUtility;
import com.citi.reghub.core.pipeline.pipe.PipeFactory;
import com.citi.reghub.core.pipeline.pipe.PipeInterface;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RhooDecisionTest {

    private static final String METADATA_DIRS = "../reghub-olympus-core/seed/metadata;../reghub-olympus-tech-core/seed/metadata";
    private static final String CACHE_NAME = "file_system";
    private static final String FACT_ID = "cust_fact";
    private static final String CONTEXT_KEY = "fact.container.current";

    private ContextInterface ci;
    private FactContainer fc;
    private Fact tf;

    @BeforeClass
    public static void setUpMetadata() {
        MetaDataLoaderUtility.loadFileSystemCache(CACHE_NAME, METADATA_DIRS);
    }

    @Before
    public void setup() {
        tf = new Fact(FACT_ID);
        fc = new FactContainer();
        fc.setFact(tf);
        ci = new ContextBase();
        ci.setContext(CONTEXT_KEY, fc);
    }

    @Test
    public void testDecisionPrice5WithTradeItem() {
        tf.set("item", "trade");
        applyDecisionAndAssert("rhoo_test_decision_check_price_5", "item", "trade");
    }

    @Test
    public void testDecisionPrice0WithEnquiry() {
        tf.set("item", "enquiry");
        applyDecisionAndAssert("rhoo_test_decision_price_0", "item", "enquiry");
    }

    @Test
    public void testDecisionPrice1WithNonTrade() {
        tf.set("item", "non-trade");
        applyDecisionAndAssert("rhoo_test_decision_price_1", "item", "non-trade");
    }

    @Test
    public void testDecisionCustStatusReportable() {
        tf.set("id", "8");
        tf.set("item", "non-trade");

        DecisionInterface decision = DecisionFactory.getDecisionFactory().getDecision("rhoo_test_decision_status_reportable");
        decision.applies(ci); // apply logic

        String actualId = tf.getString("id");
        System.out.println("Resulting id: " + actualId);

        assertThat(actualId).isEqualTo("8"); // update if transformation applies
    }

    /**
     * Utility to apply a decision rule and assert field matches expected.
     */
    private void applyDecisionAndAssert(String decisionId, String field, String expectedValue) {
        try {
            DecisionInterface decision = DecisionFactory.getDecisionFactory().getDecision(decisionId);

            boolean applied = decision.applies(ci);
            System.out.println("Decision applied: " + applied);

            String result = tf.getString(field);
            System.out.printf("Field '%s' after decision: %s%n", field, result);

            assertThat(result).isEqualTo(expectedValue);

        } catch (Exception e) {
            e.printStackTrace();
            throw new AssertionError("Decision application failed for: " + decisionId, e);
        }
    }
}
