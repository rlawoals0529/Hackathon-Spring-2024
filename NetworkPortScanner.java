import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class NetworkPortScanner {
    public static void main(String[] args) throws IOException {
        String subnet = "192.168.0";
        Scanner scan = new Scanner(System.in);
        System.out.println("Specify the port of the recipient");
        int port = scan.nextInt();
        for (int i = 1; i <= 255; i++) {
            String host = subnet + "." + i;
            Thread thread = new Thread(new PortRunner(host, port));
            thread.start();
        }
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
                socket.close();
                System.out.println("Server open here: " + host);
            } catch (IOException e) {
                // No open port
            }
        }
    }
}