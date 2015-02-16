package com.yooiistudios.newsflow.model.news;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 16.
 *
 * NLNewsFeedParseHandler 클래스를 사용해 파싱 결과를 리턴하는 인터페이스 격의 클래스.
 */
public class NewsFeedParser {
//    public static NewsFeed read(URL url) throws SAXException, IOException {
//        return read(url.openStream());
//    }

    public static NewsFeed read(InputStream stream) throws SAXException, IOException {
        try {

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            NewsFeedParseHandler handler = new NewsFeedParseHandler();
            InputSource input = new InputSource(stream);

            reader.setContentHandler(handler);
            reader.parse(input);

            return handler.getResult();
        } catch (ParserConfigurationException e) {
            throw new SAXException();
        }
    }
}
