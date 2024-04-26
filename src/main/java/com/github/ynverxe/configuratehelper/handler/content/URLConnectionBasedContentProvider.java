package com.github.ynverxe.configuratehelper.handler.content;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

public class URLConnectionBasedContentProvider implements ContentChannel {

  protected final @NotNull URL urlToContent;

  public URLConnectionBasedContentProvider(@NonNull URL urlToContent) {
    this.urlToContent = urlToContent;
  }

  @Override
  public @Nullable InputStream inputStream() throws IOException {
    URLConnection connection = urlToContent.openConnection();
    connection.setUseCaches(true);
    try {
      return connection.getInputStream();
    } catch (NoSuchFileException | FileNotFoundException e) {
      return null;
    }
  }

  @Override
  public @NonNull String asString() {
    return urlToContent.toString();
  }

  @Override
  public @NotNull OutputStream outputStream() throws IOException {
    URLConnection connection = urlToContent.openConnection();
    connection.setUseCaches(true);
    return connection.getOutputStream();
  }
}
