import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class HAngman extends JFrame implements ActionListener {
    private JTextField usernameField;
    private JButton loginButton;
    private JButton ExitButton;
    private JButton gameExitButton; // Exit button for game panel
    private Iserver server;
    private Iclient clientImpl;
    private JTextArea messagesArea;
    private JFrame gameFrame;
    private JFrame currentGuessFrame; // Track the current guessing window
    private volatile boolean gameEnded = false; // Flag to prevent multiple result reports

    public HAngman() {
        setTitle("Hangman Game Login");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(3, 1));

        panel.add(new JLabel("Enter your username:"));
        usernameField = new JTextField();
        panel.add(usernameField);

        loginButton = new JButton("Login");
        panel.add(loginButton);
        loginButton.addActionListener(this);
        ExitButton = new JButton("Exit");
        panel.add(ExitButton);
        ExitButton.addActionListener(this);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        // Add window closing listener for proper cleanup
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleExit();
            }
        });

        add(panel);
    }

    public static void main(String[] args) {
        new HAngman().setVisible(true);
    }

    public void showEnterWordForm(String opponent) {
        // Reset game ended flag for new game
        gameEnded = false;

        // Hide the main game frame when showing word form
        if (gameFrame != null) {
            gameFrame.setVisible(false);
        }

        JFrame wordFrame = new JFrame("Enter Word for " + opponent);
        wordFrame.setSize(300, 200);
        wordFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel label = new JLabel("Enter a word to send to " + opponent + ":");
        JTextField wordField = new JTextField();
        JButton sendButton = new JButton("Send");
        JButton cancelButton = new JButton("Cancel");

        sendButton.addActionListener(e -> {
            String word = wordField.getText().trim().toLowerCase();
            if (!word.isEmpty()) {
                try {
                    server.SendWord(usernameField.getText(), opponent, word);
                    wordFrame.dispose();
                    JOptionPane.showMessageDialog(null, "Word sent! Waiting for " + opponent + " to send their word...");
                } catch (RemoteException ex) {
                    JOptionPane.showMessageDialog(this, "Failed to send word: " + ex.getMessage());
                }
            }
        });

        cancelButton.addActionListener(e -> {
            wordFrame.dispose();
            if (gameFrame != null) {
                gameFrame.setVisible(true);
            }
        });

        // Allow Enter key to send word
        wordField.addActionListener(e -> sendButton.doClick());

        // Add window closing listener
        wordFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (gameFrame != null) {
                    gameFrame.setVisible(true);
                }
            }
        });

        panel.add(label);
        panel.add(wordField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(sendButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel);

        wordFrame.add(panel);
        wordFrame.setVisible(true);
    }

    // In HAngman.java, replace the showGuessForm method with this fixed version:

    public void showGuessForm(String opponentWord, String opponent) {
        // Reset game ended flag for new guessing session
        gameEnded = false;

        // Close any existing guess frame
        if (currentGuessFrame != null) {
            currentGuessFrame.dispose();
        }

        currentGuessFrame = new JFrame("Guess the Word from " + opponent);
        currentGuessFrame.setSize(400, 350);
        currentGuessFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(7, 1));

        Set<Character> guessedChars = new HashSet<>();
        JLabel maskedLabel = new JLabel(maskWord(opponentWord, guessedChars), SwingConstants.CENTER);
        maskedLabel.setFont(new Font("Monospaced", Font.BOLD, 16));

        JTextField charField = new JTextField();
        JButton guessButton = new JButton("Send Guess");
        JButton giveUpButton = new JButton("Give Up");

        int[] remainingChances = {6};
        JLabel chancesLabel = new JLabel("Remaining chances: " + remainingChances[0], SwingConstants.CENTER);

        // Add a label to show guessed letters
        JLabel guessedLabel = new JLabel("Guessed letters: ", SwingConstants.CENTER);

        panel.add(new JLabel("Guess the word: ", SwingConstants.CENTER));
        panel.add(maskedLabel);
        panel.add(chancesLabel);
        panel.add(guessedLabel);
        panel.add(new JLabel("Enter a character:"));
        panel.add(charField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(guessButton);
        buttonPanel.add(giveUpButton);
        panel.add(buttonPanel);

        currentGuessFrame.add(panel);
        currentGuessFrame.setVisible(true);

        // Give up button functionality
        giveUpButton.addActionListener(e -> {
            if (!gameEnded) {
                int confirm = JOptionPane.showConfirmDialog(
                        currentGuessFrame,
                        "Are you sure you want to give up? This will end the game for both players.",
                        "Give Up",
                        JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    gameEnded = true;
                    try {
                        // Report "giveup" instead of "loss" to notify both players
                        server.reportGameResult(usernameField.getText(), "giveup");
                    } catch (RemoteException ex) {
                        JOptionPane.showMessageDialog(null, "Error reporting result: " + ex.getMessage());
                    }
                }
            }
        });

        guessButton.addActionListener(e -> {
            // Check if game has already ended
            if (gameEnded) {
                return;
            }

            String input = charField.getText().trim().toLowerCase();
            charField.setText("");

            if (input.length() != 1 || !Character.isLetter(input.charAt(0))) {
                JOptionPane.showMessageDialog(null, "Please enter a single valid letter.");
                return;
            }

            char guess = input.charAt(0);

            if (guessedChars.contains(guess)) {
                JOptionPane.showMessageDialog(null, "You already guessed this letter!");
                return;
            }

            guessedChars.add(guess);
            guessedLabel.setText("Guessed letters: " + guessedChars.toString());

            String newMasked = maskWord(opponentWord, guessedChars);
            maskedLabel.setText(newMasked);

            if (!opponentWord.contains(String.valueOf(guess))) {
                remainingChances[0]--;
                chancesLabel.setText("Remaining chances: " + remainingChances[0]);
            }

            // Check win condition
            if (!newMasked.contains("_")) {
                // Player WON by guessing the word
                gameEnded = true;
                try {
                    server.reportGameResult(usernameField.getText(), "win");
                    // Don't close the frame here - let gameEnded method handle it
                } catch (RemoteException ex) {
                    JOptionPane.showMessageDialog(null, "Error reporting result: " + ex.getMessage());
                }
            } else if (remainingChances[0] == 0) {
                // Player LOST - show message and close their window, but let opponent continue
                gameEnded = true;

                // Show message to player that they lost their chances
                JOptionPane.showMessageDialog(currentGuessFrame,
                        "You lost! You ran out of chances.\nThe word was: " + opponentWord +
                                "\n" + opponent + " continues playing...");

                // Close this player's guessing window
                currentGuessFrame.dispose();
                currentGuessFrame = null;

                try {
                    // Report loss but don't end the entire game
                    server.reportGameResult(usernameField.getText(), "lost_chances");
                } catch (RemoteException ex) {
                    JOptionPane.showMessageDialog(null, "Error reporting result: " + ex.getMessage());
                }
            }
        });

        // Allow Enter key to submit guess
        charField.addActionListener(e -> guessButton.doClick());

        // Set focus to the text field
        charField.requestFocus();

        // Add window closing listener
        currentGuessFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (!gameEnded) {
                    int confirm = JOptionPane.showConfirmDialog(
                            currentGuessFrame,
                            "Closing this window will forfeit the game. Continue?",
                            "Forfeit Game",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (confirm == JOptionPane.YES_OPTION) {
                        gameEnded = true;
                        try {
                            // Report as giveup when closing window during game
                            server.reportGameResult(usernameField.getText(), "giveup");
                        } catch (RemoteException ex) {
                            // Handle silently
                        }
                        currentGuessFrame.dispose();
                    }
                } else {
                    currentGuessFrame.dispose();
                }
            }
        });
    }
    public void showGameEndDialog(String result, String opponent, String opponentResult) {
        System.out.println("RECEIVED GAME END: " + result + " vs " + opponent + "(" + opponentResult + ")");

        // Set game ended flag FIRST
        gameEnded = true;

        // Close any open guessing window IMMEDIATELY
        SwingUtilities.invokeLater(() -> {
            if (currentGuessFrame != null) {
                System.out.println("Closing guess frame for game end");
                currentGuessFrame.dispose();
                currentGuessFrame = null;
            }
        });

        String message;
        String title;

        if (result.equals("win")) {
            message = "🎉 Congratulations! You WON the game!";
            title = "Victory!";
            message += "\n" + opponent + " lost.";
        } else if (result.equals("loss")) {
            message = "😞 You LOST the game.";
            title = "Game Over";
            message += "\n" + opponent + " won by guessing their word correctly!";
        } else if (result.equals("giveup")) {
            message = "🤷 Game Ended - " + opponent + " gave up.";
            title = "Game Ended";
            message += "\nNo winner, no loser.";
        } else  { // tie
            message = "🤝 It's a TIE!";
            title = "Tie Game";
            message += "\nBoth players lost their words!";
        }

        // Show dialog with more delay to ensure UI is ready
        String finalMessage = message;
        SwingUtilities.invokeLater(() -> {
            Timer delayTimer = new Timer(200, evt -> {
                System.out.println("Showing game end dialog: " + title);
                int choice = JOptionPane.showOptionDialog(
                        null,
                        finalMessage + "\n\nWhat would you like to do?",
                        title,
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        new String[]{"Play Again", "Exit"},
                        "Play Again"
                );

                if (choice == 0) { // Play Again
                    if (gameFrame != null) {
                        gameFrame.setVisible(true);
                        gameFrame.toFront();
                    }
                } else { // Exit
                    handleExit();
                }
            });
            delayTimer.setRepeats(false);
            delayTimer.start();
        });
    }

    private String maskWord(String word, Collection<Character> guessedChars) {
        StringBuilder masked = new StringBuilder();
        for (char c : word.toCharArray()) {
            if (guessedChars.contains(c)) {
                masked.append(c).append(" ");
            } else {
                masked.append("_ ");
            }
        }
        return masked.toString();
    }

    void showGamePanel(String username, Iserver server, Iclient client, JTextArea messagesArea) {
        gameFrame = new JFrame("Welcome " + username);
        gameFrame.setSize(400, 350);
        gameFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        gameFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel inviteLabel = new JLabel("Enter username to connect with:");
        JTextField userToInvite = new JTextField();
        JButton connectButton = new JButton("Send Invite");

        JTextArea statusArea = new JTextArea(5, 20);
        statusArea.setEditable(false);

        // Create Exit button for game panel
        gameExitButton = new JButton("Exit Game");
        gameExitButton.addActionListener(this);

        connectButton.addActionListener(e -> {
            String targetUser = userToInvite.getText().trim();
            if (!targetUser.isEmpty() && !targetUser.equals(username)) {
                try {
                    statusArea.append("Sending invitation to: " + targetUser + "\n");
                    server.Sendinv(username, targetUser);
                    connectButton.setEnabled(false);
                    // Re-enable button after 5 seconds
                    Timer timer = new Timer(5000, evt -> {
                        connectButton.setEnabled(true);
                        userToInvite.setText("");
                    });
                    timer.setRepeats(false);
                    timer.start();
                } catch (RemoteException ex) {
                    statusArea.append("Failed to send invite: " + ex.getMessage() + "\n");
                }
            } else {
                statusArea.append("Invalid username.\n");
            }
        });

        panel.add(inviteLabel);
        panel.add(userToInvite);
        panel.add(connectButton);
        panel.add(new JScrollPane(statusArea));
        panel.add(new JLabel("Messages:"));
        panel.add(new JScrollPane(messagesArea));
        panel.add(gameExitButton); // Add exit button to game panel

        gameFrame.add(panel);

        // Add window closing listener for game frame
        gameFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleExit();
            }
        });

        gameFrame.setVisible(true);
        this.setVisible(false);
    }

    // Centralized exit handling method
    private void handleExit() {
        try {
            String username = usernameField.getText().trim();
            if (server != null && username != null && !username.isEmpty()) {
                server.dissconnect(username);
            }

            int confirm = JOptionPane.showConfirmDialog(
                    null,
                    "Are you sure you want to exit?",
                    "Confirm Exit",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                // Close all frames
                if (gameFrame != null) {
                    gameFrame.dispose();
                }
                if (currentGuessFrame != null) {
                    currentGuessFrame.dispose();
                }
                dispose();
                System.exit(0);
            }
        } catch (RemoteException ex) {
            // If disconnect fails, still allow exit
            System.err.println("Disconnect failed: " + ex.getMessage());
            System.exit(0);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String username = usernameField.getText().trim();

        if (e.getSource().equals(loginButton)) {
            if (!username.isEmpty()) {
                try {
                    Registry registry = LocateRegistry.getRegistry("localhost", 2000);
                    server = (Iserver) registry.lookup("GameServer");
                    clientImpl = new ClientIMp(username, server, this);
                    boolean b = server.RegistirationPlayers(username, clientImpl);
                    if (b) {
                        messagesArea = new JTextArea();
                        showGamePanel(username, server, clientImpl, messagesArea);
                    } else {
                        JOptionPane.showMessageDialog(this, "Username already in use.");
                    }
                } catch (RemoteException | NotBoundException ex) {
                    JOptionPane.showMessageDialog(this, "Login failed: " + ex.getMessage());
                }
            }
        }

        if (e.getSource().equals(ExitButton) || e.getSource().equals(gameExitButton)) {
            handleExit();
        }
    }
}