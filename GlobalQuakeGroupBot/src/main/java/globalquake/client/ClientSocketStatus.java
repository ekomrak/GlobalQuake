package globalquake.client;

public enum ClientSocketStatus {

    DISCONNECTED("Disconnected"),
    CONNECTING("Connecting..."),
    CONNECTED("Connected");

    private final String name;

    ClientSocketStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
