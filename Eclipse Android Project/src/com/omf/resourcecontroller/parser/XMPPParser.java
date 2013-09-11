package com.omf.resourcecontroller.parser;

import java.io.IOException;
import java.io.StringReader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;


import android.util.Log;

import com.omf.resourcecontroller.OMF.OMFMessage;
import com.omf.resourcecontroller.generator.MessageType;

public class XMPPParser {
	public static final String TAG = "XMPPParser";
	
    private enum ParserState {
    	PARSE_MESSAGE_TYPE,
    	PARSE_MESSAGE_DATA,
    	PARSE_PROPS,
    	PARSE_TEXT,
    	PARSE_END_TAG,
    }

    private enum ExpectedText {
    	EXPECT_NONE,
    	EXPECT_INFORM,
    	EXPECT_CREATE,
    	EXPECT_REQUEST,
    	EXPECT_CONFIGURE,
    	EXPECT_RELEASE,
    	EXPECT_SRC,
    	EXPECT_TS,
    	EXPECT_REPLYTO,
    }
    
    private static final String acceptedXMLNs = "http://schema.mytestbed.net/omf/6.0/protocol";
    
    private void failParse(String message) {
		//Log.e(TAG, message);
		// TODO Find a better way to exit
		System.exit(-1);
    }
    
