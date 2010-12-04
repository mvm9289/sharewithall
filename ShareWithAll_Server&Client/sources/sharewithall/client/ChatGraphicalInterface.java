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
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JScrollPane;

public class ChatGraphicalInterface extends JFrame
{

    private JPanel contentPane;

    private String contactUsername;
    private String contactClient;
    private SWAClient client;
    private String username;
    private MainGraphicalInterface father;
    JTextArea TA_Read;
    private JTextArea TA_Write;
    private JButton B_Send;
    private JScrollPane scrollPane;
    
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
        setBounds(100, 100, 615, 762);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        GridBagLayout gbl_contentPane = new GridBagLayout();
        gbl_contentPane.columnWidths = new int[]{375, 0, 0};
        gbl_contentPane.rowHeights = new int[]{28, 77, 0};
        gbl_contentPane.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
        gbl_contentPane.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
        contentPane.setLayout(gbl_contentPane);
        
        TA_Read = new JTextArea();
        TA_Read.setLineWrap(true);
        TA_Read.setEditable(false);
        GridBagConstraints gbc_TA_Read = new GridBagConstraints();
        gbc_TA_Read.gridwidth = 2;
        gbc_TA_Read.fill = GridBagConstraints.BOTH;
        gbc_TA_Read.insets = new Insets(0, 0, 5, 5);
        gbc_TA_Read.gridx = 0;
        gbc_TA_Read.gridy = 0;
        contentPane.add(TA_Read, gbc_TA_Read);
        
        scrollPane = new JScrollPane();
        GridBagConstraints gbc_scrollPane = new GridBagConstraints();
        gbc_scrollPane.insets = new Insets(0, 0, 0, 5);
        gbc_scrollPane.fill = GridBagConstraints.BOTH;
        gbc_scrollPane.gridx = 0;
        gbc_scrollPane.gridy = 1;
        contentPane.add(scrollPane, gbc_scrollPane);
        
        TA_Write = new JTextArea();
        scrollPane.setViewportView(TA_Write);
        TA_Write.setRows(4);
        TA_Write.setWrapStyleWord(true);
        
        B_Send = new JButton("OK");
        GridBagConstraints gbc_B_Send = new GridBagConstraints();
        gbc_B_Send.fill = GridBagConstraints.BOTH;
        gbc_B_Send.gridx = 1;
        gbc_B_Send.gridy = 1;
        contentPane.add(B_Send, gbc_B_Send);
    }
}
