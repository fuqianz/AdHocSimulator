package edu.sse.ustc;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;

public class AdHocTestCommsPeer extends AdHocPeer {

    boolean deployMode = true;
    boolean squadLeader = false;
    boolean dead = false;

    double myX, myY;

    double dir, dist;
    final double STEP = 1.0;
    double dx, dy;

    Point2D.Double myPosn;
    Vector msgs;
    long timeChecked = 0L, timeOfLastMovement = -1L, timeOfLastTrack;
    double x,y;
    int count;
    double lastDir;

    GPSSensor s;
    AdHocEffector testCommsAdHocEffector;

    AdHocTestCommsPeer thisSender, lastSender;
    long thisTimeStamp, lastTimeStamp;
    boolean tracking = false;
    double lastX, lastY, thisX, thisY, trackX, trackY, trackVX, trackVY;
    double timeElapsed;


    public AdHocTestCommsPeer(String name){
		super(name);
	}

    public Point2D.Double predictedTargetPosn() {
	if (! tracking)
	    return null;
	else {
	    double timeDelta = (System.currentTimeMillis() - timeOfLastTrack)/1000.0;
	    double x = trackX + trackVX*timeDelta;
	    double y = trackY + trackVY*timeDelta;
	    //  System.out.println(x + "  " + y + "  " + trackVX + "  " + trackVY);
	    return new Point2D.Double(x, y);
	}
    }


    private boolean alarm = false;

    public boolean isTracking() {
	return tracking;
    }

    public boolean alarmed() {
	return alarm;
    }

    public boolean isDead() {
	return dead;
    }

    public void die() {
	dead = true;
    }

    public boolean isSquadLeader() {
	return squadLeader;
    }

    private void chooseSquadLeader () {

	// find agent closest to the center of the layout

	if (s.centerPeer() == this)
	    squadLeader = true;
	else
	    squadLeader = false;
    }

}



class GPSSensor extends AdHocSensor {

    AdHocPeer self;
    Point2D.Double myState;


    public GPSSensor(AdHocPeer a) {
	self = a;
    }

    public Point2D.Double getMyPosn() {
	HashMap entityStates = ((AdHocTestCommsState)theEnvState).getEntityStates();
	Point2D.Double myState = (Point2D.Double)entityStates.get(self);
	return myState;
    }

    public AdHocTestCommsPeer centerPeer() {
	double sx = 0.0, sy = 0.0, cx, cy, x, y;
	int count = 0;
	HashMap entityStates = ((AdHocTestCommsState)theEnvState).getEntityStates();
	Iterator it = entityStates.keySet().iterator();
	while (it.hasNext()) {
	    AdHocPeer a = (AdHocPeer) it.next();
	    if (a instanceof AdHocTestCommsPeer) {
		if (((AdHocTestCommsPeer)a).isDead()) continue;
		sx += ((Point2D.Double)entityStates.get(a)).getX();
		sy += ((Point2D.Double)entityStates.get(a)).getY();
		count++;
	    }
	}
	cx = sx/count;
	cy = sy/count;
	it = entityStates.keySet().iterator();
	AdHocTestCommsPeer closest=null;
	double closestDist = 1000000.0;
	while (it.hasNext()) {
	    AdHocPeer a = (AdHocPeer) it.next();
	    if (a instanceof AdHocTestCommsPeer) {
		if (((AdHocTestCommsPeer)a).isDead()) continue;
		x = ((Point2D.Double)entityStates.get(a)).getX();
		y = ((Point2D.Double)entityStates.get(a)).getY();
		double d = Math.sqrt((x-cx)*(x-cx) + (y-cy)*(y-cy));
		if (closest==null ||
		    d < closestDist) {
		    closest = (AdHocTestCommsPeer) a;
		    closestDist = d;
		}
	    }
	}

	return closest;
    }


    public boolean targetSeen() {
	HashMap entityStates = ((AdHocTestCommsState)theEnvState).getEntityStates();
	Iterator it = entityStates.keySet().iterator();
	while (it.hasNext()) {
	    AdHocPeer a = (AdHocPeer) it.next();
	    if (a instanceof TargetPeer) {
		Point2D.Double s =
		    (Point2D.Double)entityStates.get((TargetPeer)a);
		Point2D.Double myState =
		    (Point2D.Double)entityStates.get(self);
		if (distance(s, myState) < 10.0)
		    ((AdHocTestCommsPeer)self).die();
		else if (distance(s, myState) < 24.0)
		    return true;
		else
		    return false;
	    }
	}
	return false;   // no target exists
    }

    public double distance(Point2D.Double a, Point2D.Double b) {
	double dx = a.getX() - b.getX();
	double dy = a.getY() - b.getY();
	return Math.sqrt(dx*dx + dy*dy);
    }

}  // GPSSensor




