import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {

    public static void main(String[] args) {
        ServerImp server = null;
        try {


            try {
                Registry registry = LocateRegistry.getRegistry(2000);
                registry.rebind("GameServer", new ServerImp());

            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Server is running...");
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }


    }
}
