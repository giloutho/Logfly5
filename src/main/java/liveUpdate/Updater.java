/**
 * Written by Periklis Master_ex Ntanasis <pntanasis@gmail.com>
 * http://masterex.github.com/
 * 
 */
package liveUpdate;

import liveUpdate.objects.Instruction;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import liveUpdate.objects.Modes;
import org.xml.sax.SAXException;
import liveUpdate.parsers.UpdateXMLParser;

/**
 *
 * @author Periklis Ntanasis
 * @author Jan-Patrick Osterloh (fixed copy method to check if destination is folder and adds filename)
 */
public class Updater {

    public void update(String instructionsxml, String tmp, Modes mode) throws SAXException,
            FileNotFoundException, IOException, InterruptedException {

        UpdateXMLParser parser = new UpdateXMLParser();
        Iterator<Instruction> iterator = parser.parse(tmp + File.separator + instructionsxml, mode).iterator();
        Instruction instruction;

        while (iterator.hasNext()) {
            instruction = (Instruction) iterator.next();
            switch (instruction.getAction()) {
                case MOVE:
                    copy(tmp + File.separator + instruction.getFilename(), instruction.getDestination());
                    break;
                case DELETE:
                    delete(instruction.getDestination());
                    break;
                case EXECUTE:
                    Runtime.getRuntime().exec("java -jar " + tmp + File.separator + instruction.getFilename());
                    break;
            }
        }

    }
    
    public void update(String instructionsxml, String tmp, String dstdir, Modes mode) throws SAXException,
            FileNotFoundException, IOException, InterruptedException {

        UpdateXMLParser parser = new UpdateXMLParser();
        Iterator<Instruction> iterator = parser.parse(tmp + File.separator + instructionsxml, mode).iterator();
        Instruction instruction;

        while (iterator.hasNext()) {
            instruction = (Instruction) iterator.next();
            switch (instruction.getAction()) {
                case MOVE:
                    copy(tmp + File.separator + instruction.getFilename(), dstdir+File.separator+instruction.getDestination());
                    break;
                case DELETE:
                    delete(dstdir+File.separator+instruction.getDestination());
                    break;
                case EXECUTE:
                    Runtime.getRuntime().exec("java -jar " + tmp + File.separator + instruction.getFilename());
                    break;
            }
        }

    }

    private void copy(String source, String destination) throws FileNotFoundException, IOException {
        File srcfile = new File(source);
        File dstfile = new File(destination);
        if (dstfile.isDirectory()) {
        	dstfile = new File(destination + File.separator + srcfile.getName());
        }

        InputStream in = new FileInputStream(srcfile);
        OutputStream out = new FileOutputStream(dstfile);

        byte[] buffer = new byte[512];
        int length;

        while ((length = in.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }

        in.close();
        out.close();
    }

    private void delete(String filename) {
        File file = new File(filename);
        file.delete();
    }
}
