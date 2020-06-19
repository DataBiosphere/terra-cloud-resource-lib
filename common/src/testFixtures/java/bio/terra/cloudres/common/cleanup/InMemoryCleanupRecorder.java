package bio.terra.cloudres.common.cleanup;

import bio.terra.cloudres.resources.CloudResourceUid;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;

/** An in-memory {@link CleanupRecorder} for testing. */
public class InMemoryCleanupRecorder implements CleanupRecorder {
  private final Multimap<CloudResourceUid, CleanupConfig> resources = ArrayListMultimap.create();

  @Override
  public void record(CloudResourceUid resource, CleanupConfig cleanupConfig) {
    resources.put(resource, cleanupConfig);
  }

  public Collection<CleanupConfig> getRecords(CloudResourceUid resource) {
    return resources.get(resource);
  }

  public boolean hasRecord(CloudResourceUid resource) {
    return resources.containsKey(resource);
  }
}
