package com.example.madassignmentsensorapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // Sensor Manager to access device sensors
    private SensorManager sensorManager;

    // The three sensors
    private Sensor accelerometer;
    private Sensor lightSensor;
    private Sensor proximitySensor;

    // UI TextViews to display data
    private TextView tvAccelerometer;
    private TextView tvLight;
    private TextView tvProximity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Link UI elements
        tvAccelerometer = findViewById(R.id.tvAccelerometer);
        tvLight = findViewById(R.id.tvLight);
        tvProximity = findViewById(R.id.tvProximity);

        // Get the SensorManager system service
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Get each sensor
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        // Check if sensors are available on this device
        if (accelerometer == null) tvAccelerometer.setText("Accelerometer: Not available");
        if (lightSensor == null) tvLight.setText("Light Sensor: Not available");
        if (proximitySensor == null) tvProximity.setText("Proximity: Not available");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register listeners when app is visible
        // SENSOR_DELAY_NORMAL = updates every ~200ms (good for display)
        if (accelerometer != null)
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        if (lightSensor != null)
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        if (proximitySensor != null)
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister listeners when app is not visible (saves battery)
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // This method is called every time a sensor value changes
        switch (event.sensor.getType()) {

            case Sensor.TYPE_ACCELEROMETER:
                // Accelerometer gives X, Y, Z values
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                tvAccelerometer.setText(String.format(
                        "X: %.2f m/s²\nY: %.2f m/s²\nZ: %.2f m/s²", x, y, z));
                break;

            case Sensor.TYPE_LIGHT:
                // Light sensor gives one value in lux
                float light = event.values[0];
                tvLight.setText(String.format("Light: %.2f lx", light));
                break;

            case Sensor.TYPE_PROXIMITY:
                // Proximity sensor gives distance in cm
                float proximity = event.values[0];
                String proximityStatus = (proximity == 0) ? "NEAR" : "FAR";
                tvProximity.setText(String.format(
                        "Proximity: %.2f cm (%s)", proximity, proximityStatus));
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for this app but must be implemented
    }
}