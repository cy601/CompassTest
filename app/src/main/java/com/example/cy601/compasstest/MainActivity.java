package com.example.cy601.compasstest;

import android.app.Service;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private SensorManager sensorManager;
    private ImageView compassImg;
    private TextView degreeTV;
    private TextView rotationTV;
    private Vibrator mVibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        compassImg = findViewById(R.id.arrow_img);
        degreeTV = findViewById(R.id.degreeTV);
        rotationTV = findViewById(R.id.rotationTV);

        mVibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(listener, magneticSensor, 3);
        sensorManager.registerListener(listener, accelerometerSensor, 3);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(listener);
        }
    }

    private SensorEventListener listener = new SensorEventListener() {
        float[] accelerometerValues = new float[3];
        float[] magneticValues = new float[3];
        private float lastRotateDegree;

        @Override
        public void onSensorChanged(SensorEvent event) {
            //判断当前是加速度传感器还是地磁传感器
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accelerometerValues = event.values.clone();//注意赋值要调用clone（）方法
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magneticValues = event.values.clone();//注意赋值要调用clone（）方法
            }
            float[] R = new float[9];
            float[] values = new float[3];
            
            SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticValues);
            SensorManager.getOrientation(R, values);

            //将计算出的旋转角度取反，用于旋转指南针背景图
            float rotateDegree = -(float) Math.toDegrees(values[0]);
            if (Math.abs(rotateDegree - lastRotateDegree) > 1) {
                RotateAnimation animation = new RotateAnimation(lastRotateDegree, rotateDegree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                animation.setFillAfter(true);
                compassImg.startAnimation(animation);
                lastRotateDegree = rotateDegree;

                int d = (int) lastRotateDegree;
                String degree = Integer.toString(-d);
                degreeTV.setText(degree + "°");

                if (d > -22.5 && d < 22.5) rotationTV.setText("N");
                if (d >= 22.5 && d < 67.5) rotationTV.setText("NE");
                if (d >= 67.5 && d < 112.5) rotationTV.setText("E");
                if (d >= 112.5 && d < 157.5) rotationTV.setText("SE");
                if ((d >= 157.5 && d < 180) || (d >= -180 && d < -157.5)) rotationTV.setText("S");
                if (d >= -157.5 && d < -112.5) rotationTV.setText("SW");
                if (d >= -112.5 && d < -67.5) rotationTV.setText("W");
                if (d >= -67.5 && d < -22.5) rotationTV.setText("NW");


                if (d > -2 && d < 2) mVibrator.vibrate(30);
            }
            Log.d("MainActivity", "value[0] is" + Math.toDegrees(values[0]));

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
}
