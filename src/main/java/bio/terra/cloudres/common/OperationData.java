package bio.terra.cloudres.common;

import com.google.auto.value.AutoValue;
import com.google.gson.JsonObject;
import java.time.Duration;
import java.util.Optional;
import java.util.OptionalInt;

/** Captures data related to a cloud operation for metrics, logging, and tracing. */
@AutoValue
public abstract class OperationData {
  /** Latency of the cloud request. */
  public abstract Duration duration();

  /** Number of retries performed, or empty if retries are not recorded. */
  public abstract OptionalInt tryCount();

  /** Exception thrown by the cloud request, or empty. */
  public abstract Optional<Exception> executionException();

  /** HTTP status code of the cloud response, or empty if not recorded. */
  public abstract OptionalInt httpStatusCode();

  /** The {@link CloudOperation} performed. */
  public abstract bio.terra.cloudres.common.CloudOperation cloudOperation();

  /** Serialized request data in JSON format. */
  public abstract JsonObject requestData();

  public static Builder builder() {
    return new AutoValue_OperationData.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setDuration(Duration duration);

    public abstract Builder setTryCount(int tryCount);

    public abstract Builder setTryCount(OptionalInt tryCount);

    public abstract Builder setExecutionException(Exception executionException);

    public abstract Builder setExecutionException(Optional<Exception> executionException);

    public abstract Builder setHttpStatusCode(int httpStatusCode);

    public abstract Builder setHttpStatusCode(OptionalInt httpStatusCode);

    public abstract Builder setCloudOperation(
        bio.terra.cloudres.common.CloudOperation cloudOperation);

    public abstract Builder setRequestData(JsonObject requestData);

    public abstract OperationData build();
  }
}
