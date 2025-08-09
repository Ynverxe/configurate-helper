package io.github.ynverxe.configuratehelper.handler.source;

import io.github.ynverxe.configuratehelper.handler.FastConfiguration;
import io.github.ynverxe.configuratehelper.handler.content.ContentChannel;
import io.github.ynverxe.configuratehelper.handler.content.ContentProvider;
import io.github.ynverxe.configuratehelper.handler.factory.ConfigurationLoaderFactory;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Creates {@link FastConfiguration} using {@link URLConfigurationFactory#create(Path, Path)} parameters
 * as relative paths to fallback and destination contents, using {@link #fallbackContentRoot} and {@link #destContentRoot}
 * as root directories.
 * This factory has an optional mode called "resource mode" that is enabled when a {@link #classLoader} isn't null. In resource
 * mode, this factory searches for fallback content using {@link ClassLoader#getResource(String)} to find the fallback content
 * instead of searching it in the system file hierarchy.
 */
@ApiStatus.Experimental
public class URLConfigurationFactory {

  private final @NotNull Path fallbackContentRoot;
  private final @NotNull Path destContentRoot;
  private final @NotNull ConfigurationLoaderFactory configurationLoaderFactory;
  /**
   * If not null, fallback content data will be searched into the classpath,
   * otherwise it will be searched in the system file hierarchy.
   */
  private final @Nullable ClassLoader classLoader;

  public URLConfigurationFactory(@NotNull Path fallbackContentRoot, @NotNull Path destContentRoot, @NotNull ConfigurationLoaderFactory configurationLoaderFactory, @Nullable ClassLoader classLoader) {
    this.fallbackContentRoot = fallbackContentRoot;
    this.destContentRoot = destContentRoot;
    this.configurationLoaderFactory = configurationLoaderFactory;
    this.classLoader = classLoader;
  }

  public @NotNull Optional<ConfigurationNode> loadConfigurationInFilesystem(@NotNull String relativePath) throws IOException {
    return loadConfigurationInFilesystem(Paths.get(relativePath));
  }

  public @NotNull Optional<ConfigurationNode> loadConfigurationInFilesystem(@NotNull Path relativePath) throws IOException {
    Path absolutePath = this.destContentRoot.resolve(relativePath);

    if (!Files.exists(absolutePath))
      return Optional.empty();

    AbstractConfigurationLoader<? extends ConfigurationNode> loader = this.configurationLoaderFactory.create()
        .path(absolutePath)
        .build();

    return Optional.of(loader.load());
  }

  public void saveConfigurationNodeInFilesystem(@NotNull Path relativePath, @NotNull ConfigurationNode node) throws IOException {
    Path absolutePath = this.destContentRoot.resolve(relativePath);

    AbstractConfigurationLoader<? extends ConfigurationNode> loader = this.configurationLoaderFactory.create()
        .path(absolutePath)
        .build();

    loader.save(node);
  }

  public void saveConfigurationNodeInFilesystem(@NotNull String relativePath, @NotNull ConfigurationNode node) throws IOException {
    saveConfigurationNodeInFilesystem(Paths.get(relativePath), node);
  }

  public @NotNull Optional<ConfigurationNode> loadConfigurationFromResource(@NotNull Path relativePath) throws IOException {
    Objects.requireNonNull(this.classLoader, "No ClassLoader provided");

    Path absolutePath = this.fallbackContentRoot.resolve(relativePath);
    URL urlToResource = this.classLoader.getResource(absolutePath.toString());

    if (urlToResource == null) {
      return Optional.empty();
    }

    return retrieveAndLoadConfiguration(urlToResource);
  }

  public @NotNull Optional<ConfigurationNode> loadConfigurationFromResource(@NotNull String relativePath) throws IOException {
    return loadConfigurationFromResource(Paths.get(relativePath));
  }

  public @NotNull Optional<ConfigurationNode> retrieveAndLoadConfiguration(@NotNull URL url) throws IOException {
    ContentProvider provider = ContentChannel.create(url);

    InputStream stream = provider.inputStream();
    if (stream == null) {
      return Optional.empty();
    }

    AbstractConfigurationLoader<? extends ConfigurationNode> loader = this.configurationLoaderFactory.create()
        .source(() -> new BufferedReader(new InputStreamReader(stream)))
        .build();

    try {
      return Optional.of(loader.load());
    } finally {
      stream.close();
    }
  }

  @Deprecated
  @ApiStatus.ScheduledForRemoval
  public @NotNull FastConfiguration create(@Nullable Path pathToFallbackContent, @NotNull Path destContentPath) throws IllegalStateException, IOException {
    ContentProvider fallbackContent = null;
    ContentChannel destContent = ContentChannel.create(destContentRoot.resolve(destContentPath).toUri().toURL());
    if (pathToFallbackContent != null) {
      Path absolutePathToFallbackContent = fallbackContentRoot.resolve(pathToFallbackContent);

      if (classLoader != null) { // search resource
        URL fallbackContentUrl = classLoader.getResource(absolutePathToFallbackContent.toString());

        if (fallbackContentUrl == null)
          throw new IllegalStateException("Resource at " + absolutePathToFallbackContent.toAbsolutePath() + " doesn't exists");

        fallbackContent = ContentChannel.create(fallbackContentUrl);
      } else { // search in system file hierarchy
        fallbackContent = ContentChannel.create(absolutePathToFallbackContent.toUri().toURL());
      }
    }

    return new FastConfiguration(destContent, fallbackContent, configurationLoaderFactory);
  }

  @Deprecated
  @ApiStatus.ScheduledForRemoval
  public @NotNull FastConfiguration create(@Nullable String pathToFallbackContent, @NotNull String destContentPath) throws IllegalStateException, IOException {
    return create(pathToFallbackContent != null ? Paths.get(pathToFallbackContent) : null, Paths.get(destContentPath));
  }

  public @NotNull Path fallbackContentRoot() {
    return fallbackContentRoot;
  }

  public @NotNull Path destContentRoot() {
    return destContentRoot;
  }

  public @NotNull ConfigurationLoaderFactory configurationLoaderFactory() {
    return configurationLoaderFactory;
  }

  public @Nullable ClassLoader classLoader() {
    return classLoader;
  }

  public @NotNull Builder toBuilder() {
    return new Builder()
        .fallbackContentRoot(fallbackContentRoot)
        .destContentRoot(destContentRoot)
        .configurationLoaderFactory(configurationLoaderFactory)
        .classLoader(classLoader);
  }

  public boolean isResourceModeEnabled() {
    return classLoader != null;
  }

  public static @NotNull Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private @MonotonicNonNull Path fallbackContentRoot;
    private @MonotonicNonNull Path destContentRoot;
    private @MonotonicNonNull ConfigurationLoaderFactory configurationLoaderFactory;
    private @Nullable ClassLoader classLoader;

    public Builder fallbackContentRoot(@Nullable Path fallbackContentRoot) {
      this.fallbackContentRoot = fallbackContentRoot;
      return this;
    }

    public Builder fallbackContentRoot(@Nullable String fallbackContentRoot) {
      return fallbackContentRoot(fallbackContentRoot != null ? Paths.get(fallbackContentRoot) : null);
    }

    public Builder destContentRoot(@NotNull Path destContentRoot) {
      this.destContentRoot = Objects.requireNonNull(destContentRoot);
      return this;
    }

    public Builder destContentRoot(@NotNull String destContentRoot) {
      return destContentRoot(Paths.get(Objects.requireNonNull(destContentRoot)));
    }

    public Builder configurationLoaderFactory(@NotNull ConfigurationLoaderFactory configurationLoaderFactory) {
      this.configurationLoaderFactory = Objects.requireNonNull(configurationLoaderFactory);
      return this;
    }

    public Builder classLoader(@Nullable ClassLoader classLoader) {
      this.classLoader = classLoader;
      return this;
    }

    public Builder withContextClassLoader() {
      return classLoader(Thread.currentThread().getContextClassLoader());
    }

    public Builder useClassClassLoader(@NotNull Class<?> clazz) {
      return classLoader(clazz.getClassLoader());
    }

    public @NotNull URLConfigurationFactory build() {
      if (fallbackContentRoot == null) {
        fallbackContentRoot = Paths.get("");
      }

      return new URLConfigurationFactory(
          fallbackContentRoot,
          Objects.requireNonNull(destContentRoot, "destContentRoot not provided"),
          Objects.requireNonNull(configurationLoaderFactory, "configuration loader builder not provided"),
          classLoader
      );
    }
  }
}