/**
 * 
 */
package sharewithall.client;

import java.util.Scanner;

import sharewithall.client.sockets.SWAClientSockets;

/**
 * Authors:
 *    Alex Catarineu
 *    Ferran Rigual
 *    Miguel Angel Vico
 *
 * Creation date: Oct 31, 2010
 */
public class SWAClient
{
    
    private static final String serverIP = "mvm9289.dyndns.org";
    private static final int serverPort = 4040;

    public static void main(String[] args)
    {
        SWAClientSockets socketsModule = new SWAClientSockets(serverIP, serverPort);
        String username, password, friend, sessionID = null;
        boolean end = false;
        
        while(!end)
        {
    		System.out.println("Choose your commmand.");
    		System.out.println("                   New User: 0 username password");
    		System.out.println("                      Login: 1 username password name { true | false }");
    		System.out.println("         Get Online Clients: 2");
    		System.out.println("        IP and port request: 3 client");
    		System.out.println("             Declare friend: 4 friend");
    		System.out.println("                Ignore user: 5 user");
    		System.out.println("Pending invitations request: 6");
            System.out.println("       Show list of friends: 8");
    		System.out.println("                       Exit: 7");
    		System.out.println("--------------------------------------------");
            
    		Scanner sc = new Scanner(System.in);
    		sc.useDelimiter("[\\s]");
    
    		
    		
    		int commandIndex = sc.nextInt();
    		switch(commandIndex)
    		{
    		    case 0: //void newUser (String username, String password)
                    if(sessionID != null)
                    {
                        System.out.println("Sorry, you are already logged in.");
                        break;
                    }    		        
                    username = sc.next();
                    password = sc.next();
                    try
                    {
                        socketsModule.newUser(username, password);
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    break;
                case 1: //String login(String username, String password, String name, boolean isPublic)
                    if(sessionID != null)
                    {
                        System.out.println("Sorry, you are already logged in.");
                        break;
                    }
                    String name;
                    boolean isPublic;
                    username = sc.next();
                    password = sc.next();
                    name = sc.next();
                    isPublic = sc.nextBoolean();
                    try
                    {
                        sessionID = socketsModule.login(username, password, name, isPublic);
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    break;
                case 2: //String[] getOnlineClients(String sessionID) 
                    String[] clients = null;
                    if(sessionID == null)
                    {
                        System.out.println("Sorry, you must be logged in.");
                        break;
                    }
                    try
                    {
                        clients = socketsModule.getOnlineClients(sessionID);
                    } catch (Exception e1)
                    {
                        e1.printStackTrace();
                    }
                    for(int i=0; i<clients.length; ++i)
                        System.out.println(clients[i]);
                    break;
                case 3: //String[] ipAndPortRequest(String sessionID, String client) 
                    String client = sc.next();
                    if(sessionID == null)
                    {
                        System.out.println("Sorry, you must be logged in.");
                        break;
                    }
                    try
                    {
                        String[] result = new String[2];
                        result = socketsModule.ipAndPortRequest(sessionID, client);
                        System.out.println(result[0] + ":" + result[1]);
                    } catch (Exception e1)
                    {
                        e1.printStackTrace();
                    }
                    break;
                case 4: //void declareFriend(sessionID, friend)
                    friend = sc.next();
                    if(sessionID == null)
                    {
                        System.out.println("Sorry, you must be logged in.");
                        break;
                    }
                    try
                    {
                        socketsModule.declareFriend(sessionID, friend);
                    } catch (Exception e1)
                    {
                        e1.printStackTrace();
                    }
                    break;
                case 5: //void ignoreUser(String sessionID, String user)
                    friend = sc.next();
                    if(sessionID == null)
                    {
                        System.out.println("Sorry, you must be logged in.");
                        break;
                    }
                    try
                    {
                        socketsModule.ignoreUser(sessionID, friend);
                    } catch (Exception e1)
                    {
                        e1.printStackTrace();
                    }
                    break;
                case 6: //String[] pendingInvitationsRequest(String sessionID)
                    if(sessionID == null)
                    {
                        System.out.println("Sorry, you must be logged in.");
                        break;
                    }
                    try
                    {
                        String[] result;
                        result = socketsModule.pendingInvitationsRequest(sessionID);
                        for(int i=0; i<result.length; ++i)
                        {
                            System.out.println(result[i]);
                        }
                    } catch (Exception e1)
                    {
                        e1.printStackTrace();
                    }
                    
                    break;
                case 7: //void logout(String sessionID)
                    if(sessionID == null)
                    {
                        System.out.println("Sorry, you must be logged in.");
                        break;
                    }
                    try
                    {
                        socketsModule.logout(sessionID);
                        sessionID = null;
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    end = true;
                    break;
                case 8: //String showListOfFriends(String sessionID)
                    if(sessionID == null)
                    {
                        System.out.println("Sorry, you must be logged in.");
                        break;
                    }
                    try
                    {
                        String[] result;
                        result = socketsModule.showListOfFriends(sessionID);
                        for(int i=0; i<result.length; ++i)
                        {
                            System.out.println(result[i]);
                        }
                    } catch (Exception e1)
                    {
                        e1.printStackTrace();
                    }
                    
                    break;
                default:
                    System.out.println("Wrong command, try again.");
    		}
    		System.out.println("Done!");
        }

    }

}
