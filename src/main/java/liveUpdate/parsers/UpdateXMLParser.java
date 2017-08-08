/**
 * Written by Periklis Master_ex Ntanasis <pntanasis@gmail.com>
 * http://masterex.github.com/
 * 
 */
package liveUpdate.parsers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import liveUpdate.objects.Modes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import liveUpdate.objects.Instruction;

/**
 *
 * @author Periklis Ntanasis
 */
public class UpdateXMLParser {

    public ArrayList<Instruction> parse(String filename, Modes mode) throws SAXException, FileNotFoundException, IOException, InterruptedException {
        XMLReader reader = XMLReaderFactory.createXMLReader();
        UpdateXMLParserHandler handler = new UpdateXMLParserHandler();
        reader.setContentHandler(handler);
        reader.setErrorHandler(handler);

        if (mode == Modes.FILE) {
            reader.parse(new InputSource(new FileReader(new File(filename))));
        } else {
            java.net.URL u = new java.net.URL(filename);
            java.net.URLConnection conn = u.openConnection();
            java.io.InputStream in = conn.getInputStream();
            reader.parse(new InputSource(in));
        }

        return handler.getInstructions();

    }
}
