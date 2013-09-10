package com.omf.resourcecontroller.generator;

public class MessageMaker {
	private static final int protocolMajor = 6;
	private static final int protocolMinor = 0;
	
	private String mid;
	private String rid;
	private long ts;
	private String topic;
	
	public MessageMaker(String mid, String rid, long ts, String topic) {
		super();
		this.mid = mid;
		this.rid = rid;
		this.ts = ts;
		this.topic = topic;
	}
	
	public String getMid() {
		return mid;
	}

	public String getRid() {
		return rid;
	}

	public long getTs() {
		return ts;
	}

	public String getTopic() {
		return topic;
	}

	public static int getProtocolMajor() {
		return protocolMajor;
	}

	public static int getProtocolMinor() {
		return protocolMinor;
	}
	
}
