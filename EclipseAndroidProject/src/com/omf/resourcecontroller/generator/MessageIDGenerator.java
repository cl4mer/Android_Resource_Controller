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

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class MessageIDGenerator {
	private static String TAG = "MessageIDGenerator";
	
	private static SecureRandom random;
	private static String prefix = null;


	static {
		try {
			random = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			System.err.println(TAG + ": " + "SHA1PRNG not available??");
			//Log.e(TAG, "SHA1PRNG not available??");
			// FIXME Find a better way to terminate
			System.exit(1);
		}
	}
	
	public static void clearPrefix() {
		MessageIDGenerator.prefix = null;
	}

	public static void setPrefix(String prefix) {
		if (MessageIDGenerator.prefix == null)
			MessageIDGenerator.prefix = prefix;
		else if (MessageIDGenerator.prefix != prefix)
			//Log.i(TAG, "Trying to overwrite non-empty message ID generator prefix \""
			//		+ MessageIDGenerator.prefix + "\" with \"" + prefix + "\"");
			System.err.println(TAG + ": " + "Trying to overwrite non-empty message ID generator prefix \""
					+ MessageIDGenerator.prefix + "\" with \"" + prefix + "\"");
	}
	
	public static String nextId() {
		if (MessageIDGenerator.prefix == null)
			setPrefix("(null)");
		int nextId = random.nextInt();
		while (nextId < 0)
			nextId = random.nextInt();
		return prefix + nextId;		
	}
}
