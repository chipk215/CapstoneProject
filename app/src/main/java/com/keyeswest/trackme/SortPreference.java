package com.keyeswest.trackme;

import java.util.HashMap;
import java.util.Map;


//Attribution:  https://stackoverflow.com/a/22951509/9128441
public enum SortPreference {

    OLDEST("O"),
    NEWEST("N"),
    SHORTEST("S"),
    LONGEST("L");

    private final String code;
    private static final Map<String,SortPreference> valuesByCode;

    static {
        valuesByCode = new HashMap<>(values().length);
        for(SortPreference value : values()) {
            valuesByCode.put(value.code, value);
        }
    }

    SortPreference(String code){
        this.code = code;
    }

    public static SortPreference lookupByCode(String code) {
        return valuesByCode.get(code);
    }

    public String getCode() {
        return code;
    }


}
