package edu.sse.ustc;

import org.mitre.sim.api3.*;
import java.util.Random;
import java.util.Iterator;
import java.io.*;

public class AdHocStatisticalAnalyzer extends Entity implements Common{


	static boolean membershipEvent= false;
	static boolean dataEvent = false;
	static boolean failureEvent= false;
	static boolean failureEventDetectedAfterBeacon= false;
	static boolean membershipEventProcessed= false;
	static boolean dataEventProcessed = false;
	static boolean failureEventProcessed= false;

	private double lastEventTimeStamp=0;
	private double timeInStateC=0;  // for membership consistency
	private double timeInStateIC=0;
	private double timeInStateICF=0;
	private double timeInStateC1=0; // for data consistency: In state C is the prerequisite
	private int state = STATE_C;

	private double dataSensingRate=DATA_SENSING_RATE; // default
	private double aggregateDataSensingRate=AGGREGATE_DATA_SENSING_RATE; // default is 2 update operations per second
	double numberOfPeers = 30; // 30 is just a place holder
	double groupSize = GROUP_n;
	double beaconT = BEACON_PERIOD;

    private String dataFileName;

	public AdHocStatisticalAnalyzer(String name){
		super(name);

	}

	public AdHocStatisticalAnalyzer(String name, double aggregateDataSensingRate, double numberOfPeers, double n, double beaconT){
			super(name);
			dataFileName = name;
			this.aggregateDataSensingRate = aggregateDataSensingRate;
			this.numberOfPeers = numberOfPeers;
			this.groupSize = n;
			this.beaconT = beaconT;

	}

	public void entityComplete() {

			double PC , PIC, PICF;
			double updateRate = 1.0/(TAU*Math.log(numberOfPeers)/Math.log(2.0));  // Math.log(x)/Math.log(2) gives log_2(x)

			PC = timeInStateC/getTimeNow();
			PIC = timeInStateIC/getTimeNow();
			PICF = timeInStateICF/getTimeNow();

	    	info("***********Statistical Analyzer completed**************");
	    	info("Final results ---  Pm = " + PC);

	    	// calculate Pdm based on Equation 2
	    	info("Final results ---  Pdm = " + PC * updateRate / (updateRate + aggregateDataSensingRate));

	    	// calculate R based on Equation 3

	    	info("Final results --- R = " + ((PC+PICF)/updateRate + 2.0*PIC/updateRate));

	    	// write the output to a file with name OUTPUT_FILE_NAME
	    	try
			  {
	    		PrintWriter out = new PrintWriter(new FileWriter(dataFileName, true));

	    		out.print("n = " + (int) groupSize + "; T= " + beaconT + ":");
	    		out.print(" Pm = " + PC);
				out.print(" Pdm = " + PC * updateRate / (updateRate + aggregateDataSensingRate));
	    		out.println(" R = " + ((PC+PICF)/updateRate + 2.0*PIC/updateRate));

	    		out.close ();

	    		}    catch (Exception e) { e.printStackTrace(); }
	  		}

	Trigger dataEventTriggered = new Trigger() {
								public boolean condition() {
									return (dataEvent == true);
								}
					};
	Trigger failureEventTriggered = new Trigger() {
									public boolean condition() {
										return (failureEvent == true);
									}
					};
	Trigger membershipEventDetected = new Trigger() {

								public boolean condition() {
									return (membershipEvent == true);
								}
					};
	Trigger failureEventDetected = new Trigger() {
								public boolean condition() {
									return (failureEventDetectedAfterBeacon == true);
								}
					};
	Trigger membershipEventCompleted = new Trigger() {
								public boolean condition() {
									return (membershipEventProcessed == true);
								}
						};
	Trigger dataEventCompleted = new Trigger() {
									public boolean condition() {
										return (dataEventProcessed== true);
									}
						};
	Trigger failureEventCompleted = new Trigger() {
										public boolean condition() {
											return (failureEventProcessed == true);
										}
					};

	Trigger[]  triggers = {dataEventTriggered,  failureEventTriggered,
											membershipEventDetected,  failureEventDetected,
	 										membershipEventCompleted, dataEventCompleted, failureEventCompleted};

	public static synchronized void actionMembershipChange() {
				//info("I am lead " + this.getName() + " ... updating membership event detected");
				membershipEvent = true;
		}

	public static synchronized void actionDataChange() {
				//info("I am lead " + this.getName() + " ... data propagation event received");
				dataEvent = true;
		}

