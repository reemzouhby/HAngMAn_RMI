public class GameSession {
    private String player1;
    private String player2;
    private String player1Word;
    private String player2Word;
    private String winner;
    private String loser;
    public boolean gameEnded; // Make this public so server can set it directly
    private boolean player1Finished;
    private boolean player2Finished;
    private String player1Result;
    private String player2Result;

    public GameSession(String player1, String player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.gameEnded = false;
        this.player1Finished = false;
        this.player2Finished = false;
    }

    public void setPlayer1Word(String word) {
        this.player1Word = word;
    }

    public void setPlayer2Word(String word) {
        this.player2Word = word;
    }

    public synchronized void setPlayerResult(String player, String result) {
        if (player.equals(player1)) {
            player1Result = result;
            player1Finished = true;
        } else if (player.equals(player2)) {
            player2Result = result;
            player2Finished = true;
        }

        // Check if both players have finished
        if (player1Finished && player2Finished) {
            determineGameResult();
        }
    }

    private void determineGameResult() {
        if (player1Result.equals("win") && player2Result.equals("loss")) {
            winner = player1;
            loser = player2;
        } else if (player2Result.equals("win") && player1Result.equals("loss")) {
            winner = player2;
            loser = player1;
        } else if (player1Result.equals("win") && player2Result.equals("win")) {
            // Both won - it's a tie
            winner = "tie";
            loser = null;
        } else {
            // Both lost - it's a tie
            winner = "tie";
            loser = null;
        }
        gameEnded = true;
    }

    public boolean isGameEnded() {
        return gameEnded;
    }

    public boolean areBothPlayersFinished() {
        return player1Finished && player2Finished;
    }

    public String getPlayer1() { return player1; }
    public String getPlayer2() { return player2; }
    public String getWinner() { return winner; }
    public String getLoser() { return loser; }
    public String getPlayer1Word() { return player1Word; }
    public String getPlayer2Word() { return player2Word; }
    public String getPlayer1Result() { return player1Result; }
    public String getPlayer2Result() { return player2Result; }

    public String getOpponent(String player) {
        return player.equals(player1) ? player2 : player1;
    }

    public String getWordForPlayer(String player) {
        return player.equals(player1) ? player2Word : player1Word;
    }

}