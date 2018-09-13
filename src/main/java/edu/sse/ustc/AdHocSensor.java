package edu.sse.ustc;

public class AdHocSensor {

    protected static AdHocEnvState theEnvState;

    public static void registerAdHocEnvState(AdHocEnvState es) {
	theEnvState = es;
    }

}

