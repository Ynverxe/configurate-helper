package com.github.ynverxe.configuratehelper.handler.content;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
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
}