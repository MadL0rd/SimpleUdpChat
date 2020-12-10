package ChatUdp;

import java.net.*;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class ChatUdpServer {

    private final int portServer = 13133;
    private final int portClient = 13137;
    private final String internalMessage = "WWA726j6xAkMkvpb";
    private final int clientClearDelayMinutes = 15;

    private ArrayList<Client> clients = new ArrayList<Client>();

    public class Client {
        InetAddress ipAddress;
        LocalTime deathTime;

        Client(InetAddress ip) {
            ipAddress = ip;
            updateTime();
        }

        public void updateTime() {
            deathTime = LocalTime.now().plus(clientClearDelayMinutes, ChronoUnit.MINUTES);
            System.out.println("Time did updated for: " + ipAddress.toString() + "\tNew death time is: " + deathTime);
        }

        public boolean isDead() {
            return deathTime.compareTo(LocalTime.now()) == 0;
        }

        public void sendMessage(String messageText) throws Exception {
            if (messageText.isEmpty()) return;

            messageText = ipAddress.toString() + ": " + messageText;

            DatagramSocket socket = new DatagramSocket();
            byte[] messageBuffer = messageText.getBytes("UTF-8");
            DatagramPacket packet = new DatagramPacket(messageBuffer, messageBuffer.length, ipAddress, portClient);
            socket.send(packet);

            System.out.println("Send msg to " + ipAddress.toString());
        }
    }

    public void run() throws Exception {
        DatagramSocket receiveSocket = new DatagramSocket(portServer);
        while (true) {
            byte[] buffer = new byte[1024];
            DatagramPacket receiveData = new DatagramPacket(buffer, buffer.length);
            receiveSocket.receive(receiveData);

            InetAddress clientAddress = receiveData.getAddress();
            addAddress(clientAddress);

            String messageText = new String(receiveData.getData());
            messageText = messageText.substring(0, receiveData.getLength());
            System.out.println("Receive msg: " + messageText);

            if (!messageText.equals(internalMessage)) {
                clients.removeIf(client -> client.isDead());
                for (Client client : clients) {
                    client.sendMessage(messageText);
                }
            }
        }
    }

    private void addAddress(InetAddress clientAddress) {
        for (Client client : clients) {
            if (client.ipAddress.equals(clientAddress)) {
                client.updateTime();
                return;
            }
        }
        clients.add(new Client(clientAddress));
    }
}