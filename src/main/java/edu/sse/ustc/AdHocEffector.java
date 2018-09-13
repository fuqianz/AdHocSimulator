package edu.sse.ustc;

public class AdHocEffector {

    protected static AdHocEnvState theEnvState;

    public static void registerAdHocEnvState(AdHocEnvState es) {
    	theEnvState = es;
    }

}

