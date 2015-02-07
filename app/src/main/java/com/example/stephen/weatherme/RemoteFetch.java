/*==================================================================================================
 * Class: RemoteFetch
 * Date: 2/6/2015
 * Description: This class communicates with the Open Weather Map API to get the weather conditions
 * for the desired city.
 *==================================================================================================
 */

package com.example.stephen.weatherme;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;
import android.content.Context;

public class RemoteFetch {

    private static final String OPEN_WEATHER_MAP_API =
            "http://api.openweathermap.org/data/2.5/weather?q=%s&units=metric";

    /*----------------------------------------------------------------------------------------------
     * Method: getJSON( Context context, String city)
     * Parameters: - Context context: The context in which the remoteFetch is called( in the case
     *             for this app this is WeatherActivity.java)
     *             - String city: The city that this will ask weather conditions for from the Open
     *             Weather Map API
     * Description: Creates and returns a JSONObject containing all the weather information from the
     * Open Weather Maps API
     *----------------------------------------------------------------------------------------------
     */
    public static JSONObject getJSON(Context context, String city){
        try {
            URL url = new URL(String.format(OPEN_WEATHER_MAP_API, city));
            HttpURLConnection connection =
                    (HttpURLConnection)url.openConnection();

            connection.addRequestProperty("x-api-key",
                    context.getString(R.string.open_weather_maps_id));

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            StringBuffer json = new StringBuffer(1024);
            String tmp="";
            while((tmp=reader.readLine())!=null)
                json.append(tmp).append("\n");
            reader.close();

            JSONObject data = new JSONObject(json.toString());

            // This value will be 404 if the request was not
            // successful
            if(data.getInt("cod") != 200){
                return null;
            }

            return data;
        }catch(Exception e){
            return null;
        }
    }
}
