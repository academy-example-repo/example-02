import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class EchoClient {
    public static void main(String[] args) {
        try(Socket socket = new Socket("localhost", 5000)){
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            Scanner scanner = new Scanner(System.in);
            String message;

            while ((message = scanner.nextLine()) != null) {
                if ("exit".equalsIgnoreCase(message)) {
                    System.out.println("연결을 종료합니다.");
                    break;
                }

                out.println(message);
                System.out.println(in.readLine());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
