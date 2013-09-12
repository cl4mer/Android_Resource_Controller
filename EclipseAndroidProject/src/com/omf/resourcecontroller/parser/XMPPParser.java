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
    	EXPECT_ITYPE,
    	EXPECT_RTYPE,
    	EXPECT_CID,
    }
    
    private static final String acceptedXMLNs = "http://schema.mytestbed.net/omf/6.0/protocol";
    
    private void failParse(String message) {
		//Log.e(TAG, message);
		System.err.println(TAG + ": " + message);
		// TODO Find a better way to exit
		System.exit(-1);
    }
    @SuppressWarnings("unused")
    private String parseStateToString(ParserState s) {
    	switch (s) {
    	case PARSE_MESSAGE_TYPE: return "PARSE_MESSAGE_TYPE";
    	case PARSE_MESSAGE_DATA: return "PARSE_MESSAGE_DATA";
    	case PARSE_PROPS: return "PARSE_PROPS";
    	case PARSE_TEXT: return "PARSE_TEXT";
    	case PARSE_END_TAG: return "PARSE_END_TAG";
    	}
    	return "PARSE_UNKNOWN";
    }
    
    private String eventTypeToString(int eventType) {
    	switch (eventType) {
    	case XmlPullParser.CDSECT: return "CDSECT";
    	case XmlPullParser.COMMENT: return "COMMENT";
    	case XmlPullParser.DOCDECL: return "DOCDECL";
    	case XmlPullParser.END_DOCUMENT: return "END_DOCUMENT";
    	case XmlPullParser.END_TAG: return "END_TAG";
    	case XmlPullParser.ENTITY_REF: return "ENTITY_REF";
    	case XmlPullParser.IGNORABLE_WHITESPACE: return "IGNORABLE_WHITESPACE";
    	case XmlPullParser.START_DOCUMENT: return "START_DOCUMENT";
    	case XmlPullParser.START_TAG: return "START_TAG";
    	case XmlPullParser.TEXT: return "TEXT";
    	}
    	return Integer.toString(eventType);
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
		
		System.out.println(xmlString);
		
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
			
			if (eventType == XmlPullParser.START_TAG)
				System.err.println(parseStateToString(state) + ": <" + xpp.getName() + ">");
			else if (eventType == XmlPullParser.END_TAG)
				System.err.println(parseStateToString(state) + ": </" + xpp.getName() + ">");
			else
				System.err.println(parseStateToString(state) + ": " + eventTypeToString(eventType));
			
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
					
					if (message.getMessageType() == MessageType.configure)
						expectedText = ExpectedText.EXPECT_CONFIGURE;
					else if (message.getMessageType() == MessageType.create)
						expectedText = ExpectedText.EXPECT_CREATE;
					else if (message.getMessageType() == MessageType.inform)
						expectedText = ExpectedText.EXPECT_INFORM;
					else if (message.getMessageType() == MessageType.release)
						expectedText = ExpectedText.EXPECT_RELEASE;
					else if (message.getMessageType() == MessageType.request)
						expectedText = ExpectedText.EXPECT_REQUEST;
					else
						failParse("Internal error: unknown message type " + message.getMessageType());
					
					state = ParserState.PARSE_MESSAGE_DATA;
				}
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
					} else if (tag.equalsIgnoreCase("itype")) {
						if (message.getMessageType() != MessageType.inform)
							failParse("Found <itype> in " + message.getMessageType() + " message");
						expectedText = ExpectedText.EXPECT_ITYPE;
						state = ParserState.PARSE_TEXT;
					} else if (tag.equalsIgnoreCase("rtype")) {
						if (message.getMessageType() != MessageType.create)
							failParse("Found <rtype> in " + message.getMessageType() + " message");
						expectedText = ExpectedText.EXPECT_RTYPE;
						state = ParserState.PARSE_TEXT;
					} else if (tag.equalsIgnoreCase("cid")) {
						if (message.getMessageType() != MessageType.inform)
							failParse("Found <itype> in " + message.getMessageType() + " message");
						expectedText = ExpectedText.EXPECT_CID;
						state = ParserState.PARSE_TEXT;
					} else
						failParse("Unknown tag \"" + tag + "\"");
				}
				break;

			case PARSE_TEXT:
				if (eventType == XmlPullParser.TEXT) {
					switch (expectedText) {
					case EXPECT_SRC: message.setSrc(xpp.getText()); break;
					case EXPECT_TS: message.setTs(Long.parseLong(xpp.getText())); break;
					case EXPECT_REPLYTO: message.setTopic(xpp.getText()); break;
					case EXPECT_ITYPE: message.setItype(xpp.getText()); break;
					case EXPECT_RTYPE: message.setRtype(xpp.getText()); break;
					case EXPECT_CID: message.setCid(xpp.getText()); break;
					case EXPECT_NONE: failParse("Can't expect NONE"); break;
					default:
						failParse("Internal error: forgot a case label in PARSE_TEXT");
					}
					state = ParserState.PARSE_END_TAG;
				} else
					failParse("Expected text tag, got " + eventTypeToString(eventType));
				break;

			case PARSE_END_TAG:
				if (eventType != XmlPullParser.END_TAG)
					failParse("Expected end tag, got " + eventTypeToString(eventType));
				else {
					switch (expectedText) {
					case EXPECT_SRC: failIfUnequal(xpp.getName(), "src"); state = ParserState.PARSE_MESSAGE_DATA; break;
					case EXPECT_TS: failIfUnequal(xpp.getName(), "ts"); state = ParserState.PARSE_MESSAGE_DATA; break;
					case EXPECT_REPLYTO: failIfUnequal(xpp.getName(), "replyto"); state = ParserState.PARSE_MESSAGE_DATA; break;
					case EXPECT_ITYPE: failIfUnequal(xpp.getName(), "itype"); state = ParserState.PARSE_MESSAGE_DATA; break;
					case EXPECT_RTYPE: failIfUnequal(xpp.getName(), "rtype"); state = ParserState.PARSE_MESSAGE_DATA; break;
					case EXPECT_CID: failIfUnequal(xpp.getName(), "cid"); state = ParserState.PARSE_MESSAGE_DATA; break;
					case EXPECT_NONE: failParse("Can't expect NONE"); state = ParserState.PARSE_MESSAGE_DATA; break;
					default:
						failParse("Internal error: forgot a case label in PARSE_END_TAG");
					}
				}
				break;
				
			case PARSE_PROPS:
				// FIXME This code just skips over props
				if (eventType == XmlPullParser.END_TAG 
					&& (xpp.getName().equalsIgnoreCase("props") || xpp.getName().equalsIgnoreCase("guard"))) {
					if (!propsOrGuardTag.equalsIgnoreCase(xpp.getName()))
						// Shouldn't happen since the parser should already catch this, but
						// it doesn't hurt to check anyway.  Also note that <props> and <guard>
						// cannot be nested (otherwise this code would be wrong).
						failParse("<" + propsOrGuardTag + "> ended with </" + xpp.getName() + ">");
					state = ParserState.PARSE_MESSAGE_DATA;
				}
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
