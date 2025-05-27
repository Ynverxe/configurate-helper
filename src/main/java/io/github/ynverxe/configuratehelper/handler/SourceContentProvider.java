package io.github.ynverxe.configuratehelper.handler;

import io.github.ynverxe.configuratehelper.handler.content.ContentProvider;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.util.Objects.requireNonNull;

@Internal
public class SourceContentProvider implements Callable<BufferedReader> {

  private final @NotNull ContentProvider destinationContentProvider;
  final @Nullable ContentProvider fallbackContentProvider;
  boolean loadFallbackResource;
  ContentProvider lastUsedProvider;

  public SourceContentProvider(@NotNull ContentProvider destinationContentProvider,
      @Nullable ContentProvider fallbackContentProvider) {
    this.destinationContentProvider = requireNonNull(destinationContentProvider, "destinationContentProvider");
    this.fallbackContentProvider = fallbackContentProvider;
  }

  /**
   * Attempts to found a valid content source.
   * If the destination {@link ContentProvider} isn't
   * able to return a non-null {@link InputStream}
   * the fallback {@link ContentProvider} should be used.
   *
   * @throws IOException If an I/O error occurs
   * @throws IllegalStateException If destination content provider returns null and no fallback
   * content provider was provided or if the fallback content provider returns null.
   */
  @Override
  public BufferedReader call() throws IOException, IllegalArgumentException {
    InputStream stream;

    if (loadFallbackResource) {
      loadFallbackResource = false;
      stream = getFallbackContent();
    } else {
      stream = destinationContentProvider.inputStream();
      this.lastUsedProvider = destinationContentProvider;

      if (stream == null) {
        try {
          stream = getFallbackContent();
        } catch (IllegalStateException e) {
          throw new IllegalStateException(
              "Resource '"
                  + destinationContentProvider.asString()
                  + "' "
                  + "is missing and cannot found a fallback provider");
        }
      }
    }

    return new BufferedReader(new InputStreamReader(stream));
  }

  private @NotNull InputStream getFallbackContent() throws IOException {
    checkNotNullFallback();

    InputStream stream = fallbackContentProvider.inputStream();
    if (stream == null) {
      throw new IllegalStateException("Fallback content '" + fallbackContentProvider.asString() + "' is "
          + "missing");
    }

    this.lastUsedProvider = fallbackContentProvider;

    return stream;
  }

  void checkNotNullFallback() {
    if (fallbackContentProvider == null) {
      throw new IllegalStateException("No fallback ContentProvider provided");
    }
  }
}
