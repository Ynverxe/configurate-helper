package com.github.ynverxe.configuratehelper.test;

import com.github.ynverxe.configuratehelper.handler.FastConfiguration;
import java.nio.file.Path;
import java.nio.file.Paths;
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

import static com.github.ynverxe.configuratehelper.handler.content.ContentChannel.*;

@TestMethodOrder(OrderAnnotation.class)
public class FastConfigurationTest {

  private static final ConfigurationNode FALLBACK_NODE;

  static {
    try {
      FALLBACK_NODE = CommentedConfigurationNode.root();
      FALLBACK_NODE.node("node")
          .node("text")
          .set("Use configurate :D");
    } catch (SerializationException e) {
      throw new RuntimeException(e);
    }
  }

  private static final Path TEST_DIR = Paths.get("src/test");
  private static final Path DESTINATION_PATH = TEST_DIR.resolve("java/out/configuration.yaml");
  private static final Path FALLBACK_RESOURCE_PATH = Paths.get("fallback.yaml");

  @Test
  @Order(1)
  public void testFallbackLoad() throws Exception {
    FastConfiguration configuration = new FastConfiguration(
      path(DESTINATION_PATH), contextResource(FALLBACK_RESOURCE_PATH), YamlConfigurationLoader.builder()
        .indent(2)
    );

    assertEquals(FALLBACK_NODE, configuration.node());
  }

  @Test
  @Order(2)
  public void testDestinationNotEmpty() {
    FastConfiguration configuration = new FastConfiguration(
        path(DESTINATION_PATH), null, YamlConfigurationLoader.builder()
        .indent(2)
    );

    assertEquals(FALLBACK_NODE, configuration.node());
  }

  @AfterAll
  public static void clear() {
    DESTINATION_PATH.toFile().delete();
  }
}