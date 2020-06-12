package bio.terra.cloudres.google.bigquery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.cloud.bigquery.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
public class SerializeUtilsTest {
  private final DatasetId datasetId = DatasetId.of("datasetId1");
  private final TableId tableId = TableId.of(datasetId.getDataset(), "tableId1");
  private final String jsonTableId =
      "{\"tableId\":{\"dataset\":\"datasetId1\",\"table\":\"tableId1\"}";

  @Test
  public void convertDatasetIdWithOptions() {
    assertEquals(
        "{\"datasetId\":{\"dataset\":\"datasetId1\"},\"datasetOptions\":[{\"rpcOption\":\"FIELDS\",\"value\":\"datasetReference,access\"},{\"rpcOption\":\"FIELDS\",\"value\":\"datasetReference,creationTime\"}]}",
        SerializeUtils.convert(
                datasetId,
                BigQuery.DatasetOption.fields(BigQuery.DatasetField.ACCESS),
                BigQuery.DatasetOption.fields(BigQuery.DatasetField.CREATION_TIME))
            .toString());
  }

  @Test
  public void convertDatasetInfoWithOptions() {
    assertEquals(
        "{\"datasetInfo\":{\"datasetId\":{\"dataset\":\"datasetId1\"},\"labels\":{\"userMap\":{}}},\"datasetOptions\":[{\"rpcOption\":\"FIELDS\",\"value\":\"datasetReference,access\"},{\"rpcOption\":\"FIELDS\",\"value\":\"datasetReference,creationTime\"}]}",
        SerializeUtils.convert(
                DatasetInfo.newBuilder(datasetId).build(),
                BigQuery.DatasetOption.fields(BigQuery.DatasetField.ACCESS),
                BigQuery.DatasetOption.fields(BigQuery.DatasetField.CREATION_TIME))
            .toString());
  }

  @Test
  public void convertDatasetInfoWithDeleteOptions() {
    assertEquals(
        "{\"datasetId\":{\"dataset\":\"datasetId1\"},\"datasetDeleteOptions\":[{\"rpcOption\":\"DELETE_CONTENTS\",\"value\":true}]}",
        SerializeUtils.convert(datasetId, BigQuery.DatasetDeleteOption.deleteContents())
            .toString());
  }

  @Test
  public void convertTableInfoWithTableOptions() {
    assertEquals(
        "{\"tableInfo\":{\"tableId\":{\"dataset\":\"datasetId1\",\"table\":\"tableId1\"},\"definition\":{\"type\":{\"constant\":\"TABLE\"},\"location\":\"location\"},\"labels\":{\"userMap\":{}}},\"tableOptions\":[{\"rpcOption\":\"FIELDS\",\"value\":\"type,tableReference\"}]}",
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
    assertEquals(
        "{\"tableId\":{\"dataset\":\"datasetId1\",\"table\":\"tableId1\"}}",
        SerializeUtils.convert(tableId).toString());
  }

  @Test
  public void convertTableIdWithTableOptions() {
    assertEquals(
        "{\"tableId\":{\"dataset\":\"datasetId1\",\"table\":\"tableId1\"},\"tableOptions\":[{\"rpcOption\":\"FIELDS\",\"value\":\"type,tableReference\"}]}",
        SerializeUtils.convert(tableId, BigQuery.TableOption.fields()).toString());
  }

  @Test
  public void convertDatasetIdWithTableOptions() {
    assertEquals(
        "{\"datasetId\":{\"dataset\":\"datasetId1\"},\"tableListOptions\":[{\"rpcOption\":\"MAX_RESULTS\",\"value\":1}]}",
        SerializeUtils.convert(datasetId, BigQuery.TableListOption.pageSize(1)).toString());
  }

  @Test
  public void convertTableIdWithTableDefinitionWithTableOption() {
    assertEquals(
        "{\"tableId\":{\"dataset\":\"datasetId1\",\"table\":\"tableId1\"},\"tableDefinition\":{\"type\":{\"constant\":\"TABLE\"},\"location\":\"location\"},\"tableOptions\":[{\"rpcOption\":\"FIELDS\",\"value\":\"type,tableReference\"}]}",
        SerializeUtils.convert(
                tableId,
                StandardTableDefinition.newBuilder().setLocation("location").build(),
                BigQuery.TableOption.fields())
            .toString());
  }
}
