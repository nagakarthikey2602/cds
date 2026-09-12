package com.vzc.adas;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.hardware.*;
import android.location.*;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener, LocationListener {
    SensorManager sm; Sensor accelSensor, gyroSensor;
    LocationManager lm;
    TextView status, speedV, accelV, gyroV, gpsV, riskV, predictionV, history;
    double ax=0,ay=0,az=0,gx=0,gy=0,gz=0,speed=0;
    boolean running=false;
    long lastPrediction=0;

    double clip(double x,double lo,double hi){return Math.max(lo,Math.min(hi,x));}

    void requestAccess(){
        if(android.os.Build.VERSION.SDK_INT>=23)
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.CAMERA},10);
    }

    void startMonitoring(){
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            requestAccess(); return;
        }
        running=true; status.setText("● MONITORING — sensors active");
        if(accelSensor!=null) sm.registerListener(this,accelSensor,SensorManager.SENSOR_DELAY_GAME);
        if(gyroSensor!=null) sm.registerListener(this,gyroSensor,SensorManager.SENSOR_DELAY_GAME);
        try { lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,1,this); } catch(SecurityException ignored){}
    }

    void stopMonitoring(){
        running=false; sm.unregisterListener(this);
        try{lm.removeUpdates(this);}catch(SecurityException ignored){}
        status.setText("Monitoring stopped");
    }

    void predict(){
        // Our research formula, fed by live device telemetry.
        double mag=Math.sqrt(ax*ax+ay*ay+az*az);
        double gyro=Math.sqrt(gx*gx+gy*gy+gz*gz);
        double speedRisk=clip(speed/120,0,1)*15;
        double impactRisk=clip((Math.max(0,mag-1.0)-0.25)/2,0,1)*30;
        double gyroRisk=clip((gyro-20)/100,0,1)*15;
        double brakeRisk=0; // computed from speed trend in a production version
        double score=clip(speedRisk+impactRisk+gyroRisk+brakeRisk,0,100);
        String p=score>=70?"CRASH RISK":score>=40?"NEAR-MISS RISK":"NORMAL DRIVING";
        double conf=clip(55+Math.abs(score-50)*0.8,50,99.9);
        riskV.setText(String.format(Locale.US,"%.1f / 100",score));
        predictionV.setText(p);
        predictionV.setTextColor(p.startsWith("CRASH")?Color.rgb(190,30,30):p.startsWith("NEAR")?Color.rgb(180,115,0):Color.rgb(20,125,60));
        history.setText(String.format(Locale.US,"Last decision: %s\nFormula confidence: %.1f%%\n\nLive accelerometer: %.2f m/s²\nLive gyroscope: %.1f deg/s",
            p,conf,mag,gyro));
    }

    @Override public void onSensorChanged(SensorEvent e){
        if(e.sensor.getType()==Sensor.TYPE_ACCELEROMETER){ax=e.values[0];ay=e.values[1];az=e.values[2];}
        if(e.sensor.getType()==Sensor.TYPE_GYROSCOPE){gx=e.values[0]*57.2958;gy=e.values[1]*57.2958;gz=e.values[2]*57.2958;}
        speedV.setText(String.format(Locale.US,"%.1f km/h",speed));
        accelV.setText(String.format(Locale.US,"%.2f m/s²",Math.sqrt(ax*ax+ay*ay+az*az)));
        gyroV.setText(String.format(Locale.US,"%.1f deg/s",Math.sqrt(gx*gx+gy*gy+gz*gz)));
        if(running && System.currentTimeMillis()-lastPrediction>500){lastPrediction=System.currentTimeMillis();predict();}
    }
    @Override public void onLocationChanged(Location l){speed=l.hasSpeed()?l.getSpeed()*3.6:0;gpsV.setText(String.format(Locale.US,"%.5f, %.5f",l.getLatitude(),l.getLongitude()));}
    public void onProviderEnabled(String p){} public void onProviderDisabled(String p){} public void onStatusChanged(String p,int s,android.os.Bundle b){}

    TextView tv(String t,int size){
        TextView v=new TextView(this);v.setText(t);v.setTextSize(size);v.setPadding(10,12,10,12);return v;
    }
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        sm=(SensorManager)getSystemService(SENSOR_SERVICE); lm=(LocationManager)getSystemService(LOCATION_SERVICE);
        accelSensor=sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); gyroSensor=sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(22,18,22,18);
        TextView title=tv("VZC-ADAS AUTO",28);title.setTextColor(Color.WHITE);title.setGravity(Gravity.CENTER);title.setBackgroundColor(Color.rgb(17,24,39));root.addView(title,new LinearLayout.LayoutParams(-1,80));
        status=tv("Ready — permission required",17);root.addView(status);
        LinearLayout data=new LinearLayout(this);data.setOrientation(LinearLayout.VERTICAL);
        speedV=tv("Speed: 0.0 km/h",18);accelV=tv("Acceleration: 0.0 m/s²",18);gyroV=tv("Gyroscope: 0.0 deg/s",18);gpsV=tv("GPS: waiting",16);
        data.addView(speedV);data.addView(accelV);data.addView(gyroV);data.addView(gpsV);root.addView(data);
        riskV=tv("Risk: —",25);riskV.setGravity(Gravity.CENTER);root.addView(riskV);
        predictionV=tv("NORMAL DRIVING",28);predictionV.setGravity(Gravity.CENTER);root.addView(predictionV);
        Button start=new Button(this);start.setText("START AUTOMATIC MONITORING");root.addView(start);start.setOnClickListener(v->startMonitoring());
        Button stop=new Button(this);stop.setText("STOP MONITORING");root.addView(stop);stop.setOnClickListener(v->stopMonitoring());
        history=tv("No prediction yet.",16);root.addView(history);
        TextView note=tv("Uses device accelerometer + gyroscope + GPS. Camera permission is requested for future traffic-light vision integration. Predictions are research alerts, not guaranteed accident detection.",13);root.addView(note);
        ScrollView sv=new ScrollView(this);sv.addView(root);setContentView(sv);
        requestAccess();
    }
    public void onAccuracyChanged(Sensor s,int a){}
}