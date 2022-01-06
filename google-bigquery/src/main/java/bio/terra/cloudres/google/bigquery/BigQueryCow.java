package bio.terra.cloudres.google.bigquery;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.OperationAnnotator;
import bio.terra.cloudres.google.api.services.common.AbstractRequestCow;
import bio.terra.cloudres.google.api.services.common.Defaults;
import bio.terra.janitor.model.CloudResourceUid;
import bio.terra.janitor.model.GoogleBigQueryDatasetUid;
import bio.terra.janitor.model.GoogleBigQueryTableUid;
import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.BigqueryScopes;
import com.google.api.services.bigquery.model.Dataset;
import com.google.api.services.bigquery.model.DatasetList;
import com.google.api.services.bigquery.model.GetIamPolicyRequest;
import com.google.api.services.bigquery.model.Policy;
import com.google.api.services.bigquery.model.SetIamPolicyRequest;
import com.google.api.services.bigquery.model.Table;
import com.google.api.services.bigquery.model.TableList;
import com.google.api.services.bigquery.model.TestIamPermissionsRequest;
import com.google.api.services.bigquery.model.TestIamPermissionsResponse;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A Cloud Object Wrapper(COW) for Google API Client Library: {@link Bigquery} */
public class BigQueryCow {

  private final Logger logger = LoggerFactory.getLogger(BigQueryCow.class);

  private final OperationAnnotator operationAnnotator;
  private final Bigquery bigQuery;

  private final ClientConfig clientConfig;

  public BigQueryCow(ClientConfig clientConfig, Bigquery.Builder bigQueryBuilder) {
    this.operationAnnotator = new OperationAnnotator(clientConfig, logger);
    this.bigQuery = bigQueryBuilder.build();
    this.clientConfig = clientConfig;
  }

  /** Create a {@link BigQueryCow} with some default configurations for convenience. */
  public static BigQueryCow create(ClientConfig clientConfig, GoogleCredentials googleCredentials)
      throws GeneralSecurityException, IOException {
    return new BigQueryCow(
        clientConfig,
        new Bigquery.Builder(
                Defaults.httpTransport(),
                Defaults.jsonFactory(),
                new HttpCredentialsAdapter(googleCredentials.createScoped(BigqueryScopes.all())))
            .setApplicationName(clientConfig.getClientName()));
  }

  public Datasets datasets() {
    return new Datasets(bigQuery.datasets());
  }

  /** See {@link Bigquery.Datasets} */
  public class Datasets {

    private final Bigquery.Datasets datasets;

    private Datasets(Bigquery.Datasets datasets) {
      this.datasets = datasets;
    }

    /** See {@link Bigquery.Datasets#delete(String, String)} */
    public Delete delete(String projectId, String datasetId) throws IOException {
      return new Delete(datasets.delete(projectId, datasetId));
    }

    /** See {@link Bigquery.Datasets#delete(String, String)} */
    public class Delete extends AbstractRequestCow<Void> {

      private final Bigquery.Datasets.Delete delete;

      private Delete(Bigquery.Datasets.Delete delete) {
        super(
            BigQueryOperation.GOOGLE_DELETE_BIGQUERY_DATASET,
            clientConfig,
            operationAnnotator,
            delete);
        this.delete = delete;
      }

      /** See {@link Bigquery.Datasets.Delete#setDeleteContents(Boolean)} */
      public Delete setDeleteContents(boolean deleteContents) {
        this.delete.setDeleteContents(deleteContents);
        return this;
      }

      public String getProjectId() {
        return delete.getProjectId();
      }

      public String getDatasetId() {
        return delete.getDatasetId();
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("projectId", getProjectId());
        result.addProperty("datasetId", getDatasetId());
        result.addProperty("deleteContents", delete.getDeleteContents());
        return result;
      }
    }

    /** See {@link Bigquery.Datasets#get(String, String)} */
    public Get get(String projectId, String datasetId) throws IOException {
      return new Get(datasets.get(projectId, datasetId));
    }

