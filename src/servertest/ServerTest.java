package servertest;

/**
 * Main class to run the test server side code
 * 
 * @author M Thomas
 * @since  25/12/16
 */
public class ServerTest {

    private static final int serverPort = 9000 ;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Starting Server");
        SingleThreadedServer server = new SingleThreadedServer(serverPort) ;
        new Thread(server).start();

        try {
            Thread.sleep(60 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();  
        }

        System.out.println("Stopping Server");
        server.stop();
    }
    
}
