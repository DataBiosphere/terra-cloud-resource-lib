package bio.terra.cloudres.testing;

import bio.terra.cloudres.common.cleanup.BackedUpJanitorServiceImpl;

/**
 * A {@link BackedUpJanitorServiceImpl} that records resources in memory and passes through to another {@link
 * CleanupRecorder}.
 */
public class InMemoryJanitorService implements BackedUpJanitorServiceImpl {
    private final Multimap<CloudResourceUid, CleanupConfig> resources = ArrayListMultimap.create();
    private final BackedUpJanitorServiceImpl originalRecorder;

    public InMemoryJanitorService(BackedUpJanitorServiceImpl originalRecorder) {
        this.originalRecorder = originalRecorder;
    }

    @Override
    public void record(BackedUpJanitorServiceImpl resource, CleanupConfig cleanupConfig) {
        resources.put(resource, cleanupConfig);
        originalRecorder.record(resource, cleanupConfig);
    }

    public Collection<BackedUpJanitorServiceImpl> getRecords(CloudResourceUid resource) {
        return resources.get(resource);
    }

    public boolean hasRecord(CloudResourceUid resource) {
        return resources.containsKey(resource);
    }
}