    /** See {@link Bigquery.Datasets#get(String, String)} */
    public class Get extends AbstractRequestCow<Dataset> {

      private final Bigquery.Datasets.Get get;

      private Get(Bigquery.Datasets.Get get) {
        super(BigQueryOperation.GOOGLE_GET_BIGQUERY_DATASET, clientConfig, operationAnnotator, get);
        this.get = get;
      }

      public String getProjectId() {
        return get.getProjectId();
      }

      public String getDatasetId() {
        return get.getDatasetId();
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("projectId", getProjectId());
        result.addProperty("datasetId", getDatasetId());
        return result;
      }
    }

    /** See {@link Bigquery.Datasets#insert(String, Dataset)} */
    public Insert insert(String projectId, Dataset content) throws IOException {
      return new Insert(
          datasets.insert(projectId, content), content.getDatasetReference().getDatasetId());
    }

    /** See {@link Bigquery.Datasets#insert(String, Dataset)} */
    public class Insert extends AbstractRequestCow<Dataset> {

      private final Bigquery.Datasets.Insert insert;
      private final String datasetId;

      private Insert(Bigquery.Datasets.Insert insert, String datasetId) {
        super(
            BigQueryOperation.GOOGLE_INSERT_BIGQUERY_DATASET,
            clientConfig,
            operationAnnotator,
            insert);
        this.insert = insert;
        this.datasetId = datasetId;
      }

      public String getProjectId() {
        return insert.getProjectId();
      }

      @Override
      protected Optional<CloudResourceUid> resourceUidCreation() {
        return Optional.of(
            new CloudResourceUid()
                .googleBigQueryDatasetUid(
                    new GoogleBigQueryDatasetUid().projectId(getProjectId()).datasetId(datasetId)));
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("projectId", getProjectId());
        result.add("content", new Gson().toJsonTree(insert.getJsonContent()).getAsJsonObject());
        return result;
      }
    }

    /** See {@link Bigquery.Datasets#list(String)} */
    public List list(String projectId) throws IOException {
      return new List(datasets.list(projectId));
    }

    /** See {@link Bigquery.Datasets#list(String)} */
    public class List extends AbstractRequestCow<DatasetList> {

      private final Bigquery.Datasets.List list;

      private List(Bigquery.Datasets.List list) {
        super(
            BigQueryOperation.GOOGLE_LIST_BIGQUERY_DATASET, clientConfig, operationAnnotator, list);
        this.list = list;
      }

      /** See {@link BigQuery.Datasets.List#setMaxResults(Long)}. */
      public List setMaxResults(Long maxResults) {
        this.list.setMaxResults(maxResults);
        return this;
      }

      public String getProjectId() {
        return list.getProjectId();
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("projectId", getProjectId());
        result.addProperty("maxResults", list.getMaxResults());
        return result;
      }
    }

    /** See {@link Bigquery.Datasets#patch(String, String, Dataset)} */
    public Patch patch(String projectId, String datasetId, Dataset content) throws IOException {
      return new Patch(datasets.patch(projectId, datasetId, content));
    }

    /** See {@link Bigquery.Datasets#patch(String, String, Dataset)} */
    public class Patch extends AbstractRequestCow<Dataset> {

      private final Bigquery.Datasets.Patch patch;

      private Patch(Bigquery.Datasets.Patch patch) {
        super(
            BigQueryOperation.GOOGLE_PATCH_BIGQUERY_DATASET,
            clientConfig,
            operationAnnotator,
            patch);
        this.patch = patch;
      }

      public String getProjectId() {
        return patch.getProjectId();
      }

      public String getDatasetId() {
        return patch.getDatasetId();
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("projectId", getProjectId());
        result.addProperty("datasetId", getDatasetId());
        result.add("content", new Gson().toJsonTree(patch.getJsonContent()).getAsJsonObject());
        return result;
      }
    }

