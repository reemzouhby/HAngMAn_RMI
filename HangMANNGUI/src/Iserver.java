import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Iserver extends Remote {
    boolean RegistirationPlayers(String username, Iclient cl1) throws RemoteException;

    void Sendinv(String fromusername, String tousername) throws RemoteException;


    void invresp(String fromUsername, String toUsername, String response) throws RemoteException;

    void dissconnect(String username) throws RemoteException;
    void SendWord(String from , String to ,String word )throws RemoteException;
    void reportGameResult(String player, String result) throws RemoteException;
}


