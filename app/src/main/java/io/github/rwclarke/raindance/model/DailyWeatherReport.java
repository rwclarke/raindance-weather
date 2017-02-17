package io.github.rwclarke.raindance.model;

/**
 * Created by ryanclarke on 2017-02-16.
 */

public class DailyWeatherReport {
    private String cityName;
    private String country;
    private int currentTemp;
    private int maxTemp;
    private int minTemp;
    private String weather;
    private String formattedDate ;

    public static final String WEATHER_TYPE_CLOUDS = "Clouds";
    public static final String WEATHER_TYPE_CLEAR = "Clear";
    public static final String WEATHER_TYPE_RAIN = "Rain";
    public static final String WEATHER_TYPE_WIND = "Wind";
    public static final String WEATHER_TYPE_SNOW = "Snow";

    public String getCityName() {
        return cityName;
    }

    public String getCountry() {
        return country;
    }

    public int getCurrentTemp() {
        return currentTemp;
    }

    public int getMaxTemp() {
        return maxTemp;
    }

    public int getMinTemp() {
        return minTemp;
    }

    public String getWeather() {
        return weather;
    }

    public String getFormattedDate() {
        return formattedDate;
    }

    public DailyWeatherReport(String cityName, String country, int currentTemp, int maxTemp, int minTemp, String weather, String formattedDate) {
        this.cityName = cityName;
        this.country = country;
        this.currentTemp = currentTemp;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
        this.weather = weather;
        this.formattedDate = rawDateToPretty(formattedDate);
    }

    public String rawDateToPretty(String rawDate) {
        // convert raw to formatted date
        return "May 1";

    }

}
