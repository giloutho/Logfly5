/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package trackgps;

import java.io.File;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import settings.configProg;

/**
 *
 * @author gil
 */
public class scoringTest {
    
    public scoringTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
   
    @Test
    public void testRunScoring() {
        // Reading settings requested
        configProg myConfig = new configProg();
        myConfig.readSettings();         
                                  
        if (myConfig.isValidConfig()) {        
            File fIGC = new File("/Users/gil/Documents/Logfly/Test2.igc");
            traceGPS igcTest = new traceGPS(fIGC, "IGC",true, myConfig);
            if (igcTest.isDecodage())  {
                scoring currScore = new scoring(0, myConfig);  
                currScore.runScoring(igcTest, "FR");   
                assertTrue(igcTest.isScored());  
                StringBuilder sbScore = new StringBuilder();
                sbScore.append("League : ").append(igcTest.getScore_League()).append("\n");
                sbScore.append("Score type : ").append(igcTest.getScore_Shape()).append("\n");
                sbScore.append("Score distance :").append(String.format("%5.2f" , igcTest.getScore_Route_Km())).append("\n");
                sbScore.append("Score points :").append(String.format("%5.2f" ,igcTest.getScore_Route_Pts())).append("\n");
                sbScore.append("Score average :").append(String.format("%5.2f" ,igcTest.getScore_Moyenne())).append("\n");
                System.out.println(sbScore.toString());  
                System.out.println(igcTest.getScore_JSON());                                 
            }        
        } else {
            System.out.println("Unable to load settings");
        }
    }

   
//    @Test
//    public void testDecodeStrJson() {
//    }
    
}
