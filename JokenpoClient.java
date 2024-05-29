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

            // Lendo mensagens iniciais do servidor
            System.out.println(input.readLine());
            System.out.println(input.readLine());

            for (int i = 0; i < 3; i++) {
                // Mensagem de instrução para escolher o movimento
                System.out.println(input.readLine());
                // Enviando movimento escolhido
                String move = scanner.nextLine();
                output.println(move);

                // Recebendo e exibindo o resultado da rodada
                String result = input.readLine();
                System.out.println(result);
            }

            // Mensagem final do jogo
            System.out.println(input.readLine());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
