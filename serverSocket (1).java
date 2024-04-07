package UniversalFileSharing;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class serverSocket {
    private static DataInputStream dataInputStream;
    private static DataOutputStream dataOutputStream;
    int port;
    ServerSocket serverSocket;
    Socket clientSocket;
    boolean receivingFile = false;

    private String filename = "";

    public String accept = "";

    public serverSocket(int port) {
        this.port = port;
    }

    public void startConnection() {
        try {
            serverSocket = new ServerSocket(port);
            listenForClient();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setReceivingFile(boolean bool) {
        receivingFile = bool;
    }

    public boolean getReceivingFile() {
        return receivingFile;
    }

    public void endConnection() throws IOException {
        serverSocket.close();
        System.out.println("Test1");
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    public void listenForClient() throws Exception {
        while(true) {
            clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

            dataInputStream = new DataInputStream(clientSocket.getInputStream());

            boolean fileReceived = false;
            while(!fileReceived) {
                try {
                    if(dataInputStream.read() == -1) {
                        System.out.println("Client Ended Connection");
                        break;
                    }
                    fileReceived = dataInputStream.readBoolean();
                } catch(Exception e) {
                    // Exceptions are expected while waiting on receiving files
                }
            }

            if(fileReceived) {
                String filename = dataInputStream.readUTF();
                if (filename.equals("END")) break;
                readFile(filename);
            }
        }
    }

    public void setAccept(String accept) {
        this.accept = accept;
    }

    public String getAccept() {
        return accept;
    }
    public void readFile(String filename) throws Exception {
        int bytes = 0;

        setReceivingFile(true);
        setFilename(filename);

        System.out.println("You are being sent " + filename + ". Would you like to accept (y/n)");

        while (!accept.toLowerCase().equals("y") && !accept.toLowerCase().equals("n")) {
            try {
                Thread.sleep(100); // Sleep for 100 milliseconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if(accept.toLowerCase().equals("n")) {
            System.out.println("You rejected the file");
            dataInputStream.readAllBytes();
            return;
        } else if(!accept.toLowerCase().equals("y")) {
            System.out.println("Invalid input. Rejecting file");
            dataInputStream.readAllBytes();
        }

        setAccept("");

        System.out.println("Receiving: " + filename);
        FileOutputStream fileOutputStream = new FileOutputStream(filename);

        long size = dataInputStream.readLong(); // Read file size
        byte[] buffer = new byte[4*1024];
        while (size > 0 && (bytes = dataInputStream.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
            fileOutputStream.write(buffer, 0, bytes);
            size -= bytes;
        }
        fileOutputStream.close();
        System.out.println("File received: " + filename);
        setReceivingFile(false);
    }






    public static void main(String[] args) {
        final int PORT = 8888;

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started. Waiting for client connection...");
            Socket clientSocket;
            while (true) {
                clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

                dataInputStream = new DataInputStream(clientSocket.getInputStream());
                dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());

                boolean flag = false;
                while(!flag) { // Wait for a file to be uploaded. Denoted by a inputStream flag
                    try {
                        if(dataInputStream.read() == -1) { // Connection closed by Client
                            System.out.println("Connection closed by Client");
                            break;
                        }
                        flag = dataInputStream.readBoolean();
                    } catch(IOException ex) {
                        // No file provided
                    }
                }
                if(flag) { // If provided a file (not closed connection)
                    String filename = dataInputStream.readUTF();
                    if (filename.equals("END")) break;
                    receiveFile(filename);
                }
            }
            // Close connections
            clientSocket.close();
            System.out.println("Connection closed.");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void receiveFile(String filename) throws Exception {
        int bytes = 0;

        System.out.println("You are being sent " + filename + ". Would you like to accept (y/n)");
        Scanner scan = new Scanner(System.in);

        String accept = scan.nextLine();
        if(accept.toLowerCase().equals("n")) {
            System.out.println("You rejected the file");
            dataInputStream.readAllBytes();
            return;
        } else if(!accept.toLowerCase().equals("y")) {
            System.out.println("Invalid input. Rejecting file");
            dataInputStream.readAllBytes();
        }

        System.out.println("Receiving: " + filename);
        FileOutputStream fileOutputStream = new FileOutputStream(filename);

        long size = dataInputStream.readLong(); // Read file size
        byte[] buffer = new byte[4*1024];
        while (size > 0 && (bytes = dataInputStream.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
            fileOutputStream.write(buffer, 0, bytes);
            size -= bytes;
        }
        fileOutputStream.close();
        System.out.println("File received: " + filename);
    }
}