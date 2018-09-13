package edu.sse.ustc;

import org.mitre.sim.api3.*;
import java.util.*;
import java.awt.*;
import java.awt.geom.*;

public class AdHocPeer extends Entity implements Common{


	private String myName;
	private double mobilityRate = MOBILITY_RATE; // default is one hexagon crossing per sec
	private double dataSensingRate=DATA_SENSING_RATE; // default is 1 update operation per second per node
	private double failureRate=FAILURE_RATE; // default is 1 failures event per hour per node
	private double beaconT=BEACON_PERIOD;

	boolean membershipChangedFlag = false;
	boolean dataChangedFlag = false;
	boolean failureDetectedFlag= false;

	Trigger membershipChanged = new Trigger() {
				public boolean condition() {
					return (membershipChangedFlag == true);
				}
		};
    Trigger dataChanged = new Trigger() {
					public boolean condition() {
						return (dataChangedFlag == true);
					}
		};
	Trigger failureDetected = new Trigger() {
						public boolean condition() {
							return (failureDetectedFlag == true);
						}
		};
	Trigger[]  triggers = {membershipChanged, dataChanged, failureDetected};

	public AdHocPeer(String name){
		super(name);
	}

    public AdHocPeer(String name, double mobilityRate, double dataSensingRate, double failureRate, double beaconT){
			super(name);
			this.mobilityRate = mobilityRate;
			this.dataSensingRate = dataSensingRate; // not used -- use aggregate data sensing rate instead in statistical analyzer
			this.failureRate = failureRate;
			this.beaconT = beaconT;
	}
	public void setName(String name){	myName = name;	}

	protected AdHocEnv theEnv;

    public synchronized void registerAdHocEnv(AdHocEnv e) {
		theEnv = e;
    }

    public double expntl(double meanTime) {
      // 'expntl' returns a psuedo-random variate from a negative
      // exponential distribution with mean "meanTime".

      return(-meanTime*Math.log(Math.random()));
    }

	public synchronized void actionLeadMembershipChange() {
			//info("I am lead " + this.getName() + " ... updating membership event received");
			membershipChangedFlag = true;
	}

    public synchronized void actionLeadDataChange() {
		//info("I am lead " + this.getName() + " ... data propagation event received");
		dataChangedFlag = true;
	}

    public synchronized void actionLeadMemberFailureDetected() {
			//info("I am lead " + this.getName() + " ... member failure event received");
			failureDetectedFlag = true;
		}


