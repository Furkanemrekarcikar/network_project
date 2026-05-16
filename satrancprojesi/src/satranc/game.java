/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package satranc;

import java.awt.Color;
import javax.swing.JButton;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import java.util.Locale;

/**
 *
 * @author ASUS
 */
public class game extends javax.swing.JFrame {

    /**
     * Creates new form game
     */
    JButton[][] boardButtons = new JButton[8][8];
    String[][] pieces = new String[8][8];

    private ChessClient client;

    int selectedRow = -1;
    int selectedCol = -1;

    Color highlightColor = Color.YELLOW;
    boolean isWhiteTurn = true;
    boolean gameOver = false;

    public game() {
        initComponents();
        boardButtons = new JButton[][]{
            {jButton1, jButton2, jButton3, jButton4, jButton5, jButton6, jButton7, jButton8},
            {jButton9, jButton10, jButton11, jButton12, jButton13, jButton14, jButton15, jButton16},
            {jButton17, jButton18, jButton19, jButton20, jButton21, jButton22, jButton23, jButton24},
            {jButton25, jButton26, jButton27, jButton28, jButton29, jButton30, jButton31, jButton32},
            {jButton33, jButton34, jButton35, jButton36, jButton37, jButton38, jButton39, jButton40},
            {jButton41, jButton42, jButton43, jButton44, jButton45, jButton46, jButton47, jButton48},
            {jButton49, jButton50, jButton51, jButton52, jButton53, jButton54, jButton55, jButton56},
            {jButton57, jButton58, jButton59, jButton60, jButton61, jButton62, jButton63, jButton64}
        };

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {

                final int r = row;
                final int c = col;

                boardButtons[row][col].addActionListener(e -> handleSquareSelection(r, c));
                boardButtons[row][col].setText("");
                boardButtons[row][col].setContentAreaFilled(false);
                boardButtons[row][col].setBorderPainted(false);
                boardButtons[row][col].setFocusPainted(false);
                boardButtons[row][col].setOpaque(false);
                boardButtons[row][col].setBorder(null);
            }
        }
        initializeBoard();

    }

    public game(ChessClient client) {
        this();
        this.client = client;

        this.client.setGameScreen(this);
        this.client.listenForMessages();
    }

    // squareClicked yerine:
    private void handleSquareSelection(int row, int col) {
        if (gameOver) {
            JOptionPane.showMessageDialog(this, "Oyun bitti!");
            return;
        }

        // Henüz bir taş seçilmemişse çalışma mantığı
        if (selectedRow == -1) {
            String currentPrc = pieces[row][col];
            
            if (currentPrc == null) {
                JOptionPane.showMessageDialog(this, "Burada taş yok!");
                return;
            }

            if (client != null) {
                String playerSide = client.getPlayerColor();
                if (playerSide == null) {
                    JOptionPane.showMessageDialog(this, "Player color could not be found!");
                    return;
                }

                playerSide = playerSide.trim().toLowerCase(Locale.ENGLISH);
                String prcColor = determineColor(currentPrc).trim().toLowerCase(Locale.ENGLISH);
                
                System.out.println("My color: " + playerSide);
                System.out.println("Piece color: " + prcColor);

                if (!prcColor.equals(playerSide)) {
                    JOptionPane.showMessageDialog(this, "You can only move your own pieces!");
                    return;
                }
            }

            boolean isWhitePrc = currentPrc.startsWith("white");
            if (isWhiteTurn && !isWhitePrc) {
                JOptionPane.showMessageDialog(this, "Beyazın sırası!");
                return;
            } else if (!isWhiteTurn && isWhitePrc) {
                JOptionPane.showMessageDialog(this, "Siyahın sırası!");
                return;
            }

            selectedRow = row;
            selectedCol = col;
            clearBoardHighlights();
            highlightLegalMoves(row, col);
            return;
        }

        // Hamlenin geçerliliğini kontrol et ve uygula
        if (!isMoveLegal(selectedRow, selectedCol, row, col)) {
            JOptionPane.showMessageDialog(this, "Bu taş buraya gidemez!");
            selectedRow = -1;
            selectedCol = -1;
            clearBoardHighlights();
            return;
        }
        
        if (exposesKingToThreat(selectedRow, selectedCol, row, col)) {
            JOptionPane.showMessageDialog(this, "Şah tehlikede! Bu hamleyi yapamazsın (Açmazdasın veya Şah çekildi).");
            selectedRow = -1;
            selectedCol = -1;
            clearBoardHighlights();
            return;
        }

        String activePiece = pieces[selectedRow][selectedCol];
        String enemyColor = determineColor(activePiece).equals("white") ? "black" : "white";

        executeMove(selectedRow, selectedCol, row, col);
        
        if (client != null) {
            client.sendMove(selectedRow, selectedCol, row, col);
        }

        if (verifyCheckmate(enemyColor)) {
            gameOver = true;
            int answer = JOptionPane.showConfirmDialog(this, "Mat! Tekrar oynamak ister misiniz?", "Oyun Bitti", JOptionPane.YES_NO_OPTION);
            
            if (answer == JOptionPane.YES_OPTION) {
                new Start_Screen().setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Oyun kapatılıyor.");
                System.exit(0);
            }
            return;
        }
        
        if (verifyCheckStatus(enemyColor)) {
            JOptionPane.showMessageDialog(this, "Şah!");
        }

        isWhiteTurn = !isWhiteTurn;
        selectedRow = -1;
        selectedCol = -1;
        clearBoardHighlights();
    }

    // setupPieces yerine:
    private void initializeBoard() {
        String w = "white_";
        String b = "black_";

        pieces[0][0] = w + "rook"; pieces[0][7] = w + "rook";
        pieces[7][0] = b + "rook"; pieces[7][7] = b + "rook";

        pieces[0][1] = w + "knight"; pieces[0][6] = w + "knight";
        pieces[7][1] = b + "knight"; pieces[7][6] = b + "knight";

        pieces[0][2] = w + "bishop"; pieces[0][5] = w + "bishop";
        pieces[7][2] = b + "bishop"; pieces[7][5] = b + "bishop";

        pieces[0][3] = w + "queen";
        pieces[7][3] = b + "queen";

        pieces[0][4] = w + "king";
        pieces[7][4] = b + "king";

        for (int i = 0; i < 8; i++) {
            pieces[1][i] = w + "pawn";
            pieces[6][i] = b + "pawn";
        }
    }

    // showPossibleMoves yerine:
    private void highlightLegalMoves(int row, int col) {
        String p = pieces[row][col];
        if (p == null) return;

        if (p.endsWith("knight")) {
            int[][] kMoves = {{-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}};
            for (int[] m : kMoves) {
                int nR = row + m[0];
                int nC = col + m[1];
                if (isWithinBounds(nR, nC)) {
                    boardButtons[nR][nC].setBorder(javax.swing.BorderFactory.createLineBorder(Color.YELLOW, 10));
                    boardButtons[nR][nC].setBorderPainted(true);
                }
            }
        } else if (p.endsWith("rook") || p.endsWith("bishop") || p.endsWith("queen")) {
            int[][] dirs = new int[0][0];
            
            if (p.endsWith("rook")) {
                dirs = new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
            } else if (p.endsWith("bishop")) {
                dirs = new int[][]{{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
            } else if (p.endsWith("queen")) {
                dirs = new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
            }

            for (int[] d : dirs) {
                int r2 = row + d[0];
                int c2 = col + d[1];

                while (isWithinBounds(r2, c2)) {
                    if (pieces[r2][c2] != null) break;
                    
                    boardButtons[r2][c2].setBorder(javax.swing.BorderFactory.createLineBorder(Color.YELLOW, 10));
                    boardButtons[r2][c2].setBorderPainted(true);
                    r2 += d[0];
                    c2 += d[1];
                }
            }
        } else if (p.endsWith("king")) {
            int[][] km = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
            for (int[] m : km) {
                int r2 = row + m[0];
                int c2 = col + m[1];
                if (isWithinBounds(r2, c2) && pieces[r2][c2] == null) {
                    boardButtons[r2][c2].setBorder(javax.swing.BorderFactory.createLineBorder(Color.YELLOW, 10));
                    boardButtons[r2][c2].setBorderPainted(true);
                }
            }
        } else if (p.endsWith("pawn")) {
            int dir = p.startsWith("white") ? 1 : -1;
            int step1 = row + dir;

            if (isWithinBounds(step1, col) && pieces[step1][col] == null) {
                boardButtons[step1][col].setBorder(javax.swing.BorderFactory.createLineBorder(Color.YELLOW, 10));
                boardButtons[step1][col].setBorderPainted(true);

                int startLine = p.startsWith("white") ? 1 : 6;
                int step2 = row + (2 * dir);

                if (row == startLine && isWithinBounds(step2, col) && pieces[step2][col] == null) {
                    boardButtons[step2][col].setBorder(javax.swing.BorderFactory.createLineBorder(Color.YELLOW, 10));
                    boardButtons[step2][col].setBorderPainted(true);
                }
            }
        }
    }

    // isValidMove yerine:
    private boolean isMoveLegal(int fRow, int fCol, int tRow, int tCol) {
        String p1 = pieces[fRow][fCol];
        if (p1 == null || arePiecesAllied(p1, pieces[tRow][tCol])) {
            return false;
        }

        int diffR = Math.abs(tRow - fRow);
        int diffC = Math.abs(tCol - fCol);

        if (p1.endsWith("knight")) {
            return (diffR == 2 && diffC == 1) || (diffR == 1 && diffC == 2);
        }
        
        if (p1.endsWith("rook")) {
            return (fRow == tRow || fCol == tCol) && isRouteUnblocked(fRow, fCol, tRow, tCol);
        }
        
        if (p1.endsWith("bishop")) {
            return (diffR == diffC) && isRouteUnblocked(fRow, fCol, tRow, tCol);
        }
        
        if (p1.endsWith("queen")) {
            return (fRow == tRow || fCol == tCol || diffR == diffC) && isRouteUnblocked(fRow, fCol, tRow, tCol);
        }
        
        if (p1.endsWith("king")) {
            return diffR <= 1 && diffC <= 1;
        }
        
        if (p1.endsWith("pawn")) {
            int pDir = p1.startsWith("white") ? 1 : -1;
            int startR = p1.startsWith("white") ? 1 : 6;
            int realDiffR = tRow - fRow;

            if (diffC == 0 && realDiffR == pDir && pieces[tRow][tCol] == null) {
                return true;
            }
            if (diffC == 0 && fRow == startR && realDiffR == (2 * pDir) && pieces[tRow][tCol] == null && pieces[fRow + pDir][fCol] == null) {
                return true;
            }
            if (diffC == 1 && realDiffR == pDir && arePiecesOpponents(p1, pieces[tRow][tCol])) {
                return true;
            }
        }
        return false;
    }

    // movePiece yerine:
    private void executeMove(int fR, int fC, int tR, int tC) {
        pieces[tR][tC] = pieces[fR][fC];
        pieces[fR][fC] = null;

        Icon ico = boardButtons[fR][fC].getIcon();
        boardButtons[tR][tC].setIcon(ico);
        boardButtons[fR][fC].setIcon(null);
    }

    // applyOpponentMove yerine:
    public void processIncomingMove(int r1, int c1, int r2, int c2) {
        executeMove(r1, c1, r2, c2);
        isWhiteTurn = !isWhiteTurn;
        selectedRow = -1;
        selectedCol = -1;
        clearBoardHighlights();
    }

    // isInsideBoard yerine:
    private boolean isWithinBounds(int r, int c) {
        return r >= 0 && r <= 7 && c >= 0 && c <= 7;
    }

    // resetHighlights yerine:
    private void clearBoardHighlights() {
        for (int i = 0; i < 64; i++) {
            int r = i / 8;
            int c = i % 8;
            boardButtons[r][c].setBorder(null);
            boardButtons[r][c].setBorderPainted(false);
        }
    }

    // isPathClear yerine:
    private boolean isRouteUnblocked(int r1, int c1, int r2, int c2) {
        int rDir = Integer.compare(r2, r1);
        int cDir = Integer.compare(c2, c1);

        int currR = r1 + rDir;
        int currC = c1 + cDir;

        while (currR != r2 || currC != c2) {
            if (pieces[currR][currC] != null) return false;
            currR += rDir;
            currC += cDir;
        }
        return true;
    }

    // isSameColor yerine:
    private boolean arePiecesAllied(String p1, String p2) {
        if (p1 == null || p2 == null) return false;
        return (p1.startsWith("white") && p2.startsWith("white")) || (p1.startsWith("black") && p2.startsWith("black"));
    }

    // isEnemyPiece yerine:
    private boolean arePiecesOpponents(String p1, String p2) {
        return p1 != null && p2 != null && !arePiecesAllied(p1, p2);
    }

    // getPieceColor yerine:
    private String determineColor(String p) {
        if (p == null) return "";
        return p.startsWith("white") ? "white" : (p.startsWith("black") ? "black" : "");
    }

    // isKingInCheck yerine:
    private boolean verifyCheckStatus(String kColor) {
        int kRow = -1, kCol = -1;
        String targetK = kColor + "_king";

        findKing:
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (targetK.equals(pieces[r][c])) {
                    kRow = r;
                    kCol = c;
                    break findKing;
                }
            }
        }

        if (kRow == -1) return false;

        for (int i = 0; i < 64; i++) {
            int r = i / 8;
            int c = i % 8;
            String p = pieces[r][c];
            if (p != null && !determineColor(p).equals(kColor)) {
                if (isSquareThreatened(r, c, kRow, kCol)) return true;
            }
        }
        return false;
    }

    // canAttackSquare yerine:
    private boolean isSquareThreatened(int fR, int fC, int tR, int tC) {
        String p = pieces[fR][fC];
        if (p == null) return false;

        int dR = Math.abs(tR - fR);
        int dC = Math.abs(tC - fC);

        if (p.endsWith("pawn")) {
            int dir = p.startsWith("white") ? 1 : -1;
            return (tR - fR) == dir && dC == 1;
        }
        if (p.endsWith("knight")) return (dR == 2 && dC == 1) || (dR == 1 && dC == 2);
        if (p.endsWith("rook")) return (fR == tR || fC == tC) && isRouteUnblocked(fR, fC, tR, tC);
        if (p.endsWith("bishop")) return (dR == dC) && isRouteUnblocked(fR, fC, tR, tC);
        if (p.endsWith("queen")) return (fR == tR || fC == tC || dR == dC) && isRouteUnblocked(fR, fC, tR, tC);
        if (p.endsWith("king")) return dR <= 1 && dC <= 1;

        return false;
    }

    // wouldLeaveKingInCheck yerine:
    private boolean exposesKingToThreat(int r1, int c1, int r2, int c2) {
        String tempP = pieces[r1][c1];
        String tempTarget = pieces[r2][c2];

        pieces[r2][c2] = tempP;
        pieces[r1][c1] = null;

        boolean inCheck = verifyCheckStatus(determineColor(tempP));

        pieces[r1][c1] = tempP;
        pieces[r2][c2] = tempTarget;

        return inCheck;
    }

    // isCheckmate yerine:
    private boolean verifyCheckmate(String targetColor) {
        if (!verifyCheckStatus(targetColor)) return false;

        for (int src = 0; src < 64; src++) {
            int r1 = src / 8;
            int c1 = src % 8;
            String p = pieces[r1][c1];

            if (p != null && determineColor(p).equals(targetColor)) {
                for (int dest = 0; dest < 64; dest++) {
                    int r2 = dest / 8;
                    int c2 = dest % 8;

                    if (isMoveLegal(r1, c1, r2, c2)) {
                        if (!exposesKingToThreat(r1, c1, r2, c2)) {
                            return false; 
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jButton13 = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();
        jButton15 = new javax.swing.JButton();
        jButton16 = new javax.swing.JButton();
        jButton17 = new javax.swing.JButton();
        jButton18 = new javax.swing.JButton();
        jButton19 = new javax.swing.JButton();
        jButton20 = new javax.swing.JButton();
        jButton21 = new javax.swing.JButton();
        jButton22 = new javax.swing.JButton();
        jButton23 = new javax.swing.JButton();
        jButton24 = new javax.swing.JButton();
        jButton25 = new javax.swing.JButton();
        jButton26 = new javax.swing.JButton();
        jButton27 = new javax.swing.JButton();
        jButton28 = new javax.swing.JButton();
        jButton29 = new javax.swing.JButton();
        jButton30 = new javax.swing.JButton();
        jButton31 = new javax.swing.JButton();
        jButton32 = new javax.swing.JButton();
        jButton33 = new javax.swing.JButton();
        jButton34 = new javax.swing.JButton();
        jButton35 = new javax.swing.JButton();
        jButton36 = new javax.swing.JButton();
        jButton37 = new javax.swing.JButton();
        jButton38 = new javax.swing.JButton();
        jButton39 = new javax.swing.JButton();
        jButton40 = new javax.swing.JButton();
        jButton41 = new javax.swing.JButton();
        jButton42 = new javax.swing.JButton();
        jButton43 = new javax.swing.JButton();
        jButton44 = new javax.swing.JButton();
        jButton45 = new javax.swing.JButton();
        jButton46 = new javax.swing.JButton();
        jButton47 = new javax.swing.JButton();
        jButton48 = new javax.swing.JButton();
        jButton49 = new javax.swing.JButton();
        jButton50 = new javax.swing.JButton();
        jButton51 = new javax.swing.JButton();
        jButton52 = new javax.swing.JButton();
        jButton53 = new javax.swing.JButton();
        jButton54 = new javax.swing.JButton();
        jButton55 = new javax.swing.JButton();
        jButton56 = new javax.swing.JButton();
        jButton57 = new javax.swing.JButton();
        jButton58 = new javax.swing.JButton();
        jButton59 = new javax.swing.JButton();
        jButton60 = new javax.swing.JButton();
        jButton61 = new javax.swing.JButton();
        jButton62 = new javax.swing.JButton();
        jButton63 = new javax.swing.JButton();
        jButton64 = new javax.swing.JButton();
        board_lbl = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/siyah_kale.jpg"))); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 20, 80, 80));

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/satranc_at_fotosu.jpg"))); // NOI18N
        jPanel1.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 20, 80, 80));

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/satranc_fil.jpg"))); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 20, 90, 80));

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/satranc_vezir.jpg"))); // NOI18N
        jPanel1.add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 20, 80, 80));

        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/satranc_sah.jpg"))); // NOI18N
        jPanel1.add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 20, 80, 80));

        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/satranc_fil.jpg"))); // NOI18N
        jPanel1.add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 20, 80, 90));

        jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/satranc_at_fotosu.jpg"))); // NOI18N
        jPanel1.add(jButton7, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 20, 90, 80));

        jButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/siyah_kale.jpg"))); // NOI18N
        jPanel1.add(jButton8, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 10, 70, 80));

        jButton9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/satranc_piyon.jpg"))); // NOI18N
        jPanel1.add(jButton9, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 120, 80, 80));

        jButton10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/satranc_piyon.jpg"))); // NOI18N
        jPanel1.add(jButton10, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 110, 90, 80));

        jButton11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/satranc_piyon.jpg"))); // NOI18N
        jPanel1.add(jButton11, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 120, 90, 80));

        jButton12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/satranc_piyon.jpg"))); // NOI18N
        jPanel1.add(jButton12, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 120, 80, 80));

        jButton13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/satranc_piyon.jpg"))); // NOI18N
        jPanel1.add(jButton13, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 120, 80, 80));

        jButton14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/satranc_piyon.jpg"))); // NOI18N
        jPanel1.add(jButton14, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 120, 70, 80));

        jButton15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/satranc_piyon.jpg"))); // NOI18N
        jPanel1.add(jButton15, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 110, 80, 80));

        jButton16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/satranc_piyon.jpg"))); // NOI18N
        jPanel1.add(jButton16, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 110, 70, 80));

        jButton17.setText("jButton17");
        jPanel1.add(jButton17, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 215, -1, 70));

        jButton18.setText("jButton18");
        jPanel1.add(jButton18, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 210, -1, 80));

        jButton19.setText("jButton19");
        jPanel1.add(jButton19, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 220, -1, 70));

        jButton20.setText("jButton20");
        jPanel1.add(jButton20, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 220, -1, 60));

        jButton21.setText("jButton21");
        jPanel1.add(jButton21, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 220, -1, 60));

        jButton22.setText("jButton22");
        jPanel1.add(jButton22, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 220, -1, 70));

        jButton23.setText("jButton23");
        jPanel1.add(jButton23, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 220, -1, 60));

        jButton24.setText("jButton24");
        jPanel1.add(jButton24, new org.netbeans.lib.awtextra.AbsoluteConstraints(720, 220, -1, 60));

        jButton25.setText("jButton25");
        jPanel1.add(jButton25, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 330, -1, 60));

        jButton26.setText("jButton26");
        jPanel1.add(jButton26, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 330, -1, 60));

        jButton27.setText("jButton27");
        jPanel1.add(jButton27, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 330, -1, 60));

        jButton28.setText("jButton28");
        jPanel1.add(jButton28, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 320, -1, 60));

        jButton29.setText("jButton29");
        jPanel1.add(jButton29, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 310, -1, 80));

        jButton30.setText("jButton30");
        jPanel1.add(jButton30, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 320, -1, 60));

        jButton31.setText("jButton31");
        jPanel1.add(jButton31, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 320, -1, 70));

        jButton32.setText("jButton32");
        jPanel1.add(jButton32, new org.netbeans.lib.awtextra.AbsoluteConstraints(720, 320, -1, 60));

        jButton33.setText("jButton33");
        jPanel1.add(jButton33, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 420, -1, 60));

        jButton34.setText("jButton34");
        jPanel1.add(jButton34, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 415, -1, 70));

        jButton35.setText("jButton35");
        jPanel1.add(jButton35, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 420, -1, 70));

        jButton36.setText("jButton36");
        jPanel1.add(jButton36, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 420, -1, 70));

        jButton37.setText("jButton37");
        jPanel1.add(jButton37, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 420, -1, 60));

        jButton38.setText("jButton38");
        jPanel1.add(jButton38, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 420, -1, 70));

        jButton39.setText("jButton39");
        jPanel1.add(jButton39, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 420, -1, 60));

        jButton40.setText("jButton40");
        jPanel1.add(jButton40, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 420, -1, 60));

        jButton41.setText("jButton41");
        jButton41.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton41ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton41, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 505, -1, 70));

        jButton42.setText("jButton42");
        jPanel1.add(jButton42, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 520, -1, 50));

        jButton43.setText("jButton43");
        jPanel1.add(jButton43, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 520, -1, 60));

        jButton44.setText("jButton44");
        jPanel1.add(jButton44, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 520, -1, 70));

        jButton45.setText("jButton45");
        jPanel1.add(jButton45, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 520, -1, 60));

        jButton46.setText("jButton46");
        jPanel1.add(jButton46, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 530, -1, 60));

        jButton47.setText("jButton47");
        jPanel1.add(jButton47, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 520, -1, 70));

        jButton48.setText("jButton48");
        jPanel1.add(jButton48, new org.netbeans.lib.awtextra.AbsoluteConstraints(720, 530, -1, 60));

        jButton49.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/satranc_beyaz_piyon.jpg"))); // NOI18N
        jPanel1.add(jButton49, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 610, 80, 70));

        jButton50.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/satranc_beyaz_piyon.jpg"))); // NOI18N
        jPanel1.add(jButton50, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 620, 90, 80));

        jButton51.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/satranc_beyaz_piyon.jpg"))); // NOI18N
        jPanel1.add(jButton51, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 630, 80, 70));

        jButton52.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/satranc_beyaz_piyon.jpg"))); // NOI18N
        jPanel1.add(jButton52, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 610, 80, 80));

        jButton53.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/satranc_beyaz_piyon.jpg"))); // NOI18N
        jPanel1.add(jButton53, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 620, 80, 70));

        jButton54.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/satranc_beyaz_piyon.jpg"))); // NOI18N
        jPanel1.add(jButton54, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 610, 80, 80));

        jButton55.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/satranc_beyaz_piyon.jpg"))); // NOI18N
        jPanel1.add(jButton55, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 610, 90, 90));

        jButton56.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/satranc_beyaz_piyon.jpg"))); // NOI18N
        jPanel1.add(jButton56, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 620, 80, 80));

        jButton57.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/satranc_beyaz_kale.jpg"))); // NOI18N
        jPanel1.add(jButton57, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 700, 70, 90));

        jButton58.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/satranc_beyaz_at.jpg"))); // NOI18N
        jButton58.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton58ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton58, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 710, 90, 80));

        jButton59.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/satranc_beyaz_fil.jpg"))); // NOI18N
        jPanel1.add(jButton59, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 710, 90, 80));

        jButton60.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/satranc_beyaz_vezir.jpg"))); // NOI18N
        jPanel1.add(jButton60, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 710, 80, 80));

        jButton61.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/satranc_beyaz_sah.jpg"))); // NOI18N
        jPanel1.add(jButton61, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 710, 80, 80));

        jButton62.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/satranc_beyaz_fil.jpg"))); // NOI18N
        jPanel1.add(jButton62, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 720, 90, 80));

        jButton63.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/satranc_beyaz_at.jpg"))); // NOI18N
        jPanel1.add(jButton63, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 720, 90, 70));

        jButton64.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/satranc_beyaz_kale.jpg"))); // NOI18N
        jPanel1.add(jButton64, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 720, 80, 70));

        board_lbl.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/3054017_stock-photo-chessboard.jpg"))); // NOI18N
        jPanel1.add(board_lbl, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 820, 790));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton41ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton41ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton41ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton58ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton58ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton58ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(game.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(game.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(game.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(game.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new game().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel board_lbl;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton16;
    private javax.swing.JButton jButton17;
    private javax.swing.JButton jButton18;
    private javax.swing.JButton jButton19;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton20;
    private javax.swing.JButton jButton21;
    private javax.swing.JButton jButton22;
    private javax.swing.JButton jButton23;
    private javax.swing.JButton jButton24;
    private javax.swing.JButton jButton25;
    private javax.swing.JButton jButton26;
    private javax.swing.JButton jButton27;
    private javax.swing.JButton jButton28;
    private javax.swing.JButton jButton29;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton30;
    private javax.swing.JButton jButton31;
    private javax.swing.JButton jButton32;
    private javax.swing.JButton jButton33;
    private javax.swing.JButton jButton34;
    private javax.swing.JButton jButton35;
    private javax.swing.JButton jButton36;
    private javax.swing.JButton jButton37;
    private javax.swing.JButton jButton38;
    private javax.swing.JButton jButton39;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton40;
    private javax.swing.JButton jButton41;
    private javax.swing.JButton jButton42;
    private javax.swing.JButton jButton43;
    private javax.swing.JButton jButton44;
    private javax.swing.JButton jButton45;
    private javax.swing.JButton jButton46;
    private javax.swing.JButton jButton47;
    private javax.swing.JButton jButton48;
    private javax.swing.JButton jButton49;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton50;
    private javax.swing.JButton jButton51;
    private javax.swing.JButton jButton52;
    private javax.swing.JButton jButton53;
    private javax.swing.JButton jButton54;
    private javax.swing.JButton jButton55;
    private javax.swing.JButton jButton56;
    private javax.swing.JButton jButton57;
    private javax.swing.JButton jButton58;
    private javax.swing.JButton jButton59;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton60;
    private javax.swing.JButton jButton61;
    private javax.swing.JButton jButton62;
    private javax.swing.JButton jButton63;
    private javax.swing.JButton jButton64;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
