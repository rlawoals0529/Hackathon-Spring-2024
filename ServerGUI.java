package UniversalFileSharing;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerGUI {
    private static JFrame frame;
    private static JPanel currentPanel;

    private static JButton openServer;

    private static serverSocket server;
    static JLabel testLabel;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void main(String[] args) {
        launchPage();
    }

    public static void launchPage() {
        frame = new JFrame("SyntaxError Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 600);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        frame.getContentPane().add(mainPanel);

        JButton launchButton = new JButton("Make Computer Discoverable?");

        launchButton.setFont(new Font("Arial", Font.PLAIN, 24)); // Set font size
        launchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GUI();
            }
        });

        testLabel = new JLabel("Waiting...");
        startUpdatingGUI();

        testLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 0, 10, 0);
        mainPanel.add(testLabel, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton yesButton = new JButton("Yes");
        JButton noButton = new JButton("No");

        yesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                server.setAccept("y");
            }
        });

        noButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                server.setAccept("n");
            }
        });

        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);

        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(buttonPanel, gbc);

        // Display the frame
        frame.setVisible(true);

        mainPanel.add(launchButton);
        frame.getContentPane().add(BorderLayout.NORTH, launchButton);

        // Display the frame
        frame.setVisible(true);
    }

    public static void updateFileText() {
        if(server != null && server.receivingFile) {
            testLabel.setText("Receiving File: " + server.getFilename());
        }
    }

    private static final Runnable guiUpdater = new Runnable() {
        @Override
        public void run() {
            updateFileText();
        }
    };

    // Method to start scheduled task
    public static void startUpdatingGUI() {
        scheduler.scheduleAtFixedRate(guiUpdater, 5, 5, TimeUnit.SECONDS);
    }
    public static void GUI() {
        JFrame frame = new JFrame("SyntaxError Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(200, 200);
        frame.setLocation(500, 200);

        final JTextArea textArea = new JTextArea(10, 40);
        frame.getContentPane().add(BorderLayout.CENTER, textArea);
        openServer = new JButton("Start");

        final JTextArea endArea = new JTextArea(20, 20);
        frame.getContentPane().add(BorderLayout.CENTER, endArea);
        final JButton endServer = new JButton("Close");

        JLabel portLabel = new JLabel("Port Number: ");
        final JTextField portField = new JTextField(5);

        JPanel portPanel = new JPanel();
        portPanel.add(portLabel);
        portPanel.add(portField);

        openServer.addActionListener(new ActionListener() { // start running a server after hitting apply
            @Override
            public void actionPerformed(ActionEvent e) {
                int port = Integer.parseInt(portField.getText());
                startServerInBackground(port);
            }
        });

        endServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    server.endConnection();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        frame.getContentPane().add(BorderLayout.SOUTH, endServer);
        frame.getContentPane().add(BorderLayout.CENTER, openServer);
        frame.getContentPane().add(BorderLayout.NORTH, portPanel);
        frame.setVisible(true);
    }

    private static void startServerInBackground(int portNumber) {
        // Disable the button while the server is running to prevent multiple server instances
        openServer.setEnabled(false);

        // Create and start the server in a separate thread
        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    server = new serverSocket(portNumber);
                    server.startConnection();
                } catch (Exception e) {
                    System.out.println("Connection Over");
                }
            }
        });
        serverThread.start();
    }
}