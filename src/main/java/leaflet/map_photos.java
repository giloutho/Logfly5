/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package leaflet;

import igc.pointIGC;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.xnap.commons.i18n.I18n;
import photos.exifReader;
import settings.configProg;
import settings.osType;
import trackgps.traceGPS;

/**
 *
 * @author gil
 */
public class map_photos {
    
    private I18n i18n;
    
    // Paramètres de configuration
    configProg myConfig;

    private boolean map_OK;
    private String map_HTML;
    private int errorCode = 0;
    private boolean reduction;    
    private final String RC = "\n";
    private StringBuilder jsTabPoints;
    private StringBuilder jsTabPhotos;   
    private StringBuilder jsTabGallery;
    private String jsLayer;
    private ArrayList<String> photoPathList = new ArrayList<>(); 
    private DateTimeFormatter dtfExif = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");
    private DateTimeFormatter dtfDay = DateTimeFormatter.ofPattern("dd/MM/yy");
    private DateTimeFormatter dtfHour = DateTimeFormatter.ofPattern("HH:mm:ss");    
    private DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();  
    private DecimalFormat decimalFormat;
    private int nbGpsPhotos;
    private int nbSimplePhotos;

    public boolean isMap_OK() {
        return map_OK;
    }

    public int getNbGpsPhotos() {
        return nbGpsPhotos;
    }

    public int getNbSimplePhotos() {
        return nbSimplePhotos;
    }        
    
    public String getMap_HTML() {
        return map_HTML;
    }

    public StringBuilder getJsTabPhotos() {
        return jsTabPhotos;
    }

    public StringBuilder getJsTabGallery() {
        return jsTabGallery;
    }
                
    public map_photos(traceGPS pTrack, File fPhotos, configProg currConfig, boolean buildMap) {
        map_HTML = null;
        map_OK = false;
        jsTabPoints = new StringBuilder();    
        jsTabPhotos = new StringBuilder();  
        jsTabGallery = new StringBuilder(); 
        decimalFormatSymbols.setDecimalSeparator('.'); 
        decimalFormat = new DecimalFormat("###.00000", decimalFormatSymbols);
        nbGpsPhotos = 0;
        nbSimplePhotos = 0;
        try {
            listPhotoFiles(fPhotos);   
            if (photoPathList.size() > 0) {
                genPhotosData();
                if (buildMap) buildHtml(pTrack);                    
            }
        } catch (Exception e) {
            errorCode = -1;    // Undefined error 
            map_OK = false;            
        }
    }  
    
    
    //Y'a un prpoblème avec les crochets de TabPoins voirs dans VSC
    
    /**
     * HTML generation of track data
     * @param tracePM
     * @return 
     */    
    private boolean genTrackData(traceGPS trackPhotos)  {
        
        Boolean res = false;        
        int step;
        int totPoints = trackPhotos.Tb_Good_Points.size();

        if (totPoints > 200 && reduction)  {
            step = totPoints / 200;
        }  else  {
            step = 1;
        }
        
        for(int i = 1; i<=totPoints; i = i+step)
        {
            pointIGC currPoint = trackPhotos.Tb_Good_Points.get(i-1);
            jsTabPoints.append("      tabPoints.push(new L.LatLng(");
            jsTabPoints.append(decimalFormat.format(currPoint.Latitude));
            jsTabPoints.append(",");
            jsTabPoints.append(decimalFormat.format(currPoint.Longitude));
            jsTabPoints.append(")); ");
            jsTabPoints.append(RC); 
        }      
        
        if (jsTabPoints.length() > 100) res = true;

        return res;
    }  

