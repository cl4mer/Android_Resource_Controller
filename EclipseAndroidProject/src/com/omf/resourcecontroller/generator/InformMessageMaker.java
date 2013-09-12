package com.omf.resourcecontroller.generator;

public class InformMessageMaker extends XMLMessageMaker {

	StringBuffer buf;
	
	public InformMessageMaker(String rid, String topic, IType type, String cid) {
		super(MessageType.inform, rid, topic);
		buf = getBuf();
		addItype(type);
		addCid(cid);
	}

	private void addItype(IType type) {
		buf.append("  <itype>").append(type).append("</itype>\n");
	}

	public void addProperties(Properties p) {
		buf.append(p.toString());
	}
	
	
	private void addCid(String cid) {
		if (cid != null)
			buf.append("  <cid>").append(cid).append("</cid>\n");
	}
	
}