	public static synchronized void actionMemberFailureEvent() {
				//info("I am lead " + this.getName() + " ... member failure event occurred");
				failureEvent = true;
		}
	public static synchronized void actionMemberFailureDetected() {
				//info("I am lead " + this.getName() + " ... member failure event detected");
				failureEventDetectedAfterBeacon = true;
		}
	public static synchronized void actionMembershipChangeProcessed() {
				//info("I am lead " + this.getName() + " ... updating membership event completed");
				membershipEventProcessed = true;
		}

	public static synchronized void actionDataChangeProcessed() {
			//info("I am lead " + this.getName() + " ... data propagation event coompleted");
				dataEventProcessed = true;
		}

	public static synchronized void actionMemberFailureDetectedProcessed() {
				//info("I am lead " + this.getName() + " ... member failure event completed");
				failureEventProcessed= true;
		}


	public void agenda () {

		double timeElapsed;

		while (true) {

		WaitResult wr = waitForActionOrTrigger(triggers, STATISTICAL_DATA_COLLECTION_PERIOD);
		timeElapsed = getTimeNow() - lastEventTimeStamp;
		lastEventTimeStamp = getTimeNow();

		 if (wr.triggerOccurred()) {
				 if (wr.getTrigger() == membershipEventDetected) { // a boundary crossing has been detected by lead
								 info("SA: At " + wr.returnTime() + " Membership Event detected by the lead");

								 switch (state) {
								 					case STATE_C: timeInStateC += timeElapsed; state = STATE_IC; break;
								 					case STATE_IC: timeInStateIC += timeElapsed; state = STATE_IC; break;
													case STATE_ICF: timeInStateICF += timeElapsed; state = STATE_ICF; break;
								 					default: break;
								 }
								 membershipEvent = false;
							 }


				 else if (wr.getTrigger() == failureEventTriggered) { // a failure has just occurred but not yet detected by lead
								 info("SA: At " + wr.returnTime() + " Member Failure Event started");

								 switch (state) {
								 					case STATE_C: timeInStateC += timeElapsed; state = STATE_ICF; break;
								 					case STATE_IC: timeInStateIC += timeElapsed; state = STATE_ICF; break;
								 					case STATE_ICF: timeInStateICF += timeElapsed; state = STATE_ICF; break;

								 					default: break;
								 }
								 failureEvent = false;
							 }
				 else if (wr.getTrigger() == failureEventDetected) { // a failure has just been detected after a beacon period
								 info("SA: At " + wr.returnTime() + " Member Failure Event detected by the lead");
								 switch (state) {
														case STATE_C: timeInStateC += timeElapsed; state = STATE_IC; break;
														case STATE_IC: timeInStateIC += timeElapsed; state = STATE_IC; break;
														case STATE_ICF: timeInStateICF += timeElapsed; state = STATE_IC; break;

														default: break;
								 }
								 failureEventDetectedAfterBeacon = false;
							 }
				 else if (wr.getTrigger() == membershipEventCompleted) { // lead has just completed an update of membership
								 info("SA: At " + wr.returnTime() + " Membership Event Processed");

								 switch (state) {

								 						case STATE_C: timeInStateC += timeElapsed; state = STATE_C; break;
														case STATE_IC: timeInStateIC += timeElapsed; state = STATE_C; break;
														case STATE_ICF: timeInStateICF += timeElapsed; state = STATE_C; break;

								 						default: break;
								 }
								 membershipEventProcessed = false;
							 }

				 else if (wr.getTrigger() == failureEventCompleted) { // lead has just completed an update of membership
								 info("SA: At " + wr.returnTime() + " Member Failure Event Processed");
								 switch (state) {
								 							case STATE_C: timeInStateC += timeElapsed; state = STATE_C; break;
															case STATE_IC: timeInStateIC += timeElapsed; state = STATE_C; break;
															case STATE_ICF: timeInStateICF += timeElapsed; state = STATE_C; break;

								 							default: break;
								 }
								 failureEventProcessed = false;

							 }
				 else info("SA: At " + wr.returnTime() + " Unknown Trigger Occurred");
			}
			else // state remains the same
			switch (state) {
											 					case STATE_C: timeInStateC += timeElapsed; break;
											 					case STATE_IC: timeInStateIC += timeElapsed; break;

											 					default: break;
								 }
			info("state = " + state + " ; Pm = " + timeInStateC/getTimeNow());
		}  // while(true)
	}  // agenda ends

}




