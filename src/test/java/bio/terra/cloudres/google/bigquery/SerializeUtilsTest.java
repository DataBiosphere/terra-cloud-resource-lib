package bio.terra.cloudres.google.bigquery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.cloud.bigquery.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
public class SerializeUtilsTest {
  private final DatasetId datasetId = DatasetId.of("datasetId1");
  private final String jsonDatasetId = "{\"datasetId\":{\"dataset\":\"datasetId1\"}";
  private final TableId tableId = TableId.of(datasetId.getDataset(), "tableId1");
  private final String jsonTableId =
      "{\"tableId\":{\"dataset\":\"datasetId1\",\"table\":\"tableId1\"}";

  private static final String REUSABLE_DATASET_ID = IntegrationUtils.randomNameWithUnderscore();

  @Test
  public void convertDatasetIdWithOptions() {
    assertEquals(
        jsonDatasetId
            + ",\"datasetOptions\":[{\"rpcOption\":\"FIELDS\",\"value\":\"datasetReference,access\"},{\"rpcOption\":\"FIELDS\",\"value\":\"datasetReference,creationTime\"}]}",
        SerializeUtils.convert(
                datasetId,
                BigQuery.DatasetOption.fields(BigQuery.DatasetField.ACCESS),
                BigQuery.DatasetOption.fields(BigQuery.DatasetField.CREATION_TIME))
            .toString());
  }

  @Test
  public void convertDatasetInfoWithOptions() {
    assertEquals(
        "{\"datasetInfo\":"
            + jsonDatasetId
            + ",\"labels\":{\"userMap\":{}}},\"datasetOptions\":[{\"rpcOption\":\"FIELDS\",\"value\":\"datasetReference,access\"},{\"rpcOption\":\"FIELDS\",\"value\":\"datasetReference,creationTime\"}]}",
        SerializeUtils.convert(
                DatasetInfo.newBuilder(datasetId).build(),
                BigQuery.DatasetOption.fields(BigQuery.DatasetField.ACCESS),
                BigQuery.DatasetOption.fields(BigQuery.DatasetField.CREATION_TIME))
            .toString());
  }

  @Test
  public void convertDatasetInfoWithDeleteOptions() {
    assertEquals(
        jsonDatasetId
            + ",\"datasetDeleteOptions\":[{\"rpcOption\":\"DELETE_CONTENTS\",\"value\":true}]}",
        SerializeUtils.convert(datasetId, BigQuery.DatasetDeleteOption.deleteContents())
            .toString());
  }

  @Test
  public void convertTableInfoWithTableOptions() {
    assertEquals(
        "{\"tableInfo\":"
            + jsonTableId
            + ",\"definition\":{\"type\":{\"constant\":\"TABLE\"},\"location\":\"location\"},\"labels\":{\"userMap\":{}}},\"tableOptions\":[{\"rpcOption\":\"FIELDS\",\"value\":\"type,tableReference\"}]}",
        SerializeUtils.convert(
                TableInfo.newBuilder(
                        tableId,
                        StandardTableDefinition.newBuilder().setLocation("location").build())
                    .build(),
                BigQuery.TableOption.fields())
            .toString());
  }

  @Test
  public void convertTableId() {
    assertEquals(jsonTableId + "}", SerializeUtils.convert(tableId).toString());
  }

  @Test
  public void convertTableIdWithTableOptions() {
    assertEquals(
        jsonTableId
            + ",\"tableOptions\":[{\"rpcOption\":\"FIELDS\",\"value\":\"type,tableReference\"}]}",
        SerializeUtils.convert(tableId, BigQuery.TableOption.fields()).toString());
  }

  @Test
  public void convertDatasetIdWithTableOptions() {
    assertEquals(
        jsonDatasetId + ",\"tableListOptions\":[{\"rpcOption\":\"MAX_RESULTS\",\"value\":1}]}",
        SerializeUtils.convert(datasetId, BigQuery.TableListOption.pageSize(1)).toString());
  }

  @Test
  public void convertTableIdWithTableDefinitionWithTableOption() {
    assertEquals(
        jsonTableId
            + ",\"tableDefinition\":{\"type\":{\"constant\":\"TABLE\"},\"location\":\"location\"},\"tableOptions\":[{\"rpcOption\":\"FIELDS\",\"value\":\"type,tableReference\"}]}",
        SerializeUtils.convert(
                tableId,
                StandardTableDefinition.newBuilder().setLocation("location").build(),
                BigQuery.TableOption.fields())
            .toString());
  }
}
