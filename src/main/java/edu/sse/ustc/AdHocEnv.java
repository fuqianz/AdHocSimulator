package edu.sse.ustc;

import java.util.*;
import java.awt.*;

public class AdHocEnv implements Runnable {
    private double simulatedTime = 0.0;
    private AdHocEnvState theAdHocEnvState;
//    private AdHocEnvState theSimDataState;
    private Vector entities = new Vector();


    Thread environmentThread;
    static int count = 0;
    Enumeration en;

    public AdHocEnv() {
		count++;
		if (count>1)
			System.out.println("Can only have one AdHocEnv");
		environmentThread = new Thread(this);
		environmentThread.setPriority(Thread.MIN_PRIORITY);
    }

    public void setState(AdHocEnvState s) {	theAdHocEnvState = s;    }
    public AdHocEnvState getState() {		return theAdHocEnvState;    }

//    public void setSimDataState(AdHocEnvState ds) {	theSimDataState = ds;    }
//    public AdHocEnvState getSimDataState() {		return theSimDataState;    }

    public void embed(AdHocPeer e, Object es) {
		entities.add(e);
		theAdHocEnvState.putState(e,es);

//		theSimDataState.putState(e,new Integer(0));

		e.registerAdHocEnv(this);

		AdHocSensor.registerAdHocEnvState(theAdHocEnvState);
		AdHocEffector.registerAdHocEnvState(theAdHocEnvState);

//		AdHocSensor.registerAdHocEnvState(theSimDataState);
//		AdHocEffector.registerAdHocEnvState(theSimDataState);
    }


	public double getTime() { return simulatedTime; }
    public void setTime(double newTime) { simulatedTime = newTime; }

    public synchronized void update(Object o, int dx, int dy) {
	theAdHocEnvState.update(o, dx, dy);
	//	ActiveDotEntityState es = (ActiveDotEntityState)(theAdHocEnvState.getEntityStates().get(o));
	//	es.update(dx,dy);
    }

    public void run () {
    }

    public void draw(Graphics g) {
	theAdHocEnvState.draw(g);
    }

    public synchronized void tick(double timeDelta) {
	theAdHocEnvState.tick(timeDelta);
    }

    public void killClosestPeer(double x, double y) {
	((AdHocTestCommsState)theAdHocEnvState).killClosestPeer(x,y);
    }
}




