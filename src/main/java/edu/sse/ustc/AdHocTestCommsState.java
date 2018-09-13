package edu.sse.ustc;

import org.mitre.sim.api3.*;
import java.util.*;
import java.awt.*;
import java.awt.geom.*;

public class AdHocTestCommsState extends AdHocEnvState implements Common {

	HashMap entityStates = new HashMap();
	HashMap simDataStates = new HashMap();
	HashMap entityFailedOrAliveStates = new HashMap();
	AdHocPeer groupLeader = new AdHocPeer("new lead");
	Population groupSet;

	Iterator it;

	public AdHocTestCommsState(Vector g) {
		super(g);
	}

	public AdHocTestCommsState(Polygon groupPolygon, Polygon combatArea, ArrayList hexagons, ArrayList rings,
			ArrayList ringCenters, ArrayList ringInfo) {
		super(groupPolygon, combatArea, hexagons, rings, ringCenters, ringInfo);
	}

	public AdHocTestCommsState(Polygon groupPolygon, Polygon combatArea, ArrayList hexagons) {
		super(groupPolygon, combatArea, hexagons);
	}

	final int h = 600;
	int peerDim = 0;
	int peerID;
	double peerX, peerY, xlen, ylen, x, y, deltaX, deltaY, angleRd, lastX, lastY, currX, currY;
	double points;
	double gridStartX, gridStartY, xglen, yglen, dxg, dyg;
	Color c;
	Integer idInt;

	public double numberOfHexagons(Point2D peer1Loc, Point2D peer2Loc) {
		// need to search for 2 peers locations
		double deltaX, deltaY, xpow, ypow;
		deltaX = Math.abs(peer1Loc.getX() - peer2Loc.getX());
		deltaY = Math.abs(peer1Loc.getY() - peer2Loc.getY());
		xpow = Math.pow(deltaX, 2);
		ypow = Math.pow(deltaY, 2);
		return Math.sqrt(xpow + ypow);
	}

	public boolean enterGroupPolygon(Point2D peerLoc) {
		int x = (int) peerLoc.getX();
		int y = (int) peerLoc.getY();
		return groupPolygon.contains(x, y);
	}

	public boolean enterCombatArea(Point2D peerLoc) {
		int x = (int) peerLoc.getX();
		int y = (int) peerLoc.getY();
		return combatArea.contains(x, y);
	}

	public void drawRingInfoCenters(Graphics g) {
		g.setColor(Color.red);
		Point2D pt;
		ArrayList rCenters = new ArrayList();
		for (int r = 0; r < ringInfo.size(); r++) {
			rCenters = (ArrayList) ringInfo.get(r);
			for (int rc = 0; rc < rCenters.size(); rc++) {
				pt = (Point2D) rCenters.get(rc);

				g.drawOval((int) Math.floor(pt.getX()), (int) Math.floor(pt.getY()), 2, 2);
			}
		}
	}

	// this is the agent locations
	public void drawRingCenters(Graphics g) {
		g.setColor(Color.black);
		Point2D pt;
		for (int r = 0; r < ringCenters.size(); r++) {
			pt = (Point2D) ringCenters.get(r);

			g.drawOval((int) Math.floor(pt.getX()), (int) Math.floor(pt.getY()), 2, 2);
		}
	}

	public void drawHexagons(Graphics g) {
		// Graphics2D g = (Graphics2D)g1;
		Point2D pt;
		ArrayList arrList;
		double startX, startY, origX, origY, endX, endY;

		for (int h = 0; h < hexagons.size(); h++) {
			arrList = (ArrayList) hexagons.get(h);

			pt = (Point2D) arrList.get(0);
			startX = pt.getX();
			startY = pt.getY();
			origX = pt.getX();
			origY = pt.getY();

			for (int i = 1; i < arrList.size(); i++) {
				pt = (Point2D) arrList.get(i);
				endX = pt.getX();
				endY = pt.getY();

				// g.setColor(Color.black);
				// g.drawString((new Integer(i)).toString(), (int)startX,
				// (int)startY);

				g.setColor(Color.red);
				g.drawLine((int) startX, (int) startY, (int) endX, (int) endY);

				startX = endX;
				startY = endY;
			}

			// g.setColor(Color.black);
			// g.drawString((new Integer(arrList.size())).toString(),
			// (int)startX, (int)startY);

			g.setColor(Color.red);
			g.drawLine((int) startX, (int) startY, (int) origX, (int) origY);
		}
	}

	public synchronized void draw(Graphics g1) {
		Graphics2D g = (Graphics2D) g1;

		// draw entire background, special stuff
		g.setColor(Color.white);
		g.fillRect(0, 0, LAYOUT_WIDTH, LAYOUT_HEIGHT);
		x = 400.0;
		y = 400.0;
		lastX = 400.0;
		lastY = 400.0;

		// draw group boundary
		g.setColor(Color.gray);
		// g.drawPolygon(groupPolygon);
		g.fillPolygon(groupPolygon);

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

		// draw group boundary
		g.setColor(Color.white);
		g.fillPolygon(combatArea);

		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);

