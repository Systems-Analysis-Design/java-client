import java.util.function.BiConsumer;

public interface Client {
    void push(MessageDto message);
    MessageDto pull();
    void subscribe(BiConsumer<String, byte[]> function);
    void close();
}
