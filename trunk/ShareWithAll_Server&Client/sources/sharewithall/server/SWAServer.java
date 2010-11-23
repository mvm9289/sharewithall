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

import sharewithall.server.jdbc.JDBCClient;
import sharewithall.server.jdbc.JDBCDBClients;
import sharewithall.server.jdbc.JDBCDBFriends;
import sharewithall.server.jdbc.JDBCDBUsers;
import sharewithall.server.jdbc.JDBCFriends;
import sharewithall.server.jdbc.JDBCPredicate;
import sharewithall.server.jdbc.JDBCUser;
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
        JDBCDBUsers DBUsers = new JDBCDBUsers();

        if (DBUsers.exists_gen(new JDBCPredicate("username", username))) {
            DBUsers.close();
            throw new Exception("This username already exists");
        }
        if (username.contains("-")) {
        	DBUsers.close();
            throw new Exception("This username contains illegal characters: '-'.");
        }

        DBUsers.insert_obj(new JDBCUser(username, sha256(password)));
        DBUsers.commit();
        DBUsers.close();
    }
    
    public String login(String username, String password, String name, boolean isPublic, String ip) throws Exception
    {
        JDBCDBUsers DBUsers = new JDBCDBUsers();
        JDBCPredicate p1 = new JDBCPredicate("username", username);
        JDBCPredicate p2 = new JDBCPredicate("password", sha256(password));
        boolean exists = DBUsers.exists_gen(p1, p2);
        DBUsers.close();
        
        if (!exists) throw new Exception("The username/password combination is not correct");
        
        JDBCDBClients DBClients = new JDBCDBClients();
        String session_id = sha256(System.currentTimeMillis() + username + (new Random()).nextLong() + password);
        ArrayList<Object> clients = DBClients.select_gen(new JDBCPredicate("ip", ip));
        int port = FIRST_CLIENT_PORT + clients.size(); 
        Timestamp last_time = new Timestamp((new Date()).getTime());
        
        exists = DBClients.exists_key(name, username);
        if (!exists) {
            JDBCClient cl = new JDBCClient(ip, port, name, isPublic, last_time, username, session_id);
            DBClients.insert_obj(cl);
            DBClients.commit();
        }
        DBClients.close();
        if (exists) throw new Exception("A client with the same name for the user already exists");
        
        return session_id;
    }
    
    public void logout(String sessionID) throws Exception
    {
        JDBCDBClients DBClients = new JDBCDBClients();
        if (!DBClients.exists_gen(new JDBCPredicate("session_id", sessionID))) throw new Exception("Invalid session");
        
        ArrayList<Object> clients = DBClients.select_gen(new JDBCPredicate("session_id", sessionID));
        JDBCClient client = (JDBCClient)clients.get(0);
        DBClients.delete_key(client.name, client.username);
        DBClients.commit();
        
        clients = DBClients.select_gen(new JDBCPredicate("username", client.username));
        for (int i = 0; i < clients.size(); i++)
        {
            //notifyListChanged(((SWAServerJDBCClient)clients.get(i)).ip, ((SWAServerJDBCClient)clients.get(i)).port);
        }
        
        if (client.is_public)
        {
            JDBCDBFriends DBFriends = new JDBCDBFriends();
            ArrayList<Object> friends = DBFriends.select_gen(new JDBCPredicate("user1", client.username), new JDBCPredicate("status", STATUS_FRIEND));
            for (int i = 0; i < friends.size(); i++)
            {
                clients = DBClients.select_gen(new JDBCPredicate("username", ((JDBCFriends)friends.get(i)).user2));
                for (int j = 0; j < clients.size(); j++)
                {
                    //notifyListChanged(((SWAServerJDBCClient)clients.get(j)).ip, ((SWAServerJDBCClient)clients.get(j)).port);
                }
            }
            
            friends = DBFriends.select_gen(new JDBCPredicate("user2", client.username), new JDBCPredicate("status", STATUS_FRIEND));
            for (int i = 0; i < friends.size(); i++)
            {
                clients = DBClients.select_gen(new JDBCPredicate("username", ((JDBCFriends)friends.get(i)).user1));
                for (int j = 0; j < clients.size(); j++)
                {
                    //notifyListChanged(((SWAServerJDBCClient)clients.get(j)).ip, ((SWAServerJDBCClient)clients.get(j)).port);
                }
            }
        }
        DBClients.close();
    }
    
    public String getOnlineClients(String sessionID) throws Exception
    {
        JDBCDBClients DBClients = new JDBCDBClients();
        
        if (!DBClients.exists_gen(new JDBCPredicate("session_id", sessionID))) throw new Exception("Invalid session");

        ArrayList<Object> clients = DBClients.select_gen(new JDBCPredicate("session_id", sessionID));
        JDBCClient client = (JDBCClient)clients.get(0);
        
        clients = DBClients.select_gen(new JDBCPredicate("username", client.username));
        String list = ((JDBCClient)clients.get(0)).name.trim();
        for (int i = 1; i < clients.size(); i++)
        {
            list += ":";
            list += ((JDBCClient)clients.get(i)).name.trim();
        }
        
        String stringFriends = showListOfFriends(sessionID);
        String[] friends = stringFriends.split(":");
        
        for(int i=0; i<friends.length; ++i)
        {
            ArrayList<Object> publicClients = DBClients.select_gen(new JDBCPredicate("username", friends[i]), new JDBCPredicate("is_public", true));
            for(int j=0; j<publicClients.size(); ++j)
                list += ":" + ((JDBCClient) publicClients.get(i)).username + "-" + ((JDBCClient) publicClients.get(i)).name;
        }
        
        DBClients.close();
        
        return list;
    }
    
    public String ipAndPortRequest(String sessionID, String clientName) throws Exception
    {
        JDBCDBClients DBClients = new JDBCDBClients();
        ArrayList<Object> clients = DBClients.select_gen(new JDBCPredicate("session_id", sessionID));
        if (clients.isEmpty()) throw new Exception("Invalid session");
        String requester = ((JDBCClient)clients.get(0)).username;
        
        //If the clientName is from a client of our own possession.
        if(clientName.indexOf("-") == -1)
        {
        	String username = ((JDBCClient) (DBClients.select_gen(new JDBCPredicate("session_id", sessionID))).get(0)).username;
            clients = DBClients.select_gen(new JDBCPredicate("username", username)
            		, new JDBCPredicate("name", clientName));
            if(clients.size() == 0)
                return "Inexistent Client.";
            JDBCClient client = (JDBCClient)clients.get(0);
            DBClients.close();
            return (client.ip.trim() + ":" + String.valueOf(client.port));
        }
        //If the clientName is from our friend's clients.
        else{
            String friendName = clientName.substring(0, clientName.indexOf("-"));
            System.out.println("FriendName = " + friendName);
            String clientFriendName = clientName.substring(clientName.indexOf("-") + 1, clientName.length());
            System.out.println("ClientFriendName = " + clientFriendName);

            clients = DBClients.select_gen(new JDBCPredicate("name", clientFriendName), new JDBCPredicate("username", friendName));
            DBClients.close();
            if(clients.size() == 0)
                throw new Exception("Cannot access client with name " + clientFriendName + " from user " + friendName);
            JDBCClient client = (JDBCClient) clients.get(0);
            
            if (requester != friendName) {
                JDBCDBFriends DBFriends = new JDBCDBFriends();
                boolean exists1 = DBFriends.exists_gen(new JDBCPredicate("user1", requester), new JDBCPredicate("user2", friendName),
                  new JDBCPredicate("status", STATUS_FRIEND));
                boolean exists2 = DBFriends.exists_gen(new JDBCPredicate("user1", friendName), new JDBCPredicate("user2", requester),
                  new JDBCPredicate("status", STATUS_FRIEND));
                DBFriends.close();
                if (!client.is_public || !exists1 || !exists2)
                    throw new Exception("Cannot access client with name " + clientFriendName + " from user " + friendName);
            }
            
            return (client.ip.trim() + ":" + String.valueOf(client.port));
        }
    }
    
    public String clientNameRequest(String sessionID, String ip, int port) throws Exception
    {
        JDBCDBClients DBClients = new JDBCDBClients();
        ArrayList<Object> clients = DBClients.select_gen(new JDBCPredicate("session_id", sessionID));
        if (clients.isEmpty()) throw new Exception("Invalid session");
        String user1 = ((JDBCClient)clients.get(0)).username;
        
        clients = DBClients.select_gen(new JDBCPredicate("ip", ip), new JDBCPredicate("port", port));
        DBClients.close();
        if (clients.isEmpty()) throw new Exception("Cannot access client with ip " + ip + " and port " + port);
        JDBCClient client = (JDBCClient) clients.get(0);
        String user2 = client.username;
        
        if (user1 != user2) {
            JDBCDBFriends DBFriends = new JDBCDBFriends();
            boolean exists1 = DBFriends.exists_gen(new JDBCPredicate("user1", user1), new JDBCPredicate("user2", user2), new JDBCPredicate("status", STATUS_FRIEND));
            boolean exists2 = DBFriends.exists_gen(new JDBCPredicate("user1", user2), new JDBCPredicate("user2", user1), new JDBCPredicate("status", STATUS_FRIEND));
            DBFriends.close();
            if (!client.is_public || !exists1 || !exists2) throw new Exception("Cannot access client with ip " + ip + " and port " + port);
        }
            
        return ((JDBCClient)clients.get(0)).username +  "-" + ((JDBCClient)clients.get(0)).name;
    }

	public void declareFriend(String sessionID, String friend) throws Exception
    {
        JDBCDBUsers DBUsers = new JDBCDBUsers();
        JDBCDBClients DBClients = new JDBCDBClients();
        JDBCDBFriends DBFriends = new JDBCDBFriends();
        
        ArrayList<Object> clients = DBClients.select_gen(new JDBCPredicate("session_id", sessionID));
        if (clients.isEmpty()) throw new Exception("Invalid session");
        JDBCClient client = (JDBCClient)clients.get(0);
        
        boolean friendExists = DBUsers.exists_gen(new JDBCPredicate("username", friend));
        if (!friendExists) throw new Exception("Friend doesn't exist");
        
        if(client.username == friend)
            throw new Exception("You can't be your own friend");
        
        boolean isDeclared = DBFriends.exists_gen(new JDBCPredicate("user1", client.username), new JDBCPredicate("user2", friend)
            , new JDBCPredicate("status", STATUS_FRIEND));
        if(isDeclared) throw new Exception("Relation already declared.");
        
        //Execution
        JDBCFriends fr = new JDBCFriends(client.username, friend, STATUS_FRIEND);
        if (DBFriends.update_obj(fr) == 0) DBFriends.insert_obj(fr);
        DBFriends.commit();
        DBFriends.close();
    }
    
    public void ignoreUser(String sessionID, String user) throws Exception
    {
        JDBCDBUsers DBUsers = new JDBCDBUsers();
        JDBCDBClients DBClients = new JDBCDBClients();
        JDBCDBFriends DBFriends = new JDBCDBFriends();
        
        boolean exists = DBClients.exists_gen(new JDBCPredicate("session_id", sessionID));
        if (!exists) throw new Exception("Invalid session");

        ArrayList<Object> clients = DBClients.select_gen(new JDBCPredicate("session_id", sessionID));
        JDBCClient client = (JDBCClient)clients.get(0);
        
        boolean userExists = DBUsers.exists_gen(new JDBCPredicate("username", user));
        if (!userExists) throw new Exception("User doesn't exist");
        
        if(client.username == user)
            throw new Exception("You can't ignore yourself");
        
        boolean isDeclared = DBFriends.exists_gen(new JDBCPredicate("user1", client.username), new JDBCPredicate("user2", user)
            , new JDBCPredicate("status", STATUS_IGNORE_USER));
        if(isDeclared) throw new Exception("Relation already declared.");
        
        //Execution
        JDBCFriends fr = new JDBCFriends(client.username, user, STATUS_IGNORE_USER);
        if (DBFriends.update_obj(fr) == 0) DBFriends.insert_obj(fr);
        DBFriends.commit();
        DBFriends.close();
    }
    
    public void updateTimestamp(String sessionID) throws Exception
    {
        JDBCDBClients DBClients = new JDBCDBClients();
        boolean exists = DBClients.exists_gen(new JDBCPredicate("session_id", sessionID));
        if (!exists) throw new Exception("Invalid session");
        
        ArrayList<Object> clients = DBClients.select_gen(new JDBCPredicate("session_id", sessionID));
        JDBCClient client = (JDBCClient)clients.get(0);
        client.last_time = new Timestamp((Calendar.getInstance()).getTime().getTime());
        DBClients.update_obj(client);
        DBClients.commit();
        DBClients.close();
    }

    public String pendingInvitationsRequest(String sessionID) throws Exception
    {
         JDBCDBClients DBClients = new JDBCDBClients();
         JDBCDBFriends DBFriends = new JDBCDBFriends();

         if (!DBClients.exists_gen(new JDBCPredicate("session_id", sessionID))) throw new Exception("Invalid session");
         
         ArrayList<Object> listaCliente = DBClients.select_gen(new JDBCPredicate("session_id", sessionID));
         DBClients.close();
         JDBCClient client = (JDBCClient) listaCliente.get(0);
         
         ArrayList<Object> relaciones = DBFriends.select_gen(new JDBCPredicate("user2", client.username)
         , new JDBCPredicate("status", STATUS_FRIEND));
         
         for(int i=relaciones.size()-1; i>=0; --i)
         {
             String originUser = ((JDBCFriends) relaciones.get(i)).user1;
             if(DBFriends.exists_gen(new JDBCPredicate("user1", client.username), new JDBCPredicate("user2", originUser)))
                 relaciones.remove(i);
         }
         DBFriends.close();
         
         if(relaciones.isEmpty())
             return "";

         String list = ((JDBCFriends) relaciones.get(0)).user1;
         for(int i=1; i<relaciones.size(); ++i)
         {
             list += ":" + ((JDBCFriends) relaciones.get(i)).user1;
         }
         
         return list;
    }
     
    private String showListOfFriends(String sessionID) throws Exception
    {
         JDBCDBClients DBClients = new JDBCDBClients();
         JDBCDBFriends DBFriends = new JDBCDBFriends();

         if (!DBClients.exists_gen(new JDBCPredicate("session_id", sessionID))) throw new Exception("Invalid session");
         
         ArrayList<Object> listaCliente = DBClients.select_gen(new JDBCPredicate("session_id", sessionID));
         DBClients.close();
         JDBCClient client = (JDBCClient) listaCliente.get(0);
         
         ArrayList<Object> relaciones = DBFriends.select_gen(new JDBCPredicate("user1", client.username)
         , new JDBCPredicate("status", STATUS_FRIEND));
         
         ArrayList<Object> result = new ArrayList<Object>();
         for(int i=0; i<relaciones.size(); ++i)
         {
             String friendName = ((JDBCFriends) relaciones.get(i)).user2;
             if(DBFriends.exists_gen(new JDBCPredicate("user1", friendName), new JDBCPredicate("user2", client.username), 
                     new JDBCPredicate("status", STATUS_FRIEND)))
                 result.add(relaciones.get(i));
         }
         DBFriends.close();
         
         if(result.isEmpty())
             return "";

         String list = ((JDBCFriends) result.get(0)).user2;
         for(int i=1; i<relaciones.size(); ++i)
             list += ":" + ((JDBCFriends) result.get(i)).user2;

         return list;
    }     
    
    public String getListOfFriends(String sessionID, int property) throws Exception
    {
         JDBCDBClients DBClients = new JDBCDBClients();
         JDBCDBFriends DBFriends = new JDBCDBFriends();

         if (!DBClients.exists_gen(new JDBCPredicate("session_id", sessionID))) throw new Exception("Invalid session");
         
         ArrayList<Object> listaCliente = DBClients.select_gen(new JDBCPredicate("session_id", sessionID));
         DBClients.close();
         JDBCClient client = (JDBCClient) listaCliente.get(0);
         
         if(property == PROPERTY_FRIENDS)
             return showListOfFriends(sessionID);
         
         ArrayList<Object> relations = new ArrayList<Object>();
         if(property == PROPERTY_DECLARED_FRIEND)
         {
             relations = DBFriends.select_gen(new JDBCPredicate("user1", client.username)
                     , new JDBCPredicate("status", STATUS_FRIEND));
             for(int i=relations.size()-1; i >= 0; --i)
             {
                 String otherUser = ((JDBCFriends) relations.get(i)).user2;
                 if(DBFriends.exists_gen(new JDBCPredicate("user1", otherUser)
                         , new JDBCPredicate("user2", client.username)
                         , new JDBCPredicate("status", STATUS_FRIEND)))
                     relations.remove(i);
             }
             DBFriends.close();
             if(relations.size() == 0)
                 return "";
             
             String result = ((JDBCFriends) relations.get(0)).user2;
             for(int i=1; i<relations.size(); ++i)
                 result += ":" + ((JDBCFriends) relations.get(i)).user2;
             
             return result;
         }
         else if(property == PROPERTY_EXPECTING)
         {
             relations = DBFriends.select_gen(new JDBCPredicate("user2", client.username)
                     , new JDBCPredicate("status", STATUS_FRIEND));
             for(int i=relations.size()-1; i >= 0; --i)
             {
                 String otherUser = ((JDBCFriends) relations.get(i)).user1;
                 if(DBFriends.exists_gen(new JDBCPredicate("user2", otherUser)
                         , new JDBCPredicate("user1", client.username)
                         , new JDBCPredicate("status", STATUS_FRIEND)))
                     relations.remove(i);
             }
             DBFriends.close();
             if(relations.size() == 0)
                 return "";
             
             String result = ((JDBCFriends) relations.get(0)).user1;
             for(int i=1; i<relations.size(); ++i)
                 result += ":" + ((JDBCFriends) relations.get(i)).user1;
             
             return result;
         }
         else if(property == PROPERTY_IGNORED)
         {
             relations = DBFriends.select_gen(new JDBCPredicate("user1", client.username)
             , new JDBCPredicate("status", STATUS_IGNORE_USER));
             DBFriends.close();
             if(relations.size() == 0)
                 return "";
             
             String result = ((JDBCFriends) relations.get(0)).user2;
             for(int i=1; i<relations.size(); ++i)
                 result += ":" + ((JDBCFriends) relations.get(i)).user2;
             
             return result;
         }
         throw new Exception("Wrong property identifier.");
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