    // agenda
	public void agenda () {
		double dx = 10.0;
		double dy = 10.0;
		double lastDX = 10.0;
		double lastDY = 10.0;
		double timeStampNow;

		double direction = (Math.random()) * 2 * Math.PI;

		// private double lastDirection;

		double maxSpeed = 2 * mobilityRate;
		double speed = (Math.random()) * maxSpeed;

		// random walk parameters: change the speed and direction after every DIRECTION_SPEED_FIXED_INTERVAL
		double timeToChangeSpeedAndDirection = getTimeNow() + DIRECTION_SPEED_FIXED_INTERVAL;

		AdHocEffector testCommsAdHocEffector = new AdHocEffector();
		DistFromGridCenterAdHocSensor s = new DistFromGridCenterAdHocSensor(this);
		AdHocTestCommsState state;

		double timeToFailure = getTimeNow() + expntl(1.0/failureRate);
		double timeToDataSensing = getTimeNow() + expntl(1.0/dataSensingRate);
		double lastEventTime = getTimeNow();
		double timeElapsed=0;

		Point2D currLoc;
		Point2D lastLoc;
		HashMap dataStates;
		HashMap failedOrAliveStates;
		HashMap entityStates;
		Iterator iter;

		while (true) {

				// info("timeToFailure = " + timeToFailure + "; timetoDataSensing = " + timeToDataSensing);
				WaitResult wr = waitForActionOrTrigger(triggers, LOCATION_UPDATE_TIME_INTERVAL);
				timeStampNow = getTimeNow();
				timeElapsed = timeStampNow - lastEventTime;

				lastEventTime = timeStampNow;
				state = ((AdHocTestCommsState)testCommsAdHocEffector.theEnvState);

				dataStates = state.getSimDataStates(); // not used
				failedOrAliveStates = state.getEntityFailedOrAliveStates();
				entityStates = state.getEntityStates();


/* not used  -- use AGGREGATE_DATA_SENSING_RATE instead in the statistical analyzer */
/****************************************************************************************************************************************
				 else if (timeToDataSensing <= timeStampNow)  {
				   		// this peer has triggered a data sensing event to the lead to propagate data update to other members

						   state.updateDataState(this);

				   		   AdHocPeer leader = state.getGroupLeader();
						   	if (leader.getName() != NONEXIST_ID) {
						   		// simulate time needed to send data change request to the lead
						   		waitForTime(TAU * ((Point2D.Double) entityStates.get(this)).distance((Point2D.Double) entityStates.get(leader))/(2*RADIUS));
							    leader.actionLeadDataChange();
								}
							timeToDataSensing = timeStampNow + expntl(1.0/dataSensingRate);
				   }
******************************************************************************************************************************************/

				if (!s.insideCombatArea()) { // bounce back
						direction = direction + Math.PI;
//						dx = speed * Math.cos(direction) - lastDX;
//						dy = speed * Math.sin(direction) - lastDY;
						dx = Math.cos(direction) - lastDX;
						dy = Math.sin(direction) - lastDY;
					}
				else { // random motion
						if (timeToChangeSpeedAndDirection <= timeStampNow)  {
							 direction = (Math.random()) * 2 * Math.PI;
							 // lastDirection = Math.atan2(dy,dx);
							 speed = (Math.random()) * maxSpeed;
							 timeToChangeSpeedAndDirection = timeStampNow + DIRECTION_SPEED_FIXED_INTERVAL;
						 }
						dx = speed * Math.cos(direction)*timeElapsed;
						dy = speed * Math.sin(direction)*timeElapsed;
					}

					currLoc = new Point2D.Double(dx,dy);
					state.updateLocation(this, currLoc);
					lastDX = dx;
					lastDY = dy;

					if (membershipChangeDueToMobility(this, currLoc)) {
						AdHocPeer leader = state.getGroupLeader();
						if (leader.getName() != NONEXIST_ID) {

							// calculate time needed to send membership change request to the lead
							double commTime1 = TAU * ((Point2D.Double) entityStates.get(this)).distance((Point2D.Double) entityStates.get(leader))/(2*RADIUS);
							schedule("adHocStatisticalAnalyzerActionMembershipChange", commTime1);

							// calculate time needed for the lead to propagate membership change to members via multicast tree
							double commTime2 = TAU*Math.log((double) (state.getGroup()).size())/Math.log(2.0);
							schedule("adHocStatisticalAnalyzerActionMembershipChangeProcessed", commTime1+commTime2);

							// simulate communication time needed for this peer to communicate with the lead for membership change
							waitForTime(commTime1);
							leader.actionLeadMembershipChange();

						}
					}

					if ((state.getGroupLeader()).getName() == this.getName()) info("** I am leader " + this.getName()+ ": Time Elapsed is " + timeElapsed);

					if (wr.triggerOccurred()) { // I must be the lead since only lead receives triggers, so propapage membership or data change
														 if (wr.getTrigger() == membershipChanged) {
													 					 info("At " + wr.returnTime() + " Membership Update Occurred");

													 					 // simulate time needed for the lead to propagate membership change to members via multicast tree
													 					 double commTime = TAU*Math.log((double) (state.getGroup()).size())/Math.log(2.0);
													 					 waitForTime(commTime);

													 	 				 membershipChangedFlag = false;
																	 }
														 // not used
														 /************************************************************************************************
														 else if (wr.getTrigger() == dataChanged) {
													 					 info("At " + wr.returnTime() + " Data Update Occurred");
													 					 dataChangedFlag = false;
																	 }
														*************************************************************************************************/
														 else if (wr.getTrigger() == failureDetected) {
													 					 info("At " + wr.returnTime() + " Member Failure Occurred");

													 					 // simulate time needed for the lead to propagate membership change to members via multicast tree
													 					 double commTime = TAU*Math.log((double) (state.getGroup()).size())/Math.log(2.0);
													 					 waitForTime(commTime);

													 	 				 failureDetectedFlag = false;
																	 }
														 else info("At " + wr.returnTime() + " Unknown Trigger Occurred");

			 			 }

					if (timeToFailure <= timeStampNow)  {
					   // this peer has failed, mark the peer as failed
					   state.putEntityFailedState(this);

					   AdHocPeer leader = state.getGroupLeader();
						if (leader.getName() != NONEXIST_ID) {

							 // simulate the beacon interval after which the failure will be detected by the lead
								schedule("adHocStatisticalAnalyzerActionMemberFailureEvent", 0.0);
								schedule("adHocStatisticalAnalyzerActionMemberFailureDetected", beaconT);

								double commTime = TAU*Math.log((double) (state.getGroup()).size())/Math.log(2.0);
								schedule("adHocStatisticalAnalyzerActionMemberFailureDetectedProcessed", beaconT+commTime);

								waitForTime(beaconT);
								leader.actionLeadMemberFailureDetected();


							}
						 break; // break out the while loop and exit
				   }

				}  // while(true)
	}  // agenda ends


