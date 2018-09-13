package edu.sse.ustc;

import org.mitre.sim.api3.*;
import java.applet.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.util.*;
import java.awt.image.*;

public class AdHocSimulation extends Simulation implements Common{

  static Panel p;
  static AdHocAnimationDisplay anim;

  static AdHocSimulation sim;
  static AdHocEnv theEnv;
  boolean started = true;
  boolean finished = false;
  Button startButton = new Button("Start");
  Button stopButton = new Button("Stop");

  static double groupLargestRingNo = GROUP_LARGEST_RING;
  static double combatLargestRingNo = COMBAT_LARGEST_RING;
  static double group_n = GROUP_n;
  static double combatArea_n = COMBAT_AREA_n;
  static double beaconT = BEACON_PERIOD;

  static class WindowCloser extends WindowAdapter {
    public void windowClosing(WindowEvent e) {
      System.exit(0);
    }
  }

  static class Mouser extends MouseAdapter {
      private AdHocEnv env;
      public Mouser(AdHocEnv en) {
	  env = en;
      }
      public void mouseClicked(MouseEvent e) {
	  int x = e.getX();
	  int y = e.getY();
	  env.killClosestPeer(x,y);
	  System.out.println("click");
      }
  }

    public ArrayList getPolygonGroupPoints(double r, double centerX, double centerY) {

		double x, y;
		Point2D pp;
		ArrayList arr = new ArrayList(6);

		for (int i = 0; i < 6; i++){

			x = r * Math.cos(STARTRADIANS + angleSegment * i) + centerX;
			y = r * Math.sin(STARTRADIANS + angleSegment * i) + centerY;

			pp = new Point2D.Double (x, y);
			arr.add(pp);
		}
		return arr;
	}
    public double getW() {
		double STARTANGLE = 0.0;
		double STARTRADIANS = STARTANGLE * Math.PI / 180.0;    // 'Convert to radians
		double x, y;
		double x1, y1;

		x = RADIUS * Math.cos(STARTRADIANS + angleSegment * 1) + FIRST_X;
		x1 = RADIUS * Math.cos(STARTRADIANS + angleSegment * 2) + FIRST_Y;

		return Math.abs(x1-x);
	}
	public double getDy(double w){
		return Math.sin(angleSegment) * w;
	}
	public ArrayList setupCenterPoints(){
        double centerX = FIRST_X;
        double centerY = FIRST_Y;
		double startX = 0.0, startY = 0.0, origX = 0.0, origY = 0.0, endX = 0.0, endY = 0.0;
		double chgY = centerY;
		//==========================================================
		// calculate another center point
		//
		//==========================================================
		double w = getW();
		double dx = 1.5 * w;
		double dy = getDy(w);
		double boundaryRadius = 2 * dy;

		ArrayList centerPts = new ArrayList();

		for (int col = 0; col < GROUP_SIZE; col++){
			centerX = centerX + dx;

			if (col % 2 == 0){
				chgY = centerY;
			}else{
				chgY = centerY + dy;
			}

			for (int row = 0; row < GROUP_SIZE; row++){
				chgY = chgY + boundaryRadius;
				centerPts.add(new Point2D.Double(centerX, chgY));
			}

		}// end for
		return centerPts;
	}
    public ArrayList getHexagonPoints(double centerX, double centerY) {
		double STARTANGLE = 0.0;
		double STARTRADIANS = STARTANGLE * Math.PI / 180.0;    // 'Convert to radians

		double x, y;
		Point2D pp;
		ArrayList arr = new ArrayList(6);

		for (int i = 0; i < 6; i++){

			x = RADIUS * Math.cos(STARTRADIANS + angleSegment * i) + centerX;
			y = RADIUS * Math.sin(STARTRADIANS + angleSegment * i) + centerY;

			pp = new Point2D.Double (x, y);
			arr.add(pp);
		}
		return arr;
	}
	public ArrayList setupHexagonPoints(ArrayList centerPts){
		//==========================================================
		// given a (centerX, centerY), get all points on the hexagon
		//
		//==========================================================
		ArrayList arrList;
		ArrayList hexagonPts = new ArrayList();
		Point2D centerPt;
		double centerX, centerY;

		for (int c = 0; c< centerPts.size(); c++){
			centerPt = (Point2D)centerPts.get(c);
			centerX = centerPt.getX();
			centerY = centerPt.getY();
			arrList= getHexagonPoints(centerX, centerY);
			hexagonPts.add(arrList);
		}
		return hexagonPts;
  }


