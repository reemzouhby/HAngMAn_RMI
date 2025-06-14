import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Scanner;

public class Register {
    public static void main(String[] args) {
        int port = 2000;
        try {
            LocateRegistry.createRegistry(port);
            Scanner key = new Scanner(System.in);
            key.nextLine();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

    }
}
