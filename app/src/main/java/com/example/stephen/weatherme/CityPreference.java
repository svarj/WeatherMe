/*==================================================================================================
 * Class: CityPreference
 * Date: 2/6/2015
 * Description: CityPreference is used to store the user's last inputted city so that they do not
 * have to repeatedly input the same city.
 *==================================================================================================
 */

package com.example.stephen.weatherme;

import android.app.Activity;
import android.content.SharedPreferences;

public class CityPreference {

    SharedPreferences prefs;

    public CityPreference(Activity activity){
        prefs = activity.getPreferences(Activity.MODE_PRIVATE);
    }

    // If the user has not chosen a city yet, return
    // Las Vegas as the default city
    public String getCity(){
        return prefs.getString("city", "Las Vegas, US");
    }

    void setCity(String city){
        prefs.edit().putString("city", city).commit();
    }

}
