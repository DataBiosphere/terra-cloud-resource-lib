package bio.terra.cloudres.common;

import bio.terra.cloudres.google.bigquery.TableCow;
import com.google.api.gax.paging.Page;
import com.google.cloud.bigquery.Table;
import com.google.common.base.Function;
import com.google.common.collect.Streams;

import java.util.stream.Collectors;

import static com.google.common.collect.Iterables.transform;

/** Implementation for {@link Page} interface for Cow */
public abstract class CowPageImpl<T, C> implements Page<C> {
    private final ClientConfig clientConfig;
    private final Page<T> originalPage;

    protected CowPageImpl(ClientConfig clientConfig, Page<T> originalPage) {
        this.clientConfig = clientConfig;
        this.originalPage = originalPage;
    }

    @Override
    public Iterable<C> iterateAll() {
        return transform(originalPage.iterateAll(), getTransformFunction());
    }

    @Override
    public Iterable<C> getValues() {
        return transform(originalPage.getValues(), getTransformFunction());
    }

    @Override
    public String getNextPageToken() {
        return originalPage.getNextPageToken();
    }

    @Override
    public boolean hasNextPage() {
        return originalPage.hasNextPage();
    }

    protected Page<T> getOriginalPage() {
       return originalPage;
    }

    protected ClientConfig getClientConfig() {
        return clientConfig;
    }

    protected abstract Function<T, C> getTransformFunction();
}