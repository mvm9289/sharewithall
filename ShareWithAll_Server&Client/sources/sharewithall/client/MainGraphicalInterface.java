package sharewithall.client;

import java.awt.Container;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class MainGraphicalInterface extends javax.swing.JFrame
{

    private SWAClient client;
    public JButton B_SendText;
    public JButton B_SendURL;
    public JButton B_File;
    private JList list;
    public JButton B_AddNew;
    public JButton B_Delete;
    public JButton B_DeclareFriend;
    public JButton B_Ignore;
    public JButton B_Logout;

    public void start()
    {
        try
        {
            setVisible(true);
            RefreshListOfFriends();
            
            B_AddNew.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    String friendName = JOptionPane.showInputDialog(null, "Friend's name?");
                    client.declareFriendCommand(friendName);
                    RefreshListOfFriends();
                }
            });
            
            B_Ignore.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    Object[] contactsName = list.getSelectedValues();
                    for(int i=0; i<contactsName.length; ++i)
                    {
                        String aux = (String) contactsName[i];
                        aux = aux.substring(0, aux.indexOf(" - "));
                        client.ignoreUserCommand(aux);
                    }
                    RefreshListOfFriends();
                }
            });
            
            B_Delete.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    JOptionPane.showMessageDialog(null, "Not implemented yet.", "Error", 0); //TODO: Implement
                }
            });
            
            B_DeclareFriend.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    Object[] contactsName = list.getSelectedValues();
                    for(int i=0; i<contactsName.length; ++i)
                    {
                        String aux = (String) contactsName[i]; 
                        aux = aux.substring(0, aux.indexOf(" - "));
                        client.declareFriendCommand(aux);
                    }
                    RefreshListOfFriends();
                }
            });
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void RefreshListOfFriends()
    {
        String[] friends = client.showListOfFriendsCommand(client.PROPERTY_FRIENDS);
        for(int i=0; i<friends.length; ++i)
            friends[i] += " - FRIEND";
        String[] declared = client.showListOfFriendsCommand(client.PROPERTY_DECLARED_FRIEND);
        for(int i=0; i<declared.length; ++i)
            declared[i] += " - DECLARED";
        String[] expecting = client.showListOfFriendsCommand(client.PROPERTY_EXPECTING);
        for(int i=0; i<expecting.length; ++i)
            expecting[i] += " - EXPECTING";
        String[] ignored = client.showListOfFriendsCommand(client.PROPERTY_IGNORED);
        for(int i=0; i<ignored.length; ++i)
            ignored[i] += " - IGNORED";
        
        String[] listContent = new String[friends.length + declared.length + expecting.length + ignored.length];
        int cont = 0;
        for(int i=0; i<friends.length; ++i,++cont)
            listContent[cont] = friends[i];
        for(int i=0; i<declared.length; ++i,++cont)
            listContent[cont] = declared[i];
        for(int i=0; i<expecting.length; ++i,++cont)
            listContent[cont] = expecting[i];
        for(int i=0; i<ignored.length; ++i,++cont)
            listContent[cont] = ignored[i];
        list.setListData(listContent);
    }

    public MainGraphicalInterface(SWAClient c)
    {
        client = c;
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize()
    {

        setBounds(100, 100, 636, 445);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(null);
        
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setBounds(12, 12, 610, 353);
        getContentPane().add(tabbedPane);
        
        JPanel P_mainTab = new JPanel();
        tabbedPane.addTab("Main tab", null, P_mainTab, null);
        P_mainTab.setLayout(null);
        
        JList LS_Conected = new JList();
        LS_Conected.setBounds(12, 12, 363, 303);
        P_mainTab.add(LS_Conected);
        
        B_SendText = new JButton("Text");
        B_SendText.setBounds(477, 12, 117, 24);
        P_mainTab.add(B_SendText);
        
        B_SendURL = new JButton("URL");
        B_SendURL.setBounds(477, 47, 117, 24);
        P_mainTab.add(B_SendURL);
        
        B_File = new JButton("File");
        B_File.setBounds(477, 82, 117, 24);
        P_mainTab.add(B_File);
        
        JPanel P_contactsTab = new JPanel();
        tabbedPane.addTab("Contacts", null, P_contactsTab, null);
        P_contactsTab.setLayout(null);
        
        list = new JList();
        list.setBounds(12, 12, 379, 303);
        P_contactsTab.add(list);
        
        B_AddNew = new JButton("Add new");
        B_AddNew.setBounds(477, 12, 117, 24);
        P_contactsTab.add(B_AddNew);
        
        B_Delete = new JButton("Delete");
        B_Delete.setBounds(477, 47, 117, 24);
        P_contactsTab.add(B_Delete);
        
        B_DeclareFriend = new JButton("Accept");
        B_DeclareFriend.setBounds(477, 82, 117, 24);
        P_contactsTab.add(B_DeclareFriend);
        
        B_Ignore = new JButton("Ignore");
        B_Ignore.setBounds(477, 117, 117, 24);
        P_contactsTab.add(B_Ignore);
        
        B_Logout = new JButton("Logout");
        B_Logout.setBounds(505, 378, 117, 24);
        getContentPane().add(B_Logout);
    }
}
