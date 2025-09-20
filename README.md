# Distributed Hangman Game

A multiplayer Hangman game built with Java RMI (Remote Method Invocation) that allows players to challenge each other in real-time word guessing battles.

## 🎮 Game Overview

This is a distributed multiplayer Hangman game where:
- Two players connect through a central server
- Players exchange secret words with each other
- Each player tries to guess their opponent's word
- The first to guess correctly wins, or both can lose creating a tie
- Players have 6 wrong guesses before losing their turn

## 🏗️ Architecture

The project uses Java RMI for distributed communication with a client-server architecture:

- **Server**: Manages player connections, game sessions, and communication
- **Client**: Provides GUI interface for players to interact with the game
- **RMI Registry**: Handles remote object registration and lookup

## 📁 Project Structure

```
├── Server.java          # Server startup class
├── ServerImp.java       # Server implementation with game logic
├── Register.java        # RMI registry setup
├── Iserver.java         # Server interface
├── HAngman.java         # Client GUI application
├── ClientIMp.java       # Client implementation
├── Iclient.java         # Client interface
└── GameSession.java     # Game state management
```

## 🚀 Getting Started

### Prerequisites

- Java 8 or higher
- Basic understanding of Java RMI

### Installation & Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/reemzouhby/distributed-hangman-game.git
   cd distributed-hangman-game
   ```

2. **Compile all Java files**
   ```bash
   javac *.java
   ```

3. **Start the RMI Registry**
   ```bash
   java Register
   ```
   Keep this terminal open and press Enter when ready to start the server.

4. **Start the Server**
   ```bash
   java Server
   ```
   You should see "Server is running..." message.

5. **Start Client(s)**
   ```bash
   java HAngman
   ```
   Run this command for each player (minimum 2 players needed for a game).

## 🎯 How to Play

1. **Login**: Enter a unique username and click "Login"
2. **Invite Players**: Enter another player's username and send an invitation
3. **Accept/Decline**: Respond to game invitations from other players
4. **Word Exchange**: When a game starts, enter a secret word for your opponent
5. **Guessing Phase**: Try to guess your opponent's word letter by letter
6. **Win Conditions**:
   - Guess the complete word correctly = Win
   - Run out of 6 chances = Lose your turn (opponent continues)
   - Both players lose = Tie game
   - Give up or close window = Forfeit game

## 🎮 Game Features

- **Real-time Multiplayer**: Play against other connected players
- **Interactive GUI**: User-friendly Swing-based interface
- **Game Invitations**: Send and receive game invitations
- **Word Masking**: See your progress with masked letters (e.g., "_ o _ d")
- **Chance Tracking**: Monitor remaining wrong guesses
- **Multiple Game Modes**: Win, lose, tie, or forfeit scenarios
- **Reconnection Support**: Players can reconnect with existing usernames

## 🔧 Technical Details

### RMI Communication
- **Registry Port**: 2000
- **Server Binding**: "GameServer"
- **Remote Interfaces**: Separate interfaces for client and server operations

### Game States
- **Online**: Available for invitations
- **Waiting**: Sent invitation, waiting for response
- **Invited**: Received invitation, need to respond
- **Busy**: Currently in a game
- **Offline**: Disconnected from server

### Concurrency
- Uses `ConcurrentHashMap` for thread-safe operations
- Synchronized game result processing
- Swing EDT for GUI updates

## 🛠️ Development Notes

### Key Classes

- **ServerImp**: Core server logic, manages player connections and game sessions
- **HAngman**: Main client GUI with login, game panels, and word guessing interface
- **GameSession**: Tracks game state, player results, and determines winners
- **ClientIMp**: Handles server communications and GUI callbacks

### Design Patterns Used
- **Remote Proxy**: RMI interfaces abstract network communication
- **Observer**: Clients receive notifications from server
- **Session**: GameSession maintains state between operations

## 🐛 Known Issues & Limitations

- Server must be running before clients can connect
- No persistent storage (game data lost on server restart)
- Limited to local network or single machine testing
- GUI windows may need manual management in some edge cases

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is open source and available under the [MIT License](LICENSE).

## 🎯 Future Enhancements

- [ ] Add player statistics and leaderboards
- [ ] Implement difficulty levels with different word categories
- [ ] Add chat functionality between players
- [ ] Create a web-based version
- [ ] Add AI opponents for single-player mode
- [ ] Implement tournament/bracket systems
- [ ] Add sound effects and animations

## 📞 Support

If you encounter any issues or have questions:
1. Check the console output for error messages
2. Ensure RMI registry and server are running
3. Verify network connectivity between client and server
4. Create an issue on GitHub with detailed error information

---

**Happy Gaming!** 🎮✨
