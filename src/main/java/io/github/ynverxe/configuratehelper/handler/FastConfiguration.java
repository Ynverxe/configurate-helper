package io.github.ynverxe.configuratehelper.handler;

import io.github.ynverxe.configuratehelper.handler.content.ContentProvider;
import io.github.ynverxe.configuratehelper.handler.content.ContentChannel;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import io.github.ynverxe.configuratehelper.handler.factory.ConfigurationLoaderFactory;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;

/**
 * An extendable helper class to load/save {@link ConfigurationNode}
 * from/to a file more easily.
 */
@Deprecated
@ApiStatus.ScheduledForRemoval
public class FastConfiguration {

  private final @NotNull ContentChannel destinationContentProvider;
  /**
   * The provider of the fallback content, used
   * when destination content is missing.
   */
  private final @Nullable ContentProvider fallbackContentProvider;
  private final AbstractConfigurationLoader<? extends ConfigurationNode> nodeLoader;
  private final SourceContentProvider sourceContentProvider;

  private volatile @NotNull ConfigurationNode node = CommentedConfigurationNode.root();

  public FastConfiguration(
      @NotNull ContentChannel destinationContentProvider,
      @Nullable ContentProvider fallbackContentProvider,
      ConfigurationLoaderFactory configurationLoaderFactory) {
    this.destinationContentProvider = destinationContentProvider;
    this.fallbackContentProvider = fallbackContentProvider;
    this.sourceContentProvider = new SourceContentProvider(destinationContentProvider, fallbackContentProvider);

    this.nodeLoader = configurationLoaderFactory.create()
        .sink(() -> new BufferedWriter(new OutputStreamWriter(destinationContentProvider.outputStream())))
        .source(this.sourceContentProvider)
        .build();

    load();
  }

  public @NotNull ContentChannel destinationContentProvider() {
    return destinationContentProvider;
  }

  public @Nullable ContentProvider fallbackContentProvider() {
    return fallbackContentProvider;
  }

  /**
   * @return The last loaded node.
   */
  public @Nullable ConfigurationNode node() {
    return node;
  }

  /**
   * Attempts to load a {@link ConfigurationNode}
   * using the content provided by {@link SourceContentProvider}.
   */
  public @NotNull ConfigurationNode load() {
    try {
      boolean forcedDefaults = sourceContentProvider.loadFallbackResource;

      this.node = nodeLoader.load();

      if (!forcedDefaults && sourceContentProvider.lastUsedProvider == sourceContentProvider.fallbackContentProvider) {
        save(this.node);
      }

      return this.node;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Attempts to load the fallback resource contents.
   *
   * @throws IllegalStateException If no fallback resource path was provided
   */
  public @NotNull ConfigurationNode loadFallbackContents() throws IllegalStateException {
    sourceContentProvider.checkNotNullFallback();
    sourceContentProvider.loadFallbackResource = true;
    return load();
  }

  public void save(@NotNull ConfigurationNode node) throws ConfigurateException {
    this.nodeLoader.save(node);
  }

  public void save() throws ConfigurateException {
    this.nodeLoader.save(node);
  }
}