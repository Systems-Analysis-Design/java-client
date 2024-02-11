public class ClientFactory {
    public static final ClientFactory INSTANCE = new ClientFactory();
    private static final int DEFAULT_TIMEOUT = 600;

    private ClientFactory() {
    }

    public Client createClient(final String url, final int timeout) {
        return new ClientImpl(url, timeout);
    }

    public Client createClient(final String url) {
        return new ClientImpl(url, DEFAULT_TIMEOUT);
    }
}