  public ArrayList createPolygon(double n, double centerOfMassX, double centerOfMassY){
	//=============================================================
	// draw polygon rings
	// n is RING
	// n = 0, ring 0, m = 0
	// n = 1, ring 1, m = 0
	// n = 2, ring 2, m = 1
	// n = b, ring b, m = b - 1;
	//=============================================================
	int m=0;
	if (n > 1)
		m = (int)n - 1;
	int Px[] = {0,0,0,0,0,0};
	int Py[] = {0,0,0,0,0,0};
	double r;

	ArrayList polygonPts;
	Point2D polygonPt;
	Polygon groupPolygon;

	double w = getW();
	double dy = getDy(getW());

	r = 2.0 * dy * n + dy;
	double xi, yi, savedXi = 0.0, savedYi = 0.0, ctXi, ctYi;
	double tempX = w / 4;
	double tempY = dy / 2;
	int block = (int)(2 * n - 1);

	polygonPts = getPolygonGroupPoints(r, centerOfMassX, centerOfMassY);
	groupPolygon = new Polygon();

	ArrayList groupPolygonCenterPts= new ArrayList();

	// clockwise drawing ring points
	// clockwise drawing ring edge center points
	for (int i = 0; i < 6; i++){
		polygonPt = (Point2D)polygonPts.get(i);
		xi = polygonPt.getX();
		yi = polygonPt.getY();

		if (i == 0){ //r-bottom
			groupPolygon.addPoint((int)(xi + tempX), (int)(yi - tempY)); //up
			ctXi = (xi + tempX - RADIUS);
			ctYi = yi-tempY;
			groupPolygonCenterPts.add(new Point2D.Double(ctXi, ctYi)); //center
			for (int u = 0; u < m; u ++){
				ctXi = ctXi - RADIUS - w/2;
				ctYi = ctYi + dy;
				groupPolygonCenterPts.add(new Point2D.Double(ctXi, ctYi)); //edge center points
			}

			savedXi = xi - tempX;
			savedYi = yi + tempY;
			groupPolygon.addPoint((int)savedXi, (int)savedYi);  //down

			for (int b = 1; b <= block; b++){
				if (b % 2 == 1){
					savedXi = savedXi - w;
				}else{
					savedXi = savedXi - w / 2;
					savedYi = savedYi + dy;
				}
				groupPolygon.addPoint((int)savedXi, (int)savedYi);
			}

		}else if (i == 1){ //bottom
			ctXi = xi;
			ctYi = yi-dy;
			groupPolygonCenterPts.add(new Point2D.Double(ctXi, ctYi)); //center
			for (int u = 0; u < m; u ++){
				ctXi = ctXi - RADIUS - w/2;
				ctYi = ctYi - dy;
				groupPolygonCenterPts.add(new Point2D.Double(ctXi, ctYi)); //edge center points
			}


			savedXi = xi + w / 2;
			savedYi = yi;
			groupPolygon.addPoint((int)savedXi, (int)savedYi);  //right

			savedXi = xi - w / 2;
			savedYi = yi;
			groupPolygon.addPoint((int)savedXi, (int)savedYi);  //left
			for (int b = 1; b <= block; b++){
				if (b % 2 == 1){
					savedXi = savedXi - w / 2;
					savedYi = savedYi - dy;
				}else{
					savedXi = savedXi - w;
				}
				groupPolygon.addPoint((int)savedXi, (int)savedYi);
			}

		}else if (i == 2){ //l-bottom, opposite of 5
			groupPolygon.addPoint((int)(xi + tempX), (int)(yi + tempY)); //down

			savedXi = xi - tempX;
			savedYi = yi - tempY;
			groupPolygon.addPoint((int)savedXi, (int)savedYi); //up
			ctXi = (savedXi + RADIUS);
			ctYi = savedYi;
			groupPolygonCenterPts.add(new Point2D.Double(ctXi, ctYi)); //center
			for (int u = 0; u < m; u ++){
				ctYi = ctYi - 2 * dy;
				groupPolygonCenterPts.add(new Point2D.Double(ctXi, ctYi)); //edge center points
			}

			for (int b = 1; b <= block; b++){
				if (b % 2 == 1){
					savedXi = savedXi + w / 2;
				}else{
					savedXi = savedXi - w / 2;
				}
				savedYi = savedYi - dy;
				groupPolygon.addPoint((int)savedXi, (int)savedYi);
			}
		}else if (i == 3){ //l-top, opposite of 0
			groupPolygon.addPoint((int)(xi - tempX), (int)(yi + tempY)); //down
			ctXi = (xi - tempX + RADIUS);
			ctYi = yi+tempY;
			groupPolygonCenterPts.add(new Point2D.Double(ctXi, ctYi)); //center
			for (int u = 0; u < m; u ++){
				ctXi = ctXi + RADIUS + w/2;
				ctYi = ctYi - dy;
				groupPolygonCenterPts.add(new Point2D.Double(ctXi, ctYi)); //edge center points
			}

			savedXi = xi + tempX;
			savedYi = yi - tempY;
			groupPolygon.addPoint((int)savedXi, (int)savedYi); //up
			for (int b = 1; b <= block; b++){
				if (b % 2 == 1){
					savedXi = savedXi + w;
				}else{
					savedXi = savedXi + w / 2;
					savedYi = savedYi - dy;
				}
				groupPolygon.addPoint((int)savedXi, (int)savedYi);
			}
		}else if (i == 4){ //top
			ctXi = xi;
			ctYi = yi+dy;
			groupPolygonCenterPts.add(new Point2D.Double(ctXi, ctYi)); //center
			for (int u = 0; u < m; u ++){
				ctXi = ctXi + RADIUS + w/2;
				ctYi = ctYi + dy;
				groupPolygonCenterPts.add(new Point2D.Double(ctXi, ctYi)); //edge center points
			}

			groupPolygon.addPoint((int)(xi - w / 2), (int)yi); //left
			savedXi = xi + w / 2;
			savedYi = yi;
			groupPolygon.addPoint((int)savedXi, (int)savedYi); //right
			for (int b = 1; b <= block; b++){
				if (b % 2 == 1){
					savedXi = savedXi + w / 2;
					savedYi = savedYi + dy;
				}else{
					savedXi = savedXi + w;
				}
				groupPolygon.addPoint((int)savedXi, (int)savedYi);
			}
		}else if (i == 5){ //r-top
			groupPolygon.addPoint((int)(xi - tempX), (int)(yi - tempY)); //up

			savedXi = xi + tempX;
			savedYi = yi + tempY;
			groupPolygon.addPoint((int)savedXi, (int)savedYi); //down
			ctXi = (savedXi - RADIUS);
			ctYi = savedYi;
			groupPolygonCenterPts.add(new Point2D.Double(ctXi, ctYi)); //center
			for (int u = 0; u < m; u ++){
				ctYi = ctYi + 2 * dy;
				groupPolygonCenterPts.add(new Point2D.Double(ctXi, ctYi)); //edge center points
			}

			for (int b = 1; b <= block; b++){
				if (b % 2 == 1){
					savedXi = savedXi - w / 2;
					savedYi = savedYi + dy;
				}else{
					savedXi = savedXi + w / 2;
					savedYi = savedYi + dy;
				}
				groupPolygon.addPoint((int)savedXi, (int)savedYi);
			}
		}else
			groupPolygon.addPoint((int)xi, (int)yi);
	}
	ArrayList retValues = new ArrayList();
	retValues.add(groupPolygon);
	retValues.add(groupPolygonCenterPts);
//	return groupPolygon;
	return retValues;
  }
  public ArrayList populateAgents(ArrayList ringInfoList, double M0, double b){
	  ArrayList agentLocs = new ArrayList();
	  Point2D ctLoc;
	  Random r = new Random();
	  int agentCount = 0, sign = -1;
	  int hexagonsOnTheRing = 1;
	  ArrayList arrList = new ArrayList();
	  double Mi = M0, chance, x, y;
	  double w = getW();
	  double dy = getDy(getW());


	  for (int i = 0; i < ringInfoList.size(); i++){
		  arrList = (ArrayList)ringInfoList.get(i);
		  hexagonsOnTheRing = arrList.size();

		  agentCount = 0;
		  System.out.println("==========ring "+i);
		  for (int j = 0; j < hexagonsOnTheRing; j++){
			  if (i > 0)
			  	Mi = M0 / (Math.pow(b, i));
			  if (Mi > 1) // density per hexagon is greater than 1
			  do {
				  Mi--;
				  agentCount++;
				  sign = -1 * sign;
				  ctLoc = (Point2D)arrList.get(j);
				   x = ctLoc.getX() + sign * r.nextDouble()*RADIUS;
				   y = ctLoc.getY() + sign * r.nextDouble()*RADIUS;
				  agentLocs.add(new Point2D.Double(x, y));
				  System.out.println(agentCount+" ("+(int)ctLoc.getX()+", "+(int)ctLoc.getY()+") (" +(int)x+", "+(int)y+")");
			  } while (Mi > 1);
			  // the density is less than or equal to 1, so toss a dice to determine if a peer should exist in this hexagon
			  chance = r.nextDouble();
			  // System.out.println(chance+" and "+Mi);
			  if (chance < Mi){
				  agentCount++;
				  sign = -1 * sign;
				  ctLoc = (Point2D)arrList.get(j);
	  			  x = ctLoc.getX() + sign * r.nextDouble()*RADIUS;
				  y = ctLoc.getY() + sign * r.nextDouble()*RADIUS;
				  agentLocs.add(new Point2D.Double(x, y));
				  System.out.println(agentCount+" ("+(int)ctLoc.getX()+", "+(int)ctLoc.getY()+") (" +(int)x+", "+(int)y+")");
			  }
		  }
	  }
	  return agentLocs;
  }



