package com.example.convertitore.converter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;

public class LanguageConverter implements Converter {

    @Override
    public double convert(String from, String to, double value) {
        return 0;
    }

    @Override
    public String getBase() {
        return null;
    }

    @Override
    public Set<String> convertibles() {
        return null;
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
            while ((inputLine = in.readLine()) != null) {
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
