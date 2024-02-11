import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import sse.EventStreamAdapter;
import sse.HttpEventStreamClient;

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
    private final List<BiConsumer<String, byte[]>> functions = new ArrayList<>();
    private Thread thread;
    private HttpEventStreamClient client;

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
        this.functions.add(function);
        if (this.client == null) {
            initializeSSEClient();
        }
        if (this.thread == null) {
//            this.thread = new Thread(this::waitForEvents);
//            this.thread.start();
        }
    }

    @Override
    public void close() {
        this.client.stop();
        this.httpClient.close();
    }

    private void initializeSSEClient() {
        this.client = new HttpEventStreamClient(this.url + "/subscribe", new EventStreamAdapter() {

            private final ObjectMapper mapper = new ObjectMapper();

            @Override
            public void onEvent(HttpEventStreamClient client, HttpEventStreamClient.Event event) {
                try {
                    MessageInnerDto messageInnerDto = this.mapper.readValue(event.getData(), MessageInnerDto.class);
                    callSubscribeFunctions(messageInnerDto.key(), messageInnerDto.value().getBytes(StandardCharsets.UTF_8));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onClose(HttpEventStreamClient client, HttpResponse<Void> response) {
                System.out.println("SSE Client closed");
            }

        });
        this.client.start().join();
    }

    private void waitForEvents() {
    }

    private void callSubscribeFunctions(String key, byte[] value) {
        this.functions.forEach(x -> x.accept(key, value));
    }
}
