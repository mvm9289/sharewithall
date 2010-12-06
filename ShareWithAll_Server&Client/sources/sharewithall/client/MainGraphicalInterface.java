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
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.BoxLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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
    private JPanel panel;
    private JPanel panel_1;

    public void start()
    {
        try
        {
            setVisible(true);
            
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
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent arg0) {
                RefreshListOfFriends();
                RefreshListOfOnlineClients();
            }
            @Override
            public void windowClosing(WindowEvent arg0) {
                client.logoutCommand();
            }
        });
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

        setBounds(100, 100, 760, 445);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{610, 0};
        gridBagLayout.rowHeights = new int[]{353, 24, 0};
        gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
        getContentPane().setLayout(gridBagLayout);
        
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
        gbc_tabbedPane.fill = GridBagConstraints.BOTH;
        gbc_tabbedPane.insets = new Insets(0, 0, 5, 0);
        gbc_tabbedPane.gridx = 0;
        gbc_tabbedPane.gridy = 0;
        getContentPane().add(tabbedPane, gbc_tabbedPane);
        
        JPanel P_mainTab = new JPanel();
        tabbedPane.addTab("Send", null, P_mainTab, null);
        GridBagLayout gbl_P_mainTab = new GridBagLayout();
        gbl_P_mainTab.columnWidths = new int[]{363, 98, 30, 0};
        gbl_P_mainTab.rowHeights = new int[]{24, 0};
        gbl_P_mainTab.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_P_mainTab.rowWeights = new double[]{1.0, Double.MIN_VALUE};
        P_mainTab.setLayout(gbl_P_mainTab);
        
        LS_Connected = new JList();
        GridBagConstraints gbc_LS_Connected = new GridBagConstraints();
        gbc_LS_Connected.fill = GridBagConstraints.BOTH;
        gbc_LS_Connected.insets = new Insets(0, 0, 0, 5);
        gbc_LS_Connected.gridx = 0;
        gbc_LS_Connected.gridy = 0;
        P_mainTab.add(LS_Connected, gbc_LS_Connected);
        
        panel = new JPanel();
        GridBagConstraints gbc_panel = new GridBagConstraints();
        gbc_panel.fill = GridBagConstraints.HORIZONTAL;
        gbc_panel.insets = new Insets(0, 0, 0, 5);
        gbc_panel.anchor = GridBagConstraints.NORTH;
        gbc_panel.gridx = 1;
        gbc_panel.gridy = 0;
        P_mainTab.add(panel, gbc_panel);
        GridBagLayout gbl_panel = new GridBagLayout();
        gbl_panel.columnWidths = new int[]{98, 0};
        gbl_panel.rowHeights = new int[]{0, 0, 0, 0};
        gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
        panel.setLayout(gbl_panel);
        
        B_SendText = new JButton("Text");
        GridBagConstraints gbc_B_SendText = new GridBagConstraints();
        gbc_B_SendText.fill = GridBagConstraints.HORIZONTAL;
        gbc_B_SendText.insets = new Insets(0, 0, 5, 0);
        gbc_B_SendText.gridx = 0;
        gbc_B_SendText.gridy = 0;
        panel.add(B_SendText, gbc_B_SendText);
        
        B_SendFile = new JButton("File");
        GridBagConstraints gbc_B_SendFile = new GridBagConstraints();
        gbc_B_SendFile.fill = GridBagConstraints.HORIZONTAL;
        gbc_B_SendFile.insets = new Insets(0, 0, 5, 0);
        gbc_B_SendFile.gridx = 0;
        gbc_B_SendFile.gridy = 1;
        panel.add(B_SendFile, gbc_B_SendFile);
        
        B_SendURL = new JButton("URL");
        GridBagConstraints gbc_B_SendURL = new GridBagConstraints();
        gbc_B_SendURL.fill = GridBagConstraints.HORIZONTAL;
        gbc_B_SendURL.gridx = 0;
        gbc_B_SendURL.gridy = 2;
        panel.add(B_SendURL, gbc_B_SendURL);
        
        JPanel P_contactsTab = new JPanel();
        tabbedPane.addTab("Contacts", null, P_contactsTab, null);
        GridBagLayout gbl_P_contactsTab = new GridBagLayout();
        gbl_P_contactsTab.columnWidths = new int[]{379, 117, 30, 0};
        gbl_P_contactsTab.rowHeights = new int[]{24, 0};
        gbl_P_contactsTab.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_P_contactsTab.rowWeights = new double[]{1.0, Double.MIN_VALUE};
        P_contactsTab.setLayout(gbl_P_contactsTab);
        
        list = new JList();
        GridBagConstraints gbc_list = new GridBagConstraints();
        gbc_list.fill = GridBagConstraints.BOTH;
        gbc_list.insets = new Insets(0, 0, 0, 5);
        gbc_list.gridx = 0;
        gbc_list.gridy = 0;
        P_contactsTab.add(list, gbc_list);
        
        panel_1 = new JPanel();
        GridBagConstraints gbc_panel_1 = new GridBagConstraints();
        gbc_panel_1.anchor = GridBagConstraints.NORTH;
        gbc_panel_1.insets = new Insets(0, 0, 0, 5);
        gbc_panel_1.fill = GridBagConstraints.HORIZONTAL;
        gbc_panel_1.gridx = 1;
        gbc_panel_1.gridy = 0;
        P_contactsTab.add(panel_1, gbc_panel_1);
        GridBagLayout gbl_panel_1 = new GridBagLayout();
        gbl_panel_1.columnWidths = new int[]{75, 0};
        gbl_panel_1.rowHeights = new int[]{28, 28, 0, 0, 0};
        gbl_panel_1.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gbl_panel_1.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        panel_1.setLayout(gbl_panel_1);
        
        B_AddNew = new JButton("Add new");
        GridBagConstraints gbc_B_AddNew = new GridBagConstraints();
        gbc_B_AddNew.fill = GridBagConstraints.HORIZONTAL;
        gbc_B_AddNew.anchor = GridBagConstraints.NORTH;
        gbc_B_AddNew.insets = new Insets(0, 0, 5, 0);
        gbc_B_AddNew.gridx = 0;
        gbc_B_AddNew.gridy = 0;
        panel_1.add(B_AddNew, gbc_B_AddNew);
        
        B_Delete = new JButton("Delete");
        GridBagConstraints gbc_B_Delete = new GridBagConstraints();
        gbc_B_Delete.fill = GridBagConstraints.HORIZONTAL;
        gbc_B_Delete.anchor = GridBagConstraints.NORTH;
        gbc_B_Delete.insets = new Insets(0, 0, 5, 0);
        gbc_B_Delete.gridx = 0;
        gbc_B_Delete.gridy = 1;
        panel_1.add(B_Delete, gbc_B_Delete);
        
        B_DeclareFriend = new JButton("Accept");
        GridBagConstraints gbc_B_DeclareFriend = new GridBagConstraints();
        gbc_B_DeclareFriend.fill = GridBagConstraints.HORIZONTAL;
        gbc_B_DeclareFriend.anchor = GridBagConstraints.NORTH;
        gbc_B_DeclareFriend.insets = new Insets(0, 0, 5, 0);
        gbc_B_DeclareFriend.gridx = 0;
        gbc_B_DeclareFriend.gridy = 2;
        panel_1.add(B_DeclareFriend, gbc_B_DeclareFriend);
        
        B_Ignore = new JButton("Ignore");
        GridBagConstraints gbc_B_Ignore = new GridBagConstraints();
        gbc_B_Ignore.fill = GridBagConstraints.HORIZONTAL;
        gbc_B_Ignore.anchor = GridBagConstraints.NORTH;
        gbc_B_Ignore.gridx = 0;
        gbc_B_Ignore.gridy = 3;
        panel_1.add(B_Ignore, gbc_B_Ignore);
        
        B_Logout = new JButton("Logout");
        GridBagConstraints gbc_B_Logout = new GridBagConstraints();
        gbc_B_Logout.anchor = GridBagConstraints.EAST;
        gbc_B_Logout.fill = GridBagConstraints.VERTICAL;
        gbc_B_Logout.gridx = 0;
        gbc_B_Logout.gridy = 1;
        getContentPane().add(B_Logout, gbc_B_Logout);
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
