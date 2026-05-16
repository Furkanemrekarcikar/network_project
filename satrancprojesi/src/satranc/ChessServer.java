package satranc;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

public class ChessServer {

    // Eşleşme bekleyen istemciyi (client) tuttuğumuz referans
    private static Socket pendingClient = null;

    public static void main(String[] args) {

        int serverPort = 5000;

        try (ServerSocket listener = new ServerSocket(serverPort)) {

            System.out.println(">Sunucu aktif. Dinlenen port: " + serverPort);
            System.out.println(">Oyuncuların bağlanması bekleniyor...\n");

            // İstemcileri sürekli olarak dinleyecek ana döngü
            while (true) {

                Socket incomingConnection = listener.accept();
                System.out.println("[Bağlantı] Yeni bir cihaz bağlandı: " + incomingConnection.getInetAddress());

                // Eğer bekleyen bir oyuncu yoksa, yeni geleni beklemeye al
                if (pendingClient == null) {

                    pendingClient = incomingConnection;
                    System.out.println("[Bilgi] İlk oyuncu bağlandı, rakip aranıyor...");

                    // Oyuncuya beklediğine dair sinyal gönder
                    pendingClient.getOutputStream().write("WAITING".getBytes());

                } else {

                    // Bekleyen biri var, yeni gelenle eşleştir
                    Socket firstClient = pendingClient;
                    Socket secondClient = incomingConnection;

                    // Yeni eşleşmeler için kuyruğu sıfırla
                    pendingClient = null;

                    System.out.println("[Bilgi] İki oyuncu eşleşti.Oyun başlatılıyor.");

                    // İstemcilere hangi renkte olduklarını bildir
                    firstClient.getOutputStream().write("COLOR:WHITE".getBytes());
                    secondClient.getOutputStream().write("COLOR:BLACK".getBytes());

                    // Her iki oyuncu için karşılıklı iletişimi yönetecek thread'leri oluştur
                    Client_Operations whiteHandler = new Client_Operations(firstClient, secondClient, "WHITE");
                    Client_Operations blackHandler = new Client_Operations(secondClient, firstClient, "BLACK");

                    // Thread'leri başlat
                    whiteHandler.start();
                    blackHandler.start();

                    System.out.println(">Maç başarıyla başlatıldı.\n");
                }
            }

        } catch (IOException e) {
            System.err.println("Sunucu başlatılırken veya dinlenirken kritik bir hata oluştu: " + e.getMessage());
        }
    }
}