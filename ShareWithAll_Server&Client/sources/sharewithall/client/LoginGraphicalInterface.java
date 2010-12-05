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
import java.awt.Font;
import javax.swing.SwingConstants;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

public class LoginGraphicalInterface extends javax.swing.JFrame
{
    private RegisterGraphicalInterface registerI;
    private SWAClient client;
    private JLabel L_Username;
    private JTextField TF_Username;
    private JLabel L_Password;
    private JPasswordField TF_Password;
    private JLabel L_Client;
    private JTextField TF_Client;
    private JLabel L_Visible;
    private JCheckBox CB_Visible;
    private JButton B_Register;
    JButton B_Login;
    private JLabel L_Gateway;
    JCheckBox CB_Gateway;
    private JPanel panel;


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
        setResizable(false);
        setTitle("Login - Share With All");
        client = c;
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize()
    {
        
        setBounds(100, 100, 344, 259);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{30, 0, 354, 30, 0};
        gridBagLayout.rowHeights = new int[]{30, -9, 26, 17, 0, 0, 0, 0};
        gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
        getContentPane().setLayout(gridBagLayout);
        
        L_Username = new JLabel("Username: ");
        GridBagConstraints gbc_L_Username = new GridBagConstraints();
        gbc_L_Username.insets = new Insets(0, 0, 5, 5);
        gbc_L_Username.gridx = 1;
        gbc_L_Username.gridy = 1;
        getContentPane().add(L_Username, gbc_L_Username);
        
        TF_Username = new JTextField();
        TF_Username.setHorizontalAlignment(SwingConstants.LEFT);
        TF_Username.setFont(new Font("SansSerif", Font.PLAIN, 12));
        TF_Username.setColumns(10);
        GridBagConstraints gbc_TF_Username = new GridBagConstraints();
        gbc_TF_Username.fill = GridBagConstraints.HORIZONTAL;
        gbc_TF_Username.insets = new Insets(0, 0, 5, 5);
        gbc_TF_Username.gridx = 2;
        gbc_TF_Username.gridy = 1;
        getContentPane().add(TF_Username, gbc_TF_Username);
        
        L_Password = new JLabel("Password:");
        GridBagConstraints gbc_L_Password = new GridBagConstraints();
        gbc_L_Password.insets = new Insets(0, 0, 5, 5);
        gbc_L_Password.gridx = 1;
        gbc_L_Password.gridy = 2;
        getContentPane().add(L_Password, gbc_L_Password);
        
        TF_Password = new JPasswordField();
        TF_Password.setColumns(10);
        GridBagConstraints gbc_TF_Password = new GridBagConstraints();
        gbc_TF_Password.fill = GridBagConstraints.HORIZONTAL;
        gbc_TF_Password.insets = new Insets(0, 0, 5, 5);
        gbc_TF_Password.gridx = 2;
        gbc_TF_Password.gridy = 2;
        getContentPane().add(TF_Password, gbc_TF_Password);
        
        L_Client = new JLabel("Client:");
        GridBagConstraints gbc_L_Client = new GridBagConstraints();
        gbc_L_Client.insets = new Insets(0, 0, 5, 5);
        gbc_L_Client.gridx = 1;
        gbc_L_Client.gridy = 3;
        getContentPane().add(L_Client, gbc_L_Client);
        
        TF_Client = new JTextField();
        TF_Client.setColumns(10);
        GridBagConstraints gbc_TF_Client = new GridBagConstraints();
        gbc_TF_Client.fill = GridBagConstraints.HORIZONTAL;
        gbc_TF_Client.insets = new Insets(0, 0, 5, 5);
        gbc_TF_Client.gridx = 2;
        gbc_TF_Client.gridy = 3;
        getContentPane().add(TF_Client, gbc_TF_Client);
        
        L_Visible = new JLabel("Visible:");
        GridBagConstraints gbc_L_Visible = new GridBagConstraints();
        gbc_L_Visible.insets = new Insets(0, 0, 5, 5);
        gbc_L_Visible.gridx = 1;
        gbc_L_Visible.gridy = 4;
        getContentPane().add(L_Visible, gbc_L_Visible);
        
        CB_Visible = new JCheckBox("");
        GridBagConstraints gbc_CB_Visible = new GridBagConstraints();
        gbc_CB_Visible.anchor = GridBagConstraints.WEST;
        gbc_CB_Visible.insets = new Insets(0, 0, 5, 5);
        gbc_CB_Visible.gridx = 2;
        gbc_CB_Visible.gridy = 4;
        getContentPane().add(CB_Visible, gbc_CB_Visible);
        
        L_Gateway = new JLabel("Use Gateway");
        GridBagConstraints gbc_L_Gateway = new GridBagConstraints();
        gbc_L_Gateway.insets = new Insets(0, 0, 5, 5);
        gbc_L_Gateway.gridx = 1;
        gbc_L_Gateway.gridy = 5;
        getContentPane().add(L_Gateway, gbc_L_Gateway);
        
        CB_Gateway = new JCheckBox("");
        GridBagConstraints gbc_CB_Gateway = new GridBagConstraints();
        gbc_CB_Gateway.anchor = GridBagConstraints.WEST;
        gbc_CB_Gateway.insets = new Insets(0, 0, 5, 5);
        gbc_CB_Gateway.gridx = 2;
        gbc_CB_Gateway.gridy = 5;
        getContentPane().add(CB_Gateway, gbc_CB_Gateway);
        
        panel = new JPanel();
        GridBagConstraints gbc_panel = new GridBagConstraints();
        gbc_panel.insets = new Insets(0, 0, 0, 5);
        gbc_panel.anchor = GridBagConstraints.WEST;
        gbc_panel.gridx = 2;
        gbc_panel.gridy = 6;
        getContentPane().add(panel, gbc_panel);
        
        B_Login = new JButton("Login");
        panel.add(B_Login);
        
        B_Register = new JButton("Register");
        panel.add(B_Register);
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
        TF_Password.setText(""); 
    }
}
