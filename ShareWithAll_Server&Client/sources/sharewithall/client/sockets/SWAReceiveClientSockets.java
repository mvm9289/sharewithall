/**
 * 
 */
package sharewithall.client.sockets;

import sharewithall.client.SWAClient;

/**
 * Authors:
 *    Alex Catarineu
 *    Ferran Rigual
 *    Miguel Angel Vico
 *
 * Creation date: Nov 6, 2010
 */
public class SWAReceiveClientSockets extends SWAReceiveSockets
{
    
    @SuppressWarnings("unused")
    private SWAClient client;

    public SWAReceiveClientSockets(int port, SWAClient client)
    {
        super(port);
        this.client = client;
    }

    @Override
    public void process(int instruction, Object data)
    {
        switch (instruction)
        {
            case SEND_URL:
                //TODO: Llamar a la funcion externa
                break;
            case SEND_TEXT:
                //TODO: Llamar a la funcion externa
                break;
            case SEND_FILE:
                //TODO: Llamar a la funcion externa
                break;
            default:
                break;
        }
    }
    
}