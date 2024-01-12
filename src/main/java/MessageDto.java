public class MessageDto {
    private final String key;
    private final byte[] value;

    public MessageDto(String key, byte[] value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public byte[] getValue() {
        return value;
    }
}
