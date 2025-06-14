import javax.swing.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ClientIMp extends UnicastRemoteObject implements Iclient {
    private String username;
    private static Iserver server;
    private HAngman gui;  // Reference to the GUI

    public ClientIMp(String username, Iserver server, HAngman gui) throws RemoteException {
        this.username = username;
        this.server = server;
        this.gui = gui;
    }

    @Override
    public void Receiveiv(String from, String to, String message) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, from + " to " + to + ": " + message);
        });

        if (message.contains("Accept? (yes/no)")) {
            SwingUtilities.invokeLater(() -> {
                int answer = JOptionPane.showConfirmDialog(null, from + message, "Game Invitation", JOptionPane.YES_NO_OPTION);
                String response = (answer == JOptionPane.YES_OPTION) ? "yes" : "no";
                try {
                    server.invresp(from, to, response);
                } catch (RemoteException ex) {
                    JOptionPane.showMessageDialog(null, "Failed to send response: " + ex.getMessage());
                }
            });
        }
    }

    @Override
    public void ReceiveWord(String from, String word) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, "You received a word from " + from + ". Start guessing!");
            // Start the guessing game with the received word
            gui.showGuessForm(word, from);
        });
    }

    @Override
    public void startWordExchange(String opponent) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            gui.showEnterWordForm(opponent);  // GUI reference passed in constructor
        });
    }

    @Override
    public void gameEnded(String result, String opponent, String opponentResult) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            gui.showGameEndDialog(result, opponent, opponentResult);
        });
    }
}