import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;


public class ThreadedEchoServer implements Runnable {
    public static final int PORT = 7777;

    private Socket sock;

    public ThreadedEchoServer(Socket s) {
        sock = s;
    }

    public void open () throws IOException {
        ServerSocket serv = new ServerSocket(PORT);
        while (true) {
            Socket sock = serv.accept();
            ThreadedEchoServer server = new ThreadedEchoServer(sock);
            Thread t = new Thread(server);
            t.start();
        }
    }
    public void run() {
        try {
            BufferedReader brd = new BufferedReader(new InputStreamReader(sock.getInputStream(), StandardCharsets.UTF_16));
            String s = brd.readLine();
            PrintWriter prw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream(), StandardCharsets.UTF_16));
            prw.print(s);
            prw.println(s);
            prw.flush();
        } catch (IOException exc) {
            System.out.println("Eccezione I/O: " + exc);
            exc.printStackTrace();
        } finally {
            try {
                sock.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
