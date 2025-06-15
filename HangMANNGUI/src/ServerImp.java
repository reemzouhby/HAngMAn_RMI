import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ServerImp extends UnicastRemoteObject implements Iserver {
    protected ServerImp() throws RemoteException {
    }

    ConcurrentHashMap<String, Iclient> Clients = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, String> Status = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, GameSession> GameSessions = new ConcurrentHashMap<>();

    @Override
    public boolean RegistirationPlayers(String username, Iclient cl1) throws RemoteException {
        if (Status.containsKey(username)) {
            String currentStatus = Status.get(username);

            if (currentStatus.equals("online") || currentStatus.equals("busy") || currentStatus.equals("waiting") || currentStatus.equals("invited")) {
                // Someone is already using this username
                cl1.Receiveiv("Server", username, "This username is already in use(It is Online). Please choose another.");
                System.out.println("Attempted login with duplicate username: " + username);
                return false;
            } else if (currentStatus.equals("offline")) {

                Clients.put(username, cl1); // Update client reference
                Status.put(username, "online");
                cl1.Receiveiv("Server", username, "Welcome back, " + username + "!");
                System.out.println("Welcome back " + username);
                return true;
            }
        }

        // New user
        Clients.put(username, cl1);
        Status.put(username, "online");
        cl1.Receiveiv("Server", username, "Registration successful. Welcome, " + username + "!");
        System.out.println("New player registered: " + username);
        return true;
    }

    @Override
    public void Sendinv(String fromusername, String tousername) throws RemoteException {
        Iclient toClient = Clients.get(tousername);
        if (toClient != null && Status.get(tousername).equals("online")) {
            toClient.Receiveiv(fromusername, tousername, " has invited you to play a game. Accept? (yes/no)");
            System.out.println(fromusername + " sent invitation to " + tousername);
            Status.put(fromusername, "waiting");
            Status.put(tousername, "invited");
        } else {
            Iclient fromClient = Clients.get(fromusername);
            if (fromClient != null) {
                fromClient.Receiveiv("Server", fromusername, tousername + " is not available.");
            }
        }
    }

    @Override
    public void invresp(String fromUsername, String toUsername, String response) throws RemoteException {
        Iclient fromClient = Clients.get(fromUsername); // who sent the invite
        Iclient toClient = Clients.get(toUsername);     // who received the invite and now responds

        if (response.equalsIgnoreCase("yes")) {
            Status.put(fromUsername, "busy");
            Status.put(toUsername, "busy");


            GameSession session = new GameSession(fromUsername, toUsername);
            GameSessions.put(fromUsername + "_" + toUsername, session);
            GameSessions.put(toUsername + "_" + fromUsername, session); // Store both ways for easy lookup

            // Notify both players
            fromClient.Receiveiv("Server", fromUsername, toUsername + " accepted your invitation. Game will start.");
            toClient.Receiveiv("Server", toUsername, "You accepted the invitation from " + fromUsername + ". Game will start.");

            // Start word exchange
            fromClient.startWordExchange(toUsername);
            toClient.startWordExchange(fromUsername);

        } else {
            Status.put(fromUsername, "online");
            Status.put(toUsername, "online");

            fromClient.Receiveiv("Server", fromUsername, toUsername + " declined your invitation.");
            toClient.Receiveiv("Server", toUsername, "You declined the invitation from " + fromUsername + ".");
        }
    }

    @Override
    public void dissconnect(String username) throws RemoteException {
        Status.replace(username, "offline");
    }

    @Override
    public void SendWord(String from, String to, String word) throws RemoteException {
        Iclient fromClient = Clients.get(from);
        Iclient toClient = Clients.get(to);

        if (Clients.containsKey(to)) {
            // Store word in game session
            String sessionKey = from + "_" + to;
            GameSession session = GameSessions.get(sessionKey);
            if (session != null) {
                if (session.getPlayer1().equals(from)) {
                    session.setPlayer1Word(word);
                } else {
                    session.setPlayer2Word(word);
                }
            }

            toClient.ReceiveWord(from, word);
        } else {
            fromClient.Receiveiv("Server", to, "Failed to send word. User " + to + " is not online.");
        }
    }

    @Override
    public void reportGameResult(String player, String result) throws RemoteException {
        System.out.println("=== REPORT GAME RESULT ===");
        System.out.println("Player: " + player + ", Result: " + result);

        // Find the game session for this player
        GameSession session = null;

        // Find session with this player
        for (String key : GameSessions.keySet()) {
            GameSession gs = GameSessions.get(key);
            if (gs.getPlayer1().equals(player) || gs.getPlayer2().equals(player)) {
                session = gs;
                System.out.println("Found session: " + gs.getPlayer1() + " vs " + gs.getPlayer2());
                break;
            }
        }

        if (session != null && !session.isGameEnded()) {
            String player1 = session.getPlayer1();
            String player2 = session.getPlayer2();
            String opponent = session.getOpponent(player);

            Iclient playerClient = Clients.get(player);
            Iclient opponentClient = Clients.get(opponent);

            System.out.println("Processing result for: " + player + " (" + result + ") against " + opponent);

            if (result.equals("giveup")) {
                // Player gave up - End game immediately with no winner/loser
                System.out.println(player + " GAVE UP! Game ends with no winner.");

                // Force end the game
                session.gameEnded = true;

                // Notify both players about the give up
                try {
                    System.out.println("Notifying player who gave up: " + player);
                    if (playerClient != null) {
                        playerClient.Receiveiv("Server", player, "You gave up. Game ended with no winner.");
                        playerClient.gameEnded("giveup", opponent, "giveup");
                        System.out.println("Successfully notified player who gave up: " + player);
                    }

                    System.out.println("Notifying opponent about give up: " + opponent);
                    if (opponentClient != null) {
                        opponentClient.Receiveiv("Server", opponent, player + " gave up. Game ended with no winner.");
                        opponentClient.gameEnded("giveup", player, "giveup");
                        System.out.println("Successfully notified opponent: " + opponent);
                    }

                } catch (RemoteException e) {
                    System.err.println("ERROR notifying players about give up: " + e.getMessage());
                    e.printStackTrace();
                }
            }else if (result.equals("lost_chances")) {
                    // Player lost their chances but opponent continues
                    System.out.println(player + " LOST THEIR CHANCES. Opponent continues playing.");
                    session.setPlayerResult(player, "loss");

                    // Notify opponent that this player lost but they can continue
                    try {
                        if (opponentClient != null) {
                            opponentClient.Receiveiv("Server", opponent, player + " ran out of chances and lost! You can continue playing.");
                            System.out.println("Notified " + opponent + " that " + player + " lost their chances");
                        }
                    } catch (RemoteException e) {
                        System.err.println("Error notifying opponent: " + e.getMessage());
                    }

                    // Don't end the game yet - let opponent continue
                    return;

                } else if (result.equals("win")) {
                    // Player won -> Game ends immediately
                    System.out.println(player + " WON! Ending game immediately.");

                    // Set results for both players
                    session.setPlayerResult(player, "win");
                    session.setPlayerResult(opponent, "loss");

                    // Force end the game
                    session.gameEnded = true;

                    // Notify both players with detailed logging
                    try {
                        System.out.println("Notifying winner: " + player);
                        if (playerClient != null) {
                            playerClient.gameEnded("win", opponent, "loss");
                            System.out.println("Successfully notified winner: " + player);
                        } else {
                            System.err.println("ERROR: playerClient is null for " + player);
                        }

                        System.out.println("Notifying loser: " + opponent);
                        if (opponentClient != null) {
                            opponentClient.gameEnded("loss", player, "win");
                            opponentClient.Receiveiv("server", opponent, "u lost");
                            System.out.println("Successfully notified loser: " + opponent);
                        } else {
                            System.err.println("ERROR: opponentClient is null for " + opponent);
                        }

                    } catch (RemoteException e) {
                        System.err.println("ERROR notifying players: " + e.getMessage());
                        e.printStackTrace();
                    }

                } else if (result.equals("loss")) {
                    // Player lost
                    System.out.println(player + " LOST.");
                    session.setPlayerResult(player, "loss");

                    if (session.areBothPlayersFinished()) {
                        // Both players finished, check opponent's result
                        String opponentResult = opponent.equals(player1) ? session.getPlayer1Result() : session.getPlayer2Result();
                        System.out.println("Both finished. Opponent result: " + opponentResult);

                        if (opponentResult.equals("win")) {
                            // Opponent won, this player lost
                            session.gameEnded = true;
                            System.out.println("GAME ENDED: " + opponent + " WON, " + player + " LOST");

                            try {
                                if (playerClient != null) {
                                    playerClient.gameEnded("loss", opponent, "win");
                                }
                                if (opponentClient != null) {
                                    opponentClient.gameEnded("win", player, "loss");
                                }
                            } catch (RemoteException e) {
                                System.err.println("Error notifying players: " + e.getMessage());
                            }

                        } else if (opponentResult.equals("loss")) {
                            // Both lost -> TIE
                            session.gameEnded = true;
                            System.out.println("GAME ENDED: TIE - Both players lost");

                            try {
                                if (playerClient != null) {
                                    playerClient.gameEnded("tie", opponent, "tie");
                                }
                                if (opponentClient != null) {
                                    opponentClient.Receiveiv("server", player, "Tie");
                                    opponentClient.gameEnded("tie", player, "tie");

                                }
                            } catch (RemoteException e) {
                                System.err.println("Error notifying players about tie: " + e.getMessage());
                            }
                        }
                    } else {
                        // Only this player lost, opponent still playing
                        System.out.println("Player " + player + " lost, opponent " + opponent + " still playing...");
                        return; // Don't clean up session yet
                    }
                }

                // Clean up session only if game ended
                if (session.isGameEnded()) {
                    System.out.println("Cleaning up game session...");
                    // Reset player status
                    Status.put(player1, "online");
                    Status.put(player2, "online");

                    // Remove session
                    GameSessions.remove(player1 + "_" + player2);
                    GameSessions.remove(player2 + "_" + player1);
                    System.out.println("Session cleanup complete.");
                }

            } else {
                if (session == null) {
                    System.err.println("ERROR: Could not find active game session for player: " + player);
                } else {
                    System.err.println("ERROR: Game already ended for player: " + player);
                }
            }
        }
    }
