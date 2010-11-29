package sharewithall.client;

import java.awt.EventQueue;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class MainGraphicalInterface
{

    private JFrame main;

    /**
     * Launch the application.
     */
    public static void main(String[] args)
    {
        EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                try
                {
                    MainGraphicalInterface window = new MainGraphicalInterface();
                    window.main.setVisible(true);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public MainGraphicalInterface()
    {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize()
    {
        main = new JFrame();
        main.setBounds(100, 100, 636, 445);
        main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        main.getContentPane().setLayout(null);
        
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setBounds(12, 12, 610, 353);
        main.getContentPane().add(tabbedPane);
        
        JPanel P_mainTab = new JPanel();
        tabbedPane.addTab("Main tab", null, P_mainTab, null);
        P_mainTab.setLayout(null);
        
        JList LS_Conected = new JList();
        LS_Conected.setBounds(12, 12, 363, 303);
        P_mainTab.add(LS_Conected);
        
        JButton B_SendText = new JButton("Text");
        B_SendText.setBounds(477, 12, 117, 24);
        P_mainTab.add(B_SendText);
        
        JButton B_SendURL = new JButton("URL");
        B_SendURL.setBounds(477, 47, 117, 24);
        P_mainTab.add(B_SendURL);
        
        JButton B_File = new JButton("File");
        B_File.setBounds(477, 82, 117, 24);
        P_mainTab.add(B_File);
        
        JPanel P_contactsTab = new JPanel();
        tabbedPane.addTab("Contacts", null, P_contactsTab, null);
        P_contactsTab.setLayout(null);
        
        JList list = new JList();
        list.setBounds(12, 12, 379, 303);
        P_contactsTab.add(list);
        
        JButton B_AddNew = new JButton("Add new");
        B_AddNew.setBounds(477, 12, 117, 24);
        P_contactsTab.add(B_AddNew);
        
        JButton B_Delete = new JButton("Delete");
        B_Delete.setBounds(477, 47, 117, 24);
        P_contactsTab.add(B_Delete);
        
        JButton B_DeclareFriend = new JButton("Accept");
        B_DeclareFriend.setBounds(477, 82, 117, 24);
        P_contactsTab.add(B_DeclareFriend);
        
        JButton B_Ignore = new JButton("Ignore");
        B_Ignore.setBounds(477, 117, 117, 24);
        P_contactsTab.add(B_Ignore);
        
        JButton B_Logout = new JButton("Logout");
        B_Logout.setBounds(505, 378, 117, 24);
        main.getContentPane().add(B_Logout);
    }
}
