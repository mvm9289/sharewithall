package sharewithall.client;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTextArea;
import javax.swing.JButton;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JScrollPane;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class ChatGraphicalInterface extends JFrame
{
    private JPanel contentPane;
    private String contactUsername;
    private String contactClient;
    private SWAClient client;
    private String username;
    JTextArea TA_Read;
    private JTextArea TA_Write;
    private JButton B_Send;
    private JScrollPane SP_Write;
    private JScrollPane SP_Read;
    
    private void sendText() {
        String text = TA_Write.getText();
        
        //Write in the text area.
        writeText(text, username);
        TA_Write.setText("");
        
        //Send to the receiver
        try {
            client.sendTextCommand(text, contactUsername, contactClient);
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(ChatGraphicalInterface.this, e.getMessage(), "Chat error", JOptionPane.ERROR_MESSAGE);
        }        
    }
    
    public void writeText(String text, String username) {
        String prevText = TA_Read.getText();
        if (!prevText.equals("")) prevText += "\n";
        TA_Read.setText(prevText + username + ": " + text);
    }
    
    public ChatGraphicalInterface(String t, SWAClient c, String u, String cu, String cc, MainGraphicalInterface f)
    {
        username = u;
        client = c;
        contactUsername = cu;
        contactClient = cc;

        initialize();
        if(!t.equals(""))
        {
            writeText(t, contactUsername + "@" + contactClient);
        }
        setVisible(true);
    }

    /**
     * Create the frame.
     */
    private void initialize()
    {
        setTitle("Chat - Share With All");
        setBounds(100, 100, 483, 520);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        GridBagLayout gbl_contentPane = new GridBagLayout();
        gbl_contentPane.columnWidths = new int[]{375, 0, 0};
        gbl_contentPane.rowHeights = new int[]{28, 100, 0};
        gbl_contentPane.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
        gbl_contentPane.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
        contentPane.setLayout(gbl_contentPane);
        
        SP_Read = new JScrollPane();
        
        GridBagConstraints gbc_SP_Read = new GridBagConstraints();
        gbc_SP_Read.insets = new Insets(0, 0, 5, 5);
        gbc_SP_Read.fill = GridBagConstraints.BOTH;
        gbc_SP_Read.gridx = 0;
        gbc_SP_Read.gridy = 0;
        contentPane.add(SP_Read, gbc_SP_Read);
        
        TA_Read = new JTextArea();
        TA_Read.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent arg0) {
                SP_Read.getVerticalScrollBar().setValue(SP_Read.getVerticalScrollBar().getMaximum());
            }
        });
        SP_Read.setViewportView(TA_Read);
        TA_Read.setLineWrap(true);
        TA_Read.setEditable(false);
        
        SP_Write = new JScrollPane();
        GridBagConstraints gbc_SP_Write = new GridBagConstraints();
        gbc_SP_Write.insets = new Insets(0, 0, 0, 5);
        gbc_SP_Write.fill = GridBagConstraints.BOTH;
        gbc_SP_Write.gridx = 0;
        gbc_SP_Write.gridy = 1;
        contentPane.add(SP_Write, gbc_SP_Write);
        
        TA_Write = new JTextArea();
        TA_Write.setLineWrap(true);
        TA_Write.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent arg0) {
                if (!arg0.isControlDown() && arg0.getKeyCode() == KeyEvent.VK_ENTER)
                    sendText();
            }
        });
        TA_Write.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent arg0) {
                if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (!arg0.isControlDown()) TA_Write.setText("");                    
                    else TA_Write.append("\n");
                }
            }
        });
        SP_Write.setViewportView(TA_Write);
        TA_Write.setWrapStyleWord(true);
        
        B_Send = new JButton("OK");
        B_Send.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                sendText();
            }
        });
        GridBagConstraints gbc_B_Send = new GridBagConstraints();
        gbc_B_Send.fill = GridBagConstraints.BOTH;
        gbc_B_Send.gridx = 1;
        gbc_B_Send.gridy = 1;
        contentPane.add(B_Send, gbc_B_Send);
    }
}
