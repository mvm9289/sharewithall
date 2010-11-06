/**
 * 
 */
package sharewithall.server;

import java.security.MessageDigest;
import java.util.ArrayList;

import sharewithall.server.jdbc.SWAServerJDBCClient;
import sharewithall.server.jdbc.SWAServerJDBCDBClients;
import sharewithall.server.jdbc.SWAServerJDBCDBFriends;
import sharewithall.server.jdbc.SWAServerJDBCDBUsers;
import sharewithall.server.jdbc.SWAServerJDBCFriends;
import sharewithall.server.jdbc.SWAServerJDBCPredicate;
import sharewithall.server.jdbc.SWAServerJDBCUser;
import sharewithall.server.sockets.SWAServerSockets;

/**
 * Authors:
 *    Alex Catarineu
 *    Ferran Rigual
 *    Miguel Angel Vico
 *
 * Creation date: Nov 6, 2010
 */
public class SWAServer
{
    
    private static final int DEFAULT_SERVER_PORT = 4040;
    
    @SuppressWarnings("unused")
    private SWAServerSockets socketsModule;
    
    public SWAServer(int port)
    {
        super();
        socketsModule = new SWAServerSockets(port, this);
    }
    
    public void newUser(String username, String password) throws Exception
    {
        SWAServerJDBCDBUsers DBUsers = new SWAServerJDBCDBUsers();
        boolean exists;
        
        try
        {
            exists = DBUsers.exists_gen(new SWAServerJDBCPredicate("username", username));
        }
        catch (Exception e)
        {
            System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
            throw new Exception("Server error");
        }
        
        if (exists) throw new Exception("This username already exists");

        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            byte[] bytes = digest.digest(password.getBytes("UTF-8"));
            
            DBUsers.insert_obj(new SWAServerJDBCUser(username, new String(bytes)));
            DBUsers.commit();
        }
        catch (Exception e)
        {
            System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
            throw new Exception("Server error");
        }
    }
    
    public String login(String username, String password, String name, boolean isPublic)
    {
        return "login( [" + username + "], [" + password + "], [" + name + "], [" + isPublic + "] ) called!";
    }
    
    public void logout(String sessionID) throws Exception
    {
        SWAServerJDBCDBClients DBClients = new SWAServerJDBCDBClients();
        boolean exists;
        
        try
        {
            exists = DBClients.exists_gen(new SWAServerJDBCPredicate("sessionID", sessionID));
        }
        catch (Exception e)
        {
            System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
            throw new Exception("Server error");
        }
        
        if (!exists) throw new Exception("Invalid session");
        
        try
        {
            ArrayList<Object> clients = DBClients.select_gen(new SWAServerJDBCPredicate("sessionID", sessionID));
            SWAServerJDBCClient client = (SWAServerJDBCClient)clients.get(0);
            DBClients.delete_key(client.ip, client.port);
            DBClients.commit();
            
            clients = DBClients.select_gen(new SWAServerJDBCPredicate("username", client.username));
            for (int i = 0; i < clients.size(); i++)
            {
                //notifyListChanged(((SWAServerJDBCClient)clients.get(i)).ip, ((SWAServerJDBCClient)clients.get(i)).port);
            }
            
            if (client.is_public)
            {
                SWAServerJDBCDBFriends DBFriends = new SWAServerJDBCDBFriends();
                ArrayList<Object> friends = DBFriends.select_gen(new SWAServerJDBCPredicate("user1", client.username), new SWAServerJDBCPredicate("status", "accepted"));
                for (int i = 0; i < friends.size(); i++)
                {
                    clients = DBClients.select_gen(new SWAServerJDBCPredicate("username", ((SWAServerJDBCFriends)friends.get(i)).user2));
                    for (int j = 0; j < clients.size(); j++)
                    {
                        //notifyListChanged(((SWAServerJDBCClient)clients.get(j)).ip, ((SWAServerJDBCClient)clients.get(j)).port);
                    }
                }
                
                friends = DBFriends.select_gen(new SWAServerJDBCPredicate("user2", client.username), new SWAServerJDBCPredicate("status", "accepted"));
                for (int i = 0; i < friends.size(); i++)
                {
                    clients = DBClients.select_gen(new SWAServerJDBCPredicate("username", ((SWAServerJDBCFriends)friends.get(i)).user1));
                    for (int j = 0; j < clients.size(); j++)
                    {
                        //notifyListChanged(((SWAServerJDBCClient)clients.get(j)).ip, ((SWAServerJDBCClient)clients.get(j)).port);
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
            throw new Exception("Server error");
        }
    }
    
    public String getOnlineClients(String sessionID) throws Exception
    {
        SWAServerJDBCDBClients DBClients = new SWAServerJDBCDBClients();
        boolean exists;
        
        try
        {
            exists = DBClients.exists_gen(new SWAServerJDBCPredicate("sessionID", sessionID));
        }
        catch (Exception e)
        {
            System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
            throw new Exception("Server error");
        }
        
        if (!exists) throw new Exception("Invalid session");
        
        try
        {
            ArrayList<Object> clients = DBClients.select_gen(new SWAServerJDBCPredicate("sessionID", sessionID));
            SWAServerJDBCClient client = (SWAServerJDBCClient)clients.get(0);
            
            clients = DBClients.select_gen(new SWAServerJDBCPredicate("username", client.username));
            String list = ((SWAServerJDBCClient)clients.get(0)).name;
            for (int i = 1; i < clients.size(); i++)
            {
                list += ":";
                list += ((SWAServerJDBCClient)clients.get(i)).name;
            }
            
            SWAServerJDBCDBFriends DBFriends = new SWAServerJDBCDBFriends();
            ArrayList<Object> friends = DBFriends.select_gen(new SWAServerJDBCPredicate("user1", client.username), new SWAServerJDBCPredicate("status", "accepted"));
            for (int i = 0; i < friends.size(); i++)
            {
                clients = DBClients.select_gen(new SWAServerJDBCPredicate("username", ((SWAServerJDBCFriends)friends.get(i)).user2), new SWAServerJDBCPredicate("is_public", true));
                for (int j = 0; j < clients.size(); j++)
                {
                    list += ":";
                    list += (((SWAServerJDBCClient)clients.get(i)).name + ((SWAServerJDBCFriends)friends.get(i)).user2);
                }
            }
            
            friends = DBFriends.select_gen(new SWAServerJDBCPredicate("user2", client.username), new SWAServerJDBCPredicate("status", "accepted"));
            for (int i = 0; i < friends.size(); i++)
            {
                clients = DBClients.select_gen(new SWAServerJDBCPredicate("username", ((SWAServerJDBCFriends)friends.get(i)).user1), new SWAServerJDBCPredicate("is_public", true));
                for (int j = 0; j < clients.size(); j++)
                {
                    list += ":";
                    list += (((SWAServerJDBCClient)clients.get(i)).name + ((SWAServerJDBCFriends)friends.get(i)).user1);
                }
            }
            
            return list;
        }
        catch (Exception e)
        {
            System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
            throw new Exception("Server error");
        }
    }
    
    public String ipAndPortRequest(String sessionID, String client) throws Exception
    {
        SWAServerJDBCDBClients DBClients = new SWAServerJDBCDBClients();
        boolean exists;
        
        try
        {
            exists = DBClients.exists_gen(new SWAServerJDBCPredicate("sessionID", sessionID));
        }
        catch (Exception e)
        {
            System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
            throw new Exception("Server error");
        }
        
        if (!exists) throw new Exception("Invalid session");
        
        try
        {
            ArrayList<Object> clients = DBClients.select_gen(new SWAServerJDBCPredicate("name", client));
            SWAServerJDBCClient client_ = (SWAServerJDBCClient)clients.get(0);
            
            return (client_.ip + ":" + String.valueOf(client_.port));
        }
        catch (Exception e)
        {
            System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
            throw new Exception("Server error");
        }
    }
    
    public void sendInvitation(String sessionID, String friend) throws Exception
    {
        
    }
    
    public void updateTimestamp(String sessionID) throws Exception
    {
        
    }

    public void acceptInvitation(String sessionID, String friend, boolean accept) throws Exception
    {
        
    }
    
     public String pendingInvitationsRequest(String sessionID) throws Exception
     {
         return "pendingInvitationsRequest( [" + sessionID + "] ) called!";
     }
    
    public static void main(String[] args)
    {
        if (args.length == 1) new SWAServer(Integer.valueOf(args[0]).intValue());
        else if (args.length == 0) new SWAServer(DEFAULT_SERVER_PORT);
        else
        {
            System.out.println("\n\tUSAGE:\n\t\tjava sharewithall.server.SWAServer [port]" +
            		"\n\n\t*Arguments between [] are optional. Default port is 4040.\n");
        }
    }

}
