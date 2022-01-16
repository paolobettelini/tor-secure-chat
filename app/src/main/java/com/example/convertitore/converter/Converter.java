package com.example.convertitore.converter;

import java.util.Set;

public interface Converter {

    double convert(String from, String to, double value);

    String getBase();

    Set<String> convertibles();

}