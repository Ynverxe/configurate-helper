package com.github.ynverxe.configuratehelper.handler.content;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

public class FileConnectionBasedContentProvider extends URLConnectionBasedContentProvider {

  public FileConnectionBasedContentProvider(@NonNull URL urlToContent) {
    super(urlToContent);
  }

  /**
   * {@link sun.net.www.protocol.file.FileURLConnection} doesn't allow
   * output.
   */
  @Override
  public @NotNull OutputStream outputStream() throws IOException {
    Path path = Paths.get(urlToContent.getFile());
    if (!Files.exists(path)) {
      Files.createDirectories(path.getParent());
      Files.createFile(path);
    }

    return Files.newOutputStream(Paths.get(urlToContent.getPath()));
  }
}