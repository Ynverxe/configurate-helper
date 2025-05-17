package com.github.ynverxe.configuratehelper.test;

import com.github.ynverxe.configuratehelper.handler.FastConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import com.github.ynverxe.configuratehelper.handler.source.URLConfigurationFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class FastConfigurationTest {

  private static final ConfigurationNode FALLBACK_NODE_EXPECT;

  static {
    try {
      FALLBACK_NODE_EXPECT = CommentedConfigurationNode.root();
      FALLBACK_NODE_EXPECT.node("node")
          .node("text")
          .set("Use configurate :D");
    } catch (SerializationException e) {
      throw new RuntimeException(e);
    }
  }

  public static final URLConfigurationFactory FACTORY = URLConfigurationFactory.newBuilder()
      .destContentRoot(Paths.get("src/test/java/out"))
      .classLoader(ClassLoader.getSystemClassLoader())
      .configurationLoaderFactory(() -> YamlConfigurationLoader.builder().indent(2))
      .build();

  @Test
  @Order(1)
  public void testFallbackLoad() throws Exception {
    FastConfiguration configuration = FACTORY.create(
        "fallback.yaml", "configuration.yml");

    assertEquals(FALLBACK_NODE_EXPECT, configuration.node());
  }

  @Test
  @Order(2)
  public void assertDestinationWrite() throws IOException {
    FastConfiguration configuration = FACTORY.create(null, "configuration.yml");

    assertEquals(FALLBACK_NODE_EXPECT, configuration.node());
  }

  @AfterAll
  public static void clear() throws IOException {
    new File("src/test/java/out").delete();
  }
}