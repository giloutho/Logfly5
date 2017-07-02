/*
 * Copyright Gil THOMAS
 * Ce fichier fait partie intégrante du projet Logfly
 * Pour tous les détails sur la licence du projet Logfly
 * Consulter le fichier LICENSE distribué avec le code source
 */
package leaflet;

import igc.pointIGC;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import org.xnap.commons.i18n.I18n;

/**
 *
 * @author gil
 */
public class map_markers {
    
    private String map_HTML;
    private static final String RC = "\n";
    private I18n i18n; 
    private static String jsLayer;
    private static int idxMap;
    DecimalFormat decimalFormat;
    private static StringBuilder btnComment;
    private static StringBuilder txtComment;
    private String strComment;
    private final ArrayList<pointIGC> pointsList = new ArrayList<>();
    
    public map_markers(I18n pI18n, int numMap)  {
        map_HTML = null;
        this.i18n = pI18n;
        idxMap = numMap;
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        // Imperatif -> forcer le point comme séparateur
        decimalFormatSymbols.setDecimalSeparator('.');        
        decimalFormat = new DecimalFormat("###.00000", decimalFormatSymbols);  
        if (pointsList.size() > 0) genMap();       
        btnComment = new StringBuilder(); 
        txtComment = new StringBuilder(); 
    }
        
    public String getMap_HTML() {
        return map_HTML;
    }

    public void setStrComment(String strComment) {
        this.strComment = strComment;
    }
    
    
    public ArrayList<pointIGC> getPointsList() {
        return pointsList;
    }
    
    private void genDefaultLayer() {
         
        // On met une valeur par defaut au cas où, pour ne pas se retrouver sans AddTo(Map)       
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
    
    private void genComment() {
                
        if (strComment != null && !strComment.equals("")) { 
            // Code de construction du bouton
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
            //Code de construction du texte de commentaire
            // On est dans du HTML pas du javascript donc l'échappement de l'apostrophe est superflu
            String removeRC = strComment.replaceAll("\\r\\n", "<br>");
            String commentOk = removeRC.replaceAll("\\\n", "<br>");            
            txtComment.append("<div id=\"comment_to_pop_up\"><a class=\"b-close-c\">x<a/>");
            txtComment.append("<p>").append(commentOk).append("</p></div>").append(RC);
            txtComment.append("<div id=\"carte\"></div>");                        
        } else {
            btnComment.append("");
            txtComment.append("<div id=\"carte\"></div>");            
        }         
    }
    
    public int genMap() {
        int res = -1;
        StringBuilder sbHTML = new StringBuilder();
        StringBuilder sbComment = new StringBuilder();
        String commentOk;
        
        try {
            try  {
             //   BufferedReader br = new BufferedReader(new InputStreamReader(map_visu.class.getResourceAsStream("/skl_marker.txt")));
                BufferedReader br = new BufferedReader(new InputStreamReader(map_visu.class.getResourceAsStream("/skl/skl_marker.txt")));
                String line = null;            
                while ((line = br.readLine()) != null) {
                    sbHTML.append(line).append(RC);                    
                }
                br.close();
            } catch (IOException e) {
                res = 8;    // Problème de chargement de la ressource
            }
            if (sbHTML.length() > 500)  {
                String beginHTML = sbHTML.toString();
                pointIGC currPoint = pointsList.get(0);
                StringBuilder sbCoord = new StringBuilder();
                sbCoord.append(decimalFormat.format(currPoint.Latitude)).append(",").append(decimalFormat.format(currPoint.Longitude));
                String pointsHTML = beginHTML.replace("%Coord%", sbCoord.toString());
                // Traitement du commentaire 
                // Pour ne pas afficher un disgracieux \' à la place de l'apostophe
                // il faut en HTML un \\' Or java a déjà placé un \ devant l'apostrophe
                // donc il suffit d'ajouter un \
                if (currPoint.Comment != null && !currPoint.Comment.equals("")) {
                    commentOk = currPoint.Comment.replace("'", "\'"); 
                    sbComment.append(".bindPopup('").append(commentOk).append("').openPopup();");
                } else {
                    sbComment.append(";");
                }
                String commentHTML = pointsHTML.replace("%Comment%", sbComment.toString());
                genDefaultLayer();
                String layerHTML = commentHTML.replace("%layer%", jsLayer);  
                genComment();                
                String btnCommentHTML = layerHTML.replace("%btnComment%", btnComment.toString());   
                map_HTML = btnCommentHTML.replace("<div id=\"carte\"></div>", txtComment.toString());     
                res = 0;    
            }
        } catch (Exception e) {
            res = -1;
        } 
        
        return res;
    }
    
}
