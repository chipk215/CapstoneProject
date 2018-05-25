package com.keyeswest.trackme.utilities;

import static java.lang.Math.abs;

public class PluralHelpers {

    private final static double EPSILON = 1E-6;

    private static boolean isSingular(double value){
        return abs(value - 1.0d) < EPSILON;
    }

    public static int getPluralQuantity(double value){
        return isSingular(value) ? 1 : 2;
    }
}
