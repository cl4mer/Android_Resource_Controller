package com.omf.resourcecontroller.OMF;

public class RecentMessageIds {
	private static final int nRecentMessageIDs = 10;

	private static String[] recentMessageIDs;
	private static int in;
	private static int nItems;
	
	static {
		recentMessageIDs = new String[nRecentMessageIDs];
		in = 0;
		nItems = 0;
	}

	public static boolean isNewId(String messageId) {
		boolean ret = true;
		
		int n = 0;
		int i = (in + nRecentMessageIDs - 1) % nRecentMessageIDs;

		while (n < nItems) {
			if (messageId.equals(recentMessageIDs[i]))	{
				ret = false;
				break;
			}
			i = (i + nRecentMessageIDs - 1) % nRecentMessageIDs;
			n++;
		}

		if (ret)
			addMessageId(messageId);
		
		return ret;
	}

	public static void addMessageId(String messageId) {
		recentMessageIDs[in] = messageId;
		in = (in + 1) % recentMessageIDs.length;
		
		if (nItems < nRecentMessageIDs)
			nItems++;
	}

}