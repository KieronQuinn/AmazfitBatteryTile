package com.kieronquinn.app.amazfitbatterytile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class LaunchAmazfitActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //Start Amazfit app if installed
        Intent intent = getPackageManager().getLaunchIntentForPackage("com.huami.watch.hmwatchmanager");
        if(intent != null){
            startActivity(intent);
        }
        //Close
        finish();
    }

}
