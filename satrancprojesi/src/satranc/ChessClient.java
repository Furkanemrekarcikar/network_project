package satranc;

import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;

public class ChessClient {

    private Socket socket;

    private InputStream inputStream;
    private OutputStream outputStream;

    private String playerColor;
    private game gameScreen;

    public ChessClient(String serverIP, int serverPort) {

        try {

            socket = new Socket(serverIP, serverPort);

            System.out.println("[CLIENT] Connected to server "
                    + serverIP + ":" + serverPort);

            System.out.println("[CLIENT] Servera baglanildi "
                    + serverIP + ":" + serverPort);

            System.out.println();

            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();

            byte[] buffer = new byte[1024];

            // Read messages until color comes
            // Renk bilgisi gelene kadar serverdan mesaj oku
            while (playerColor == null) {

                int bytesRead = inputStream.read(buffer);

                if (bytesRead == -1) {
                    System.out.println("[CLIENT] Server disconnected");
                    break;
                }

                String serverMessage = new String(buffer, 0, bytesRead);

                System.out.println("[CLIENT] Server message: " + serverMessage);
                System.out.println("[CLIENT] Server mesaji: " + serverMessage);

                if (serverMessage.equals("WAITING")) {

                    javax.swing.JOptionPane.showMessageDialog(
                            null,
                            "Waiting for opponent..."
                    );

                } else if (serverMessage.toUpperCase().contains("WHITE")) {

                    playerColor = "WHITE";

                } else if (serverMessage.toUpperCase().contains("BLACK")) {

                    playerColor = "BLACK";
                }
            }

            System.out.println("[CLIENT] Your color: " + playerColor);
            System.out.println("[CLIENT] Renginiz: " + playerColor);

        } catch (Exception e) {

            System.out.println("[CLIENT] Error / Hata: " + e.getMessage());
        }
    }

    // Send move to server
    // Hamleyi servera gonder
    public void sendMove(int oldRow, int oldCol,
            int newRow, int newCol) {

        try {

            String moveMessage
                    = "MOVE:" + oldRow + "," + oldCol + ","
                    + newRow + "," + newCol;

            outputStream.write(moveMessage.getBytes());
            outputStream.flush();

            System.out.println("[CLIENT] Move sent: " + moveMessage);
            System.out.println("[CLIENT] Hamle gonderildi: " + moveMessage);

        } catch (Exception e) {

            System.out.println("[CLIENT] Send Error / Gonderme Hatasi: "
                    + e.getMessage());
        }
    }

    public String getPlayerColor() {
        return playerColor;
    }

    public void setGameScreen(game gameScreen) {
        this.gameScreen = gameScreen;
    }

    public void listenForMessages() {

        Thread listenerThread = new Thread(() -> {

            try {

                byte[] buffer = new byte[1024];

                while (true) {

                    int bytesRead = inputStream.read(buffer);

                    if (bytesRead == -1) {
                        System.out.println("[CLIENT] Server disconnected");
                        break;
                    }

                    String message = new String(buffer, 0, bytesRead);

                    System.out.println("[CLIENT] Message received: " + message);

                    if (message.equals("DISCONNECTED")) {

                        javax.swing.JOptionPane.showMessageDialog(
                                null,
                                "Opponent disconnected!"
                        );

                        System.exit(0);
                    }

                    if (message.startsWith("MOVE:")) {

                        String data = message.substring(5);
                        String[] parts = data.split(",");

                        int oldRow = Integer.parseInt(parts[0]);
                        int oldCol = Integer.parseInt(parts[1]);
                        int newRow = Integer.parseInt(parts[2]);
                        int newCol = Integer.parseInt(parts[3]);

                        if (gameScreen != null) {
                            gameScreen.processIncomingMove(
                                    oldRow,
                                    oldCol,
                                    newRow,
                                    newCol
                            );
                        }
                    }
                }

            } catch (Exception e) {

                System.out.println("[CLIENT] Listen Error / Dinleme Hatasi: "
                        + e.getMessage());

                javax.swing.JOptionPane.showMessageDialog(
                        null,
                        "Opponent disconnected!"
                );

                System.exit(0);
            }
        });

        listenerThread.start();
    }
}