		g.setColor(Color.gray);
		// g.drawPolygon(groupPolygon);
		g.fillPolygon(groupPolygon);

		drawHexagons(g);

		// draw each entity
		it = entityStates.keySet().iterator();
		int num = 0;
		AdHocPeer peer;
		AdHocPeer se;
		Point2D.Double curr;
		// Point2D.Double last;
		while (it.hasNext()) {
			se = (AdHocPeer) it.next();
			curr = (Point2D.Double) entityStates.get(se);
			// last = curr;
			if (se instanceof AdHocPeer) {
				peer = (AdHocPeer) se;

				if (!peer.alive()) { // entity has failed
					g.setColor(Color.yellow);
					peerDim = 14;
				} else if (enterGroupPolygon(curr)) { // inside group
					g.setColor(Color.red);
					peerDim = 14;
				} else { // outside group, inside combat area
					g.setColor(Color.green);
					peerDim = 10;
				}
				g.fillRect((int) Math.floor(curr.getX() - 5), (int) Math.floor(curr.getY() - 5), 14, 14);

				/*
				 * // draw tracking peer number: 0 as BLACK DOT if
				 * (peer.getName().endsWith(TRACKED_AGENT_NAME)){
				 * 
				 */
				// draw the current lead with BLACK DOT
				if (peer.getName().compareTo(groupLeader.getName()) == 0) {
					g.setColor(Color.black);
					// g.drawString(simDataStates.get(peer).toString(),
					// (int)startX, (int)startY);
					g.fillRect((int) Math.floor(curr.getX()), (int) Math.floor(curr.getY()), 8, 8);
				}

				// draw peer border as BLACK
				g.setColor(Color.black);
				g.drawRect((int) Math.floor(curr.getX() - 5), (int) Math.floor(curr.getY() - 5), 14, 14);

				num++;
			}
		} // end while

	}// end draw()

	public synchronized HashMap getEntityStates() {
		return entityStates;
	}

	public synchronized HashMap getSimDataStates() {
		return simDataStates;
	}

	public synchronized HashMap getEntityFailedOrAliveStates() {
		return entityFailedOrAliveStates;
	}

	public synchronized void setGroupLeader(AdHocPeer e) {
		groupLeader = e;
	}

	public synchronized AdHocPeer getGroupLeader() {
		return groupLeader;
	}

	public synchronized String getGroupLeaderName() {
		return groupLeader.getName();
	}

	public synchronized void setGroup(Population g) {
		groupSet = g;
	}

	public synchronized Population getGroup() {
		return groupSet;
	}

	public synchronized void putState(AdHocPeer e, Object es) {
		entityStates.put(e, es);
		simDataStates.put(e, new Integer(0));
		entityFailedOrAliveStates.put(e, new Boolean(true));
	}

	public synchronized void putSimDataState(AdHocPeer e, Object ds) {
		simDataStates.put(e, ds);
	}

	public synchronized void putEntityFailedState(AdHocPeer e) {
		entityFailedOrAliveStates.put(e, new Boolean(false));
	}

	public synchronized void putEntityAliveState(AdHocPeer e) {
		entityFailedOrAliveStates.put(e, new Boolean(true));
	}

	public synchronized boolean getEntityAliveState(AdHocPeer e) {
		return ((Boolean) entityFailedOrAliveStates.get(e)).booleanValue();
	}

	public synchronized void updateLocation(Object entity, Object delta) {
		Point2D.Double currentState = (Point2D.Double) entityStates.get(entity);

		Point2D.Double d = (Point2D.Double) delta;
		double x = currentState.getX();
		double y = currentState.getY();
		double dx = d.getX();
		double dy = d.getY();
		currentState.setLocation(x + dx, y + dy);
	}

	public synchronized void updateDataState(Object entity) {
		Integer mobileStates = (Integer) simDataStates.get(entity);
		int val = mobileStates.intValue() + 1;

		mobileStates = new Integer(val);
		simDataStates.put(entity, mobileStates);
	}

	public void killClosestPeer(double x, double y) {
		Iterator it = entityStates.keySet().iterator();
		double closestDistance = 1E20;
		double d, xa, ya, dx, dy;
		AdHocTestCommsPeer closest = null;
		while (it.hasNext()) {
			AdHocPeer e = (AdHocPeer) it.next();
			if (e instanceof TargetPeer)
				continue;
			AdHocTestCommsPeer a = (AdHocTestCommsPeer) e;
			if (a.isDead())
				continue;
			xa = ((Point2D.Double) entityStates.get(a)).getX();
			ya = ((Point2D.Double) entityStates.get(a)).getY();
			dx = x - xa;
			dy = (h - y) - ya;
			d = Math.sqrt(dx * dx + dy * dy);
			if (d < closestDistance) {
				closestDistance = d;
				closest = a;
			}
		}
		if (closestDistance < 10.0)
			closest.die();
	}

}