    /** See {@link Bigquery.Datasets#update(String, String, Dataset)} */
    public Update update(String projectId, String datasetId, Dataset content) throws IOException {
      return new Update(datasets.update(projectId, datasetId, content));
    }

    /** See {@link Bigquery.Datasets#update(String, String, Dataset)} */
    public class Update extends AbstractRequestCow<Dataset> {

      private final Bigquery.Datasets.Update update;

      private Update(Bigquery.Datasets.Update update) {
        super(
            BigQueryOperation.GOOGLE_UPDATE_BIGQUERY_DATASET,
            clientConfig,
            operationAnnotator,
            update);
        this.update = update;
      }

      public String getProjectId() {
        return update.getProjectId();
      }

      public String getDatasetId() {
        return update.getDatasetId();
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("projectId", getProjectId());
        result.addProperty("datasetId", getDatasetId());
        result.add("content", new Gson().toJsonTree(update.getJsonContent()).getAsJsonObject());
        return result;
      }
    }
  }

  public Tables tables() {
    return new Tables(bigQuery.tables());
  }

  /** See {@link Bigquery.Datasets} */
  public class Tables {

    private final Bigquery.Tables tables;

    private Tables(Bigquery.Tables tables) {
      this.tables = tables;
    }

    /** See {@link Bigquery.Tables#delete(String, String, String)} */
    public Delete delete(String projectId, String datasetId, String tableId) throws IOException {
      return new Tables.Delete(tables.delete(projectId, datasetId, tableId));
    }

    /** See {@link Bigquery.Tables#delete(String, String, String)} */
    public class Delete extends AbstractRequestCow<Void> {

      private final Bigquery.Tables.Delete delete;

      private Delete(Bigquery.Tables.Delete delete) {
        super(
            BigQueryOperation.GOOGLE_DELETE_BIGQUERY_TABLE,
            clientConfig,
            operationAnnotator,
            delete);
        this.delete = delete;
      }

      public String getProjectId() {
        return delete.getProjectId();
      }

      public String getDatasetId() {
        return delete.getDatasetId();
      }

      public String getTableId() {
        return delete.getTableId();
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("projectId", getProjectId());
        result.addProperty("datasetId", getDatasetId());
        result.addProperty("tableId", getTableId());
        return result;
      }
    }

    /** See {@link Bigquery.Tables#get(String, String, String)} */
    public Get get(String projectId, String datasetId, String tableId) throws IOException {
      return new Tables.Get(tables.get(projectId, datasetId, tableId));
    }

    /** See {@link Bigquery.Tables#get(String, String, String)} */
    public class Get extends AbstractRequestCow<Table> {

      private final Bigquery.Tables.Get get;

      private Get(Bigquery.Tables.Get get) {
        super(BigQueryOperation.GOOGLE_GET_BIGQUERY_TABLE, clientConfig, operationAnnotator, get);
        this.get = get;
      }

      public String getProjectId() {
        return get.getProjectId();
      }

      public String getDatasetId() {
        return get.getDatasetId();
      }

      public String getTableId() {
        return get.getTableId();
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("projectId", getProjectId());
        result.addProperty("datasetId", getDatasetId());
        result.addProperty("tableId", getTableId());
        return result;
      }
    }

    /**
     * Wrapper around {@link Bigquery.Tables#getIamPolicy(String, GetIamPolicyRequest)}. For
     * consistency with other methods, this method handles combining projectId, datasetId, and
     * tableId into a single resource identifier.
     */
    public GetIamPolicy getIamPolicy(
        String projectId, String datasetId, String tableId, GetIamPolicyRequest content)
        throws IOException {
      return new Tables.GetIamPolicy(
          tables.getIamPolicy(tableResourceName(projectId, datasetId, tableId), content));
    }

    /** See {@link Bigquery.Tables#getIamPolicy(String, GetIamPolicyRequest)} */
    public class GetIamPolicy extends AbstractRequestCow<Policy> {

      private final Bigquery.Tables.GetIamPolicy getIamPolicy;

