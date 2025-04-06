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

    private static final String METADATA_DIRS = "../reghub-olympus-core/seed/metadata;" + "./seed";
    private static final String CACHE_NAME = "file_system_cache";
    private static final String FACT_ID = "cust_fact";
    private static final String CONTEXT_KEY = "fact.container.current";
    private static final String CREATED_DATE_KEY = "createdDate";
    private static final LocalDate EXPECTED_LOCAL_DATE = LocalDate.of(2025, 5, 25);
    private static final LocalDateTime EXPECTED_DATE_TIME = LocalDateTime.of(2025, 5, 25, 20, 35, 20, 26);

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
        testFact.set(CREATED_DATE_KEY, EXPECTED_DATE_TIME); // Enrichment should convert this to LocalDate

        try {
            PipeInterface pipe = PipeFactory.getPipeFactory().getPipe("rhoo_test_pipe_CreatedDate");
            pipe.execute(contextInterface);

            LocalDate result = testFact.getLocalDate(CREATED_DATE_KEY);
            assertThat(result)
                    .as("Validate LocalDate enrichment after pipe execution")
                    .isEqualTo(EXPECTED_LOCAL_DATE);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFactCurrentDateManualEnrichment() {
        // Arrange
        LocalDateFieldEnrichment enrichment = configureEnrichment("yyyy-MM-dd");

        FactInterface custFact = new Fact(FACT_ID);
        custFact.set(CREATED_DATE_KEY, null);
        FactContainer fc = new FactContainer();
        fc.setFact(custFact);

        ContextInterface ci = new ContextBase();
        ci.setContext(CONTEXT_KEY, fc);

        enrichment.getValue(fc); // should do nothing since value is null

        // Act
        custFact.set(CREATED_DATE_KEY, LocalDate.of(2025, 3, 23));
        fc.setFact(custFact);

        Object result = enrichment.getValue(fc);

        System.out.println("Manual Enrichment Result = " + result);
        assertEquals("2025-03-23", result.toString());
    }

    @Test
    public void testNullCreatedDateManualEnrichment() {
        LocalDateFieldEnrichment enrichment = configureEnrichment("test");

        FactContainer fc = new FactContainer();
        ContextInterface ci = new ContextBase();
        ci.setContext(CONTEXT_KEY, fc);

        assertNull(enrichment.getValue(fc));
    }

    private LocalDateFieldEnrichment configureEnrichment(String format) {
        LocalDateFieldEnrichment enrichment = new LocalDateFieldEnrichment();
        Map<String, Object> params = new HashMap<>();
        params.put("fieldFrom", FACT_ID + ":" + CREATED_DATE_KEY);
        params.put("date_format", format);
        enrichment.configure(params);
        return enrichment;
    }
}
