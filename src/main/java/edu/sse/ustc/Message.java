package edu.sse.ustc;

import java.util.*;

public class Message {

    private long timeStamp;

    public Message() {
	timeStamp = System.currentTimeMillis();
    }

    public long getTimeStamp() {
	return timeStamp;
    }

    public void setTimeStamp(long t) {
	timeStamp = t;
    }

}














