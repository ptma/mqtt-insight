package com.mqttinsight.codec;

import org.apache.avro.Schema;
import org.apache.avro.specific.SpecificRecord;
import org.apache.avro.specific.SpecificRecordBase;

import java.io.IOException;

public class AvroPojo extends SpecificRecordBase implements SpecificRecord {

    public static final org.apache.avro.Schema SCHEMA$;

    static {
        try {
            SCHEMA$ = new Schema.Parser().parse(
                AvroPojo.class.getResourceAsStream("/test.avsc"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String name;
    private Integer favoriteNumber;
    private String favoriteColor;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getFavoriteNumber() {
        return favoriteNumber;
    }

    public void setFavoriteNumber(Integer favoriteNumber) {
        this.favoriteNumber = favoriteNumber;
    }

    public String getFavoriteColor() {
        return favoriteColor;
    }

    public void setFavoriteColor(String favoriteColor) {
        this.favoriteColor = favoriteColor;
    }

    @Override
    public Schema getSchema() {
        return SCHEMA$;
    }

    @Override
    public Object get(int field) {
        return switch (field) {
            case 0 -> name;
            case 1 -> favoriteNumber;
            case 2 -> favoriteColor;
            default -> throw new IndexOutOfBoundsException("Invalid index: " + field);
        };
    }

    @Override
    public void put(int field, Object value) {
        switch (field) {
            case 0 -> name = (String) value;
            case 1 -> favoriteNumber = (Integer) value;
            case 2 -> favoriteColor = (String) value;
            default -> throw new IndexOutOfBoundsException("Invalid index: " + field);
        }
    }

}
