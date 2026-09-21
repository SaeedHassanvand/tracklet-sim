package ir.tracklet.sim.service;

public class ObjectNotFoundException extends RuntimeException {
    public ObjectNotFoundException(String fleetId, String objectId) {
        super("Object '" + objectId + "' was not found in fleet '" + fleetId + "'");
    }
}
