package com.github.ynverxe.configuratehelper.handler.factory;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;

public interface ConfigurationLoaderFactory {

  @NotNull AbstractConfigurationLoader.Builder<?, ? extends AbstractConfigurationLoader<? extends ConfigurationNode>> create();

}