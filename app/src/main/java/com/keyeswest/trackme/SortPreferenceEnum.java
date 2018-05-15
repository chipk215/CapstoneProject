package com.keyeswest.trackme;

import java.util.HashMap;
import java.util.Map;


//Attribution:  https://stackoverflow.com/a/22951509/9128441
public enum SortPreferenceEnum {

    OLDEST("O"),
    NEWEST("N"),
    SHORTEST("S"),
    LONGEST("L");

    private final String code;
    private static final Map<String,SortPreferenceEnum> valuesByCode;

    static {
        valuesByCode = new HashMap<>(values().length);
        for(SortPreferenceEnum value : values()) {
            valuesByCode.put(value.code, value);
        }
    }

    SortPreferenceEnum(String code){
        this.code = code;
    }

    public static SortPreferenceEnum lookupByCode(String code) {
        return valuesByCode.get(code);
    }

    public String getCode() {
        return code;
    }


}
