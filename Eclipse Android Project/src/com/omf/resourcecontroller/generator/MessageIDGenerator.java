package com.omf.resourcecontroller.generator;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import android.util.Log;

public class MessageIDGenerator {
	private static String TAG = "MessageIDGenerator";
	
	private static SecureRandom random;
	private static String prefix = null;


	static {
		try {
			random = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			Log.e(TAG, "SHA1PRNG not available??");
			// FIXME Find a better way to terminate
			System.exit(1);
		}
	}
	
	public static void setPrefix(String prefix) {
		if (MessageIDGenerator.prefix == null)
			MessageIDGenerator.prefix = prefix;
		else if (MessageIDGenerator.prefix != prefix)
			Log.i(TAG, "Trying to pverwrite non-empty message ID generator prefix \""
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