  public void initialize() {
    setPace(1); // a simulation time unit is 1 millisecond in real time
    setTimeLast(TOTAL_SIMULATION_PERIOD); // simulation ends after TOTAL_SIMULATION_PERIOD is elapsed

    p = new Panel();
    p.add(startButton);
    p.add(stopButton);
    p.setBackground(Color.black);

    startButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	started = true;
	finished = false;
	stopButton.setEnabled(true);
	startButton.setEnabled(false);
	sim.resumeSimulation(); // resume simulation
      }});

    stopButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        started = false;
        finished = true;
	stopButton.setEnabled(false);
	startButton.setEnabled(true);
	sim.pauseSimulation(); //pause simulation
      }});

    stopButton.setEnabled(true);
    startButton.setEnabled(false);

    Frame f = new Frame(LAYOUT_NAME);

 	// ========================================================
	// add the environment to the simulation.
  	// ========================================================

	// ========================================================
	// create each hexagon
	// ========================================================
	ArrayList centers = sim.setupCenterPoints();
	ArrayList hexagons = sim.setupHexagonPoints(centers);
	//	int centerOfMass_Index = (int)Math.floor(centers.size() / 2 + GROUP_SIZE / 2);
	int centerOfMass_Index = (int)Math.floor(centers.size() / 2 - 1);
	Point2D pt = (Point2D)centers.get(centerOfMass_Index);
	double centerOfMass_X = pt.getX();
	double centerOfMass_Y = pt.getY();

	// ========================================================
	// create peers
	// ========================================================
	double xpt, ypt;
	int randx, randy, peerNum;

	Point2D pp;
	AdHocPeer peer;



	//========================================================
	//
	// add my data structure (Below)
	//========================================================
	double n = 0.0;
	ArrayList retList;
	Polygon ring;
	ArrayList ringsArrList = new ArrayList();
	ArrayList ringCentersArrList = new ArrayList();
	for (int r = 0; r < groupLargestRingNo+1; r++){
		retList = sim.createPolygon(n, centerOfMass_X, centerOfMass_Y);
		ring = (Polygon)retList.get(0);
		ringsArrList.add(ring);
		n++;
	}
	// ring 0, hexagon = 1;
	// ring 1, hexagon = 6;
	ArrayList ringInfoList = new ArrayList();
	ArrayList tArrList = new ArrayList();
	tArrList.add(new Point2D.Double(centerOfMass_X, centerOfMass_Y));
	ringInfoList.add(tArrList);
	for (int r = 1; r < groupLargestRingNo; r++){
		retList=sim.createPolygon(r, centerOfMass_X, centerOfMass_Y);
		tArrList = (ArrayList)retList.get(1);
	    ringInfoList.add(tArrList);
	}

	// retList[0, 1] = [group/combatArea Polygon, centerPts]
	retList=sim.createPolygon(groupLargestRingNo, centerOfMass_X, centerOfMass_Y);

	Polygon group = (Polygon)retList.get(0);
	ringCentersArrList = (ArrayList)retList.get(1);
    ringInfoList.add(ringCentersArrList);

	retList = sim.createPolygon(combatLargestRingNo, centerOfMass_X, centerOfMass_Y);

	Polygon combatArea = (Polygon)retList.get(0);
	ArrayList temp=new ArrayList();
	for (int r = 0; r < ringInfoList.size()-1; r++){
		temp = (ArrayList)ringInfoList.get(r);
	    System.out.println(temp.size()+"===="+r);
	}
	ArrayList agentInfoList = sim.populateAgents(ringInfoList, M0, B); // based on the inhomogeneous population model

    AdHocTestCommsState stat = new AdHocTestCommsState(group, combatArea, hexagons, ringsArrList, agentInfoList, ringInfoList);
    theEnv.setState(stat);

	ArrayList arr;
    for (int i=0; i<agentInfoList.size(); i++) {
		peer = (AdHocPeer) register(new AdHocPeer(String.valueOf(i), MOBILITY_RATE, DATA_SENSING_RATE, FAILURE_RATE, beaconT)); // id, data sensing rate, failure rate
		peer.setName(""+i);
//		System.out.println(peer.getName());
		theEnv.embed(peer, (Point2D.Double)agentInfoList.get(i));
	}

	// register peer manager for membership and lead maintenance
		register (new AdHocPeerManager("Ad Hoc Peer Manager"));

		// register statistical analyzer to collect data
	register(new AdHocStatisticalAnalyzer("analyzer.data", AGGREGATE_DATA_SENSING_RATE, (double) agentInfoList.size(), group_n, beaconT));

	//========================================================
	// (Above)
	//========================================================

    anim = new AdHocAnimationDisplay(theEnv);
    register(new AdHocAnimationDisplayEntity("Animation Display", 30, anim));
    f.add(p, BorderLayout.SOUTH);
    // anim.addMouseListener(new Mouser(theEnv));
    f.add(anim, BorderLayout.CENTER);
    f.setSize(LAYOUT_WIDTH, LAYOUT_HEIGHT);
    // f.setPreferedSize(LAYOUT_WIDTH, LAYOUT_HEIGHT);
    f.addWindowListener(new WindowCloser());
    f.setVisible(true);
//System.exit(0);
  }

public static void main(String[] args) { // args[0] = n, args[1] = T if args.length >= 2
    theEnv = new AdHocEnv();
    sim = new AdHocSimulation();
    if (args.length >= 1) {
    		group_n = Double.valueOf(args[0]).doubleValue();
    		groupLargestRingNo = group_n - 1;
    		combatLargestRingNo = groupLargestRingNo + 1;
    		combatArea_n = combatLargestRingNo + 1;
		}
	if (args.length >= 2)
			beaconT = Double.valueOf(args[1]).doubleValue();

    sim.setVisible(true);
    sim.start();

  }


}




