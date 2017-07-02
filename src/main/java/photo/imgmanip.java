/*
 * Copyright Gil THOMAS
 * Ce fichier fait partie intégrante du projet Logfly
 * Pour tous les détails sur la licence du projet Logfly
 * Consulter le fichier LICENSE distribué avec le code source
 */
package photo;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritablePixelFormat;
import javax.imageio.ImageIO;

/**
 *
 * @author gil
 */
public class imgmanip {
    
    private int widthImg;     // Pour le HTML, on avait besoin de connaitre cette valeur
    private int heightImg;    // Pour le HTML, on avait besoin de connaitre cette valeur
    private String strImage; 
    private int errorCode;

    public int getWidthImg() {
        return widthImg;
    }
        
    public int getHeightImg() {
        return heightImg;
    }
  
    public String getStrImage() {
        return strImage;
    }

    public int getErrorCode() {
        return errorCode;
    }
            
    
    /**
     * procédure de réduction simple d'une photo
     * provenant de https://www.mkyong.com/java/how-to-resize-an-image-in-java/
     * @param originalImage
     * @param type
     * @param imgWidth
     * @param imgHeight
     * @return 
     */
    private BufferedImage resizeImage(BufferedImage originalImage){    
        int type = originalImage.getType() == 0? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
	BufferedImage resizedImage = new BufferedImage(widthImg, heightImg, type);
	Graphics2D g = resizedImage.createGraphics();
	g.drawImage(originalImage, 0, 0, widthImg, heightImg, null);
	g.dispose();

	return resizedImage;
    }
    
    /**
     * Procédure de réduction avec moindre perte de qualité 
     * différence avec resizeImage pas évidente pour des petits formats
     * source provenant de https://www.mkyong.com/java/how-to-resize-an-image-in-java/
     * @param originalImage
     * @param type
     * @param imgWidth
     * @param imgHeight
     * @return 
     */
    private BufferedImage resizeImageWithHint(BufferedImage originalImage){        
        int type = originalImage.getType() == 0? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
	BufferedImage resizedImage = new BufferedImage(widthImg, heightImg, type);
	Graphics2D g = resizedImage.createGraphics();
	g.drawImage(originalImage, 0, 0, widthImg, heightImg, null);
	g.dispose();
	g.setComposite(AlphaComposite.Src);

	g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
	RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	g.setRenderingHint(RenderingHints.KEY_RENDERING,
	RenderingHints.VALUE_RENDER_QUALITY);
	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	RenderingHints.VALUE_ANTIALIAS_ON);

