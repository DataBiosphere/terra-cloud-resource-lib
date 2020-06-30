package bio.terra.cloudres.common.cleanup;

import bio.terra.cloudres.resources.CloudResourceUid;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;

/**
 * A {@link CleanupRecorder} that records resources in memory and passes through to another {@link
 * CleanupRecorder}.
 */
public class InMemoryCleanupRecorder implements CleanupRecorder {
  private final Multimap<CloudResourceUid, CleanupConfig> resources = ArrayListMultimap.create();
  private final CleanupRecorder originalRecorder;

  public InMemoryCleanupRecorder(CleanupRecorder originalRecorder) {
    this.originalRecorder = originalRecorder;
  }

  @Override
  public void record(CloudResourceUid resource, CleanupConfig cleanupConfig) {
    resources.put(resource, cleanupConfig);
    originalRecorder.record(resource, cleanupConfig);
  }

  public Collection<CleanupConfig> getRecords(CloudResourceUid resource) {
    return resources.get(resource);
  }

  public boolean hasRecord(CloudResourceUid resource) {
    return resources.containsKey(resource);
  }
}
