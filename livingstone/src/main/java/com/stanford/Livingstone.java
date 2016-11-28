package com.stanford;

import java.util.Random;

public class Livingstone {

    /*
     This function that generates a float value between 0.0 and 1.0 represents scan result
     and a scan message associated with the diagnostic
      */
    public static class GeneratePrediction {
        public static Diagnostic diagnose(Long imageReference, String scanMode) {
            Diagnostic diagnostic = new Diagnostic(imageReference, scanMode);
            diagnostic.setScanResult(new Random().nextFloat());
            diagnostic.setScanMessage("Scan Message");
            return diagnostic;
        }
    }
}
