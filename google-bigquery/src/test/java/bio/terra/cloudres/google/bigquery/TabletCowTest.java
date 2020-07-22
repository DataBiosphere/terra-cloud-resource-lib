package bio.terra.cloudres.google.bigquery;

import static bio.terra.cloudres.google.bigquery.BigQueryIntegrationUtils.assertTableIdEqual;
import static bio.terra.cloudres.google.bigquery.BigQueryIntegrationUtils.defaultBigQueryCow;
import static org.junit.jupiter.api.Assertions.*;

import bio.terra.cloudres.testing.IntegrationUtils;
import bio.terra.cloudres.testing.MockJanitorService;
import com.google.cloud.bigquery.DatasetInfo;
import com.google.cloud.bigquery.Table;
import org.junit.jupiter.api.*;

@Tag("integration")
public class TabletCowTest {
  private static final String REUSABLE_DATASET_ID = IntegrationUtils.randomNameWithUnderscore();
  private static BigQueryCow bigQueryCow = defaultBigQueryCow();
  private static DatasetInfo reusableDataset;
  private final ResourceTracker resourceTracker =
      new ResourceTracker(bigQueryCow, REUSABLE_DATASET_ID);
  private MockJanitorService mockJanitorService;

  @BeforeAll
  public static void createReusableDataset() {
    // Bring up a mock service.
    MockJanitorService beforeAllMockService = new MockJanitorService();
    beforeAllMockService.setup();
    reusableDataset =
        bigQueryCow.create(DatasetInfo.newBuilder(REUSABLE_DATASET_ID).build()).getDatasetInfo();
    beforeAllMockService.stop();
  }

  @AfterAll
  public static void deleteReusableDataset() {
    bigQueryCow.delete(REUSABLE_DATASET_ID);
  }

  @BeforeEach
  public void setUp() {
    mockJanitorService = new MockJanitorService();
    mockJanitorService.setup();
  }

  @AfterEach
  public void tearDown() {
    resourceTracker.tearDown();
    mockJanitorService.stop();
  }

  @Test
  public void reload() {
    TableCow tableCow = resourceTracker.createTableCow();
    TableCow reloadedTableCow = tableCow.reload();

    assertTableIdEqual(
        tableCow.getTableInfo().getTableId(), reloadedTableCow.getTableInfo().getTableId());
  }

  @Test
  public void update() {
    String description = "des";
    TableCow tableCow = resourceTracker.createTableCow();

    TableCow updatedTableCow =
        new TableCow(
            IntegrationUtils.DEFAULT_CLIENT_CONFIG,
            (Table) tableCow.getTableInfo().toBuilder().setDescription(description).build());
    updatedTableCow.update();
    assertEquals(description, updatedTableCow.reload().getTableInfo().getDescription());
  }

  @Test
  public void exists() {
    TableCow tableCow = resourceTracker.createTableCow();

    assertTrue(tableCow.exists());
  }

  @Test
  public void delete() {
    TableCow tableCow = resourceTracker.createTableCow();
    tableCow.delete();

    assertNull(bigQueryCow.getTable(tableCow.getTableInfo().getTableId()).getTableInfo());
  }
}
