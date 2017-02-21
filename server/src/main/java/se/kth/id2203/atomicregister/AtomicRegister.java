package se.kth.id2203.atomicregister;

import se.sics.kompics.PortType;
import se.kth.id2203.atomicregister.*;

/**
 * Created by sindrikaldal on 21/02/17.
 */
public class AtomicRegister extends PortType {
    {
        request(AR_Read_Request.class);
        request(AR_Write_Request.class);
        indication(AR_Read_Response.class);
        indication(AR_Write_Response.class);
    }
}
