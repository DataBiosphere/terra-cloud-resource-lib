package bio.terra.cloudres.google.storage;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.cloudres.common.OperationAnnotator;
import com.google.cloud.ReadChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.CopyWriter;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A Cloud Object Wrapper(COW) for {@link Blob}. */
public class BlobCow {
  private final Logger logger = LoggerFactory.getLogger(BlobCow.class);

  private final OperationAnnotator operationAnnotator;
  private final Blob blob;

  public BlobCow(ClientConfig clientConfig, Blob blob) {
    this.operationAnnotator = new OperationAnnotator(clientConfig, logger);
    this.blob = blob;
  }

  /** See {@link Blob#copyTo(BlobId, Blob.BlobSourceOption...)} */
  public CopyWriter copyTo(BlobId targetblob) {
    return operationAnnotator.executeCowOperation(
        CloudOperation.GOOGLE_COPY_BLOB,
        () -> blob.copyTo(targetblob),
        () -> {
          // TODO probably need to adjust for serialization.
          JsonObject request = new JsonObject();
          request.add("source", SerializeUtils.convert(blob.getBlobId()));
          request.add("target", SerializeUtils.convert(targetblob));
          return request;
        });
  }

  /** See {@link Blob#delete(Blob.BlobSourceOption...)} */
  public boolean delete() {
    return operationAnnotator.executeCowOperation(
        CloudOperation.GOOGLE_DELETE_BLOB,
        () -> blob.delete(),
        () -> SerializeUtils.convert(blob.getBlobId()));
  }

  /** See {@link Blob#reader(Blob.BlobSourceOption...)}. */
  public ReadChannel reader() {
    return operationAnnotator.executeCowOperation(
        CloudOperation.GOOGLE_READ_BLOB,
        () -> blob.reader(),
        () -> SerializeUtils.convert(blob.getBlobId()));
  }
}
