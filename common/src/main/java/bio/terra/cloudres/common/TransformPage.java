package bio.terra.cloudres.common;

import static com.google.common.collect.Iterables.transform;

import com.google.api.gax.paging.Page;
import com.google.common.base.Function;

/**
 * Implementation for {@link Page} interface which supports transform {@code Page<F>}to {@code
 * Page<T>}
 */
public class TransformPage<F, T> implements Page<T> {
  private final Function<? super F, ? extends T> transformFn;
  private final Page<F> originalPage;

  public TransformPage(Page<F> originalPage, Function<F, T> transformFn) {
    this.transformFn = transformFn;
    this.originalPage = originalPage;
  }

  @Override
  public Iterable<T> iterateAll() {
    return transform(originalPage.iterateAll(), transformFn);
  }

  @Override
  public Page<T> getNextPage() {
    return new TransformPage(originalPage.getNextPage(), transformFn);
  }

  @Override
  public Iterable<T> getValues() {
    return transform(originalPage.getValues(), transformFn);
  }

  @Override
  public String getNextPageToken() {
    return originalPage.getNextPageToken();
  }

  @Override
  public boolean hasNextPage() {
    return originalPage.hasNextPage();
  }
}
