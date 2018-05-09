package com.kieronquinn.app.amazfitbatterytile;

import com.huami.watch.transport.DataBundle;

public class BatteryInfo {

    //Stores percentage and charging state
    public int percent = 100;
    public boolean isCharging = false;

    //Create from DataBundle in format identical to that of Amazfit App
    public static BatteryInfo fromDataBundle(DataBundle dataBundle){
        //Create instance
        BatteryInfo batteryInfo = new BatteryInfo();
        //Set percentage and charging state
        batteryInfo.percent = dataBundle.getInt("BatteryLevel");
        batteryInfo.isCharging = dataBundle.getBoolean("BatteryIsCharging");
        //Return instance
        return batteryInfo;
    }

}
