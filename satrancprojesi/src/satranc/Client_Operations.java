/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package satranc;

import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author ASUS
 */
public class Client_Operations extends Thread {

    private Socket mySocket;
    private Socket opponentSocket;
    private String playerColor;

    public Client_Operations(Socket mySocket, Socket opponentSocket, String playerColor) {
        this.mySocket = mySocket;
        this.opponentSocket = opponentSocket;
        this.playerColor = playerColor;
    }

    @Override
    public void run() {

        try {
            InputStream inputStream = mySocket.getInputStream();
            OutputStream opponentOutputStream = opponentSocket.getOutputStream();

            byte[] buffer = new byte[1024];

            while (true) {

                // Read message from this client
                // Bu clienttan mesaj oku
                int bytesRead = inputStream.read(buffer);

                if (bytesRead == -1) {

                    System.out.println("[SERVER] " + playerColor + " disconnected");
                    System.out.println("[SERVER] " + playerColor + " baglantisi koptu");

                    // INFORM OPPONENT
                    // Rakibe bilgi gonder
                    opponentOutputStream.write("DISCONNECTED".getBytes());
                    opponentOutputStream.flush();
                    break;
                }

                String receivedMessage = new String(buffer, 0, bytesRead);

                System.out.println("[SERVER] Received from " + playerColor + ": " + receivedMessage);
                System.out.println("[SERVER] " + playerColor + " tarafindan alinan mesaj: " + receivedMessage);

                // Forward message to opponent
                // Mesaji rakip oyuncuya gonder
                opponentOutputStream.write(receivedMessage.getBytes());

                System.out.println("[SERVER] Message forwarded to opponent");
                System.out.println("[SERVER] Mesaj rakip oyuncuya iletildi");
                System.out.println();
            }

        } catch (Exception e) {

            System.out.println("[SERVER] ClientHandler Error / Hata: " + e.getMessage());

            try {
                OutputStream opponentOutputStream = opponentSocket.getOutputStream();
                opponentOutputStream.write("DISCONNECTED".getBytes());
                opponentOutputStream.flush();

                System.out.println("[SERVER] Opponent informed about disconnect");
                System.out.println("[SERVER] Rakibe baglanti kopma bilgisi gonderildi");

            } catch (Exception ex) {
                System.out.println("[SERVER] Could not inform opponent / Rakibe bilgi gonderilemedi: "
                        + ex.getMessage());
            }
        }
    }
}
