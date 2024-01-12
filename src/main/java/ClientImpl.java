import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.function.BiConsumer;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientImpl implements Client{
    private static final String PUSH = "push";
    private static final String PULL = "pull";
    private static final String SUBSCRIBE = "subscribe";
    private static final String HTTP = "http";
    private final HttpClient httpClient;
    private final String url;
    private final ObjectMapper mapper;

    public ClientImpl(final String url, final int timeout) {
        this.url = url;
        this.mapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(timeout)).build();
    }

    @Override
    public void push(final MessageDto message) throws URISyntaxException, IOException, InterruptedException {
        final URI uri = new URI(HTTP, this.url, PUSH, null);
        final String body = this.mapper.writeValueAsString(message);
        final HttpRequest request = HttpRequest.newBuilder()
                                               .uri(uri)
                                               .POST(HttpRequest.BodyPublishers.ofString(body))
                                               .build();
        this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Override
    public MessageDto pull() throws URISyntaxException, IOException, InterruptedException {
        final URI uri = new URI(HTTP, this.url, PULL, null);
        final HttpRequest request = HttpRequest.newBuilder()
                                               .uri(uri)
                                               .GET()
                                               .build();
        final HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return this.mapper.readValue(response.body(), MessageDto.class);
    }

    @Override
    public void subscribe(final BiConsumer<String, byte[]> function) {

    }

    @Override
    public void close() {
        this.httpClient.close();
    }
}
