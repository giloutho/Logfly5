/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package airspacelib;
import java.util.EnumSet;

/**
 * Created by Rob Verhoef on 20-10-2015.
 *   was originally in AirspacesImportApp/src/com/mobileaviationtools/AirspacesData/
 */
public enum AirspaceCategory {
    A,
    AWY,
    B,
    C,
    CTR,
    CTA,
    D,
    DANGER,
    Q,
    E,
    F,
    G,
    GP,
    GLIDING,
    GSEC,
    OTH,
    RESTRICTED,
    R,
    TMA,
    TMZ,
    TSA,
    WAVE,
    W,
    PROHIBITED,
    P,
    FIR,
    UIR,
    RMZ,
    Z,
    ZP,
    ZSM,
    UKN;

    @Override
    public String toString() {
        return super.toString();
    }

    public static EnumSet<AirspaceCategory> doNotInsertSet()
    {
        return EnumSet.of(AWY, UKN);
    }
}