	public void adHocStatisticalAnalyzerActionMembershipChange()	{
		AdHocStatisticalAnalyzer.actionMembershipChange();
	}
	public void adHocStatisticalAnalyzerActionMembershipChangeProcessed() {
		AdHocStatisticalAnalyzer.actionMembershipChangeProcessed();
	}
	public void adHocStatisticalAnalyzerActionMemberFailureEvent() {
			AdHocStatisticalAnalyzer.actionMemberFailureEvent();
	}
	public void adHocStatisticalAnalyzerActionMemberFailureDetected() {
			AdHocStatisticalAnalyzer.actionMemberFailureDetected();
	}
	public void adHocStatisticalAnalyzerActionMemberFailureDetectedProcessed() {
			AdHocStatisticalAnalyzer.actionMemberFailureDetectedProcessed();
	}

	public boolean insideCombatArea() {
		DistFromGridCenterAdHocSensor s = new DistFromGridCenterAdHocSensor(this);
		return s.insideCombatArea();
	}

	public boolean insideGroupArea() {
			DistFromGridCenterAdHocSensor s = new DistFromGridCenterAdHocSensor(this);
			return s.insideGroupArea();
	}

	public boolean alive(){
			AdHocEffector testCommsAdHocEffector = new AdHocEffector();
			AdHocTestCommsState state = (AdHocTestCommsState)testCommsAdHocEffector.theEnvState;
			return state.getEntityAliveState(this);
	}

	public synchronized boolean membershipChangeDueToMobility(Object entity, Object delta) {
			AdHocEffector testCommsAdHocEffector = new AdHocEffector();
			AdHocTestCommsState state = ((AdHocTestCommsState)testCommsAdHocEffector.theEnvState);
			HashMap entityStates = state.getEntityStates();

			Polygon groupPolygon = state.getGroupPolygon();

			Point2D.Double currentState = (Point2D.Double) entityStates.get(entity);
			Point2D.Double d = (Point2D.Double) delta;
			double x = currentState.getX();
			double y = currentState.getY();
			double dx = d.getX();
			double dy = d.getY();

			boolean lastIn = groupPolygon.contains(x, y);
			boolean currIn = groupPolygon.contains(x+dx, y+dy);

			// if boundary is crossed, then return true to group membership change
			if (( lastIn && !currIn) || (!lastIn && currIn))  return (true);
			else return (false);
   }
}

class DistFromGridCenterAdHocSensor extends AdHocSensor implements Common{
    AdHocPeer self;
    AdHocEffector testCommsAdHocEffector = new AdHocEffector();
    AdHocTestCommsState state;

    public DistFromGridCenterAdHocSensor(AdHocPeer a) {	self = a; }

    public boolean insideCombatArea() {
		state = ((AdHocTestCommsState)testCommsAdHocEffector.theEnvState);
		HashMap entityStates = state.getEntityStates();

		Point2D.Double myState = (Point2D.Double)entityStates.get(self);

		Polygon combatAreaBoundary = state.getCombatArea();
// if (combatAreaBoundary.contains((int)myState.getX(), (int)myState.getY()))
//		   System.out.println("insideCombatArea: True");
//		else System.out.println("insideCombatArea: False");

		return combatAreaBoundary.contains((int)myState.getX(), (int)myState.getY());
    }

    public boolean insideGroupArea() {
			state = ((AdHocTestCommsState)testCommsAdHocEffector.theEnvState);
			HashMap entityStates = state.getEntityStates();

			Point2D.Double myState = (Point2D.Double)entityStates.get(self);

			Polygon groupAreaBoundary = state.getGroupPolygon();
	// if (groupAreaBoundary.contains((int)myState.getX(), (int)myState.getY()))
	//		   System.out.println("insideGroupArea: True");
	//		else System.out.println("insideGroupArea: False");

			return groupAreaBoundary.contains((int)myState.getX(), (int)myState.getY());
    }

    public boolean acrossGroupPolygon(double newX, double newY) {
		state = ((AdHocTestCommsState)testCommsAdHocEffector.theEnvState);
		HashMap entityStates = state.getEntityStates();
		Point2D.Double myState = (Point2D.Double)entityStates.get(self);
		Polygon groupBoundary = state.getGroupPolygon();
		boolean currIn = groupBoundary.contains((int)myState.getX(), (int)myState.getY());
		boolean lastIn = groupBoundary.contains((int)(myState.getX()+newX), (int)(myState.getX()+newY));
		boolean returnVal = false;
		if ((lastIn && ! currIn) || (! lastIn && currIn))
			returnVal = true;
		return returnVal;
    }
}




