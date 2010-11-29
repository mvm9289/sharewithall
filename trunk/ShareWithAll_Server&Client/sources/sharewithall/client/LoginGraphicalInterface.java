package sharewithall.client;

import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class LoginGraphicalInterface
{

    private JFrame login;
    static private SWAClient client;
    private JTextField TF_Username;
    private JTextField TF_Password;
    private JTextField TF_Client;

    private static void printUsage()
    {
        System.out.println(
            "\n\tUSAGE:\n\t\t" +
                "java sharewithall.server.SWAClient [serverIP:serverPort]" +
                "\n\n\t\tor\n\n\t\t" +
                "java sharewithall.server.SWAClient [serverIP]" +
            "\n\n\t*Arguments between [] are optional." +
            "Default server IP and port are " + client.DEFAULT_SERVER_IP + ":" + client.DEFAULT_SERVER_PORT + ".\n");
    }    
    
    /**
     * Launch the application.
     */
    public static void main(final String[] args)
    {

        
        EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                try
                {
                    LoginGraphicalInterface window = new LoginGraphicalInterface();
                    window.login.setVisible(true);
                    if (args.length == 1)
                    {
                        String[] aux = args[0].split(":");
                        if (aux.length == 1) client = new SWAClient(aux[0], client.DEFAULT_SERVER_PORT);
                        else if (aux.length == 2) client = new SWAClient(aux[0], Integer.valueOf(aux[1]).intValue());
                        else printUsage();
                    }
                    else if (args.length == 0) new SWAClient(client.DEFAULT_SERVER_IP, client.DEFAULT_SERVER_PORT);
                    else printUsage();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public LoginGraphicalInterface()
    {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize()
    {
        login = new JFrame();
        login.setBounds(100, 100, 384, 248);
        login.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        login.getContentPane().setLayout(null);
        
        JPanel panel = new JPanel();
        panel.setBorder(new LineBorder(new Color(0, 0, 0)));
        panel.setBounds(12, 12, 350, 144);
        login.getContentPane().add(panel);
        panel.setLayout(null);
        
        JLabel L_Username = new JLabel("Username: ");
        L_Username.setBounds(12, 12, 86, 14);
        panel.add(L_Username);
        
        JLabel L_Password = new JLabel("Password:");
        L_Password.setBounds(12, 38, 86, 14);
        panel.add(L_Password);
        
        TF_Username = new JTextField();
        TF_Username.setBounds(116, 10, 175, 18);
        panel.add(TF_Username);
        TF_Username.setColumns(10);
        
        TF_Password = new JTextField();
        TF_Password.setColumns(10);
        TF_Password.setBounds(116, 36, 175, 18);
        panel.add(TF_Password);
        
        JLabel L_Client = new JLabel("Client:");
        L_Client.setBounds(12, 64, 70, 14);
        panel.add(L_Client);
        
        TF_Client = new JTextField();
        TF_Client.setColumns(10);
        TF_Client.setBounds(116, 62, 175, 18);
        panel.add(TF_Client);
        
        JCheckBox CB_Visible = new JCheckBox("");
        CB_Visible.setBounds(116, 83, 129, 22);
        panel.add(CB_Visible);
        
        JLabel L_Visible = new JLabel("Visible:");
        L_Visible.setBounds(12, 87, 70, 14);
        panel.add(L_Visible);
        
        JButton B_Login = new JButton("Login");
        B_Login.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                client.loginCommand(TF_Username.getText(), TF_Password.getText(), TF_Client.getText(), true); //TODO canviar true
            }
        });
        B_Login.setBounds(168, 112, 171, 24);
        panel.add(B_Login);
        
        JButton B_Register = new JButton("Register");
        B_Register.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                client.newUserCommand(TF_Username.getText(), TF_Password.getText());
            }
        });
        B_Register.setBounds(12, 167, 171, 24);
        login.getContentPane().add(B_Register);
        
        JButton B_Forgot = new JButton("Forgot Password?");
        B_Forgot.setBounds(191, 167, 171, 24);
        login.getContentPane().add(B_Forgot);
        initDataBindings();
    }
    protected void initDataBindings() {
    }
}
