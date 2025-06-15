import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Iclient extends Remote {

    void Receiveiv(String from, String to, String message) throws RemoteException;
    void ReceiveWord(String from,  String word) throws RemoteException;

    void startWordExchange(String opponent) throws RemoteException;

    void gameEnded(String result, String opponent, String opponentResult) throws RemoteException;

}

