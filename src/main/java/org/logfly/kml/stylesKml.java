/* 
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package org.logfly.kml;

/**
 *
 * @author gil
 * Styling kml
 */
public class stylesKml {
    private static final String RC = "\n";
    
    public static String getStyle()  {
        StringBuilder res = new StringBuilder();
        
        res.append("     <Style id=\"multiTrack_h\">").append(RC);
        res.append("          <IconStyle>").append(RC);
        res.append("               <scale>1.2</scale>").append(RC);
        res.append("               <Icon>").append(RC);
        res.append("                    <href>http://earth.google.com/images/kml-icons/track-directional/track-0.png</href>").append(RC);
        res.append("               </Icon>").append(RC);
        res.append("          </IconStyle>").append(RC);
        res.append("          <LineStyle>").append(RC);
        res.append("               <color>99ffac59</color>").append(RC);
        res.append("               <width>8</width>").append(RC);
        res.append("          </LineStyle>").append(RC);
        res.append("     </Style>").append(RC);
        res.append("     <StyleMap id=\"multiTrack\">").append(RC);
        res.append("          <Pair>").append(RC);
        res.append("               <key>normal</key>").append(RC);
        res.append("               <styleUrl>#multiTrack_n</styleUrl>").append(RC);
        res.append("          </Pair>").append(RC);
        res.append("          <Pair>").append(RC);
        res.append("               <key>highlight</key>").append(RC);
        res.append("               <styleUrl>#multiTrack_h</styleUrl>").append(RC);
        res.append("          </Pair>").append(RC);
        res.append("     </StyleMap>").append(RC);
        res.append("     <Style id=\"multiTrack_n\">").append(RC);
        res.append("          <IconStyle>").append(RC);
        res.append("               <Icon>").append(RC);
        res.append("                    <href>http://earth.google.com/images/kml-icons/track-directional/track-0.png</href>").append(RC);
        res.append("               </Icon>").append(RC);
        res.append("          </IconStyle>").append(RC);
        res.append("          <LineStyle>").append(RC);
        res.append("               <color>99ffac59</color>").append(RC);
        res.append("               <width>6</width>").append(RC);
        res.append("          </LineStyle>").append(RC);
        res.append("     </Style>").append(RC);
        res.append("     <Style id=\"Liste_Radio\">").append(RC);
        res.append("          <ListStyle>").append(RC);
        res.append("               <listItemType>radioFolder</listItemType>").append(RC);
        res.append("          </ListStyle>").append(RC);
        res.append("     </Style>").append(RC);
        res.append("     <Style id=\"Liste_Coche\">").append(RC);
        res.append("          <ListStyle>").append(RC);
        res.append("               <listItemType>checkHideChildren</listItemType>").append(RC);   // Individual placemarks don't appear in the list view
        res.append("          </ListStyle>").append(RC);
        res.append("     </Style>").append(RC);
        res.append("     <Style id=\"Point_Rouge\">").append(RC);    // Alt Max by example
        res.append("          <BalloonStyle>").append(RC);
        res.append("               <text>$[description]</text>").append(RC);
        res.append("          </BalloonStyle>").append(RC);
        res.append("          <IconStyle>").append(RC);
        res.append("               <Icon>").append(RC);
        res.append("                    <href>http://maps.google.com/mapfiles/kml/pal4/icon25.png</href>").append(RC);
        res.append("               </Icon>").append(RC);
        res.append("               <color>ff0020ff</color>").append(RC);
        res.append("               <scale>1</scale>").append(RC);
        res.append("          </IconStyle>").append(RC);
        res.append("          <LabelStyle>").append(RC);
        res.append("               <color>ffffffff</color>").append(RC);
        res.append("               <scale>0.774596669241</scale>").append(RC);
        res.append("          </LabelStyle>").append(RC);
        res.append("     </Style>").append(RC);
        res.append("     <Style id=\"Point_Bleu\">").append(RC);    
        res.append("          <BalloonStyle>").append(RC);
        res.append("               <text>$[description]</text>").append(RC);
        res.append("          </BalloonStyle>").append(RC);
        res.append("          <IconStyle>").append(RC);
        res.append("               <Icon>").append(RC);
        res.append("                    <href>http://maps.google.com/mapfiles/kml/pal4/icon25.png</href>").append(RC);
        res.append("               </Icon>").append(RC);
        res.append("               <color>ffff2000</color>").append(RC);
        res.append("               <scale>0.774596669241</scale>").append(RC);
        res.append("          </IconStyle>").append(RC);
        res.append("          <LabelStyle>").append(RC);
        res.append("               <color>ffffffff</color>").append(RC);
        res.append("               <scale>0.774596669241</scale>").append(RC);
        res.append("          </LabelStyle>").append(RC);
        res.append("     </Style>").append(RC);
        res.append("     <Style id=\"Line_Trans\">").append(RC);
        res.append("          <LineStyle>").append(RC);
        res.append("               <color>F000FF00</color>").append(RC);
        res.append("               <width>4</width>").append(RC);
        res.append("               <gx:labelVisibility>1</gx:labelVisibility>").append(RC);
        res.append("          </LineStyle>").append(RC);
        res.append("     </Style>").append(RC);
        res.append("     <Style id=\"Pushpin_G\">").append(RC);    
        res.append("          <IconStyle>").append(RC);
        res.append("               <Icon>").append(RC);
        res.append("                    <href>http://maps.google.com/mapfiles/kml/pushpin/grn-pushpin_maps.png</href>").append(RC);
        res.append("               </Icon>").append(RC);
        res.append("               <scale>1.2</scale>").append(RC);
        res.append("          </IconStyle>").append(RC);
        res.append("     </Style>").append(RC);
        res.append("     <Style id=\"Pushpin_P\">").append(RC);    
        res.append("          <IconStyle>").append(RC);
        res.append("               <Icon>").append(RC);
        res.append("                    <href>http://maps.google.com/mapfiles/kml/pushpin/pink-pushpin_maps.png</href>").append(RC);
        res.append("               </Icon>").append(RC);
        res.append("               <scale>1.2</scale>").append(RC);
        res.append("          </IconStyle>").append(RC);
        res.append("     </Style>").append(RC);
        res.append("     <Style id=\"Line_Gain\">").append(RC);
        res.append("          <LineStyle>").append(RC);
        res.append("               <color>F09900FF</color>").append(RC);
        res.append("               <width>4</width>").append(RC);
        res.append("               <gx:labelVisibility>1</gx:labelVisibility>").append(RC);
        res.append("          </LineStyle>").append(RC);
        res.append("     </Style>").append(RC);
        res.append("     <Style id=\"sn_track-0\">").append(RC);
        res.append("          <IconStyle>").append(RC);
        res.append("               <scale>0</scale>").append(RC);
        res.append("               <Icon>").append(RC);
        res.append("                    <href>http://earth.google.com/images/kml-icons/track-directional/track-0.png</href>").append(RC);
        res.append("               </Icon>").append(RC);
        res.append("          </IconStyle>").append(RC);
        res.append("          <ListStyle>").append(RC);
        res.append("          </ListStyle>").append(RC);
        res.append("          <LineStyle>").append(RC);
        res.append("               <color>990000ff</color>").append(RC);
        res.append("               <width>4</width>").append(RC);
        res.append("          </LineStyle>").append(RC);
        res.append("     </Style>").append(RC);
        res.append("     <Style id=\"sh_track-0\">").append(RC);
        res.append("          <IconStyle>").append(RC);
        res.append("               <scale>0</scale>").append(RC);
        res.append("               <Icon>").append(RC);
        res.append("                    <href>http://earth.google.com/images/kml-icons/track-directional/track-0.png</href>").append(RC);
        res.append("               </Icon>").append(RC);
        res.append("          </IconStyle>").append(RC);
        res.append("          <ListStyle>").append(RC);
        res.append("          </ListStyle>").append(RC);
        res.append("          <LineStyle>").append(RC);
        res.append("               <color>990000ff</color>").append(RC);
        res.append("               <width>5</width>").append(RC);
        res.append("          </LineStyle>").append(RC);
        res.append("     </Style>").append(RC);
        res.append("     <StyleMap id=\"msn_track-0\">").append(RC);
        res.append("          <Pair>").append(RC);
        res.append("               <key>normal</key>").append(RC);
        res.append("               <styleUrl>#sn_track-0</styleUrl>").append(RC);
        res.append("          </Pair>").append(RC);
        res.append("          <Pair>").append(RC);
        res.append("               <key>highlight</key>").append(RC);
        res.append("               <styleUrl>#sh_track-0</styleUrl>").append(RC);
        res.append("          </Pair>").append(RC);
        res.append("     </StyleMap>").append(RC);
        
        return res.toString();
    }
    
}
