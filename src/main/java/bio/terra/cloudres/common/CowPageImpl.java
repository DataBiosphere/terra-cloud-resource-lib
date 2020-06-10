package bio.terra.cloudres.common;

import static com.google.common.collect.Iterables.transform;

import com.google.api.gax.paging.Page;
import com.google.common.base.Function;

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
