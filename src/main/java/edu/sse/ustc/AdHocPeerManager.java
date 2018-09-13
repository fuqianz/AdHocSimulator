package edu.sse.ustc;

import org.mitre.sim.api3.*;
import java.util.Random;
import java.util.Iterator;

public class AdHocPeerManager extends Entity implements Common{

	private Population allPeerSet, groupPeerSet, aliveGroupPeerSet;
	private AdHocPeer groupLeader;

	public AdHocPeerManager(String name){
		super(name);
	}

	public void agenda () {
		AdHocEffector testCommsAdHocEffector = new AdHocEffector();
		AdHocTestCommsState state = ((AdHocTestCommsState)testCommsAdHocEffector.theEnvState);

		allPeerSet = createPopulation(AdHocPeer.class);
		groupPeerSet = allPeerSet.applyFilter(new groupPeerFilter());
		aliveGroupPeerSet = groupPeerSet.applyFilter(new aliveGroupPeerFilter());

		Trigger noGroup = aliveGroupPeerSet.sizeBelow(1);
		Trigger groupTooSmall = aliveGroupPeerSet.sizeBelow(15);
    	Trigger groupTooBig = aliveGroupPeerSet.sizeExceeds(25); //testing
    	Trigger groupLeaderMoved = new Trigger() {
			public boolean condition() {
				return (!aliveGroupPeerSet.contains((Entity)groupLeader));
			}
		};

		while (true) {

		groupLeader = getGroupLeader();
		state.setGroupLeader(groupLeader);
		state.setGroup(aliveGroupPeerSet);
		info("Leader is " + groupLeader.getName() + " in a group of size " + aliveGroupPeerSet.size());
		waitForTime(100); // time interval to check membership
		WaitResult wr = waitForActionOrTrigger(groupLeaderMoved, noGroup);

		 if (wr.triggerOccurred() && wr.getTrigger() == groupLeaderMoved) {
						groupLeader = getGroupLeader();
						state.setGroupLeader(groupLeader);
						state.setGroup(aliveGroupPeerSet);
						if (groupLeader.getName().compareTo(NONEXIST_ID) !=0)
						   info("Leader Change Detected: New leader is " + groupLeader.getName());
						else
							 {
								 info("No peer inside the group at this moment to be the leader");
								 stopSimulation();
							  }

			}
		else if (wr.triggerOccurred() && wr.getTrigger() == noGroup) {
				        info("Group size zero");
				        stopSimulation();
              }
		 else if (wr.triggerOccurred() && wr.getTrigger() == groupTooSmall) {
				        info("Below 15");
              }
         else if (wr.triggerOccurred() && wr.getTrigger() == groupTooBig) {
		 				        info("Above 25");
              }

			}  // while(true)
	}  // agenda ends



	public AdHocPeer getGroupLeader() {
		String smallestName = new String(NONEXIST_ID);
		AdHocPeer current;
		AdHocPeer lead = new AdHocPeer(smallestName);
		Iterator iter = aliveGroupPeerSet.iterator();

		while (iter.hasNext()) {
			current = (AdHocPeer)iter.next();
			if (current.getName().compareTo(smallestName) < 0) {
				smallestName = current.getName();
				lead = current;
			}
		} // end while
		return lead;
	}

}

class groupPeerFilter implements Filter {
	public boolean passesFilter(Entity e) {
		return ((AdHocPeer)e).insideGroupArea();
	}
}

class aliveGroupPeerFilter implements Filter {
	public boolean passesFilter(Entity e) {
		return ((AdHocPeer)e).alive();
	}
}





