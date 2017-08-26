/*
 * Copyright Gil THOMAS
 * This file forms an integral part of Logfly project
 * See the LICENSE file distributed with source code
 * for details of Logfly licence project
 */
package gps;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Gpsmodel;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author gil
 */
public class flytec20Test {
    
    private ObservableList <Gpsmodel> dataImport; 
    
    public flytec20Test() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    @Test
    public void testInit() {
        // Mac OS test        
//        String currNamePort = "/dev/cu.usbserial";
//        try {
//            flytec20 fls = new flytec20();
//            assertTrue(fls.init(currNamePort));
//            System.out.println("Ini test : "+fls.getDeviceType()+" serial "+fls.getDeviceSerial());
//        } catch (Exception e) {
//            System.out.println(e.toString());
//        }        
    }

    @Test
    public void testIniForFlights() {
    }

    @Test
    public void testGetListFlights() {
        // Mac OS test
//        String currNamePort = "/dev/cu.usbserial";
//        try {
//            flytec20 fls = new flytec20();
//            if (fls.init(currNamePort)) {                    
//                dataImport = FXCollections.observableArrayList();  
//                fls.getListFlights(dataImport);
//                assertTrue(dataImport.size() > 0);     
//                System.out.println("Flights list");
//                for (Gpsmodel nbItem : dataImport){                 
//                    System.out.println(nbItem.getDate()+" "+nbItem.getHeure()+" "+nbItem.getCol4());
//                }
//            }
//        } catch (Exception e) {
//            System.out.println(e.toString());
//        }        
    }

    @Test
    public void testGetIGC() {
    }

    @Test
    public void testClosePort() {
    }
    
}
