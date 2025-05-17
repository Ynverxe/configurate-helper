package com.github.ynverxe.configuratehelper.handler.content;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

public interface ContentChannel extends ContentProvider {
  @NotNull OutputStream outputStream() throws IOException;

  static @NotNull ContentChannel create(@NonNull URL url) {
    if ("file".equals(url.getProtocol())) {
      return new FileConnectionBasedContentProvider(url);
    }

    return new URLConnectionBasedContentProvider(url);
  }
}