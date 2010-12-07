package sharewithall.client;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class RegisterGraphicalInterface extends javax.swing.JFrame
{
    private SWAClient client;
    private LoginGraphicalInterface loginI;
    private JLabel L_NewPassword;
    private JTextField TF_Username;
    private JLabel L_RepeatPassword;
    private JPasswordField TF_NewPassword;
    private JPasswordField TF_RepeatPassword;
    JButton B_Register;
    JButton B_Cancel;
    private JLabel L_Username;
    private JPanel panel;

    public RegisterGraphicalInterface(SWAClient c, LoginGraphicalInterface loginI)
    {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent arg0) {
                RegisterGraphicalInterface.this.loginI.setVisible(true);
            }
        });
        setResizable(false);
        setTitle("Register - Share With All");
        client = c;
        this.loginI = loginI;
        initialize();
        setVisible(true);
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize()
    {
        setBounds(100, 100, 369, 211);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{30, 0, 434, 30, 0};
        gridBagLayout.rowHeights = new int[]{30, 0, 30, 0, 30, 0};
        gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, 1.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
        getContentPane().setLayout(gridBagLayout);
        
        L_Username = new JLabel("Username:");
        GridBagConstraints gbc_L_Username = new GridBagConstraints();
        gbc_L_Username.anchor = GridBagConstraints.WEST;
        gbc_L_Username.insets = new Insets(0, 0, 5, 5);
        gbc_L_Username.gridx = 1;
        gbc_L_Username.gridy = 1;
        getContentPane().add(L_Username, gbc_L_Username);
        
        TF_Username = new JTextField();
        TF_Username.setColumns(10);
        GridBagConstraints gbc_TF_Username = new GridBagConstraints();
        gbc_TF_Username.insets = new Insets(0, 0, 5, 5);
        gbc_TF_Username.fill = GridBagConstraints.HORIZONTAL;
        gbc_TF_Username.gridx = 2;
        gbc_TF_Username.gridy = 1;
        getContentPane().add(TF_Username, gbc_TF_Username);
        
        L_NewPassword = new JLabel("Password:");
        GridBagConstraints gbc_L_NewPassword = new GridBagConstraints();
        gbc_L_NewPassword.anchor = GridBagConstraints.WEST;
        gbc_L_NewPassword.insets = new Insets(0, 0, 5, 5);
        gbc_L_NewPassword.gridx = 1;
        gbc_L_NewPassword.gridy = 2;
        getContentPane().add(L_NewPassword, gbc_L_NewPassword);
        
        TF_NewPassword = new JPasswordField();
        TF_NewPassword.setColumns(10);
        GridBagConstraints gbc_TF_NewPassword = new GridBagConstraints();
        gbc_TF_NewPassword.insets = new Insets(0, 0, 5, 5);
        gbc_TF_NewPassword.fill = GridBagConstraints.HORIZONTAL;
        gbc_TF_NewPassword.gridx = 2;
        gbc_TF_NewPassword.gridy = 2;
        getContentPane().add(TF_NewPassword, gbc_TF_NewPassword);
        
        L_RepeatPassword = new JLabel("Repeat Password:");
        GridBagConstraints gbc_L_RepeatPassword = new GridBagConstraints();
        gbc_L_RepeatPassword.anchor = GridBagConstraints.WEST;
        gbc_L_RepeatPassword.insets = new Insets(0, 0, 5, 5);
        gbc_L_RepeatPassword.gridx = 1;
        gbc_L_RepeatPassword.gridy = 3;
        getContentPane().add(L_RepeatPassword, gbc_L_RepeatPassword);
        
        TF_RepeatPassword = new JPasswordField();
        TF_RepeatPassword.setColumns(10);
        GridBagConstraints gbc_TF_RepeatPassword = new GridBagConstraints();
        gbc_TF_RepeatPassword.insets = new Insets(0, 0, 5, 5);
        gbc_TF_RepeatPassword.fill = GridBagConstraints.HORIZONTAL;
        gbc_TF_RepeatPassword.gridx = 2;
        gbc_TF_RepeatPassword.gridy = 3;
        getContentPane().add(TF_RepeatPassword, gbc_TF_RepeatPassword);
        
        panel = new JPanel();
        GridBagConstraints gbc_panel = new GridBagConstraints();
        gbc_panel.anchor = GridBagConstraints.WEST;
        gbc_panel.insets = new Insets(0, 0, 0, 5);
        gbc_panel.gridx = 2;
        gbc_panel.gridy = 4;
        getContentPane().add(panel, gbc_panel);
        
        B_Register = new JButton("Register");
        B_Register.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if(getPassword() != null){
                    try {
                        client.newUserCommand(getUsername(), getPassword());
                        dispose();
                        loginI.setVisible(true);
                    }
                    catch (Exception e) {
                        JOptionPane.showMessageDialog(RegisterGraphicalInterface.this, e.getMessage(), "Register error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        panel.add(B_Register);
        
        B_Cancel = new JButton("Cancel");
        B_Cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                dispose();
                loginI.setVisible(true);
            }
        });
        panel.add(B_Cancel);
    }

    public String getUsername()
    {
        return TF_Username.getText(); 
    }
    
    public String getPassword()
    {
        String p1 = String.valueOf(TF_NewPassword.getPassword());
        String p2 = String.valueOf(TF_RepeatPassword.getPassword());
        
        if(!p1.equals(p2))
        {
            JOptionPane.showMessageDialog(this, "The password don't match.", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        return p1; 
    }
}
