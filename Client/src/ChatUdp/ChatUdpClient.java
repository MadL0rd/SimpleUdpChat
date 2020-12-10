package ChatUdp;

import javax.swing.*;
import java.awt.*;

import java.net.*;
import java.io.*;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ChatUdpClient extends JFrame {

    public void run() {
        drawFrame();
        configureLayoutActions();

        Thread aliveMessageSander = new Thread(new ThreadAliveMessageSander());
        aliveMessageSander.start();

        Thread receiver = new Thread(new ThreadReceiver());
        receiver.start();
    }

    private class ThreadReceiver implements Runnable {
        @Override
        public void run() {
            System.out.println("ThreadReceiver start");
            try {
                configureReceiver();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        private void configureReceiver() throws Exception {
            DatagramSocket receiveSocket = new DatagramSocket(portClient);
            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket receiveData = new DatagramPacket(buffer, buffer.length);
                receiveSocket.receive(receiveData);

                InetAddress clientAddress = receiveData.getAddress();

                String messageText = new String(receiveData.getData());
                chatTextArea.append(messageText + "\n\n\r");
                chatTextArea.setCaretPosition(chatTextArea.getText().length());

                System.out.println("Receive cycle iteration");
            }
        }
    }

    private class ThreadAliveMessageSander implements Runnable {

        final private int secondsToSleep = 15000;

        @Override
        public void run() {
            System.out.println("ThreadAliveMessageSander start");
            try {
                configure();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        private void configure() throws Exception {
            while (true) {
                try {
                    Thread.sleep(secondsToSleep);

                    DatagramSocket socket = new DatagramSocket();
                    InetAddress ipAddress = getMsgTargetAddress();
                    byte[] messageBuffer = internalMessage.getBytes("UTF-8");
                    DatagramPacket packet = new DatagramPacket(messageBuffer, messageBuffer.length, ipAddress, portServer);
                    socket.send(packet);
                } catch (Exception exception) {}
            }
        }
    }

    // UDP
    private final int portServer = 13133;
    private final int portClient = 13137;
    private final String internalMessage = "WWA726j6xAkMkvpb";
    private final String ipBroadcast = "127.0.0.1";

    private InetAddress getMsgTargetAddress() throws UnknownHostException {
        String userIp = ipTextField.getText();

        InetAddress ipAddress;
        if (userIp.isEmpty()) {
            ipAddress = InetAddress.getByName(ipBroadcast);
        } else {
            ipAddress = InetAddress.getByName(userIp);
        }
        return ipAddress;
    }

    // Layout
    private JTextArea chatTextArea;
    private JScrollPane chatTextScroll;
    private JTextField ipTextField;
    private JPanel sendMessagePanel;
    private JTextField textInput;
    private JButton sendButton;

    private final String frameTitle = "UDP Chat";
    private final int frameXLocation = 100;
    private final int frameYLocation = 100;
    private final int frameWidth = 400;
    private final int frameHeight = 600;

    private void drawFrame() {
        chatTextArea = new JTextArea(frameHeight / 18, 50);
        chatTextScroll = new JScrollPane(chatTextArea);
        ipTextField = new JTextField();
        sendMessagePanel = new JPanel();
        textInput = new JTextField();
        sendButton = new JButton();

        chatTextArea.setLineWrap(true);
        chatTextArea.setEnabled(false);

        sendButton.setText("Send");

        setTitle(frameTitle);
        setLocation(frameXLocation, frameYLocation);
        setSize(frameWidth, frameHeight);
        setResizable(false);
        getContentPane().add(BorderLayout.CENTER, chatTextScroll);
        getContentPane().add(BorderLayout.NORTH, ipTextField);
        getContentPane().add(BorderLayout.SOUTH, sendMessagePanel);
        sendMessagePanel.add(textInput);
        sendMessagePanel.add(sendButton);
        sendMessagePanel.setLayout(new GridLayout(0, 1));

        setVisible(true);
    }

    // Layout actions
    private void configureLayoutActions() {
        sendButton.addActionListener(e -> {
            try {
                sendMessage();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
        textInput.addActionListener(e -> {
            try {
                sendMessage();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private void sendMessage() throws Exception {
        String messageText = textInput.getText();
        if (messageText.isEmpty()) return;
        textInput.setText("");

        DatagramSocket socket = new DatagramSocket();
        InetAddress ipAddress = getMsgTargetAddress();
        byte[] messageBuffer = messageText.getBytes("UTF-8");
        DatagramPacket packet = new DatagramPacket(messageBuffer, messageBuffer.length, ipAddress, portServer);
        socket.send(packet);
    }
}

