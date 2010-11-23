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
    private static final int PROPERTY_FRIENDS = 0;
    private static final int PROPERTY_DECLARED_FRIEND = 1;
    private static final int PROPERTY_EXPECTING = 2;
    private static final int PROPERTY_IGNORED = 3;
    
    @SuppressWarnings("unused")
    private static SWAServerSockets socketsModule;
    
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
        if (username.contains("-")) {
        	DBUsers.close();
            throw new Exception("This username contains illegal characters: '-'.");
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
            ArrayList<Object> clients = DBClients.select_gen(new SWAServerJDBCPredicate("ip", ip));
            int port = FIRST_CLIENT_PORT + clients.size(); 
            Timestamp last_time = new Timestamp((new Date()).getTime());
            
            // TODO: Comprobar unicidad de (username, name)
            
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
            
            String stringFriends = showListOfFriends(sessionID);
            String[] friends = stringFriends.split(":");
            
            for(int i=0; i<friends.length; ++i)
            {
                ArrayList<Object> publicClients = DBClients.select_gen(new SWAServerJDBCPredicate("username", friends[i]), new SWAServerJDBCPredicate("is_public", true));
                for(int j=0; j<publicClients.size(); ++j)
                    list += ":" + ((SWAServerJDBCClient) publicClients.get(i)).username + "-" + ((SWAServerJDBCClient) publicClients.get(i)).name;
            }
            return list;
                
        }
        catch (Exception e)
        {
            System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
            throw new Exception("Server error");
        }
    }
    
    public String ipAndPortRequest(String sessionID, String clientName) throws Exception
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
            //If the clientName is from a client of our own possession.
            if(clientName.indexOf("-") == -1)
            {
            	String username = ((SWAServerJDBCClient) (DBClients.select_gen(new SWAServerJDBCPredicate("session_id", sessionID))).get(0)).username;
                ArrayList<Object> clients = DBClients.select_gen(new SWAServerJDBCPredicate("username", username)
                		, new SWAServerJDBCPredicate("name", clientName));
                if(clients.size() == 0)
                    return "Inexistent Client.";
                SWAServerJDBCClient client = (SWAServerJDBCClient)clients.get(0);
                
                return (client.ip.trim() + ":" + String.valueOf(client.port));
            }
            //If the clientName is from our friend's clients.
            else{
                String stringFriends = showListOfFriends(sessionID);
                String[] friends = stringFriends.split(":");
                String friendName = clientName.substring(0, clientName.indexOf("-"));
                System.out.println("FriendName = " + friendName);
                String clientFriendName = clientName.substring(clientName.indexOf("-") + 1, clientName.length());
                System.out.println("ClientFriendName = " + clientFriendName);
                for(int i=0; i<friends.length; ++i)
                {
                    if(friendMatch(friendName, friends[i]))
                    {
                        ArrayList<Object> clients = DBClients.select_gen(new SWAServerJDBCPredicate("username", friendName)
                            , new SWAServerJDBCPredicate("name", clientName)
                            , new SWAServerJDBCPredicate("name", clientName)
                                );
                        if(clients.size() == 0)
                            throw new Exception("Client " + clientFriendName + " doesn't exists.");
                        SWAServerJDBCClient client = (SWAServerJDBCClient)clients.get(0);
                        return (client.ip.trim() + ":" + String.valueOf(client.port));
                    }
                }
                throw new Exception("Friend "+ friendName +" doesn't exists.");
            }
        }
        catch (Exception e)
        {
            System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
            throw new Exception("Server error");
        }
    }
    
    private boolean friendMatch(String friendName, String DBNameComplete) {
    	String DBName = DBNameComplete.substring(0, DBNameComplete.indexOf(" "));
    	System.out.println("DBName:"+DBName+".");
    	System.out.println("friendName:"+friendName+".");
    	System.out.println("friendMatch result: "+friendName.equals(DBName)+".");
		return friendName.equals(DBName);
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

         try
         {
             boolean exists = DBClients.exists_gen(new SWAServerJDBCPredicate("session_id", sessionID));
             if (!exists) throw new Exception("Invalid session");
             
             ArrayList<Object> listaCliente = DBClients.select_gen(new SWAServerJDBCPredicate("session_id", sessionID));
             SWAServerJDBCClient client = (SWAServerJDBCClient) listaCliente.get(0);
             
             ArrayList<Object> relaciones = DBFriends.select_gen(new SWAServerJDBCPredicate("user2", client.username)
             , new SWAServerJDBCPredicate("status", STATUS_FRIEND));
             
             for(int i=relaciones.size()-1; i>=0; --i)
             {
                 String originUser = ((SWAServerJDBCFriends) relaciones.get(i)).user1;
                 if(DBFriends.exists_gen(new SWAServerJDBCPredicate("user1", client.username), new SWAServerJDBCPredicate("user2", originUser)))
                     relaciones.remove(i);
             }

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
     
    private String showListOfFriends(String sessionID) throws Exception
    {
         SWAServerJDBCDBClients DBClients = new SWAServerJDBCDBClients();
         SWAServerJDBCDBFriends DBFriends = new SWAServerJDBCDBFriends();

         try
         {
             boolean exists = DBClients.exists_gen(new SWAServerJDBCPredicate("session_id", sessionID));
             if (!exists) throw new Exception("Invalid session");
             
             ArrayList<Object> listaCliente = DBClients.select_gen(new SWAServerJDBCPredicate("session_id", sessionID));
             SWAServerJDBCClient client = (SWAServerJDBCClient) listaCliente.get(0);
             
             ArrayList<Object> relaciones = DBFriends.select_gen(new SWAServerJDBCPredicate("user1", client.username)
             , new SWAServerJDBCPredicate("status", STATUS_FRIEND));
             
             ArrayList<Object> result = new ArrayList<Object>();
             for(int i=0; i<relaciones.size(); ++i)
             {
                 String friendName = ((SWAServerJDBCFriends) relaciones.get(i)).user2;
                 if(DBFriends.exists_gen(new SWAServerJDBCPredicate("user1", friendName), new SWAServerJDBCPredicate("user2", client.username), 
                         new SWAServerJDBCPredicate("status", STATUS_FRIEND)))
                     result.add(relaciones.get(i));
             }
             
             if(result.isEmpty())
                 return "";

             String list = ((SWAServerJDBCFriends) result.get(0)).user2;
             for(int i=1; i<relaciones.size(); ++i)
                 list += ":" + ((SWAServerJDBCFriends) result.get(i)).user2;

             return list;
         }
         catch (Exception e)
         {
             System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
             throw new Exception("Server error");
         }
    }     
    
    public String getListOfFriends(String sessionID, int property) throws Exception
    {
         SWAServerJDBCDBClients DBClients = new SWAServerJDBCDBClients();
         SWAServerJDBCDBFriends DBFriends = new SWAServerJDBCDBFriends();

         try
         {
             boolean exists = DBClients.exists_gen(new SWAServerJDBCPredicate("session_id", sessionID));
             if (!exists) throw new Exception("Invalid session");
             
             ArrayList<Object> listaCliente = DBClients.select_gen(new SWAServerJDBCPredicate("session_id", sessionID));
             SWAServerJDBCClient client = (SWAServerJDBCClient) listaCliente.get(0);
             
             if(property == PROPERTY_FRIENDS)
                 return showListOfFriends(sessionID);
             
             ArrayList<Object> relations = new ArrayList<Object>();
             if(property == PROPERTY_DECLARED_FRIEND)
             {
                 relations = DBFriends.select_gen(new SWAServerJDBCPredicate("user1", client.username)
                         , new SWAServerJDBCPredicate("status", STATUS_FRIEND));
                 for(int i=relations.size()-1; i >= 0; --i)
                 {
                     String otherUser = ((SWAServerJDBCFriends) relations.get(i)).user2;
                     if(DBFriends.exists_gen(new SWAServerJDBCPredicate("user1", otherUser)
                             , new SWAServerJDBCPredicate("user2", client.username)
                             , new SWAServerJDBCPredicate("status", STATUS_FRIEND)))
                         relations.remove(i);
                 }
                 
                 if(relations.size() == 0)
                     return "";
                 
                 String result = ((SWAServerJDBCFriends) relations.get(0)).user2;
                 for(int i=1; i<relations.size(); ++i)
                     result += ":" + ((SWAServerJDBCFriends) relations.get(i)).user2;
                 
                 return result;
             }
             else if(property == PROPERTY_EXPECTING)
             {
                 relations = DBFriends.select_gen(new SWAServerJDBCPredicate("user2", client.username)
                         , new SWAServerJDBCPredicate("status", STATUS_FRIEND));
                 for(int i=relations.size()-1; i >= 0; --i)
                 {
                     String otherUser = ((SWAServerJDBCFriends) relations.get(i)).user1;
                     if(DBFriends.exists_gen(new SWAServerJDBCPredicate("user2", otherUser)
                             , new SWAServerJDBCPredicate("user1", client.username)
                             , new SWAServerJDBCPredicate("status", STATUS_FRIEND)))
                         relations.remove(i);
                 }
                 
                 if(relations.size() == 0)
                     return "";
                 
                 String result = ((SWAServerJDBCFriends) relations.get(0)).user1;
                 for(int i=1; i<relations.size(); ++i)
                     result += ":" + ((SWAServerJDBCFriends) relations.get(i)).user1;
                 
                 return result;
             }
             else if(property == PROPERTY_IGNORED)
             {
                 relations = DBFriends.select_gen(new SWAServerJDBCPredicate("user1", client.username)
                 , new SWAServerJDBCPredicate("status", STATUS_IGNORE_USER));
                 
                 if(relations.size() == 0)
                     return "";
                 
                 String result = ((SWAServerJDBCFriends) relations.get(0)).user2;
                 for(int i=1; i<relations.size(); ++i)
                     result += ":" + ((SWAServerJDBCFriends) relations.get(i)).user2;
                 
                 return result;
             }
             throw new Exception("Wrong property identifier.");
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
            System.out.println(
                "\n\tUSAGE:\n\t\t" +
                    "java sharewithall.server.SWAServer [port]" +
        		"\n\n\t*Arguments between [] are optional. Default port is " + DEFAULT_SERVER_PORT + ".\n");
        }
    }
}

