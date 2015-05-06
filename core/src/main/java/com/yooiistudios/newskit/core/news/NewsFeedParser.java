package com.yooiistudios.newskit.core.news;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 16.
 *
 * NLNewsFeedParseHandler 클래스를 사용해 파싱 결과를 리턴하는 인터페이스 격의 클래스.
 */
public class NewsFeedParser {
    public static NewsFeed read(String content, String encoding) throws SAXException, IOException {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            NewsFeedParseHandler handler = new NewsFeedParseHandler();
            InputSource input = new InputSource(new StringReader(content));
            if (encoding.length() > 0) {
                input.setEncoding(encoding);
            }

            reader.setContentHandler(handler);
            reader.parse(input);

            return handler.getResult();
        } catch (ParserConfigurationException e) {
            throw new SAXException();
        }
    }
}
