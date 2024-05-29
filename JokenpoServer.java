import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JokenpoServer {
    private static final int PORT = 12345;
    private static Socket player1;
    private static Socket player2;
    
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor iniciado na porta " + PORT);
            
            ExecutorService pool = Executors.newFixedThreadPool(2);
            
            while (true) {
                if (player1 == null) {
                    player1 = serverSocket.accept();
                    System.out.println("Jogador 1 conectado");
                } else if (player2 == null) {
                    player2 = serverSocket.accept();
                    System.out.println("Jogador 2 conectado");
                }
                
                if (player1 != null && player2 != null) {
                    pool.execute(new GameHandler(player1, player2));
                    player1 = null;
                    player2 = null;
                }
            }
        } catch (IOException e) {
        }
    }
}

class GameHandler implements Runnable {
    private Socket player1;
    private Socket player2;
    private BufferedReader input1;
    private BufferedReader input2;
    private PrintWriter output1;
    private PrintWriter output2;

    public GameHandler(Socket player1, Socket player2) {
        this.player1 = player1;
        this.player2 = player2;
        
        try {
            input1 = new BufferedReader(new InputStreamReader(player1.getInputStream()));
            input2 = new BufferedReader(new InputStreamReader(player2.getInputStream()));
            output1 = new PrintWriter(player1.getOutputStream(), true);
            output2 = new PrintWriter(player2.getOutputStream(), true);
        } catch (IOException e) {
        }
    }

    @Override
    public void run() {
        try {
            output1.println("Jogo iniciado! Você é o Jogador 1.");
            output2.println("Jogo iniciado! Você é o Jogador 2.");

            String move1 = input1.readLine();
            String move2 = input2.readLine();

            String result = determineWinner(move1, move2);

            output1.println(result);
            output2.println(result);

            player1.close();
            player2.close();
        } catch (IOException e) {
        }
    }

    private String determineWinner(String move1, String move2) {
        if (move1.equalsIgnoreCase(move2)) {
            return "Empate! Ambos escolheram " + move1;
        }
        
        return switch (move1) {
            case "pedra" -> (move2.equalsIgnoreCase("tesoura")) ? "Jogador 1 vence! Pedra quebra tesoura." : "Jogador 2 vence! Papel cobre pedra.";
            case "papel" -> (move2.equalsIgnoreCase("pedra")) ? "Jogador 1 vence! Papel cobre pedra." : "Jogador 2 vence! Tesoura corta papel.";
            case "tesoura" -> (move2.equalsIgnoreCase("papel")) ? "Jogador 1 vence! Tesoura corta papel." : "Jogador 2 vence! Pedra quebra tesoura.";
            default -> "Movimento inválido.";
        };
    }
}
