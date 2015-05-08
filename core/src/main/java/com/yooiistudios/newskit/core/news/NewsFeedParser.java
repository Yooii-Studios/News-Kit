package com.yooiistudios.newskit.core.news;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 16.
 *
 * NLNewsFeedParseHandler 클래스를 사용해 파싱 결과를 리턴하는 인터페이스 격의 클래스.
 */
public class NewsFeedParser {
    public static NewsFeed read(InputStream inputStream, String encoding) throws SAXException, IOException {
        try {
            XMLReader reader = createXMLReader();
            NewsFeedParseHandler handler = new NewsFeedParseHandler();
            reader.setContentHandler(handler);

            InputSource input = createInputSource(inputStream, encoding);
            try {
                reader.parse(input);
            } catch(SAXException e) {
                throwIfNotBreakParsing(e);
            }

            return handler.getResult();
        } catch (ParserConfigurationException e) {
            throw new SAXException();
        }
    }

    private static void throwIfNotBreakParsing(SAXException e) throws SAXException {
        boolean isBreakParsing = e.getCause() instanceof NewsFeedParseHandler.BreakParsingException;
        if (!isBreakParsing) {
            throw e;
        }
    }

    private static InputSource createInputSource(InputStream inputStream, String encoding) throws UnsupportedEncodingException {
        InputSource input;
        if (encoding.length() > 0) {
            input = new InputSource(new BufferedReader(new InputStreamReader(inputStream, encoding)));
        } else {
            input = new InputSource(new BufferedReader(new InputStreamReader(inputStream)));
            input.setEncoding(encoding);
        }
        return input;
    }

    private static XMLReader createXMLReader() throws ParserConfigurationException, SAXException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        return parser.getXMLReader();
    }
}
