package edu.sse.ustc;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;

public class TargetPeer extends AdHocPeer {

    private final double RADIUS = 200.0;
    private double angle, dx, dy, lastDX, lastDY, lastAngle;
    private final double STEP = 2.0;

	public TargetPeer(String name){
		super(name);
	}

}


class DistFromMiddleSensor extends AdHocSensor {

    AdHocPeer self;

    public DistFromMiddleSensor(AdHocPeer a) {
	self = a;
    }

    public double getDist() {
	HashMap entityStates = ((AdHocTestCommsState)theEnvState).getEntityStates();
	Point2D.Double myState = (Point2D.Double)entityStates.get(self);
	double dx = myState.getX() - 400.0;
	double dy = myState.getY() - 330.0;
	return Math.sqrt(dx*dx + dy*dy);
    }

}




