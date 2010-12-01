package sharewithall.client;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTextArea;
import javax.swing.JButton;

public class ChatGraphicalInterface extends JFrame
{

    private JPanel contentPane;

    private String contactUsername;
    private String contactClient;
    private SWAClient client;
    private JTextArea TA_Write;
    public JTextArea TA_Read;
    private JButton B_Add;
    private JButton B_File;
    private JButton B_Send;
    private String username;
    private MainGraphicalInterface father;
    
    public void start()
    {
        setVisible(true);
        B_Send.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                String text = TA_Write.getText();
                
                //Write in the text area.
                TA_Read.setText(TA_Read.getText() + "\n" + username + ": " + text);
                TA_Write.setText("");
                
                //Send to the receiver
                client.sendTextCommand(text, contactUsername, contactClient);
            }
        });
        
        B_Add.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                JOptionPane.showMessageDialog(null, "Not implemented yet.", "Error", 0); //TODO: Implement
            }
        });
        
        B_File.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                JOptionPane.showMessageDialog(null, "Not implemented yet.", "Error", 0); //TODO: Implement
            }
        });
    }
    
    public void dispose()
    {
        setVisible(false);
    }
    
    public ChatGraphicalInterface(String t, SWAClient c, String u, String cu, String cc, MainGraphicalInterface f)
    {
        username = u;
        client = c;
        contactUsername = cu;
        contactClient = cc;
        father = f;
        initialize();
        if(!t.equals(""))
        {
            TA_Read.setText(TA_Read.getText() + "\n" + "[" + contactUsername + "@" + contactClient + "]: " + t);
        }
    }

    /**
     * Create the frame.
     */
    private void initialize()
    {
        setTitle("Chat - Share With All");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 378, 488);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        
        JPanel panel = new JPanel();
        contentPane.add(panel, BorderLayout.CENTER);
        panel.setLayout(null);
        
        TA_Write = new JTextArea();
        TA_Write.setBounds(10, 324, 275, 110);
        panel.add(TA_Write);
        
        TA_Read = new JTextArea();
        TA_Read.setEditable(false);
        TA_Read.setBounds(10, 12, 344, 264);
        panel.add(TA_Read);
        
        B_Add = new JButton("Add");
        B_Add.setBounds(284, 288, 70, 24);
        panel.add(B_Add);
        
        B_File = new JButton("File");
        B_File.setBounds(211, 288, 70, 24);
        panel.add(B_File);
        
        B_Send = new JButton("OK");
        B_Send.setBounds(297, 323, 57, 111);
        panel.add(B_Send);
    }
}
