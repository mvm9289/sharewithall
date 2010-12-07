package sharewithall.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

public class ShareWithAll
{
    public static SWAClient client;
    public static final String DEFAULT_SERVER_IP = "mvm9289.dyndns.org";
    public static final int DEFAULT_SERVER_PORT = 4040;
    static private LoginGraphicalInterface loginI;
    
    private static void printUsage()
    {
        System.out.println(
            "\n\tUSAGE:\n\t\t" +
                "java sharewithall.server.SWAClient [serverIP:serverPort]" +
                "\n\n\t\tor\n\n\t\t" +
                "java sharewithall.server.SWAClient [serverIP]" +
            "\n\n\t*Arguments between [] are optional." +
            "Default server IP and port are " + DEFAULT_SERVER_IP + ":" + DEFAULT_SERVER_PORT + ".\n");
    }
    
    public static void main(String[] args)
    {
        try
        {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            if (args.length == 1)
            {
                String[] aux = args[0].split(":");
                if (aux.length == 1) client = new SWAClient(aux[0], DEFAULT_SERVER_PORT);
                else if (aux.length == 2) client = new SWAClient(aux[0], Integer.valueOf(aux[1]).intValue());
                else printUsage();
            }
            else if (args.length == 0) client = new SWAClient(DEFAULT_SERVER_IP, DEFAULT_SERVER_PORT);
            else{
                printUsage();
                return;
            }
            loginI = new LoginGraphicalInterface(client);
            
            loginI.B_Login.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {

                }
            });
            
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}