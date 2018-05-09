package com.kieronquinn.app.amazfitbatterytile;

import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Handler;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import com.huami.watch.transport.DataBundle;
import com.huami.watch.transport.TransportDataItem;
import com.kieronquinn.library.amazfitcommunication.Transporter;

public class BatteryTile extends TileService {

    //Handler for waits
    private Handler handler = new Handler();

    //Has been updated
    private boolean hasUpdated = false;

    @Override
    public void onStartListening(){
        //Get QS tile
        Tile tile = getQsTile();
        //If not null, get the battery info
        if(tile != null){
            getBatteryInfo(tile);
        }
    }

    @Override
    public void onClick(){
        //Open Amazfit App if installed
        Intent intent = getPackageManager().getLaunchIntentForPackage("com.huami.watch.hmwatchmanager");
        if(intent != null){
            startActivity(intent);
        }
        //Close notifications
        Intent closeIntent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        sendBroadcast(closeIntent);
    }

    public void getBatteryInfo(final Tile tile){
        //Update
        hasUpdated = false;
        //Create transporter
        final Transporter transporter = Transporter.get(this, "com.huami.watch.companion");
        //Add a data listener for the sync reply
        transporter.addDataListener(new Transporter.DataListener() {
            @Override
            public void onDataReceived(TransportDataItem transportDataItem) {
                //Check this is actually the battery sync reply
                if(transportDataItem.getAction().equals("com.huami.watch.companion.transport.SyncBattery")){
                    //Decode battery info
                    BatteryInfo batteryInfo = BatteryInfo.fromDataBundle(transportDataItem.getData());
                    //Set icon
                    tile.setIcon(Icon.createWithResource(BatteryTile.this, getBatteryResource(batteryInfo.percent, batteryInfo.isCharging)));
                    //Set label
                    tile.setLabel(getString(R.string.amazfit_percent, String.valueOf(batteryInfo.percent), getString(R.string.percent)));
                    //Update the tile (required as this tile is passive)
                    tile.updateTile();
                    //Disconnect transporter
                    transporter.disconnectTransportService();
                    hasUpdated = true;
                }
            }
        });
        //Have to use ServiceConnectionListener as Channel Listener doesn't seem to work in this service?
        transporter.addServiceConnectionListener(new Transporter.ServiceConnectionListener() {
            @Override
            public void onServiceConnected(Bundle bundle) {
                //Service connected is unreliable and often is called before it's ready, so wait a second
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Send sync request
                        syncRequestBattery(transporter);
                    }
                }, 500);
            }

            @Override
            public void onServiceConnectionFailed(Transporter.ConnectionResult connectionResult) {
                //Failed so set tile accordingly
                setTileFailed(tile);
            }

            @Override
            public void onServiceDisconnected(Transporter.ConnectionResult connectionResult) {

            }
        });
        //Connect after a second
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                transporter.connectTransportService();
            }
        }, 500);
        //Disconnect after 5 seconds if required
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Return if the transporter isn't connected
                if(!transporter.isTransportServiceConnected())return;
                //Disconnect
                transporter.disconnectTransportService();
                //Set failed state rather than showing stale data
                if(!hasUpdated)setTileFailed(tile);
            }
        }, 5000);
    }

    private void setTileFailed(Tile tile) {
        //Return if tile is null
        if(tile == null)return;
        //Update tile to be in unknown state
        tile.setIcon(Icon.createWithResource(BatteryTile.this, R.drawable.battery_unknown));
        tile.setLabel(getString(R.string.amazfit_percent, getString(R.string.unknown), ""));
        //Update tile (required as tile is passive)
        tile.updateTile();
    }

    //Copied from Amazfit App
    private void syncRequestBattery(Transporter arg2) {
        if(arg2 != null) {
            //Create DataBundle with sync value for some reason
            DataBundle v0 = new DataBundle();
            v0.putBoolean("Sync", true);
            //Send sync request
            arg2.send("com.huami.watch.companion.transport.SyncBattery", v0);
        }
    }

    //Returns a battery icon for a given percentage and charging state
    private int getBatteryResource(int percent, boolean isCharging){
        //Simply return correct icon
        if(percent >= 100)return isCharging ? R.drawable.battery_charging_100 : R.drawable.battery;
        if(percent >= 90)return isCharging ? R.drawable.battery_charging_90 : R.drawable.battery_90;
        if(percent >= 80)return isCharging ? R.drawable.battery_charging_80 : R.drawable.battery_80;
        if(percent >= 70)return isCharging ? R.drawable.battery_charging_70 : R.drawable.battery_70;
        if(percent >= 60)return isCharging ? R.drawable.battery_charging_60 : R.drawable.battery_60;
        if(percent >= 50)return isCharging ? R.drawable.battery_charging_50 : R.drawable.battery_50;
        if(percent >= 40)return isCharging ? R.drawable.battery_charging_40 : R.drawable.battery_40;
        if(percent >= 30)return isCharging ? R.drawable.battery_charging_30 : R.drawable.battery_30;
        if(percent >= 20)return isCharging ? R.drawable.battery_charging_20 : R.drawable.battery_20;
        if(percent >= 10)return isCharging ? R.drawable.battery_charging_10 : R.drawable.battery_10;
        //Unknown value so unknown icon
        return R.drawable.battery_unknown;
    }

}
