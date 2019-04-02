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
import java.util.Random;
import org.xnap.commons.i18n.I18n;
import photo.imgmanip;
import trackgps.traceGPS;

/**
 *
 * @author Gil
 * HTML generation of a little leaflet map with a simplified track
 */
public class map_pm {
    
    private boolean map_OK;
    private int errorCode;
    private String map_HTML;
    private boolean reduction;    
    private final String RC = "\n";
    private StringBuilder jsTabPoints;
    private String jsLayer;
    private StringBuilder jsInfo;
    private StringBuilder btnComment;
    private StringBuilder txtComment;
    private StringBuilder btnPhoto;
    private StringBuilder txtPhoto;
    private int idxMap;
    private I18n i18n;
        

    public boolean isMap_OK() {
        return map_OK;
    }

    public String getMap_HTML() {
        return map_HTML;
    }

    public int getErrorCode() {
        return errorCode;
    }
                          
    public map_pm(traceGPS tracePM, boolean isReduc, int numMap, I18n currLang)
    {
        map_HTML = null;
        map_OK = false;
        reduction = isReduc;
        idxMap = numMap;
        i18n = currLang;
        jsTabPoints = new StringBuilder();
        jsInfo = new StringBuilder(); 
        btnComment = new StringBuilder(); 
        txtComment = new StringBuilder(); 
        btnPhoto  = new StringBuilder(); 
        txtPhoto  = new StringBuilder(); 
        cartePM(tracePM);       
    } 
    
    
    /**
     * HTML generation of track data
     * @param tracePM
     * @return 
     */    
    private boolean genData(traceGPS tracePM)  {
        
        Boolean res = false;
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');        
        DecimalFormat decimalFormat = new DecimalFormat("###.00000", decimalFormatSymbols);
        
        int step;
        int totPoints = tracePM.Tb_Good_Points.size();

        if (totPoints > 200 && reduction)  {
            step = totPoints / 200;
        }  else  {
            step = 1;
        }
        
        for(int i = 1; i<=totPoints; i = i+step)
        {
            pointIGC currPoint = tracePM.Tb_Good_Points.get(i-1);
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
     * Default layer of the map
     */
    private void genDefaultLayer() {
         
        // We put a default value to avoid an undefined case    
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
    
    /**
     * HTML generation of a little info panel
     * @param tracePM 
     */
    private void genInfo(traceGPS tracePM)  {
     //   jsInfo.append("this._div.innerHTML += '").append(i18n.tr("Voile")).append(":").append(tracePM.getsVoile()).append("<br>';").append(RC);
        jsInfo.append("            this._div.innerHTML += '").append(i18n.tr("Glider")).append(":").append(tracePM.getsVoile()).append("<br>';").append(RC);
        pointIGC ptAltMax = tracePM.getAlt_Maxi_GPS();    
        jsInfo.append("            this._div.innerHTML += '").append(i18n.tr("Max GPS Alt")).append(" : ").append(String.valueOf(ptAltMax.AltiGPS)).append("m<br>';").append(RC);
         pointIGC ptVarioMax = tracePM.getVario_Max();
        jsInfo.append("            this._div.innerHTML += '").append(i18n.tr("Max climb")).append(" : ").append(String.format("%2.2f",ptVarioMax.Vario)).append("m/s<br>';").append(RC);
        jsInfo.append("            this._div.innerHTML += '").append(i18n.tr("Max gain")).append(" : ").append(String.valueOf(tracePM.getBestGain())).append("m<br>';").append(RC);
        
    }
    
    /**
     * Generation of HTML code of comment if exists
     */
    private void genComment(traceGPS tracePM) {
        
        String myComment = tracePM.getComment();        
        if (myComment != null && !myComment.equals("")) { 
            // Button building
            btnComment.append("        L.easyButton({").append(RC);  
            btnComment.append("            id: 'bt_comment',  // an id for the generated button").append(RC);  
            btnComment.append("            position: 'topleft',      // inherited from L.Control -- the corner it goes in").append(RC);  
            btnComment.append("            type: 'replace',          // set to animate when you're comfy with css").append(RC);  
            btnComment.append("            leafletClasses: true,     // use leaflet classes to style the button?").append(RC);  
            btnComment.append("            states:[{                 // specify different icons and responses for your button").append(RC);  
            btnComment.append("                stateName: 'get-comment',").append(RC);  
            btnComment.append("                title: 'show me the middle',").append(RC);  
            btnComment.append("                icon: 'fa-comment-o fa-lg'").append(RC);  
            btnComment.append("            }]").append(RC);  
            btnComment.append("        }).addTo(map);").append(RC).append(RC);                        
            btnComment.append("        $('#bt_comment').click(function(){").append(RC);  
            btnComment.append("            $('#comment_to_pop_up').bPopup(").append(RC);  
            btnComment.append("                {closeClass:'b-close-c',").append(RC);  
            btnComment.append("                 opacity: 0.1,").append(RC);  
            btnComment.append("                 position:[20,20]}").append(RC);  
            btnComment.append("            );").append(RC);  
            btnComment.append("        });").append(RC).append(RC);  
            btnComment.append("        $('#bt_comment').trigger( \"click\" );").append(RC);   
            // Text building
            String removeRC = myComment.replaceAll("\\r\\n", "<br>");
            String removeR = removeRC.replaceAll("\\n", "<br>");
            // To avoid an unsightly \ in place of apostrophe
            // in HTML a  \\' is needed but java alredy place one \ before apostrophe
            // therefore only one is added            
            //String commentOk = removeRC.replace("\'", "\'");    
            // Finally not needed -:)
            String commentOk = removeRC;
            txtComment.append("<div id=\"comment_to_pop_up\"><a class=\"b-close-c\">x<a/>");
            txtComment.append("<p>").append(commentOk).append("</p></div>").append(RC);
            txtComment.append("<div id=\"carte\"></div>");                        
        } else {
            btnComment.append("");
            txtComment.append("<div id=\"carte\"></div>");            
        }         
    }
    
    /**
     * Special method for the picture of the flight
     * Necessary due to a webview bug 
     * for a picture located on the disk 
     * if the picture content change but not the name, the webview cache is not updated
     * Known problem found in Stackoverflow
     * a random name is generated and old files are deleted     
     * @return 
     */
    private File newTempJpg() {
        Random rand = new Random();
        // renvoie un entier compris entre 0 inclus et n exclu donc ici entre 0 et 1000
        int nombre = rand.nextInt(10001);  
        // Génère un fichier avec un nom aléatoire situé dans un répertoire temporaire
        File fPhoto = systemio.tempacess.getAppFile("Logfly", String.valueOf(nombre)+".jpg");
        // Détruit tous les fichiers .jpg de ce répertoire temporaire
        File folder = new File(fPhoto.getParent());
        for (File file : folder.listFiles()) {
            if (file.getName().endsWith(".jpg")) {
                file.delete();
            }
        }
        
        return fPhoto;
    }
    
    /**
     * HTML generation for the picture of the flight
     * @param tracePM 
     */
    private void genPhoto(traceGPS tracePM) {
        
        String myPhoto = tracePM.getPhoto();        
        if (myPhoto != null && !myPhoto.equals("")) {
            File fPhoto = newTempJpg();
            imgmanip currImage = new imgmanip();
            int errPhoto = currImage.strDecode(myPhoto, 500, 500, fPhoto.getAbsolutePath()); 
            if ( errPhoto == 0 )  {
                btnPhoto.append("        L.easyButton({").append(RC);  
                btnPhoto.append("            id: 'bt_photo',  // an id for the generated button").append(RC);  
                btnPhoto.append("            position: 'topleft',      // inherited from L.Control -- the corner it goes in").append(RC);  
                btnPhoto.append("            type: 'replace',          // set to animate when you're comfy with css").append(RC);  
                btnPhoto.append("            leafletClasses: true,     // use leaflet classes to style the button?").append(RC);  
                btnPhoto.append("            states:[{                 // specify different icons and responses for your button").append(RC);  
                btnPhoto.append("                stateName: 'get-center',").append(RC);  
                btnPhoto.append("                title: 'show me the middle',").append(RC);  
                btnPhoto.append("                icon: 'fa-picture-o fa-lg'").append(RC);  
                btnPhoto.append("            }]").append(RC);  
                btnPhoto.append("        }).addTo(map);").append(RC).append(RC);  
                btnPhoto.append("        // Attention on avait appellé jQuery juste avant l'apple à bPopUp").append(RC);  
                btnPhoto.append("        // mais il était déjà appelé par ailleurs (Google api ?)").append(RC);  
                btnPhoto.append("        // ce double appel de jQuery provoque un plantage de bPopup").append(RC);  
                btnPhoto.append("        // solution mettre le nPopup en dernier dans la liste des appels").append(RC).append(RC);    
                btnPhoto.append("        $('#bt_photo').click(function(){").append(RC);  
                btnPhoto.append("            $('#element_to_pop_up').bPopup(").append(RC);  
                btnPhoto.append("                { speed: 650,").append(RC);  
                btnPhoto.append("                  transition: 'slideIn',").append(RC);  
                btnPhoto.append("                  transitionClose: 'slideBack' }").append(RC);  
                btnPhoto.append("            );").append(RC);  
                btnPhoto.append("        });").append(RC).append(RC);  
                btnPhoto.append("        $('#bt_photo').trigger( \"click\" );").append(RC);
                // code de construction du lien image dans le body de la page
                txtPhoto.append("<body>").append(RC);
                txtPhoto.append("<div id=\"element_to_pop_up\"><a class=\"b-close\">x<a/><img src=\"");
                txtPhoto.append("file://localhost//").append(fPhoto.getAbsolutePath());
                txtPhoto.append("\" width=").append(String.valueOf(currImage.getWidthImg()));
                txtPhoto.append(" height=").append(String.valueOf(currImage.getHeightImg())).append(" /></div>");                
            } else {
                // decoding problem
                errorCode = errPhoto;
                btnPhoto.append("");
                txtPhoto.append("<body>");
            }            
        } else {
            btnPhoto.append("");
            txtPhoto.append("<body>");
        }      
    }
    
    /**
     * HTML generation for a little map with a simplified track
     * @param tracePM 
     */
    public void cartePM(traceGPS tracePM)  {
        
        StringBuilder sbHTML = new StringBuilder();
        
        try {
            try  {
                    BufferedReader br = new BufferedReader(new InputStreamReader(map_visu.class.getResourceAsStream("/skl/skl_pm_ph.txt")));    // V1 : skl_pm_topo.txt                    
                    String line = null;            
                    while ((line = br.readLine()) != null) {
                        sbHTML.append(line).append(RC);                    
                    }
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
		}
            if (sbHTML.length() > 1000 && genData(tracePM))  {  
                String beginHTML = sbHTML.toString();
                String pointsHTML = beginHTML.replace("%tabPoints%", jsTabPoints.toString());                          
                genDefaultLayer();
                String layerHTML = pointsHTML.replace("%layer%", jsLayer);  
                genInfo(tracePM);
                String infoHTML = layerHTML.replace("%info%", jsInfo.toString());   
                genComment(tracePM);
                genPhoto(tracePM);
                String btnCommentHTML = infoHTML.replace("%btnComment%", btnComment.toString());   
                String btnPhotoHTML = btnCommentHTML.replace("%btnPhoto%",btnPhoto.toString());
                String txtPhotoHTML = btnPhotoHTML.replace("<body>", txtPhoto.toString());                
                map_HTML = txtPhotoHTML.replace("<div id=\"carte\"></div>", txtComment.toString());                   
                map_OK = true;            
            }
        } catch (Exception e) {
            map_OK = false;
            errorCode = -1;    // Undefined error
        }                               
    } 
    
}
