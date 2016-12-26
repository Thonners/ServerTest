package servertest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import sun.misc.IOUtils;

/**
 * Code for the client side, to connect to the server
 * 
 * @author M Thomas
 * @since 25/2/16
 */
public class ClientTest {
    
//    private static String serverURL = "thonners.ddns.net" ; 
    private static String serverURL = "localhost" ;

    public static void main(String[] args) {
        
        System.out.println("Starting client...");      
        
        String serverMsg = "No message :(" ;
        
        try {
            Socket s = new Socket(serverURL, 9000);
            
            System.out.println("Socket conection created. Getting data...");

            PrintWriter outp = null;
            BufferedReader inp = null;
            try {
                outp = new PrintWriter(s.getOutputStream(), true);
                inp = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String line = "" ;
                while ((line = inp.readLine()) != null) {
                    serverMsg += line + "\n" ;
                }
            } catch (IOException e) {
                System.out.println("Error getting input/output stream:");
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            System.out.println("Error creating socket:");
            e.printStackTrace() ;
            return ;
        }
        
       
            System.out.println();
            System.out.println();
            System.out.println("Server message: " + serverMsg);
       
    }
}
