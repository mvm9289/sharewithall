/**
 * 
 */
package sharewithall.server;

import java.security.MessageDigest;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import sharewithall.client.SWAClient;
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
    private static final int FIRST_CLIENT_PORT = 4100;
    private static final int DEFAULT_SERVER_PORT = 4040;
    private static final int STATUS_FRIEND = 1;
    private static final int STATUS_IGNORE_USER = 0;
    
    private SWAServerSockets socketsModule;
    
    public SWAServer(int port)
    {
        super();
        socketsModule = new SWAServerSockets(port, this);
    }
    
    private String bytes_to_hex(byte[] b)
    {
        String ret = "";
        for (int i = 0; i < b.length; ++i)
        {
            for (int j = 1; j >= 0; --j)
            {
                int val = (b[i] >> 4*j)&0xF;
                if (val < 10) ret += (char)((int)'0' + val);
                else ret += (char)((int)'a' + val - 10);
            }
        }
        return ret;
    }
    
    private String sha256(String password) throws Exception {
        MessageDigest d = MessageDigest.getInstance("SHA-256");
        d.reset();
        return bytes_to_hex(d.digest(password.getBytes("UTF-8")));
    }
    
    public void newUser(String username, String password) throws Exception
    {
        SWAServerJDBCDBUsers DBUsers = new SWAServerJDBCDBUsers();
        boolean exists = false;
        
        try
        {
            exists = DBUsers.exists_gen(new SWAServerJDBCPredicate("username", username));
        }
        catch (Exception e)
        {
            DBUsers.close();
            System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
            throw new Exception("Server error");
        }
        
        if (exists) {
            DBUsers.close();
            throw new Exception("This username already exists");
        }

        try
        {
            DBUsers.insert_obj(new SWAServerJDBCUser(username, sha256(password)));
            DBUsers.commit();
        }
        catch (Exception e)
        {
            System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
            throw new Exception("Server error");
        }
        finally {
            DBUsers.close();
        }
    }
    
    public String login(String username, String password, String name, boolean isPublic, String ip) throws Exception
    {
        SWAServerJDBCDBUsers DBUsers = new SWAServerJDBCDBUsers();
        boolean exists = false;
        
        try
        {
            SWAServerJDBCPredicate p1 = new SWAServerJDBCPredicate("username", username);
            SWAServerJDBCPredicate p2 = new SWAServerJDBCPredicate("password", sha256(password));
            exists = DBUsers.exists_gen(p1, p2);
        }
        catch (Exception e)
        {
            System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
            throw new Exception("Server error");
        }
        finally {
            DBUsers.close();
        }
        
        if (!exists) throw new Exception("The username/password combination is not correct");
        
        SWAServerJDBCDBClients DBClients = new SWAServerJDBCDBClients();
        String session_id = sha256(System.currentTimeMillis() + username + (new Random()).nextLong() + password);
        try
        {
            ArrayList<Object> clientes = DBClients.select_gen(new SWAServerJDBCPredicate("ip", ip));
            int port = FIRST_CLIENT_PORT + clientes.size(); 
            Timestamp last_time = new Timestamp((new Date()).getTime());
            
            // TODO: Comprobar unicidad de (username, name)
            
            System.out.println("ip: " + ip + ", port: " + port);
            SWAServerJDBCClient cl = new SWAServerJDBCClient(ip, port, name, isPublic, last_time, username, session_id);
            DBClients.insert_obj(cl);
            DBClients.commit();
        }
        catch (Exception e)
        {
            System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
            throw new Exception("Server error");
        }
        finally {
            DBClients.close();
        }
        
        return session_id;
    }
    
    public void logout(String sessionID) throws Exception
    {
        SWAServerJDBCDBClients DBClients = new SWAServerJDBCDBClients();
        boolean exists;
        
        try
        {
            exists = DBClients.exists_gen(new SWAServerJDBCPredicate("session_id", sessionID));
        }
        catch (Exception e)
        {
            System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
            throw new Exception("Server error");
        }
        
        if (!exists) throw new Exception("Invalid session");
        
        try
        {
            ArrayList<Object> clients = DBClients.select_gen(new SWAServerJDBCPredicate("session_id", sessionID));
            SWAServerJDBCClient client = (SWAServerJDBCClient)clients.get(0);
            DBClients.delete_key(client.name, client.username);
            DBClients.commit();
            
            clients = DBClients.select_gen(new SWAServerJDBCPredicate("username", client.username));
            for (int i = 0; i < clients.size(); i++)
            {
                //notifyListChanged(((SWAServerJDBCClient)clients.get(i)).ip, ((SWAServerJDBCClient)clients.get(i)).port);
            }
            
            if (client.is_public)
            {
                SWAServerJDBCDBFriends DBFriends = new SWAServerJDBCDBFriends();
                ArrayList<Object> friends = DBFriends.select_gen(new SWAServerJDBCPredicate("user1", client.username), new SWAServerJDBCPredicate("status", STATUS_FRIEND));
                for (int i = 0; i < friends.size(); i++)
                {
                    clients = DBClients.select_gen(new SWAServerJDBCPredicate("username", ((SWAServerJDBCFriends)friends.get(i)).user2));
                    for (int j = 0; j < clients.size(); j++)
                    {
                        //notifyListChanged(((SWAServerJDBCClient)clients.get(j)).ip, ((SWAServerJDBCClient)clients.get(j)).port);
                    }
                }
                
                friends = DBFriends.select_gen(new SWAServerJDBCPredicate("user2", client.username), new SWAServerJDBCPredicate("status", STATUS_FRIEND));
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
            exists = DBClients.exists_gen(new SWAServerJDBCPredicate("session_id", sessionID));
        }
        catch (Exception e)
        {
            System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
            throw new Exception("Server error");
        }
        
        if (!exists) throw new Exception("Invalid session");
        
        try
        {
            ArrayList<Object> clients = DBClients.select_gen(new SWAServerJDBCPredicate("session_id", sessionID));
            SWAServerJDBCClient client = (SWAServerJDBCClient)clients.get(0);
            
            clients = DBClients.select_gen(new SWAServerJDBCPredicate("username", client.username));
            String list = ((SWAServerJDBCClient)clients.get(0)).name.trim();
            for (int i = 1; i < clients.size(); i++)
            {
                list += ":";
                list += ((SWAServerJDBCClient)clients.get(i)).name.trim();
            }
            
            SWAServerJDBCDBFriends DBFriends = new SWAServerJDBCDBFriends();
            ArrayList<Object> friends = DBFriends.select_gen(new SWAServerJDBCPredicate("user1", client.username), new SWAServerJDBCPredicate("status", STATUS_FRIEND));
            for (int i = 0; i < friends.size(); i++)
            {
                clients = DBClients.select_gen(new SWAServerJDBCPredicate("username", ((SWAServerJDBCFriends)friends.get(i)).user2), new SWAServerJDBCPredicate("is_public", true));
                for (int j = 0; j < clients.size(); j++)
                {
                    list += ":";
                    list += (((SWAServerJDBCClient)clients.get(i)).name.trim() + " - " + ((SWAServerJDBCFriends)friends.get(i)).user2.trim());
                }
            }
            
            friends = DBFriends.select_gen(new SWAServerJDBCPredicate("user2", client.username), new SWAServerJDBCPredicate("status", STATUS_FRIEND));
            for (int i = 0; i < friends.size(); i++)
            {
                clients = DBClients.select_gen(new SWAServerJDBCPredicate("username", ((SWAServerJDBCFriends)friends.get(i)).user1), new SWAServerJDBCPredicate("is_public", true));
                for (int j = 0; j < clients.size(); j++)
                {
                    list += ":";
                    list += (((SWAServerJDBCClient)clients.get(i)).name.trim() + " - " + ((SWAServerJDBCFriends)friends.get(i)).user1.trim());
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
            exists = DBClients.exists_gen(new SWAServerJDBCPredicate("session_id", sessionID));
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
            
            return (client_.ip.trim() + ":" + String.valueOf(client_.port));
        }
        catch (Exception e)
        {
            System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
            throw new Exception("Server error");
        }
    }
    
    public void declareFriend(String sessionID, String friend) throws Exception
    {
        SWAServerJDBCDBUsers DBUsers = new SWAServerJDBCDBUsers();
        SWAServerJDBCDBClients DBClients = new SWAServerJDBCDBClients();
        SWAServerJDBCDBFriends DBFriends = new SWAServerJDBCDBFriends();
        
        try
        {
            boolean exists = DBClients.exists_gen(new SWAServerJDBCPredicate("session_id", sessionID));
            if (!exists) throw new Exception("Invalid session");

            ArrayList<Object> clients = DBClients.select_gen(new SWAServerJDBCPredicate("session_id", sessionID));
            SWAServerJDBCClient client = (SWAServerJDBCClient)clients.get(0);
            
            boolean friendExists = DBUsers.exists_gen(new SWAServerJDBCPredicate("username", friend));
            if (!friendExists) throw new Exception("Friend doesn't exist");
            
            if(client.username == friend)
                throw new Exception("You can't be your own friend");
            
            boolean isDeclared = DBFriends.exists_gen(new SWAServerJDBCPredicate("user1", client.username), new SWAServerJDBCPredicate("user2", friend)
                , new SWAServerJDBCPredicate("status", STATUS_FRIEND));
            if(isDeclared) throw new Exception("Relation already declared.");
            
            //Execution
            DBFriends.delete_gen(new SWAServerJDBCPredicate("user1", client.username), new SWAServerJDBCPredicate("user2", friend)
                    , new SWAServerJDBCPredicate("status", STATUS_IGNORE_USER));
            DBFriends.insert_obj(new SWAServerJDBCFriends(client.username, friend, STATUS_FRIEND));
            DBFriends.commit();
        }
        catch (Exception e)
        {
            System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
            throw new Exception("Server error");
        }
    }
    
    public void ignoreUser(String sessionID, String user) throws Exception
    {
        SWAServerJDBCDBUsers DBUsers = new SWAServerJDBCDBUsers();
        SWAServerJDBCDBClients DBClients = new SWAServerJDBCDBClients();
        SWAServerJDBCDBFriends DBFriends = new SWAServerJDBCDBFriends();
        
        try
        {
            boolean exists = DBClients.exists_gen(new SWAServerJDBCPredicate("session_id", sessionID));
            if (!exists) throw new Exception("Invalid session");

            ArrayList<Object> clients = DBClients.select_gen(new SWAServerJDBCPredicate("session_id", sessionID));
            SWAServerJDBCClient client = (SWAServerJDBCClient)clients.get(0);
            
            boolean userExists = DBUsers.exists_gen(new SWAServerJDBCPredicate("username", user));
            if (!userExists) throw new Exception("User doesn't exist");
            
            if(client.username == user)
                throw new Exception("You can't ignore yourself");
            
            boolean isDeclared = DBFriends.exists_gen(new SWAServerJDBCPredicate("user1", client.username), new SWAServerJDBCPredicate("user2", user)
                , new SWAServerJDBCPredicate("status", STATUS_IGNORE_USER));
            if(isDeclared) throw new Exception("Relation already declared.");
            
            //Execution
            DBFriends.delete_gen(new SWAServerJDBCPredicate("user1", client.username), new SWAServerJDBCPredicate("user2", user)
                    , new SWAServerJDBCPredicate("status", STATUS_FRIEND));
            DBFriends.insert_obj(new SWAServerJDBCFriends(client.username, user, STATUS_IGNORE_USER));
            DBFriends.commit();
        }
        catch (Exception e)
        {
            System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
            throw new Exception("Server error");
        }
    }
    
    public void updateTimestamp(String sessionID) throws Exception
    {
        SWAServerJDBCDBClients DBClients = new SWAServerJDBCDBClients();
        try
        {
            boolean exists = DBClients.exists_gen(new SWAServerJDBCPredicate("session_id", sessionID));
            if (!exists) throw new Exception("Invalid session");
            
            ArrayList<Object> clients = DBClients.select_gen(new SWAServerJDBCPredicate("session_id", sessionID));
            SWAServerJDBCClient client = (SWAServerJDBCClient)clients.get(0);
            client.last_time = new Timestamp((Calendar.getInstance()).getTime().getTime());
            DBClients.insert_obj(client);
        }
        catch (Exception e)
        {
            System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
            throw new Exception("Server error");
        }
    }

    
     public String pendingInvitationsRequest(String sessionID) throws Exception
     {
         SWAServerJDBCDBClients DBClients = new SWAServerJDBCDBClients();
         SWAServerJDBCDBFriends DBFriends = new SWAServerJDBCDBFriends();
         System.out.println("Starting pending invitations request...");
         try
         {
             boolean exists = DBClients.exists_gen(new SWAServerJDBCPredicate("session_id", sessionID));
             if (!exists) throw new Exception("Invalid session");
             
             ArrayList<Object> listaCliente = DBClients.select_gen(new SWAServerJDBCPredicate("session_id", sessionID));
             SWAServerJDBCClient cliente = (SWAServerJDBCClient) listaCliente.get(0);
             
             ArrayList<Object> relaciones = DBFriends.select_gen(new SWAServerJDBCPredicate("user2", cliente.username)
             , new SWAServerJDBCPredicate("status", STATUS_FRIEND));

             if(relaciones.isEmpty())
                 return "";

             String list = ((SWAServerJDBCFriends) relaciones.get(0)).user1;
             for(int i=1; i<relaciones.size(); ++i)
             {
                 list += ":" + ((SWAServerJDBCFriends) relaciones.get(i)).user1;
             }
             return list;
         }
         catch (Exception e)
         {
             System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
             throw new Exception("Server error");
         }
     }
     
     public String showListOfFriends(String sessionID) throws Exception
     {
         SWAServerJDBCDBClients DBClients = new SWAServerJDBCDBClients();
         SWAServerJDBCDBFriends DBFriends = new SWAServerJDBCDBFriends();

         try
         {
             boolean exists = DBClients.exists_gen(new SWAServerJDBCPredicate("session_id", sessionID));
             if (!exists) throw new Exception("Invalid session");
             
             ArrayList<Object> listaCliente = DBClients.select_gen(new SWAServerJDBCPredicate("session_id", sessionID));
             SWAServerJDBCClient cliente = (SWAServerJDBCClient) listaCliente.get(0);
             
             ArrayList<Object> relaciones1 = DBFriends.select_gen(new SWAServerJDBCPredicate("user1", cliente.username)
             , new SWAServerJDBCPredicate("status", STATUS_FRIEND));
             
             ArrayList<Object> relaciones2 = DBFriends.select_gen(new SWAServerJDBCPredicate("user2", cliente.username)
             , new SWAServerJDBCPredicate("status", STATUS_FRIEND));

             if(relaciones1.isEmpty() && relaciones2.isEmpty())
                 return "";

             if(!relaciones1.isEmpty())
             {
                 String list = ((SWAServerJDBCFriends) relaciones1.get(0)).user2;

                 for(int i=1; i<relaciones1.size(); ++i)
                     list += ":" + ((SWAServerJDBCFriends) relaciones1.get(i)).user2;
                 for(int i=0; i<relaciones2.size(); ++i)
                     list += ":" + ((SWAServerJDBCFriends) relaciones2.get(i)).user1;
                 
                 return list;
             }
             else
             {
                 String list = ((SWAServerJDBCFriends) relaciones2.get(0)).user1;
                 
                 for(int i=1; i<relaciones2.size(); ++i)
                     list += ":" + ((SWAServerJDBCFriends) relaciones2.get(i)).user1;
                 
                 return list;
             }
         }
         catch (Exception e)
         {
             System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
             throw new Exception("Server error");
         }
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
