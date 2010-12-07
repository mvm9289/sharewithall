package sharewithall.client;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
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
    private JLabel L_Receiving;
    private JCheckBox CB_Receiving;
    private JLabel L_Links;
    private JCheckBox CB_Links;
    
    public LoginGraphicalInterface(SWAClient c)
    {
        setResizable(false);
        setTitle("Login - Share With All");
        client = c;
        initialize();
        setVisible(true);
    }
    
    /**
     * Initialize the contents of the frame.
     */
    private void initialize()
    {
        
        setBounds(100, 100, 375, 304);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{30, 0, 354, 30, 0};
        gridBagLayout.rowHeights = new int[]{30, -9, 26, 17, 0, 0, 0, 0, 0, 0};
        gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
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
        CB_Gateway.setSelected(true);
        GridBagConstraints gbc_CB_Gateway = new GridBagConstraints();
        gbc_CB_Gateway.anchor = GridBagConstraints.WEST;
        gbc_CB_Gateway.insets = new Insets(0, 0, 5, 5);
        gbc_CB_Gateway.gridx = 2;
        gbc_CB_Gateway.gridy = 5;
        getContentPane().add(CB_Gateway, gbc_CB_Gateway);
        
        L_Receiving = new JLabel("Allow receiving files");
        GridBagConstraints gbc_L_Receiving = new GridBagConstraints();
        gbc_L_Receiving.insets = new Insets(0, 0, 5, 5);
        gbc_L_Receiving.gridx = 1;
        gbc_L_Receiving.gridy = 6;
        getContentPane().add(L_Receiving, gbc_L_Receiving);
        
        CB_Receiving = new JCheckBox("");
        GridBagConstraints gbc_CB_Receiving = new GridBagConstraints();
        gbc_CB_Receiving.anchor = GridBagConstraints.WEST;
        gbc_CB_Receiving.insets = new Insets(0, 0, 5, 5);
        gbc_CB_Receiving.gridx = 2;
        gbc_CB_Receiving.gridy = 6;
        getContentPane().add(CB_Receiving, gbc_CB_Receiving);
        
        L_Links = new JLabel("Open links automatically");
        GridBagConstraints gbc_L_Links = new GridBagConstraints();
        gbc_L_Links.insets = new Insets(0, 0, 5, 5);
        gbc_L_Links.gridx = 1;
        gbc_L_Links.gridy = 7;
        getContentPane().add(L_Links, gbc_L_Links);
        
        CB_Links = new JCheckBox("");
        GridBagConstraints gbc_CB_Links = new GridBagConstraints();
        gbc_CB_Links.anchor = GridBagConstraints.WEST;
        gbc_CB_Links.insets = new Insets(0, 0, 5, 5);
        gbc_CB_Links.gridx = 2;
        gbc_CB_Links.gridy = 7;
        getContentPane().add(CB_Links, gbc_CB_Links);
        
        panel = new JPanel();
        GridBagConstraints gbc_panel = new GridBagConstraints();
        gbc_panel.insets = new Insets(0, 0, 0, 5);
        gbc_panel.anchor = GridBagConstraints.WEST;
        gbc_panel.gridx = 2;
        gbc_panel.gridy = 8;
        getContentPane().add(panel, gbc_panel);
        GridBagLayout gbl_panel = new GridBagLayout();
        gbl_panel.columnWidths = new int[]{0, 75, 0};
        gbl_panel.rowHeights = new int[]{28, 0};
        gbl_panel.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
        gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
        panel.setLayout(gbl_panel);
        
        B_Register = new JButton("Register");
        B_Register.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                setVisible(false);
                new RegisterGraphicalInterface(client, LoginGraphicalInterface.this);
            }
        });
        
        B_Login = new JButton("Login");
        B_Login.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                client.gateway = CB_Gateway.isSelected();
                client.open_links = CB_Links.isSelected();
                client.receive_files = CB_Receiving.isSelected();
                
                try {
                    client.loginCommand(getUsername(), getPassword(), getClient(), getPublic());
                }
                catch (Exception e) {
                    JOptionPane.showMessageDialog(LoginGraphicalInterface.this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    clearFields();
                    return;
                }
                String username = getUsername();
                clearFields();
                dispose();
                
                new MainGraphicalInterface(client, username, LoginGraphicalInterface.this);
            }
        });
        GridBagConstraints gbc_B_Login = new GridBagConstraints();
        gbc_B_Login.insets = new Insets(0, 0, 0, 5);
        gbc_B_Login.fill = GridBagConstraints.BOTH;
        gbc_B_Login.gridx = 0;
        gbc_B_Login.gridy = 0;
        panel.add(B_Login, gbc_B_Login);
        GridBagConstraints gbc_B_Register = new GridBagConstraints();
        gbc_B_Register.fill = GridBagConstraints.BOTH;
        gbc_B_Register.gridx = 1;
        gbc_B_Register.gridy = 0;
        panel.add(B_Register, gbc_B_Register);
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
        return String.valueOf(TF_Password.getPassword());
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
