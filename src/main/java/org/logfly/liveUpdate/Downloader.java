/**
 * Written by Periklis Master_ex Ntanasis <pntanasis@gmail.com>
 * http://masterex.github.com/
 */
package org.logfly.liveUpdate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Iterator;

import org.logfly.liveUpdate.objects.Modes;
import org.logfly.liveUpdate.parsers.DownloaderXMLParser;
import org.xml.sax.SAXException;

/**
 *
 * @author Periklis Ntanasis
 */
public class Downloader {

    public void download(String filesxml, String destinationdir, Modes mode) throws SAXException,
            FileNotFoundException, IOException, InterruptedException {

        DownloaderXMLParser parser = new DownloaderXMLParser();
        Iterator iterator = parser.parse(filesxml, mode).iterator();
        java.net.URL url;

        File dir = new File(destinationdir);
        if (!dir.exists()) {
            dir.mkdir();
        }

        while (iterator.hasNext()) {
            url = new java.net.URL((String) iterator.next());
            wget(url, destinationdir + File.separator + new File(url.getFile()).getName());
        }

    }

    private void wget(java.net.URL url, String destination) throws MalformedURLException, IOException {
        java.net.URLConnection conn = url.openConnection();
        java.io.InputStream in = conn.getInputStream();

        File dstfile = new File(destination);
        OutputStream out = new FileOutputStream(dstfile);

        byte[] buffer = new byte[512];
        int length;

        while ((length = in.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }

        in.close();
        out.close();
    }
}
