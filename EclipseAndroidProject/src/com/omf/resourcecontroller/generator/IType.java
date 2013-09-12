package com.omf.resourcecontroller.generator;

public class IType {
	public enum ITypes {
		CREATION_OK,
		CREATION_FAILED,
		STATUS,
		RELEASED,
		ERROR,
		WARN,
		NONSTANDARD,
	}

	public static final IType creationOk = new IType(ITypes.CREATION_OK);
	public static final IType creationFailed = new IType(ITypes.CREATION_FAILED);
	public static final IType status = new IType(ITypes.STATUS);
	public static final IType released = new IType(ITypes.RELEASED);
	public static final IType error = new IType(ITypes.ERROR);
	public static final IType warn = new IType(ITypes.WARN);
	
	private ITypes itype;
	private String nonstandardType;
	
	public IType(ITypes itype) {
		this.itype = itype;
		assert (this.itype != ITypes.NONSTANDARD);
		this.nonstandardType = null;
	}
	
	public IType(String s) {
		this.itype = itypeFromString(s);
		if (this.itype == ITypes.NONSTANDARD)
			this.nonstandardType = s;
		else
			this.nonstandardType = null;
	}
	
	private ITypes itypeFromString(String s) {
		if (s.equals("CREATION.OK"))
			return ITypes.CREATION_OK;
		else if (s.equals("CREATION.FAILED"))
			return ITypes.CREATION_FAILED;
		else if (s.equals("STATUS"))
			return ITypes.STATUS;
		else if (s.equals("RELEASED"))
			return ITypes.RELEASED;
		else if (s.equals("ERROR"))
			return ITypes.ERROR;
		else if (s.equals("WARN"))
			return ITypes.WARN;
		else
			return ITypes.NONSTANDARD;
	}

	public String toString() {
		switch(itype) {
		case CREATION_OK: return "CREATION.OK";
		case CREATION_FAILED: return "CREATION.FAILED";
		case STATUS: return "STATUS";
		case RELEASED: return "RELEASED";
		case ERROR: return "ERROR";
		case WARN: return "WARN";
		case NONSTANDARD: return nonstandardType;
		}
		return "unknown";
	}	
}
