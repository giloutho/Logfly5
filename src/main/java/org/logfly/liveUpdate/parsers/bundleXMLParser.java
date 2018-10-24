/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package org.logfly.liveUpdate.parsers;

import java.io.FileNotFoundException;
import java.io.IOException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author gil
 */
public class bundleXMLParser {

    private String winUrl = null;
    private String macUrl = null;
    private String linuxUrl = null;

    public String getWinUrl() {
        return winUrl;
    }

    public String getMacUrl() {
        return macUrl;
    }

    public String getLinuxUrl() {
        return linuxUrl;
    }


     public void parse(String filename) throws SAXException, FileNotFoundException, IOException, InterruptedException {

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            DefaultHandler handler = new DefaultHandler() {
                boolean bwindows = false;
                boolean bmacos = false;
                boolean blinux = false;            

                public void startElement(String uri, String localName,String qName,
                        Attributes attributes) throws SAXException {                 

                    if (qName.equalsIgnoreCase("WINDOWS")) {
                            bwindows = true;
                    }

                    if (qName.equalsIgnoreCase("MACOS")) {
                            bmacos = true;
                    }

                    if (qName.equalsIgnoreCase("LINUX")) {
                            blinux = true;
                    }
                }

                public void characters(char ch[], int start, int length) throws SAXException {
                        if (bwindows) {
                                winUrl = new String(ch, start, length);
                                bwindows = false;
                        }

                        if (bmacos) {
                                macUrl = new String(ch, start, length);
                                bmacos = false;
                        }

                        if (blinux) {
                                linuxUrl = new String(ch, start, length);
                                blinux = false;
                        }
                }
        };

        saxParser.parse(filename, handler);

        } catch (Exception e) {
           e.printStackTrace();
        }                      
     }                      
    
}
