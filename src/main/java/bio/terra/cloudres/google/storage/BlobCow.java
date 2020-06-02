package bio.terra.cloudres.google.storage;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.cloudres.common.CowOperation;
import bio.terra.cloudres.common.OperationAnnotator;
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

    BlobCow(ClientConfig clientConfig, Blob blob) {
        this.operationAnnotator = new OperationAnnotator(clientConfig, logger);
        this.blob = blob;
    }

    /** See {@link Blob#copyTo(BlobId, Blob.BlobSourceOption...)}*/
    public CopyWriter copyTo(BlobId targetblob) {
        return operationAnnotator.executeCowOperation(new CowOperation<CopyWriter>() {
            @Override
            public CloudOperation getCloudOperation() {
                return CloudOperation.GOOGLE_COPY_BLOB;
            }

            @Override
            public CopyWriter execute() {
                return blob.copyTo(targetblob);
            }

            @Override
            public String serializeRequest() {
                // TODO probably need to adjust for serialization.
                JsonObject request = new JsonObject();
                request.addProperty("source", SerializeUtils.convert(blob.getBlobId()));
                request.addProperty("target", SerializeUtils.convert(targetblob));
                return request.toString();
            }
        });
    }

    /** See {@link Blob#delete(Blob.BlobSourceOption...)}*/
    public boolean delete() {
        return operationAnnotator.executeCowOperation(new CowOperation<Boolean>() {
            @Override
            public CloudOperation getCloudOperation() {
                return CloudOperation.GOOGLE_DELETE_BLOB;
            }

            @Override
            public Boolean execute() {
                return blob.delete();
            }

            @Override
            public String serializeRequest() {
                return SerializeUtils.convert(blob.getBlobId());
            }
        });
    }
}
