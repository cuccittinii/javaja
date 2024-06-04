import java.io.*;
import java.net.*;
import java.util.*;

public class GameServer {
    private List<ClientHandler> clients = new ArrayList<>();
    private int port;
    private int serverWins = 0;
    private int serverLosses = 0;
    private int serverDraws = 0;

    public GameServer(int port) {
        this.port = port;
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor iniciado na porta " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
        }
    }

    public synchronized void broadcastMessage(String message, ClientHandler excludeClient) {
        for (ClientHandler client : clients) {
            if (client != excludeClient) {
                client.sendMessage(message);
            }
        }
    }

    public synchronized void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    public synchronized void processChoice(String choice, ClientHandler clientHandler) {
        if (choice.equals("1")) {
            clientHandler.sendMessage("Você escolheu jogar contra outro jogador. Aguardando conexão do segundo jogador...");
            clientHandler.setPlayAgainstServer(false);
        } else if (choice.equals("2")) {
            clientHandler.sendMessage("Você escolheu jogar contra o servidor. Digite sua escolha (pedra, papel ou tesoura):");
            clientHandler.setPlayAgainstServer(true);
        } else {
            clientHandler.sendMessage("Escolha inválida. Encerrando a conexão.");
            removeClient(clientHandler);
        }
    }

    public synchronized void processMove(String move, ClientHandler clientHandler) {
        if (clientHandler.isPlayAgainstServer()) {
            String serverMove = generateServerMove();
            String result = determineWinner(move, serverMove);
            clientHandler.sendMessage("Você escolheu: " + move + ". Servidor escolheu: " + serverMove + ". " + result);
            clientHandler.sendMessage("Digite sua escolha (pedra, papel ou tesoura):");
        } else {
            if (clients.size() == 2) {
                ClientHandler opponent = clients.get(0) == clientHandler ? clients.get(1) : clients.get(0);
                clientHandler.setMove(move);
                if (clientHandler.getMove() != null && opponent.getMove() != null) {
                    String result = determineWinner(clientHandler.getMove(), opponent.getMove());
                    clientHandler.sendMessage("Você escolheu: " + clientHandler.getMove() + ". Oponente escolheu: " + opponent.getMove() + ". " + result);
                    opponent.sendMessage("Você escolheu: " + opponent.getMove() + ". Oponente escolheu: " + clientHandler.getMove() + ". " + result);
                    clientHandler.sendMessage("Digite sua escolha (pedra, papel ou tesoura):");
                    opponent.sendMessage("Digite sua escolha (pedra, papel ou tesoura):");
                    clientHandler.resetMove();
                    opponent.resetMove();
                }
            } else {
                clientHandler.sendMessage("Esperando outro jogador se conectar...");
            }
        }
    }

    private String generateServerMove() {
        String[] moves = {"pedra", "papel", "tesoura"};
        return moves[new Random().nextInt(moves.length)];
    }

    private String determineWinner(String move1, String move2) {
        if (move1.equals(move2)) {
            serverDraws++;
            return "Empate! Ambos escolheram " + move1;
        } else if ((move1.equals("pedra") && move2.equals("tesoura")) ||
                   (move1.equals("papel") && move2.equals("pedra")) ||
                   (move1.equals("tesoura") && move2.equals("papel"))) {
            serverWins++;
            return "Você venceu!";
        } else {
            serverLosses++;
            return "Você perdeu!";
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: java GameServer <porta>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);
        GameServer server = new GameServer(port);
        server.startServer();
    }

    public int getServerWins() {
        return serverWins;
    }

    public int getServerLosses() {
        return serverLosses;
    }

    public int getServerDraws() {
        return serverDraws;
    }
}

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private GameServer server;
    private PrintWriter out;
    private BufferedReader in;
    private String move;
    private boolean playAgainstServer;
    private int clientWins = 0;
    private int clientLosses = 0;
    private final int clientDraws = 0;

    public ClientHandler(Socket socket, GameServer server) {
        this.clientSocket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            sendMessage("Bem-vindo ao Jokenpô!");
            sendMessage("Escolha o modo de jogo:");
            sendMessage("1 - Jogar contra outro jogador");
            sendMessage("2 - Jogar contra o servidor");

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Mensagem recebida do cliente: " + inputLine);
                server.processChoice(inputLine, this);
            }
        } catch (IOException e) {
            System.err.println("Erro na comunicação com o cliente: " + e.getMessage());
        } finally {
            try {
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Erro ao fechar recursos do cliente: " + e.getMessage());
            }
            server.removeClient(this);
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public void setMove(String move) {
        this.move = move;
    }

    public String getMove() {
        return move;
    }

    public void resetMove() {
        this.move = null;
    }

    public void setPlayAgainstServer(boolean playAgainstServer) {
        this.playAgainstServer = playAgainstServer;
    }

    public boolean isPlayAgainstServer() {
        return playAgainstServer;
    }
}
