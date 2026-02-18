package nl.adgroot.pdfsummarizer.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import nl.adgroot.pdfsummarizer.config.AppConfig;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OllamaClient {

  private static final MediaType JSON = MediaType.parse("application/json");
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private OkHttpClient http = new OkHttpClient();
  private final String url;
  private final String model;
  private final double temperature;

  public OllamaClient(AppConfig.OllamaConfig cfg) {

    this.url = cfg.url;
    this.model = cfg.model;
    this.temperature = cfg.temperature;
    Duration t = Duration.ofSeconds(cfg.timeoutSeconds);

    this.http = new OkHttpClient.Builder()
        .connectTimeout(t)
        .readTimeout(t)
        .writeTimeout(t)
        .callTimeout(t)
        .build();
  }

  public String generate(String prompt) throws IOException {
    ObjectNode req = MAPPER.createObjectNode();
    req.put("model", model);
    req.put("prompt", prompt);
    req.put("stream", false);
    req.put("temperature", temperature);

    Request request = new Request.Builder()
        .url(url)
        .post(RequestBody.create(req.toString(), JSON))
        .build();

    try (Response resp = http.newCall(request).execute()) {
      if (!resp.isSuccessful()) {
        String body = resp.body() != null ? resp.body().string() : "";
        throw new IOException("Ollama error: " + resp.code() + " " + resp.message() + "\n" + body);
      }
      String body = Objects.requireNonNull(resp.body()).string();
      JsonNode json = MAPPER.readTree(body);
      return json.path("response").asText("");
    }
  }
}
