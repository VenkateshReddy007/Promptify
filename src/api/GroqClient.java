import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class GroqClient {
    private final String apiKey;
    private final String apiUrl;
    private final List<String> modelChain;
    private final HttpClient httpClient;

    public GroqClient() {
        Properties props = new Properties();
        String configPath = new File("config.properties").getAbsolutePath();
        try (FileInputStream fis = new FileInputStream(configPath)) {
            props.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load config.properties for GroqClient", e);
        }
        this.apiKey = props.getProperty("groq.api.key");
        this.apiUrl = props.getProperty("groq.api.url",
                "https://api.groq.com/openai/v1/chat/completions");
        this.modelChain = buildModelChain(props);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    private List<String> buildModelChain(Properties props) {
        List<String> models = new ArrayList<>();
        String primary = props.getProperty("groq.model", "llama-3.3-70b-versatile");
        if (primary != null && !primary.isBlank()) {
            models.add(primary.trim());
        }
        String fallbacks = props.getProperty("groq.fallback.models",
                "llama-3.1-8b-instant,meta-llama/llama-4-scout-17b-16e-instruct,groq/compound-mini");
        for (String model : fallbacks.split(",")) {
            String trimmed = model.trim();
            if (!trimmed.isEmpty() && !models.contains(trimmed) && !isDeprecatedModel(trimmed)) {
                models.add(trimmed);
            }
        }
        if (models.isEmpty()) {
            models.add("llama-3.3-70b-versatile");
        }
        return models;
    }

    private boolean isDeprecatedModel(String model) {
        return model.contains("mixtral-8x7b")
                || model.contains("gemma-7b");
    }

    public String generateContent(String userPrompt) {
        System.out.println("[GroqClient] Sending request to Groq API...");
        String lastError = "No Groq models configured.";

        for (String model : modelChain) {
            System.out.println("[GroqClient] Trying model: " + model);
            String result = callModel(model, userPrompt);
            if (!result.startsWith("Error:")) {
                return result;
            }
            lastError = result;
            if (result.contains("decommissioned") || result.contains("model_decommissioned")) {
                System.out.println("[GroqClient] Model decommissioned, skipping: " + model);
                continue;
            }
            System.out.println("[GroqClient] Failed on " + model + ": " + summarizeError(result));
        }
        return lastError;
    }

    private String callModel(String model, String userPrompt) {
        try {
            String jsonBody = buildJsonBody(model, userPrompt);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();
            System.out.println("[GroqClient] Response received. Length: " + responseBody.length());

            if (response.statusCode() != 200) {
                return formatHttpError(response.statusCode(), responseBody);
            }

            return parseContentFromResponse(responseBody);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    private String buildJsonBody(String model, String userPrompt) {
        String escaped = escapeJson(userPrompt);
        return "{\"model\":\"" + model + "\",\"messages\":[{\"role\":\"user\",\"content\":\""
                + escaped + "\"}],\"temperature\":0.7}";
    }

    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String parseContentFromResponse(String responseBody) {
        int contentIndex = responseBody.indexOf("\"content\"");
        if (contentIndex < 0) {
            return "Error: No content field in Groq response";
        }
        String substring = responseBody.substring(contentIndex + 9).trim();
        if (!substring.startsWith(":")) {
            return "Error: Unexpected character after content key in response";
        }
        substring = substring.substring(1).trim();
        if (!substring.startsWith("\"")) {
            return "Error: Content is not a JSON string in response";
        }

        StringBuilder result = new StringBuilder();
        for (int i = 1; i < substring.length(); i++) {
            char c = substring.charAt(i);
            if (c == '\\') {
                if (i + 1 < substring.length()) {
                    char next = substring.charAt(i + 1);
                    if (next == '"') {
                        result.append('"');
                        i++;
                    } else if (next == '\\') {
                        result.append('\\');
                        i++;
                    } else if (next == 'n') {
                        result.append('\n');
                        i++;
                    } else if (next == 'r') {
                        result.append('\r');
                        i++;
                    } else if (next == 't') {
                        result.append('\t');
                        i++;
                    } else {
                        result.append('\\');
                    }
                } else {
                    result.append('\\');
                }
            } else if (c == '"') {
                return result.toString();
            } else {
                result.append(c);
            }
        }
        return "Error: Unclosed string in JSON response";
    }

    private String formatHttpError(int statusCode, String responseBody) {
        String summary = summarizeError(responseBody);
        if (statusCode == 429) {
            return "Error: HTTP 429 - Groq rate limit exceeded. Wait a moment and retry. " + summary;
        }
        if (statusCode == 400 && responseBody.contains("model_decommissioned")) {
            return "Error: HTTP 400 - model_decommissioned - " + summary;
        }
        return "Error: HTTP " + statusCode + " - " + summary;
    }

    private String summarizeError(String responseBody) {
        int msgIndex = responseBody.indexOf("\"message\":");
        if (msgIndex >= 0) {
            String fragment = responseBody.substring(msgIndex + 11);
            if (fragment.startsWith("\"")) {
                int end = fragment.indexOf("\"", 1);
                if (end > 0) {
                    return fragment.substring(1, end);
                }
            }
        }
        return responseBody.length() > 300 ? responseBody.substring(0, 300) + "..." : responseBody;
    }
}
