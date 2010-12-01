package sharewithall.client;

import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JPasswordField;

public class LoginGraphicalInterface extends javax.swing.JFrame
{
    private RegisterGraphicalInterface registerI;
    private SWAClient client;
    private JTextField TF_Username;
    private JTextField TF_Client;
    private JCheckBox CB_Visible;
    
    private JButton B_Register;
    public JButton B_Login;
    private JPasswordField TF_Password;


    public void start()
    {
        try
        {
            setVisible(true);
            B_Register.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    registerI = new RegisterGraphicalInterface(client);
                    registerI.start();
                    
                    registerI.B_Cancel.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent arg0) {
                            registerI.dispose();
                        }
                    });
                    
                    registerI.B_Register.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent arg0) {
                            if(registerI.getPassword() != null){
                                client.newUserCommand(registerI.getUsername(), registerI.getPassword());
                                registerI.dispose();
                            }
                        }
                    });
                }
            });
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public LoginGraphicalInterface(SWAClient c)
    {
        setTitle("Login - Share With All");
        client = c;
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize()
    {
        
        setBounds(100, 100, 384, 196);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(null);
        
        JPanel panel = new JPanel();
        panel.setBorder(new LineBorder(new Color(0, 0, 0)));
        panel.setBounds(12, 12, 350, 144);
        getContentPane().add(panel);
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
        
        JLabel L_Client = new JLabel("Client:");
        L_Client.setBounds(12, 64, 70, 14);
        panel.add(L_Client);
        
        TF_Client = new JTextField();
        TF_Client.setColumns(10);
        TF_Client.setBounds(116, 62, 175, 18);
        panel.add(TF_Client);
        
        CB_Visible = new JCheckBox("");
        CB_Visible.setBounds(116, 83, 129, 22);
        panel.add(CB_Visible);
        
        JLabel L_Visible = new JLabel("Visible:");
        L_Visible.setBounds(12, 87, 70, 14);
        panel.add(L_Visible);
        
        B_Login = new JButton("Login");
        B_Login.setBounds(186, 112, 153, 24);
        panel.add(B_Login);
        
        B_Register = new JButton("Register");
        B_Register.setBounds(12, 112, 153, 24);
        panel.add(B_Register);
        
        TF_Password = new JPasswordField();
        TF_Password.setBounds(116, 36, 175, 18);
        panel.add(TF_Password);
        initDataBindings();
    }
    protected void initDataBindings() {
    }
    
    public String getUsername()
    {
        return TF_Username.getText(); 
    }
    public String getPassword()
    {
        return TF_Password.getText(); 
    }
    public String getClient()
    {
        return TF_Client.getText(); 
    }
    public boolean getPublic()
    {
        return CB_Visible.isSelected();
    }


    public void clearFields()
    {
        TF_Username.setText("");
        TF_Password.setText(""); 
        TF_Client.setText("");
        CB_Visible.setSelected(false);
    }
}
