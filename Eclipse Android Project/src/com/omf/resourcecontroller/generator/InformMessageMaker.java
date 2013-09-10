package com.omf.resourcecontroller.generator;

public class InformMessageMaker extends MessageMaker {
	private String iType;
	private String cid;
	
	public InformMessageMaker(String mid, String rid, long ts, String topic) {
		super(mid, rid, ts, topic);
	}

	public String getIType() {
		return iType;
	}

	public void setIType(String iType) {
		this.iType = iType;
	}

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}
	
	
}
