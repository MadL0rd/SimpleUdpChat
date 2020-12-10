package ChatUdp;


public class Main {

    public static void main(String[] args) {
        ChatUdpServer server = new ChatUdpServer();
        try {
            server.run();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
