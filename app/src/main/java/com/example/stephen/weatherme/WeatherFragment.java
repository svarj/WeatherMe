/*==================================================================================================
 * Class: WeatherFragment
 * Date: 2/6/2015
 * Description: This class is a fragment that is called by WeatherActivity. It is responsible for
 * providing functionality to all of the widgets defined in fragment_weather.xml as well as setting
 * the text in all of the textViews to what is provided by OpenWeatherMap's API.
 * =================================================================================================
 */

package com.example.stephen.weatherme;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONObject;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class WeatherFragment extends Fragment {

    Typeface weatherFont;

    TextView cityField;
    TextView updatedField;
    TextView detailsField;
    TextView currentTemperatureField;
    TextView weatherIcon;

    Button tButton;

    boolean isCelsius;
    double temp;

    Handler handler;

    public WeatherFragment(){
        handler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_weather, container, false);
        cityField = (TextView)rootView.findViewById(R.id.city_field);
        updatedField = (TextView)rootView.findViewById(R.id.updated_field);
        detailsField = (TextView)rootView.findViewById(R.id.details_field);
        currentTemperatureField = (TextView)rootView.findViewById(R.id.temperature_field);
        weatherIcon = (TextView)rootView.findViewById(R.id.weather_icon);

        //tButton = Change to ºC and ºF buttons
        tButton = (Button)rootView.findViewById(R.id.temp_toggle_button);
        tButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if( isCelsius ) {
                    // convert temp to Fahrenheit
                    temp = (temp * ((double) 9/5)) + 32;
                    currentTemperatureField.setText(String.format("%.2f ºF", temp));
                    tButton.setText(R.string.button_celsius);
                    isCelsius = false;
                } else {
                    // convert temp to Celsius
                    temp = (temp - 32) * ((double) 5/9);
                    currentTemperatureField.setText( String.format("%.2f ºC", temp) );
                    tButton.setText(R.string.button_fahrenheit);
                    isCelsius = true;
                }
            }
        });

        weatherIcon.setTypeface(weatherFont);
        return rootView;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        weatherFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/weather.ttf");
        updateWeatherData(new CityPreference(getActivity()).getCity());
    }

    /*----------------------------------------------------------------------------------------------
     * Method: updateWeatherData( final String city)
     * Parameter: final String city: city that will be looked for in the Open Weather Maps API
     * Description: Creates the thread used by renderWeather to update the weather data
     * ---------------------------------------------------------------------------------------------
     */
    private void updateWeatherData(final String city){
        new Thread(){
            public void run(){
                final JSONObject json = RemoteFetch.getJSON(getActivity(), city);
                if(json == null){
                    handler.post(new Runnable(){
                        public void run(){
                            Toast.makeText(getActivity(),
                                    getActivity().getString(R.string.place_not_found),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    handler.post(new Runnable(){
                        public void run(){
                            renderWeather(json);
                        }
                    });
                }
            }
        }.start();
    }

    /*----------------------------------------------------------------------------------------------
     * Method: renderWeather
     * Parameter: - JSONObject json: object containing the weather data from Open Weather Map's API
     * Description: Uses the inputted JSONObject to write the relevant weather data to the text
     * views.
     * ---------------------------------------------------------------------------------------------
     */
    private void renderWeather(JSONObject json){
        try {
            cityField.setText(json.getString("name").toUpperCase(Locale.US) +
                    ", " +
                    json.getJSONObject("sys").getString("country"));

            JSONObject details = json.getJSONArray("weather").getJSONObject(0);
            JSONObject main = json.getJSONObject("main");
            detailsField.setText(
                    details.getString("description").toUpperCase(Locale.US) +
                            "\n" + "Humidity: " + main.getString("humidity") + "%" +
                            "\n" + "Pressure: " + main.getString("pressure") + " hPa");

            currentTemperatureField.setText(
                    String.format("%.2f", main.getDouble("temp"))+ "ºC");
            temp = main.getDouble("temp");
            isCelsius = true;

            DateFormat df = DateFormat.getDateTimeInstance();
            String updatedOn = df.format(new Date(json.getLong("dt")*1000));
            updatedField.setText("Last update: " + updatedOn);

            setWeatherIcon(details.getInt("id"),
                    json.getJSONObject("sys").getLong("sunrise") * 1000,
                    json.getJSONObject("sys").getLong("sunset") * 1000);

        }catch(Exception e){
            Log.e("SimpleWeather", "One or more fields not found in the JSON data");
        }
    }

    /*----------------------------------------------------------------------------------------------
     * Method: setWeatherIcon(int actualId, long sunrise, long sunset)
     * Parameters: - int actualId: Open Weather Map's id for the inputted weather condition
     *             - long sunrise: The time when the sunrise occurs in the inputted city
     *             - long sunset: The time when the sunset occurs in the inputted city
     * Description: Called by renderWeather to set the weather icon (in the main/assets/fonts
     * folder) based on the current time of day and weather conditions.
     *----------------------------------------------------------------------------------------------
     */
    private void setWeatherIcon(int actualId, long sunrise, long sunset){
        int id = actualId / 100;
        String icon = "";
        if(actualId == 800){
            long currentTime = new Date().getTime();
            if(currentTime>=sunrise && currentTime<sunset) {
                icon = getActivity().getString(R.string.weather_sunny);
            } else {
                icon = getActivity().getString(R.string.weather_clear_night);
            }
        } else {
            switch(id) {
                case 2 : icon = getActivity().getString(R.string.weather_thunder);
                    break;
                case 3 : icon = getActivity().getString(R.string.weather_drizzle);
                    break;
                case 7 : icon = getActivity().getString(R.string.weather_foggy);
                    break;
                case 8 : icon = getActivity().getString(R.string.weather_cloudy);
                    break;
                case 6 : icon = getActivity().getString(R.string.weather_snowy);
                    break;
                case 5 : icon = getActivity().getString(R.string.weather_rainy);
                    break;
            }
        }
        weatherIcon.setText(icon);
    }

    /*----------------------------------------------------------------------------------------------
     * Method: updateWeatherData(final String city)
     * Parameter: String city: The city that will the search will be changed to
     * Description: Calls updateWeather(city) to change the weather information in the View to the
     * specified city's weather information.
     * ---------------------------------------------------------------------------------------------
    */
    public void changeCity(String city){
        updateWeatherData(city);
    }

}
