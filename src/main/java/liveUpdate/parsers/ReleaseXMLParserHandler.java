/**
 * Written by Periklis Master_ex Ntanasis <pntanasis@gmail.com>
 * http://masterex.github.com/.
 */
package liveUpdate.parsers;

import liveUpdate.objects.Release;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Periklis Ntanasis
 */
public class ReleaseXMLParserHandler extends DefaultHandler {

    private String currentelement = "";
    private Release releaseinfo = new Release();

    public ReleaseXMLParserHandler() {
        super();
    }

    @Override
    public void startElement(String uri, String name,
            String qName, Attributes atts) {
        currentelement = qName;
    }

    @Override
    public void characters(char ch[], int start, int length) {
        String value = null;
        if (!currentelement.equals("")) {
            value = String.copyValueOf(ch, start, length).trim();
        }
        if (currentelement.equals("pubDate")) {
            releaseinfo.setPubDate(value);
        } else if (currentelement.equals("pkgver")) {
            releaseinfo.setpkgver(value);
        } else if (currentelement.equals("pkgrel")) {
            releaseinfo.setPkgrel(value);
        } else if (currentelement.equals("severity")) {
            releaseinfo.setseverity(value);
        } else if (currentelement.equals("message")) {
            releaseinfo.setMessage(value);
        }
        currentelement = "";
    }

    public Release getInfo() {
        return releaseinfo;
    }
}