    /**
     * Search is recursive, photos are in a single folder
     * @param dir
     * @throws Exception 
     */
    private void listPhotoFiles(File dir) throws Exception {              
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            String fileName = files[i].getName();
            // put in your filter here
            if (fileName.endsWith(".jpg") || fileName.endsWith(".JPG")) {                                   
                if (files[i].isFile()) {
                    photoPathList.add(files[i].getPath());       
                }
            }
            if (files[i].isDirectory()) {
                listPhotoFiles(files[i]);
            }
        }
    } 

    
    private void genPhotosData() throws Exception {  
        String pathPhoto;
        Collections.sort(photoPathList);
        jsTabPhotos.append("    var photos = [];").append(RC);  
        jsTabGallery.append("    var galerie = [];").append(RC);    
        for (String sPhotoPath : photoPathList) {
            File fPhoto = new File(sPhotoPath);
            if(fPhoto.exists() && fPhoto.isFile()) {   
                try {
                    exifReader metaPhoto = new exifReader(i18n);
                    metaPhoto.decodeGPS(fPhoto);
                    jsTabGallery.append("           galerie.push({'class' : 'fancybox','href': 'file://localhost/");
                    if (myConfig.getOS() == osType.WINDOWS) {
                        String sPath = fPhoto.getAbsolutePath();
                        pathPhoto = sPath.replaceAll("\\\\", "/");
                    } else
                        pathPhoto = fPhoto.getAbsolutePath();
                    jsTabGallery.append(pathPhoto).append("'});").append(RC);                       
                    if(metaPhoto.isInfoGPS()) {                                                                  
                        if (metaPhoto.isInfoGPS()) {
                            jsTabPhotos.append("          photos.push({\"latLng\":[").append(decimalFormat.format(metaPhoto.getTagLatitude()));
                            jsTabPhotos.append(",").append(decimalFormat.format(metaPhoto.getTagLongitude())).append("],");                        
                            jsTabPhotos.append("\"title\":");
                            jsTabPhotos.append("\"").append(dtfHour.format(metaPhoto.getLdtOriginal())).append("\"});");
                            jsTabPhotos.append(RC);   
                            nbGpsPhotos++;
                        }
                    }
                    nbSimplePhotos++;
                } catch (Exception e) {
                    
                }
            } 
        }        
    }
    
    private void genDefaultLayer() {
         
        // We put a default value to avoid an undefined case   
        int idxMap = myConfig.getIdxMap();
        if(idxMap == 0) {
            jsLayer = "    osmlayer.addTo(map);";
        } else if (idxMap ==1) { 
            jsLayer = "    OpenTopoMap.addTo(map);";
        } else if (idxMap ==2) {
            jsLayer = "    mtklayer.addTo(map);";
        } else if (idxMap ==3) {
            jsLayer = "    fouryoulayer.addTo(map);";
        } else if (idxMap ==4) {
            jsLayer = "     map.addLayer(googleLayer);";
        } else {
            jsLayer = "    OpenTopoMap.addTo(map);";
        }
        
    }    
    
    
    private void buildHtml(traceGPS trackPhotos) {
        
        StringBuilder sbHTML = new StringBuilder();
        
        try {
            try  {                
                BufferedReader br = new BufferedReader(new InputStreamReader(map_visu.class.getResourceAsStream("/skl/skl_map_photos.txt")));                
                String line = null;            
                while ((line = br.readLine()) != null) {
                    sbHTML.append(line).append(RC);                    
                }
                br.close();
            } catch (IOException e) {
                System.out.println("Erreur skl "+e.getMessage());                               
            }
            if (sbHTML.length() > 1000 && genTrackData(trackPhotos) && nbGpsPhotos > 0)  {
                String beginHTML = sbHTML.toString();
                String pointsHTML = beginHTML.replace("%tabPoints%", jsTabPoints.toString());
                String photosHTML = pointsHTML.replace("%photos%", jsTabPhotos.toString());
                String galleryHTML = photosHTML.replace("%Gallery%", jsTabGallery.toString());
                genDefaultLayer();
                String layerHTML = galleryHTML.replace("%layer%", jsLayer);
                map_HTML = layerHTML;
                map_OK = true;   
                /** ----- Debut Debug --------*/ 
                final Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putString(map_HTML);            
                clipboard.setContent(content);
                /** ----- Fin Debug --------*/ 
            } else {
                errorCode = 2;    // no photos
                map_OK = false;
            }
        } catch (Exception e) {
            errorCode = -1;    // Undefined error 
            map_OK = false;
        }              
    }    
    
}
