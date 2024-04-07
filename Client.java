import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client {
    private static Socket socket = null;
    private static List<File> selectedFiles = new ArrayList<>();
    private static JComboBox<String> openPortsComboBox;
    private static JTextField selectedIPAddressField;
    private static JTextField portField;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Client::initializeGUI);
    }

    public static void initializeGUI() {
        JFrame frame = new JFrame("Client Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        JPanel panel = new JPanel();
        frame.setLayout(new FlowLayout());

        JButton chooseFilesButton = new JButton("Choose Files");
        panel.add(chooseFilesButton);

        JTextArea textArea = new JTextArea(10, 30);
        JScrollPane scrollPane = new JScrollPane(textArea);
        panel.add(scrollPane);

        JButton submitFilesButton = new JButton("Submit Files");
        panel.add(submitFilesButton);

        // Components for port scanning
        portField = new JTextField(10);
        JButton scanButton = new JButton("Scan Network Ports");
        panel.add(new JLabel("Port:"));
        panel.add(portField);
        panel.add(scanButton);

        // Displaying open ports and selected IP address
        openPortsComboBox = new JComboBox<>();
        selectedIPAddressField = new JTextField(15);
        panel.add(new JLabel("Open IPs:"));
        panel.add(openPortsComboBox);
        panel.add(new JLabel("Selected IP:"));
        panel.add(selectedIPAddressField);

        frame.add(panel, BorderLayout.CENTER);
        // Progress bar for file upload
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        frame.add(progressBar, BorderLayout.SOUTH);

        // Event Listeners
        chooseFilesButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(true);
            int result = fileChooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedFiles.clear();
                StringBuilder filenames = new StringBuilder();
                for (File file : fileChooser.getSelectedFiles()) {
                    selectedFiles.add(file);
                    filenames.append(file.getName()).append("\n");
                }
                textArea.setText(filenames.toString());
            }
        });
        // Submit Files Button
        submitFilesButton.addActionListener(e -> new Thread(() -> {
            selectedFiles.forEach(file -> {
                try {
                    sendFile(file.getPath(), progressBar);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }).start());
        // Finding IP Address
        scanButton.addActionListener(e -> {
            int port = Integer.parseInt(portField.getText().trim());
            String subnet = "192.168.0";
            openPortsComboBox.removeAllItems();
            for (int i = 1; i <= 255; i++) {
                final String host = subnet + "." + i;
                new Thread(() -> {
                    try {
                        Socket socket = new Socket(host, port);
                        socket.close();
                        SwingUtilities.invokeLater(() -> openPortsComboBox.addItem(host));
                        System.out.println("Server open here: " + host);
                    } catch (IOException ignored) {}
                }).start();
            }
        });

        openPortsComboBox.addActionListener(e -> selectedIPAddressField.setText((String) openPortsComboBox.getSelectedItem()));

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void setPortField(JTextField portField) {
        Client.portField = portField;
    }

    public static class PortRunner implements Runnable {
        private String host;
        private int port;

        public PortRunner(String host, int port) {
            this.host = host;
            this.port = port;
        }

        @Override
        public void run() {
            try {
                Socket socket = new Socket(host, port);
                System.out.println("Server open here: " + host);
                socket.close();
            } catch (IOException e) {
            }
        }
    }

    private static void sendFile(String path, JProgressBar progressBar) {
        try {
            if (socket == null || socket.isClosed()) {
                String host = selectedIPAddressField.getText().trim(); // Get the selected IP address
                String portString;
                portString = portField.getText().trim();

                // Validate that the host and port fields are not empty
                if (!host.isEmpty() && !portString.isEmpty()) {
                    try {
                        int port = Integer.parseInt(portString); // Parse the port number
                        socket = new Socket(host, port); // Use the selected IP address and parsed port
                    } catch (NumberFormatException ex) {
                        System.out.println("Invalid port number.");
                        return;
                    }
                } else {
                    System.out.println("No IP address or port number selected.");
                    return;
                }
            }
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

            File file = new File(path);
            long fileSize = file.length();
            String filename = file.getName();

            dataOutputStream.write(1);
            dataOutputStream.writeBoolean(true);
            dataOutputStream.writeUTF(filename);
            dataOutputStream.writeLong(fileSize);

            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytesSent = 0;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                dataOutputStream.write(buffer, 0, bytesRead);
                totalBytesSent += bytesRead;
                int progress = (int) ((totalBytesSent * 100) / fileSize);

                final int finalProgress = progress;
                SwingUtilities.invokeLater(() -> progressBar.setValue(finalProgress));
            }
            fileInputStream.close();

            dataOutputStream.writeUTF("END");
            System.out.println("File sent: " + filename);

            SwingUtilities.invokeLater(() -> progressBar.setValue(0));

            dataOutputStream.close();
            socket.close();
            socket = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
