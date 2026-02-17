package nl.adgroot.pdfsummarizer.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigLoader {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  public static AppConfig load(Path path) throws IOException {

    if (!Files.exists(path)) {
      throw new IOException("Config file not found: " + path.toAbsolutePath());
    }

    return MAPPER.readValue(path.toFile(), AppConfig.class);
  }

}