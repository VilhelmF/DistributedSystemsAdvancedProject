package se.kth.id2203.atomicregister;

import se.kth.id2203.failuredetector.Topology_Change;
import se.sics.kompics.PortType;

public class AtomicRegister extends PortType {
    {
        request(AR_Read_Request.class);
        request(AR_Write_Request.class);
        request(AR_CAS_Request.class);
        request(Topology_Change.class);
        indication(AR_Read_Response.class);
        indication(AR_Write_Response.class);
        indication(AR_CAS_Response.class);
    }
}
