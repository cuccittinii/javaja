import java.io.*;
import java.net.*;
import java.util.Scanner;

public class JokenpoClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Conectado ao servidor.");
            
            // Lendo mensagem inicial do servidor
            System.out.println(input.readLine());
            
            // Escolhendo movimento
            System.out.println("Escolha seu movimento (pedra, papel ou tesoura): ");
            String move = scanner.nextLine();
            output.println(move);

            // Recebendo resultado
            String result = input.readLine();
            System.out.println(result);
        } catch (IOException e) {
        }
    }
}
