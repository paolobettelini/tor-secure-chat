package com.example.convertitore;

import com.example.convertitore.converter.Converter;
import com.example.convertitore.converter.CurrencyConverter;
import com.example.convertitore.converter.TemperatureConverter;

public class ConverterHandler {

    private static Converter currencyConverter;
    private static Converter temperatureConverter;

    static {
        currencyConverter = new CurrencyConverter();
        temperatureConverter = new TemperatureConverter();
    }

    public static Converter getCurrencyConverter() {
        return currencyConverter;
    }

    public static Converter getTemperatureConverter() {
        return temperatureConverter;
    }

}