package com.iip.datafusion.dgs.model.accuracy;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class KeyValue {

    @JsonProperty("key")
    private String key;

    @JsonProperty("value")
    private String value;

    @JsonCreator
    public KeyValue(@JsonProperty("key") String key,
                    @JsonProperty("value") String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
