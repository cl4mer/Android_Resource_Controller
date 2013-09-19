/* Copyright (c) 2013 ETH Zürich. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of ETH Zürich nor the names of other contributors 
 *      may be used to endorse or promote products derived from this software 
 *      without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT 
 * HOLDERBE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY 
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