      private GetIamPolicy(Bigquery.Tables.GetIamPolicy getIamPolicy) {
        super(
            BigQueryOperation.GOOGLE_GET_IAM_POLICY_BIGQUERY_TABLE,
            clientConfig,
            operationAnnotator,
            getIamPolicy);
        this.getIamPolicy = getIamPolicy;
      }

      public String getResource() {
        return getIamPolicy.getResource();
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("resource", getResource());
        result.add(
            "content", new Gson().toJsonTree(getIamPolicy.getJsonContent()).getAsJsonObject());
        return result;
      }
    }

    /** See {@link Bigquery.Tables#insert(String, String, Table)} */
    public Insert insert(String projectId, String datasetId, Table content) throws IOException {
      return new Tables.Insert(
          tables.insert(projectId, datasetId, content), content.getTableReference().getTableId());
    }

    /** See {@link Bigquery.Tables#insert(String, String, Table)} */
    public class Insert extends AbstractRequestCow<Table> {

      private final Bigquery.Tables.Insert insert;
      private final String tableId;

      private Insert(Bigquery.Tables.Insert insert, String tableId) {
        super(
            BigQueryOperation.GOOGLE_INSERT_BIGQUERY_TABLE,
            clientConfig,
            operationAnnotator,
            insert);
        this.insert = insert;
        this.tableId = tableId;
      }

      public String getProjectId() {
        return insert.getProjectId();
      }

      public String getDatasetId() {
        return insert.getDatasetId();
      }

      @Override
      protected Optional<CloudResourceUid> resourceUidCreation() {
        return Optional.of(
            new CloudResourceUid()
                .googleBigQueryTableUid(
                    new GoogleBigQueryTableUid()
                        .projectId(getProjectId())
                        .datasetId(getDatasetId())
                        .tableId(tableId)));
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("projectId", getProjectId());
        result.addProperty("datasetId", getDatasetId());
        result.add("content", new Gson().toJsonTree(insert.getJsonContent()).getAsJsonObject());
        return result;
      }
    }

    /** See {@link Bigquery.Tables#list(String, String)} */
    public List list(String projectId, String datasetId) throws IOException {
      return new Tables.List(tables.list(projectId, datasetId));
    }

    /** See {@link Bigquery.Tables#list(String, String)} */
    public class List extends AbstractRequestCow<TableList> {

      private final Bigquery.Tables.List list;

      private List(Bigquery.Tables.List list) {
        super(BigQueryOperation.GOOGLE_LIST_BIGQUERY_TABLE, clientConfig, operationAnnotator, list);
        this.list = list;
      }

      public String getProjectId() {
        return list.getProjectId();
      }

      public String getDatasetId() {
        return list.getDatasetId();
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("projectId", getProjectId());
        result.addProperty("datasetId", getDatasetId());
        return result;
      }
    }

    /** See {@link Bigquery.Tables#patch(String, String, String, Table)} */
    public Patch patch(String projectId, String datasetId, String tableId, Table content)
        throws IOException {
      return new Tables.Patch(tables.patch(projectId, datasetId, tableId, content));
    }

    /** See {@link Bigquery.Tables#patch(String, String, String, Table)} */
    public class Patch extends AbstractRequestCow<Table> {

      private final Bigquery.Tables.Patch patch;

      private Patch(Bigquery.Tables.Patch patch) {
        super(
            BigQueryOperation.GOOGLE_PATCH_BIGQUERY_TABLE, clientConfig, operationAnnotator, patch);
        this.patch = patch;
      }

      public String getProjectId() {
        return patch.getProjectId();
      }

      public String getDatasetId() {
        return patch.getDatasetId();
      }

      public String getTableId() {
        return patch.getTableId();
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("projectId", getProjectId());
        result.addProperty("datasetId", getDatasetId());
        result.addProperty("tableId", getTableId());
        result.add("content", new Gson().toJsonTree(patch.getJsonContent()).getAsJsonObject());
        return result;
      }
    }

