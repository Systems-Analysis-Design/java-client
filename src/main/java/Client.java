import java.io.IOException;
import java.net.URISyntaxException;
import java.util.function.BiConsumer;

public interface Client {
    void push(MessageDto message) throws URISyntaxException, IOException, InterruptedException;
    MessageDto pull() throws URISyntaxException, IOException, InterruptedException;
    void subscribe(BiConsumer<String, byte[]> function);
    void close();
}
