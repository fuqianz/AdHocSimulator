package edu.sse.ustc;

import java.util.*;
import java.awt.*;

public class AdHocEnvState implements Common{

    private Vector messages = new Vector();
    Vector grids = new Vector();
    Polygon groupPolygon = new Polygon();
    Polygon combatArea = new Polygon();
	ArrayList hexagons = new ArrayList();
	ArrayList rings = new ArrayList();

	// all centers in ring edge (outer ring)
	ArrayList ringCenters = new ArrayList();

	// all centers in all rings
	ArrayList ringInfo = new ArrayList();

    private long holdTime = 2000;  // 2 seconds
	public AdHocEnvState(Vector g){
		grids = g;
	}
	public AdHocEnvState(Polygon groupPolygon, Polygon combatArea, ArrayList hexagons, ArrayList rings, ArrayList ringCenters, ArrayList ringInfo){
		this.groupPolygon = groupPolygon;
		this.combatArea = combatArea;
		this.hexagons = hexagons;
		this.rings = rings;
		this.ringCenters = ringCenters;
		this.ringInfo = ringInfo;
	}
	public AdHocEnvState(Polygon groupPolygon, Polygon combatArea, ArrayList hexagons){
		this.groupPolygon = groupPolygon;
		this.combatArea = combatArea;
		this.hexagons = hexagons;
	}
	public synchronized Polygon getGroupPolygon(){
		return groupPolygon;
	}
	public synchronized Polygon getCombatArea(){
		return combatArea;
	}
	public synchronized ArrayList getHexagons(){
		return hexagons;
	}
	public synchronized ArrayList getRings(){
		return rings;
	}
	public synchronized ArrayList getRingCenters(){
		return ringCenters;
	}
	public synchronized ArrayList getRingInfo(){
		return ringInfo;
	}
	public Vector getGrids(){
		return grids;
	}
    public void draw(Graphics g) {
	// draw background, special stuff

	// draw each entity
    }

    public synchronized void update(Object o, int dx, int dy) {
	System.out.println("not updating");
    }

    public synchronized void putState(AdHocPeer e, Object o) {
	System.out.println("not setting state");
    }

    public synchronized void tick(double simTime) {
	System.out.println("not ticking");
    }

    public void setHoldTime(double messageHoldTime) {
	// convert seconds to milliseconds
	holdTime = (long) Math.floor(messageHoldTime*1000);
    }

    public synchronized void postMessage(Message msg) {
	msg.setTimeStamp(System.currentTimeMillis());
	messages.add(msg);
    }

    public synchronized Vector getAvailableMessages() {
	long expireTime = System.currentTimeMillis() - holdTime;
	removeMessagesEarlierThan(expireTime);
	return (Vector)messages.clone();
    }

    public synchronized Vector getMessagesSince(long timeAlreadySeen) {
	Message m;
	long expireTime = System.currentTimeMillis() - holdTime;
	removeMessagesEarlierThan(expireTime);
	Vector newMessages = (Vector) messages.clone();
	while (! newMessages.isEmpty()) {
	    m = (Message) newMessages.firstElement();
	    if (m.getTimeStamp() <= timeAlreadySeen)
		newMessages.removeElementAt(0);
	    else  // all other messages are later
		break;
	}
	return newMessages;
    }

    private void removeMessagesEarlierThan(long time) {
	Message m;
	while (! messages.isEmpty()) {
	    m = (Message) messages.firstElement();
	    if (m.getTimeStamp() < time)
		messages.removeElementAt(0);
	    else // quit looking; all other messages are later
		break;
	}
	return;
    }

}