    /**
     * Wrapper around {@link Bigquery.Tables#setIamPolicy(String, SetIamPolicyRequest)}. For
     * consistency with other methods, this method handles combining projectId, datasetId, and
     * tableId into a single resource identifier.
     */
    public SetIamPolicy setIamPolicy(
        String projectId, String datasetId, String tableId, SetIamPolicyRequest content)
        throws IOException {
      return new Tables.SetIamPolicy(
          tables.setIamPolicy(tableResourceName(projectId, datasetId, tableId), content));
    }

    /** See {@link Bigquery.Tables#setIamPolicy(String, SetIamPolicyRequest)} */
    public class SetIamPolicy extends AbstractRequestCow<Policy> {

      private final Bigquery.Tables.SetIamPolicy setIamPolicy;

      private SetIamPolicy(Bigquery.Tables.SetIamPolicy setIamPolicy) {
        super(
            BigQueryOperation.GOOGLE_SET_IAM_POLICY_BIGQUERY_TABLE,
            clientConfig,
            operationAnnotator,
            setIamPolicy);
        this.setIamPolicy = setIamPolicy;
      }

      public String getResource() {
        return setIamPolicy.getResource();
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("resource", getResource());
        result.add(
            "content", new Gson().toJsonTree(setIamPolicy.getJsonContent()).getAsJsonObject());
        return result;
      }
    }

    /**
     * Wrapper around {@link Bigquery.Tables#testIamPermissions(String, TestIamPermissionsRequest)}.
     * For consistency with other methods, this method handles combining projectId, datasetId, and
     * tableId into a single resource identifier.
     */
    public TestIamPermissions testIamPermissions(
        String projectId, String datasetId, String tableId, TestIamPermissionsRequest content)
        throws IOException {
      return new Tables.TestIamPermissions(
          tables.testIamPermissions(tableResourceName(projectId, datasetId, tableId), content));
    }

    /** See {@link Bigquery.Tables#testIamPermissions(String, TestIamPermissionsRequest)} */
    public class TestIamPermissions extends AbstractRequestCow<TestIamPermissionsResponse> {

      private final Bigquery.Tables.TestIamPermissions testIamPermissions;

      private TestIamPermissions(Bigquery.Tables.TestIamPermissions testIamPermissions) {
        super(
            BigQueryOperation.GOOGLE_TEST_IAM_POLICY_BIGQUERY_TABLE,
            clientConfig,
            operationAnnotator,
            testIamPermissions);
        this.testIamPermissions = testIamPermissions;
      }

      public String getResource() {
        return testIamPermissions.getResource();
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("resource", getResource());
        result.add(
            "content",
            new Gson().toJsonTree(testIamPermissions.getJsonContent()).getAsJsonObject());
        return result;
      }
    }

    /** See {@link Bigquery.Tables#update(String, String, String, Table)} */
    public Update update(String projectId, String datasetId, String tableId, Table content)
        throws IOException {
      return new Tables.Update(tables.update(projectId, datasetId, tableId, content));
    }

    /** See {@link Bigquery.Tables#update(String, String, String, Table)} */
    public class Update extends AbstractRequestCow<Table> {

      private final Bigquery.Tables.Update update;

      private Update(Bigquery.Tables.Update update) {
        super(
            BigQueryOperation.GOOGLE_UPDATE_BIGQUERY_TABLE,
            clientConfig,
            operationAnnotator,
            update);
        this.update = update;
      }

      public String getProjectId() {
        return update.getProjectId();
      }

      public String getDatasetId() {
        return update.getDatasetId();
      }

      public String getTableId() {
        return update.getTableId();
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("projectId", getProjectId());
        result.addProperty("datasetId", getDatasetId());
        result.addProperty("tableId", getTableId());
        result.add("content", new Gson().toJsonTree(update.getJsonContent()).getAsJsonObject());
        return result;
      }
    }

    private String tableResourceName(String projectId, String datasetId, String tableId) {
      return String.format("projects/%s/datasets/%s/tables/%s", projectId, datasetId, tableId);
    }
  }
}
