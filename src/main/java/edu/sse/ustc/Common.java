package edu.sse.ustc;

public interface Common {

	  static final String LAYOUT_NAME = "Ad Hoc Networks Simulation";
	  // marks each grid dimension
	  // ie, w x h = 50 x 100
	  static final double GRID_WIDTH = 50.0;
	  static final double GRID_HEIGHT = 100.0;

	  // marks the boundary of the small grid area
	  // ie, 2x2, grids within small area is LOCAL.
//	  static final double GRID_LOCAL = 4.0;

	  int GRID_DIMENSION = 6;
	  int TOTAL_GRIDS = GRID_DIMENSION  * GRID_DIMENSION;
	  int GRID_DIMENSION_LAYER = GRID_DIMENSION / 2;
	  int GRID_LOCAL_DIMENSION = 4;

	  int INNER_LAYER = 0;
	  int LOCAL_LAYER = 1;
	  int OUTER_LAYER = 2;
	  int INNER_Agents = 4;
	  int LOCAL_Agents = 2;
	  int OUTER_Agents = 1;

//	  String TRACKED_AGENT_NAME = "outer: 2";
	  String TRACKED_AGENT_NAME = "20";
	  String INNER_NAME = "inner: ";
	  String LOCAL_NAME = "local: ";
	  String OUTER_NAME = "outer: ";

	  // marks the center point
	  // ie, (400, 300)
	  double SIM_CENTER_X = 400.0;
	  double SIM_CENTER_Y = 400.0;

	  int GROUP_SIZE = 17;
	  double RADIUS = 30.0;
	  double FIRST_X = 10.0;
	  double FIRST_Y = 10.0;

	  static final int LAYOUT_WIDTH = 600;
	  static final int LAYOUT_HEIGHT = 600;

	  double angleSegment = 2.0 * Math.PI / 6.0;
	  double STARTANGLE = 30.0;
	  double STARTRADIANS = STARTANGLE * Math.PI / 180.0; // Convert to radians

	  //========================================
	  // simulation settings
	  //========================================
	  double TOTAL_SIMULATION_PERIOD = 100000.0; // in ms


	  double GROUP_LARGEST_RING = 3;  // n is one larger than this


	  double COMBAT_LARGEST_RING = GROUP_LARGEST_RING + 1;

	  // group size n
	  double GROUP_n = GROUP_LARGEST_RING + 1;

	  // combat size n
	  double COMBAT_AREA_n = COMBAT_LARGEST_RING + 1;

	  // how often an entity detects its location in msec
	  double LOCATION_UPDATE_TIME_INTERVAL = 50.0;

	  // how often for each peer to send its beacon message
	  double BEACON_PERIOD = 100.0;   // 0.1 second

	  // how often to change direction and speed under the random mobility model
	  double DIRECTION_SPEED_FIXED_INTERVAL = 1000.0; // change in every second

	  String NONEXIST_ID = "99999999999999";

	  int STATE_C = 1;
	  int STATE_IC = 2;
	  int STATE_ICF = 3;
	  int STATE_C1 = 4;

	  double TAU=10.0; // per hop communication delay = 0.01 sec

	  double STATISTICAL_DATA_COLLECTION_PERIOD = 50.0;

	  double MOBILITY_RATE = 0.2 * 2.0*RADIUS/1000.0; // mobility rate is 0.2 hexagon per second per node

	  double DATA_SENSING_RATE = 2.0/1000.0;  // one data sensing event per second per node ** not used **

	  double AGGREGATE_DATA_SENSING_RATE = 2.0/1000.0; // aggregate data sensing rate

	  double FAILURE_RATE = 10.0/3600.0/1000.0;  // failure rate is ten per hour per node

	  double M0 = 4;

	  double B = 4;
	}