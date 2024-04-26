package com.github.ynverxe.configuratehelper.handler.content;

import java.io.IOException;
import java.io.InputStream;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface ContentProvider {
  @Nullable InputStream inputStream() throws IOException;

  @NonNull String asString();
}