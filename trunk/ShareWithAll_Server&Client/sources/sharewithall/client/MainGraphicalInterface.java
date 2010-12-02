package sharewithall.client;

import java.util.ArrayList;

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
    private JButton B_SendText;
    private JButton B_SendURL;
    private JButton B_SendFile;
    private JList list;
    private JButton B_AddNew;
    private JButton B_Delete;
    private JButton B_DeclareFriend;
    private JButton B_Ignore;
    public JButton B_Logout;
    private JList LS_Connected;
    private String username;
    private ArrayList<chatInfo> openChats;

    public void start()
    {
        try
        {
            setVisible(true);
            RefreshListOfOnlineClients();
            RefreshListOfFriends();
            
            B_SendText.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    String[] receiverInfo = obtainReceiver();
                    if(receiverInfo!=null)
                        openChat(receiverInfo[0], receiverInfo[1]);
                }
            });
            
            B_SendURL.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    String[] receiverInfo = obtainReceiver();
                    if(receiverInfo!=null)
                    {
                        String URL = JOptionPane.showInputDialog(null, "What is the URL you want to send?");
                        if(URL != null)
                                client.sendURLCommand(URL, receiverInfo[0], receiverInfo[1]);
                    }
                }
            });
            
            B_SendFile.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    String[] receiverInfo = obtainReceiver();
                    if(receiverInfo!=null)
                    {
                        String path = JOptionPane.showInputDialog(null, "What is the path of the file you want to send?");
                        if(path != null)
                            client.sendFileCommand(path, receiverInfo[0], receiverInfo[1]);
                    }
                }
            });

            B_AddNew.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    String friendName = JOptionPane.showInputDialog(null, "Friend's name?");
                    if(friendName != null)
                    {
                        client.declareFriendCommand(friendName);
                        RefreshListOfFriends();
                    }
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

    public void openChat(String receiverUsername, String receiverClient)
    {
        if(!isOpenedChat(receiverClient))
        {
            ChatGraphicalInterface chat = new ChatGraphicalInterface("", client, username, receiverUsername, receiverClient, this);
            chat.start();
            openChats.add(new chatInfo(receiverClient, chat));
        }
        else
        {
            getOpenedChat(receiverClient).setVisible(true);
        }
    }
    
    public void openChat(String text, String receiverUsername, String receiverClient)
    {
        ChatGraphicalInterface chat;
        if(!isOpenedChat(receiverClient))
        {
            chat = new ChatGraphicalInterface(text, client, username, receiverUsername, receiverClient, this);
            chat.start();
            openChats.add(new chatInfo(receiverClient, chat));
        }
        else
        {
            chat = getOpenedChat(receiverClient);
            chat.setVisible(true);
            chat.TA_Read.setText(chat.TA_Read.getText() + "\n" + "[" + receiverUsername + "@" + receiverClient + "]: " + text);
        }
    }
    
    private boolean isOpenedChat(String receiverClient)
    {
        for(int i=0; i<openChats.size(); ++i)
        {
            if(openChats.get(i).receiver.equals(receiverClient))
                return true;
        }
        return false;
    }
    
    private ChatGraphicalInterface getOpenedChat(String rc)
    {
        for(int i=0; i<openChats.size(); ++i)
        {
            if(openChats.get(i).receiver.equals(rc))
                return openChats.get(i).chat;
        }
        return null;
    }
    
    private String[] obtainReceiver()
    {
        String receiver = (String) LS_Connected.getSelectedValue();
        String[] receiverInfo = new String[2];
        if(receiver == null)
        {
            JOptionPane.showMessageDialog(null, "You must select a contact.", "Error", 0);
            return null;
        }
        if(receiver.indexOf(":") == -1)
        { //Sending to myself.
            receiverInfo[0] = username;
            receiverInfo[1] = receiver;
        }
        else
        { //Sending to another user.
            receiverInfo[0] = receiver.substring(0, receiver.indexOf(":"));
            receiverInfo[1] = receiver.substring(receiver.indexOf(":")+1, receiver.length());
        }
        return receiverInfo;
    }
    
    public void RefreshListOfOnlineClients()
    {
        String[] onlineClients = client.getOnlineClientsCommand();
        
        LS_Connected.setListData(client.getOnlineClientsCommand());
    }
    
    public void RefreshListOfFriends()
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

    public void receiveText(String username, String client, String text)
    {
        openChat(text, username, client);
    }
    
    public void receiveFile(String username, String client, String path)
    {
        JOptionPane.showMessageDialog(null, username + "@" + client + " has sent you a file: " + path + ".", "File received", 0);
    }
    
    public MainGraphicalInterface(SWAClient c, String u)
    {
        username = u;
        client = c;
        client.program = this;
        openChats = new ArrayList();
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize()
    {
        setTitle("Share With All");

        setBounds(100, 100, 636, 445);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(null);
        
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setBounds(12, 12, 610, 353);
        getContentPane().add(tabbedPane);
        
        JPanel P_mainTab = new JPanel();
        tabbedPane.addTab("Send", null, P_mainTab, null);
        P_mainTab.setLayout(null);
        
        LS_Connected = new JList();
        LS_Connected.setBounds(12, 12, 363, 303);
        P_mainTab.add(LS_Connected);
        
        B_SendText = new JButton("Text");
        B_SendText.setBounds(477, 12, 117, 24);
        P_mainTab.add(B_SendText);
        
        B_SendURL = new JButton("URL");
        B_SendURL.setBounds(477, 47, 117, 24);
        P_mainTab.add(B_SendURL);
        
        B_SendFile = new JButton("File");
        B_SendFile.setBounds(477, 82, 117, 24);
        P_mainTab.add(B_SendFile);
        
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


class chatInfo
{
    public String receiver;
    public ChatGraphicalInterface chat;
    public boolean isShown;
    
    public chatInfo(String r, ChatGraphicalInterface c)
    {
        receiver = r;
        chat = c;
        isShown = true;
    }
    
    public int busca(String rc)
    {
        if(rc == receiver)
            return 1;
        return -1;
    }
}

