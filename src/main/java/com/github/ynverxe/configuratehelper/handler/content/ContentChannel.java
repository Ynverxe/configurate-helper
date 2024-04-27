package com.github.ynverxe.configuratehelper.handler.content;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

public interface ContentChannel extends ContentProvider {
  @NotNull OutputStream outputStream() throws IOException;

  static @NotNull ContentChannel url(@NonNull URL url) {
    if ("file".equals(url.getProtocol())) {
      return new FileConnectionBasedContentProvider(url);
    }

    return new URLConnectionBasedContentProvider(url);
  }

  static @NonNull ContentChannel path(@NonNull Path path) {
    try {
      return url(path.toUri().toURL());
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  static @NonNull ContentChannel resource(@NonNull Path path, @NonNull Class<?> clazz) {
    URL url = clazz.getResource(path.toString());

    if (url == null)
      throw new IllegalArgumentException("Missing resource '" + path + "'");

    return url(url);
  }

  static @NonNull ContentChannel resource(@NonNull String pathName, @NonNull Class<?> clazz) {
    return resource(Paths.get(pathName), clazz);
  }


  static @NonNull ContentChannel contextResource(@NonNull Path path) {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();

    if (loader == null) {
      loader = ClassLoader.getSystemClassLoader();
    }

    URL url = loader.getResource(path.toString());

    if (url == null)
      throw new IllegalArgumentException("Missing resource '" + path + "'");

    return url(url);
  }

  static @NonNull ContentChannel contextResource(@NonNull String pathName) {
    return contextResource(Paths.get(pathName));
  }
}