    /**
	 * XML Parser
	 * @param xmlString XML String 
	 * @throws IOException 
	 * @throws XmlPullParserException 
	 * @returns OMFMessage
	 */
	public OMFMessage XMLParse (String xmlString) throws XmlPullParserException, IOException {
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();		
		OMFMessage message = new OMFMessage();
		
		ParserState state = ParserState.PARSE_MESSAGE_TYPE;

		factory.setNamespaceAware(true);
		XmlPullParser xpp = factory.newPullParser();

		//Set input
		xpp.setInput ( new StringReader ( xmlString) );

		String propsNamespace = null;
		String propsNamespaceUrl = null;
		String guardNamespace = null;
		String guardNamespaceUrl = null;
		String propsOrGuardTag = null;
		ExpectedText expectedText = ExpectedText.EXPECT_NONE;

		int eventType = xpp.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			switch (state) {
			case PARSE_MESSAGE_TYPE:
				if (eventType == XmlPullParser.START_TAG) {
					MessageType type = MessageType.fromString(xpp.getName());
					if (type != null)
						message.setMessageType(type);
					else
						failParse("Unknown message type \"" + xpp.getName() + "\"");

					for (int i = 0; i < xpp.getAttributeCount(); i++) {
						String attribute = xpp.getAttributeName(i);
						if (attribute.equalsIgnoreCase("xmlns")) {
							checkProtocolVersion(xpp.getAttributeValue(i));
							if (message.getProtocolId() == null)
								message.setProtocolId(xpp.getAttributeValue(i));
							else
								failParse("Attribute \"xmlns\" set more than once (values \""
										+ message.getProtocolId() + "\" and \"" + xpp.getAttributeValue(i) + ")");
						} else if (attribute.equalsIgnoreCase("mid")) {
							if (message.getMessageId() == null)
								message.setMessageId(xpp.getAttributeValue(i));
							else
								failParse("Attribute \"mid\" set more than once (values \""
										+ message.getMessageId() + "\" and \"" + xpp.getAttributeValue(i) + ")");
						} else
							failParse("Unknown attribute \"" + attribute + "\" for " + xpp.getName() + " message");
					}
				} else
					failParse("Expected start tag, got " + eventType);

				state = ParserState.PARSE_END_TAG;

				if (message.getMessageType() == MessageType.configure())
					expectedText = ExpectedText.EXPECT_CONFIGURE;
				else if (message.getMessageType() == MessageType.create())
					expectedText = ExpectedText.EXPECT_CREATE;
				else if (message.getMessageType() == MessageType.inform())
					expectedText = ExpectedText.EXPECT_INFORM;
				else if (message.getMessageType() == MessageType.release())
					expectedText = ExpectedText.EXPECT_RELEASE;
				else if (message.getMessageType() == MessageType.request())
					expectedText = ExpectedText.EXPECT_REQUEST;
				else
					failParse("Internal error: unknown message type");
				
				break;

			case PARSE_MESSAGE_DATA:
				if (eventType == XmlPullParser.START_TAG) {
					String tag = xpp.getName();

					if (tag.equalsIgnoreCase("props") || tag.equalsIgnoreCase("guard")) {
						propsOrGuardTag = tag;

						String namespace = null;
						String namespaceUrl = null;

						for (int i = 0; i < xpp.getAttributeCount(); i++) {
							if (xpp.getAttributeNamespace(i).equalsIgnoreCase("xmlns")) {
								if (namespace != null || namespaceUrl != null)
									failParse("Already have namespace in element \"" + propsOrGuardTag + "\"");
								else {
									namespace = xpp.getAttributeName(i);
									namespaceUrl = xpp.getAttributeValue(i);
								}
							} else
								failParse("Element \"" + propsOrGuardTag + "\" has attribute \"" 
										+ xpp.getAttributeName(i) + "\"without xmlns namespace");

						}

						if (propsOrGuardTag.equalsIgnoreCase("props")) {
							propsNamespace = namespace;
							propsNamespaceUrl = namespaceUrl;
						} else {
							guardNamespace = namespace;
							guardNamespaceUrl = namespaceUrl;
						}
						state = ParserState.PARSE_PROPS;
					} else if (tag.equalsIgnoreCase("src")) {
						expectedText = ExpectedText.EXPECT_SRC;
						state = ParserState.PARSE_TEXT;
					} else if (tag.equalsIgnoreCase("ts")) {
						expectedText = ExpectedText.EXPECT_TS;
						state = ParserState.PARSE_TEXT;
					} else if (tag.equalsIgnoreCase("replyto")) {
						expectedText = ExpectedText.EXPECT_REPLYTO;
						state = ParserState.PARSE_TEXT;
					} else
						failParse("Unknown tag \"" + tag + "\"");
				} else
					failParse("Expected start tag, got " + eventType);
				break;

			case PARSE_TEXT:
				if (eventType == XmlPullParser.TEXT) {
					switch (expectedText) {
					case EXPECT_SRC: message.setSrc(xpp.getText()); break;
					case EXPECT_TS: message.setTs(Long.parseLong(xpp.getText())); break;
					case EXPECT_REPLYTO: message.setTopic(xpp.getText()); break;
					case EXPECT_NONE: failParse("Can't expect NONE"); break;
					}
					state = ParserState.PARSE_END_TAG;
				} else
					failParse("Expected text tag, got " + eventType);
				break;

			case PARSE_END_TAG:
				if (eventType != XmlPullParser.END_TAG)
					failParse("Expected end tag, got " + eventType);
				else {
					switch (expectedText) {
					case EXPECT_SRC: failIfUnequal(xpp.getName(), "src"); state = ParserState.PARSE_MESSAGE_DATA; break;
					case EXPECT_TS: failIfUnequal(xpp.getName(), "ts"); state = ParserState.PARSE_MESSAGE_DATA; break;
					case EXPECT_REPLYTO: failIfUnequal(xpp.getName(), "replyto"); state = ParserState.PARSE_MESSAGE_DATA; break;
					case EXPECT_NONE: failParse("Can't expect NONE"); state = ParserState.PARSE_MESSAGE_DATA; break;
					}
				}
				break;
				
			case PARSE_PROPS:
				if (eventType == XmlPullParser.END_TAG && 
				(xpp.getName().equalsIgnoreCase("props") || xpp.getName().equalsIgnoreCase("guard")))
					state = ParserState.PARSE_MESSAGE_DATA;
				break;
			}
			
			eventType = xpp.next();
		}
		
		return message;
	}

	private void failIfUnequal(String actual, String expected) {
		if (!actual.equalsIgnoreCase(expected))
			failParse("Expected \"" + expected + "\", got \"" + actual + "\"");
	}

	private void checkProtocolVersion(String xmlns) {
		if (!xmlns.equalsIgnoreCase(acceptedXMLNs))
			failParse("Expected \"" + acceptedXMLNs + "\", got \"" + xmlns + "\"");
	}	
	
}
