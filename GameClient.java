import java.io.*;
import java.net.*;
import java.util.Scanner;

public class GameClient {
    private String hostname;
    private int port;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private int clientWins = 0;
    private int clientLosses = 0;
    private int clientDraws = 0;

    public GameClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public void startClient() {
        try {
            socket = new Socket(hostname, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            Thread serverResponseThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String serverResponse;
                        while ((serverResponse = in.readLine()) != null) {
                            System.out.println("Mensagem do servidor: " + serverResponse);
                            if (serverResponse.startsWith("Você escolheu")) {
                                updateScore(serverResponse);
                            }
                        }
                    } catch (IOException e) {
                        System.err.println("Erro ao ler mensagem do servidor: " + e.getMessage());
                    }
                }
            });
            serverResponseThread.start();

            Scanner scanner = new Scanner(System.in);
            String choice;
            while (true) {
                System.out.println("Escolha o modo de jogo:");
                System.out.println("1 - Jogar contra outro jogador");
                System.out.println("2 - Jogar contra o servidor");
                choice = scanner.nextLine();
                if (choice.equals("1") || choice.equals("2")) {
                    break;
                } else {
                    System.out.println("Escolha inválida. Tente novamente.");
                }
            }
            out.println(choice);

            if (choice.equals("2")) {
                System.out.println("Digite sua escolha (pedra, papel ou tesoura):");
                String userInput;
                while (true) {
                    userInput = scanner.nextLine();
                    if (userInput.equals("pedra") || userInput.equals("papel") || userInput.equals("tesoura")) {
                        break;
                    } else {
                        System.out.println("Escolha inválida. Tente novamente.");
                    }
                }
                out.println(userInput);
            }
        } catch (IOException e) {
            System.err.println("Erro ao conectar ao servidor: " + e.getMessage());
        } finally {
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                System.err.println("Erro ao fechar recursos do cliente: " + e.getMessage());
            }
        }
    }

    private void updateScore(String result) {
        if (result.contains("venceu")) {
            clientWins++;
        } else if (result.contains("perdeu")) {
            clientLosses++;
        } else if (result.contains("Empate")) {
            clientDraws++;
        }
        System.out.println("Placar: Vitórias: " + clientWins + ", Derrotas: " + clientLosses + ", Empates: " + clientDraws);
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Uso: java GameClient <endereço IP> <porta>");
            System.exit(1);
        }

        String serverIp = args[0];
        int port = Integer.parseInt(args[1]);
        GameClient client = new GameClient(serverIp, port);
        client.startClient();
    }
}
