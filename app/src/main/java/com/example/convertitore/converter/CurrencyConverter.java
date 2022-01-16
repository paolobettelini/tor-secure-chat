package com.example.convertitore.converter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Set;

public class CurrencyConverter implements Converter {

    private static final String API_KEY = "ea60c9d7cab23125cad08b453068b9fb";

    private Map<String, Double> rates;

    public CurrencyConverter() throws RuntimeException {
        String json = request("http://api.exchangeratesapi.io/v1/latest?access_key=" + API_KEY);

        if (json == null) {
            throw new RuntimeException("Could not reach exchange rates API");
        }

        // parse json
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        this.rates = new Gson().fromJson(
                obj.getAsJsonObject("rates"),
                new TypeToken<Map<String, Double>>() {
                }.getType()
        );
    }

    @Override
    public double convert(String from, String to, double value) {
        return value / rates.get(from) * rates.get(to);
    }

    @Override
    public String getBase() {
        return "EUR";
    }

    @Override
    public Set<String> convertibles() {
        return rates.keySet();
    }

    private static String request(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");

            if (connection.getResponseCode() != 200) {
                return null;
            }

            BufferedReader in = new BufferedReader(
                    new InputStreamReader((connection.getInputStream())));

            String inputLine;
            StringBuilder response = new StringBuilder();
            while((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();
            connection.disconnect();

            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}