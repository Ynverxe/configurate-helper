package com.github.ynverxe.configuratehelper.handler;

import com.github.ynverxe.configuratehelper.handler.content.ContentProvider;
import com.github.ynverxe.configuratehelper.handler.content.ContentChannel;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;

/**
 * An extendable helper class to load/save {@link CommentedConfigurationNode}
 * from/to a file more easily.
 */
public class FastConfiguration {

  private final @NotNull ContentChannel destinationContentProvider;
  /**
   * The provider of the fallback content, used
   * when destination content is missing.
   */
  private final @Nullable ContentProvider fallbackContentProvider;
  private final AbstractConfigurationLoader<CommentedConfigurationNode> nodeLoader;
  private final SourceContentProvider sourceContentProvider;

  private volatile @NotNull CommentedConfigurationNode node = CommentedConfigurationNode.root();

  public FastConfiguration(
      @NotNull ContentChannel destinationContentProvider,
      @Nullable ContentProvider fallbackContentProvider,
      AbstractConfigurationLoader.Builder<?, ?
          extends AbstractConfigurationLoader<CommentedConfigurationNode>> builder) {
    this.destinationContentProvider = destinationContentProvider;
    this.fallbackContentProvider = fallbackContentProvider;
    this.sourceContentProvider = new SourceContentProvider(destinationContentProvider, fallbackContentProvider);

    this.nodeLoader = builder
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
  public @Nullable CommentedConfigurationNode node() {
    return node;
  }

  /**
   * Attempts to load a {@link CommentedConfigurationNode}
   * using the content provided by {@link SourceContentProvider}.
   */
  public @NotNull CommentedConfigurationNode load() {
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
  public @NotNull CommentedConfigurationNode loadFallbackContents() throws IllegalStateException {
    sourceContentProvider.checkNotNullFallback();
    sourceContentProvider.loadFallbackResource = true;
    return load();
  }

  public void save(@NotNull CommentedConfigurationNode node) throws ConfigurateException {
    this.nodeLoader.save(node);
  }

  public void save() throws ConfigurateException {
    this.nodeLoader.save(node);
  }
}