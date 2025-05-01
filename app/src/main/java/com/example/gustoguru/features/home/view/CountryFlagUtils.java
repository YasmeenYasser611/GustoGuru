package com.example.gustoguru.features.home.view;
import java.util.HashMap;
import java.util.Map;

public class CountryFlagUtils {

    private static final Map<String, String> COUNTRY_TO_ISO = new HashMap<>();

    static {
        // Full mapping for all countries in TheMealDB
        COUNTRY_TO_ISO.put("American", "us");
        COUNTRY_TO_ISO.put("British", "gb");
        COUNTRY_TO_ISO.put("Canadian", "ca");
        COUNTRY_TO_ISO.put("Chinese", "cn");
        COUNTRY_TO_ISO.put("Croatian", "hr");
        COUNTRY_TO_ISO.put("Dutch", "nl");
        COUNTRY_TO_ISO.put("Egyptian", "eg");
        COUNTRY_TO_ISO.put("Filipino", "ph");
        COUNTRY_TO_ISO.put("French", "fr");
        COUNTRY_TO_ISO.put("Greek", "gr");
        COUNTRY_TO_ISO.put("Indian", "in");
        COUNTRY_TO_ISO.put("Irish", "ie");
        COUNTRY_TO_ISO.put("Italian", "it");
        COUNTRY_TO_ISO.put("Jamaican", "jm");
        COUNTRY_TO_ISO.put("Japanese", "jp");
        COUNTRY_TO_ISO.put("Kenyan", "ke");
        COUNTRY_TO_ISO.put("Malaysian", "my");
        COUNTRY_TO_ISO.put("Mexican", "mx");
        COUNTRY_TO_ISO.put("Moroccan", "ma");
        COUNTRY_TO_ISO.put("Polish", "pl");
        COUNTRY_TO_ISO.put("Portuguese", "pt");
        COUNTRY_TO_ISO.put("Russian", "ru");
        COUNTRY_TO_ISO.put("Spanish", "es");
        COUNTRY_TO_ISO.put("Thai", "th");
        COUNTRY_TO_ISO.put("Tunisian", "tn");
        COUNTRY_TO_ISO.put("Turkish", "tr");
        COUNTRY_TO_ISO.put("Ukrainian", "ua");
        COUNTRY_TO_ISO.put("Uruguayan", "uy");
        COUNTRY_TO_ISO.put("Vietnamese", "vn");
    }


    public static String getFlagUrl(String mealDbCountryName) {
        String isoCode = COUNTRY_TO_ISO.getOrDefault(mealDbCountryName, "unknown");
        return "https://flagcdn.com/w160/" + isoCode + ".png";
    }


    public static String getIsoCode(String mealDbCountryName) {
        return COUNTRY_TO_ISO.getOrDefault(mealDbCountryName, "un");
    }
}