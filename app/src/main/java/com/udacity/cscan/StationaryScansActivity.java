package com.udacity.cscan;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class StationaryScansActivity extends AppCompatActivity {
    ListView stationaryScansListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stationary_scans);
        if(!checkCameraHardware(this)){
            Toast.makeText(this, "No camera device found.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get ListView object from xml
        stationaryScansListView = (ListView) findViewById(R.id.stationary_scans_list_view);

        // Defined Array dummy values to show in ListView
        String[] values = new String[]{"Right Leg",
                "Left Leg",
                "Right Arm",
                "Left Arm"
        };

        // Define a new Adapter
        // First parameter - Context
        // Second parameter - Layout for the row
        // Third parameter - ID of the TextView to which the data is written
        // Forth - the Array of data

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, values);


        // Assign adapter to ListView
        stationaryScansListView.setAdapter(adapter);

        // ListView Item Click Listener
        stationaryScansListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item value
                String itemValue = (String) stationaryScansListView.getItemAtPosition(position);

                // Show Alert
                Toast.makeText(getApplicationContext(),
                        "Position :" + position + "  ListItem : " + itemValue, Toast.LENGTH_SHORT)
                        .show();

            }

        });
    }
    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        // Return True if this device has a camera
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    // Launch Live Scan custom camera activity

    public void LiveScanOnClick(View view) {
//        Intent liveScanIntent = new Intent(this,LiveScanActivity.class);
//        startActivity(liveScanIntent);
    }
}
