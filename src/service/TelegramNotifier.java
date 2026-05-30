import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Properties;

public class TelegramNotifier {
    private final String botToken;
    private final String chatId;
    private final HttpClient httpClient;

    public TelegramNotifier() {
        Properties props = new Properties();
        String configPath = new File("config.properties").getAbsolutePath();
        try (FileInputStream fis = new FileInputStream(configPath)) {
            props.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load config.properties for TelegramNotifier", e);
        }
        this.botToken = props.getProperty("telegram.bot.token");
        this.chatId = props.getProperty("telegram.chat.id");
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public boolean sendAlert(String message) {
        System.out.println("[TelegramNotifier] Action: sendAlert - message: " + message);
        try {
            String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
            String escapedMessage = escapeJson(message);
            String jsonBody = "{\"chat_id\":\"" + chatId + "\",\"text\":\"" + escapedMessage
                    + "\",\"parse_mode\":\"HTML\"}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();
            System.out.println("[TelegramNotifier] Status: " + statusCode);
            return statusCode == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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
}
