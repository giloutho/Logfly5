/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
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
 * HTML generation of a leaflet map with markers
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
     * Generation of HTML code of comment if exists
     */
    private void genComment() {
                
        if (strComment != null && !strComment.equals("")) { 
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
            // It's HTML not javascript apostrophe escape is not necessary
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
    
    /**
     * Generation of HTML code of the map
     * @return 
     */
    public int genMap() {
        int res = -1;
        StringBuilder sbHTML = new StringBuilder();
        StringBuilder sbComment = new StringBuilder();
        String commentOk;
        
        try {
            try  {
                BufferedReader br = new BufferedReader(new InputStreamReader(map_visu.class.getResourceAsStream("/skl/skl_marker.txt")));
                String line = null;            
                while ((line = br.readLine()) != null) {
                    sbHTML.append(line).append(RC);                    
                }
                br.close();
            } catch (IOException e) {
                res = 8;    // Unabel to load the resource
            }
            if (sbHTML.length() > 500)  {
                String beginHTML = sbHTML.toString();
                pointIGC currPoint = pointsList.get(0);
                StringBuilder sbCoord = new StringBuilder();
                sbCoord.append(decimalFormat.format(currPoint.Latitude)).append(",").append(decimalFormat.format(currPoint.Longitude));
                String pointsHTML = beginHTML.replace("%Coord%", sbCoord.toString());
                // Comment treatment
                // To avoid an unsightly \ in place of apostrophe
                // in HTML a  \\' is needed but java alredy place one \ before apostrophe
                // therefore only one is added
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