	return resizedImage;
    }
    
    public BufferedImage reduitPhoto(BufferedImage originalImage,int maxWidth, int maxHeight) {
        // Calcul du facteur de réduction
        int currWidth = originalImage.getWidth();
        int currHeight = originalImage.getHeight();
        double factor = Math.min((double)maxWidth/currWidth,(double) maxHeight/currHeight);     
        factor = Math.min(factor, 1.0);  // Pour ne pas resizer si on a un facteur < 1
        double dWidth = currWidth * factor;
        widthImg = (int) dWidth;
        double dHeight = currHeight * factor;
        heightImg = (int) dHeight;                                

        BufferedImage resizedImg = resizeImage(originalImage);
        
        return resizedImg;
        
    }
    
    /**
     * La photo est extraite de la db sous forme d'une string
     * Dans xLogfly, j'avais galéré sur le stockage des photos dans sqlLite
     * Finalement j'étais tombé sur un post expliquant que le plus simple était de transformer le JPEG en string 
     * https://forum.xojo.com/6984-write-picture-to-file avec encodage et décodage en base 64
     * Pour des raisons de compatibilité, on garde cette procédure 
     * Décodage et encodage en java : http://www.rgagnon.com/javadetails/java-0598.html
     * La solution : https://myjeeva.com/convert-image-to-string-and-string-to-image-in-java.html
     * Cette string est transformé en tableau de bytes puis en BufferedImage
     * L'image est ensuite ajustée aux paramètres demandés : maxWidth et maxHeight
     * Même en provenance de la db, on fixe une limite max car dans xLogfly, il y avait un bug : 
     * la taille de la photo n'était pas réellement "limitée". 
     * la taille de la photo était directement liée à la taille de la fenêtre d'xLogfly
     * 
     * Elle est finalement sauvegardée sur le disque 
     * 
     * @param strPhoto
     * @param maxWidth
     * @param maxHeight 
     */
    public int strDecode(String strPhoto, int maxWidth, int maxHeight, String fAbsPath ) {       
        int res = -1;
        try {
            byte[] decodedValue = Base64.getDecoder().decode(strPhoto);   
            // conversion du  byte array en BufferedImage
            InputStream in = new ByteArrayInputStream(decodedValue);
            BufferedImage originalImage = ImageIO.read(in);                       
            // Calcul du facteur de réduction
            int currWidth = originalImage.getWidth();
            int currHeight = originalImage.getHeight();
            double factor = Math.min((double)maxWidth/currWidth,(double) maxHeight/currHeight);     
            factor = Math.min(factor, 1.0);  // Pour ne pas resizer si on a un facteur < 1
            double dWidth = currWidth * factor;
            widthImg = (int) dWidth;
            double dHeight = currHeight * factor;
            heightImg = (int) dHeight;                                

            BufferedImage resizeImageJpg = resizeImage(originalImage);
            ImageIO.write(resizeImageJpg, "jpg", new File(fAbsPath));
            // Emploi de la réduction améliorée
            // BufferedImage resizeImageHintJpg = resizeImageWithHint(originalImage, type,  newWidth, newHeight);
            // ImageIO.write(resizeImageHintJpg, "jpg", new File(fAbsPath)); 
            res = 0;
        } catch (Exception e) {
            res = 1042; // Problème sur le décodage de la photo
        }         
        return res;
    }
    
    public Image strToImage(String strPhoto, int maxWidth, int maxHeight ) {  
        Image finalImage = null;
        try {
            byte[] decodedValue = Base64.getDecoder().decode(strPhoto);   
            // conversion du  byte array en BufferedImage
            InputStream in = new ByteArrayInputStream(decodedValue);
            BufferedImage originalImage = ImageIO.read(in);                        
            // Calcul du facteur de réduction
            int currWidth = originalImage.getWidth();
            int currHeight = originalImage.getHeight();
            double factor = Math.min((double)maxWidth/currWidth,(double) maxHeight/currHeight);     
            factor = Math.min(factor, 1.0);  // Pour ne pas resizer si on a un facteur < 1
            double dWidth = currWidth * factor;
            widthImg = (int) dWidth;
            double dHeight = currHeight * factor;
            heightImg = (int) dHeight;                                

            BufferedImage resizeImageJpg = resizeImage(originalImage);
            // Astuce de transformation trouvée sur http://java-buddy.blogspot.fr/2013/01/convert-javaawtimagebufferedimage-to.html
            finalImage = SwingFXUtils.toFXImage(resizeImageJpg, null);
        } catch (Exception e) {
            errorCode = 1042; // Problème sur le décodage de la photo
        }          
        
        return finalImage;        
    }
    
    public int imageFileToStr(String fAbsPath) {
        int res = -1;
        File file = new File(fAbsPath);
        try {                       
            FileInputStream imageInFile = new FileInputStream(file);
            byte imageData[] = new byte[(int)file.length()];           
            imageInFile.read(imageData);
            // Encodage de l'image
            strImage = Base64.getEncoder().encodeToString(imageData);            
            imageInFile.close();
            res = 0;
        } catch (FileNotFoundException e) {
            res =1;
        } catch (IOException ioe) {
            res =2;
        }     
        
        return res;
    }
    
    /**
     * http://stackoverflow.com/questions/7178937/java-bufferedimage-to-png-format-base64-string
     * @param img
     * @param formatName
     * @return 
     */
    public String imgToBase64String(final RenderedImage img, final String formatName) {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, formatName, Base64.getEncoder().wrap(os));            
            return os.toString(StandardCharsets.ISO_8859_1.name());
        } catch (final IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }
    
    /**
     * http://stackoverflow.com/questions/7178937/java-bufferedimage-to-png-format-base64-string
     * @param base64String
     * @return 
     */
    public BufferedImage base64StringToImg(final String base64String) {
        try {
            return ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(base64String)));
        } catch (final IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }
    
    /**
     * Procédure pur JavaFX pour transformer une "image" javafx en bytearray
     * vue à plusieurs repises sur stackoverflow
     *    http://stackoverflow.com/questions/37008675/pure-javafx-convert-image-to-bytes-array-opposit-operation-whats-wrong
     *    http://stackoverflow.com/questions/34396679/javafx-image-to-byte-array-closed
     * Transformation en string semble fonctionner mais  transformation inverse ne passe pas...
     * Inutilisée mais gardée pour exploration ultérieure (Cf projet imgbytearray)
     * @param img
     * @return 
     */    
    public String imgToString(Image img) {
        PixelReader pr = img.getPixelReader();

        WritablePixelFormat<ByteBuffer> pixelformat = WritablePixelFormat.getByteBgraInstance();

        int w = (int) img.getWidth();
        int h = (int) img.getHeight();
        int offset = 0;
        int scanlineStride = w * 4;

        byte[] buffer = new byte[w * h * 4];

        pr.getPixels(0, 0, w, h, pixelformat, buffer, offset, scanlineStride);
        

        String strImg = Base64.getEncoder().encodeToString(buffer);    
        
        return strImg;
        
    }
        
    
}
