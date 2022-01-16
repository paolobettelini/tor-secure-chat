package com.example.convertitore.converter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TemperatureConverter implements Converter {

    private Map<String, Double[]> values;

    public TemperatureConverter() {
        this.values = new HashMap<>();

        this.values.put("C", new Double[]{1.0, 0.0});
        this.values.put("F", new Double[]{9.0 / 5.0, 32.0});
        this.values.put("K", new Double[]{1.0, 273.15});
    }

    @Override
    public double convert(String from, String to, double value) {
        Double[] fromValues = values.get(from);
        Double[] toValues = values.get(to);

        return (value - fromValues[1]) / fromValues[0] * toValues[0] + toValues[1];
    }

    @Override
    public String getBase() {
        return "C";
    }

    @Override
    public Set<String> convertibles() {
        return values.keySet();
    }

}