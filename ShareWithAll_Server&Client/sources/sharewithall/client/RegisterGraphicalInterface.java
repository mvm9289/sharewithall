package sharewithall.client;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class RegisterGraphicalInterface extends javax.swing.JFrame
{
    private SWAClient client;
    private JTextField TF_Username;
    private JTextField TF_NewPassword;
    private JTextField TF_RepeatPassword;
    
    public JButton B_Register;
    public JButton B_Cancel;

    public void start()
    {
        try
        {
            setVisible(true);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public RegisterGraphicalInterface(SWAClient c)
    {
        client = c;
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize()
    {
        setBounds(100, 100, 450, 166);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JPanel panel = new JPanel();
        getContentPane().add(panel, BorderLayout.CENTER);
        panel.setLayout(null);
        
        JLabel L_Username = new JLabel("New Username:");
        L_Username.setBounds(12, 12, 136, 14);
        panel.add(L_Username);
        
        JLabel L_NewPassword = new JLabel("New Password:");
        L_NewPassword.setBounds(12, 38, 136, 14);
        panel.add(L_NewPassword);
        
        JLabel L_RepeatPassword = new JLabel("Repeat Password:");
        L_RepeatPassword.setBounds(12, 64, 136, 14);
        panel.add(L_RepeatPassword);
        
        TF_Username = new JTextField();
        TF_Username.setBounds(166, 10, 270, 18);
        panel.add(TF_Username);
        TF_Username.setColumns(10);
        
        TF_NewPassword = new JTextField();
        TF_NewPassword.setColumns(10);
        TF_NewPassword.setBounds(166, 36, 270, 18);
        panel.add(TF_NewPassword);
        
        TF_RepeatPassword = new JTextField();
        TF_RepeatPassword.setColumns(10);
        TF_RepeatPassword.setBounds(166, 62, 270, 18);
        panel.add(TF_RepeatPassword);
        
        B_Register = new JButton("Register");
        B_Register.setBounds(319, 92, 117, 24);
        panel.add(B_Register);
        
        B_Cancel = new JButton("Cancel");
        B_Cancel.setBounds(190, 92, 117, 24);
        panel.add(B_Cancel);
    }

    public String getUsername()
    {
        return TF_Username.getText(); 
    }
    public String getPassword()
    {
        if(!TF_NewPassword.getText().equals(TF_RepeatPassword.getText()))
        {
            JOptionPane.showMessageDialog(null, "The password don't match.", "Error", 0);
            return null;
        }

        return TF_NewPassword.getText(); 
    }
}
