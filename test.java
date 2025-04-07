package com.citi.reghub.rhoo.cust;

import com.citi.reghub.core.context.ContextBase;
import com.citi.reghub.core.context.ContextInterface;
import com.citi.reghub.core.fact.Fact;
import com.citi.reghub.core.fact.FactContainer;
import com.citi.reghub.core.fact.FactInterface;
import com.citi.reghub.core.field.fact.enrichment.LocalDateFieldEnrichment;
import com.citi.reghub.core.metadata.local.MetaDataLoaderUtility;
import com.citi.reghub.core.pipeline.pipe.PipeFactory;
import com.citi.reghub.core.pipeline.pipe.PipeInterface;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RhooCreatedDtTest {

    private static final String METADATA_DIRS = "../reghub-olympus-core/seed/metadata;./seed";
    private static final String CACHE_NAME = "file_system_cache";
    private static final String FACT_ID = "cust_fact";
    private static final String CONTEXT_KEY = "fact.container.current";
    private static final String CREATED_DATE_KEY = "createdDate";

    private ContextInterface contextInterface;
    private FactContainer factContainer;
    private Fact testFact;

    @BeforeClass
    public static void setUpBeforeClass() {
        MetaDataLoaderUtility.loadFileSystemCache(CACHE_NAME, METADATA_DIRS);
    }

    @Before
    public void setUp() {
        testFact = new Fact(FACT_ID);
        factContainer = new FactContainer();
        factContainer.setFact(testFact);
        contextInterface = new ContextBase();
        contextInterface.setContext(CONTEXT_KEY, factContainer);
    }

    @Test
    public void testCreatedDateEnrichmentViaPipe() {
        testFact.set(CREATED_DATE_KEY, LocalDateTime.of(2025, 5, 25, 20, 35, 20, 26));
        executePipeAndAssertDate(LocalDate.of(2025, 5, 25));
    }

    private void executePipeAndAssertDate(LocalDate expectedDate) {
        try {
            PipeInterface pipe = PipeFactory.getPipeFactory().getPipe("rhoo_test_pipe_cust_fact_createdDate");
            pipe.execute(contextInterface);
            LocalDate createdDate = testFact.getLocalDateTime(CREATED_DATE_KEY).toLocalDate();
            assertThat(createdDate).isEqualTo(expectedDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFactCurrentDateManualEnrichment() {
        LocalDateFieldEnrichment enrichment = new LocalDateFieldEnrichment();
        Map<String, Object> params = new HashMap<>();
        params.put("fieldFrom", FACT_ID + ":" + CREATED_DATE_KEY);
        params.put("date_format", "yyyy-MM-dd");
        enrichment.configure(params);

        FactContainer fc = new FactContainer();
        FactInterface custFact = new Fact(FACT_ID);
        custFact.set(CREATED_DATE_KEY, null);
        fc.setFact(custFact);

        ContextInterface ci = new ContextBase();
        ci.setContext(CONTEXT_KEY, fc);
        enrichment.getValue(fc);

        custFact.set(CREATED_DATE_KEY, LocalDate.of(2025, 3, 23));
        fc.setFact(custFact);

        Object result = enrichment.getValue(fc);
        assertEquals("2025-03-23", result.toString());
    }

    @Test
    public void testNullCreatedDateManualEnrichment() {
        LocalDateFieldEnrichment enrichment = new LocalDateFieldEnrichment();
        Map<String, Object> params = new HashMap<>();
        params.put("fieldFrom", FACT_ID + ":" + CREATED_DATE_KEY);
        params.put("date_format", "test");
        enrichment.configure(params);

        FactContainer fc = new FactContainer();
        ContextInterface ci = new ContextBase();
        ci.setContext(CONTEXT_KEY, fc);

        assertNull(enrichment.getValue(fc));
    }
}
