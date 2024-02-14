import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class ClientImpl implements Client{
    private final HttpClient httpClient;
    private final String url;
    private final ObjectMapper mapper;
    private final List<BiConsumer<String, byte[]>> subscribers = new ArrayList<>();

    public ClientImpl(final String url, final int timeout) {
        this.url = url;
        this.mapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(timeout)).build();
    }

    @Override
    public void push(final MessageDto message) {
        try {
            final URI uri = new URI(this.url + "/push");
            MessageInnerDto messageInnerDto = new MessageInnerDto(message.key(), new String(message.value(), StandardCharsets.UTF_8));
            final String body = this.mapper.writeValueAsString(messageInnerDto);
            final HttpRequest request = HttpRequest.newBuilder()
                                                   .header("Content-Type", "application/json")
                                                   .uri(uri)
                                                   .POST(HttpRequest.BodyPublishers.ofString(body))
                                                   .build();
            this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            this.notifySubscribers();
        } catch (Exception e) {
            System.out.println("push failed");
        }
    }

    @Override
    public MessageDto pull() {
        try {
            final URI uri = new URI(this.url + "/pull");
            final HttpRequest request = HttpRequest.newBuilder()
                                                   .uri(uri)
                                                   .GET()
                                                   .build();
            final HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            MessageInnerDto messageInnerDto = this.mapper.readValue(response.body(), MessageInnerDto.class);
            return new MessageDto(messageInnerDto.key(), messageInnerDto.value().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.out.println("pull failed");
            return null;
        }

    }

    @Override
    public void subscribe(final BiConsumer<String, byte[]> function) {
        this.subscribers.add(function);
    }

    @Override
    public void close() {
        this.subscribers.clear();
        this.httpClient.close();
    }

    private void notifySubscribers(){
        if (!this.subscribers.isEmpty()) {
            BiConsumer<String, byte[]> first = this.subscribers.removeFirst();
            MessageDto message = this.pull();
            if (message.value() != null) {
                first.accept(message.key(), message.value());
            }
            this.subscribers.add(first);
        }
    }
}
