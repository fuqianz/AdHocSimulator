package edu.sse.ustc;

import org.mitre.sim.api3.Entity;

public class AdHocAnimationDisplayEntity extends Entity implements Common{


    int framesPerSecond;
	long frameTimeDelta;
	AdHocAnimationDisplay animDisplay;
	private String myName;

	public AdHocAnimationDisplayEntity(String name, int fps,
	          AdHocAnimationDisplay anim){
		super(name);
		animDisplay = anim;
		framesPerSecond = fps;
	    frameTimeDelta = 1000L/framesPerSecond;

	}

	public void setName(String name){	myName = name;	}

    protected AdHocEnv theEnv;

    public synchronized void registerAdHocEnv(AdHocEnv e) {
		theEnv = e;
    }

	public void agenda () {

		while (true) {

				waitForTime(frameTimeDelta);
				animDisplay.repaint();

			}  // while(true)
	}  // agenda ends
}






