/**
 * Copyright (C) 2013 Geoffrey Falk
 */

package org.maskmedia.roboliterate.uicontroller;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import org.maskmedia.roboliterate.R;
import org.maskmedia.roboliterate.robotcomms.Robot;
import org.maskmedia.roboliterate.robotcomms.Robot.Motor;
import org.maskmedia.roboliterate.robotcomms.Robot.Sensor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


/**
 * Activity for configuring robot to chosen ports.
 * TODO Change so that it does not use Robot file, which should not really be accessible to UI Layer
 */
public class ConfigureDeviceActivity  extends Activity  {

    // for debugging

    private static final String TAG = "ConfigureDeviceActivity";
    private static final String MY_PREFS = "MyPrefs";

    private ArrayList<Integer> mMotorPortList;
    private ArrayList<Integer> mSensorPortList;
    private boolean isUsingSensors;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 11) {
          //  on older android devices activity will not be closed on touch outside.
            setFinishOnTouchOutside(false);
        }

        // Set result CANCELED in case the user backs out

        setResult(Activity.RESULT_CANCELED);

       String callingClass = getCallingActivity().getClassName();

        if (callingClass.equals(RemoteControlActivity.class.getName()))
            isUsingSensors = false;
        else
        isUsingSensors = true;
//
       setTitle("Configure your "+((Robot.getRobotType()==Robot.MODEL_EV3)? "EV3" : "NXT"));


        // For mapping port letters to numbers when a selection is made

        mMotorPortList = new ArrayList<Integer>();
        int[] actualPortNumbers = Robot.getMotorPortNumbers();
        for (int i : actualPortNumbers) {
            mMotorPortList.add(i);
        }
        mMotorPortList.add(-1);


        super.onCreate(savedInstanceState);
        setContentView((isUsingSensors)?R.layout.activity_configure_robot:R.layout.activity_configure_motors_only);
//
        ArrayAdapter<CharSequence> motorPortAdapter = ((Robot.getRobotType() == Robot.MODEL_NXT) ?
                ArrayAdapter.createFromResource(this, R.array.nxt_motor_ports, android.R.layout.simple_spinner_item) :
                ArrayAdapter.createFromResource(this, R.array.ev3_motor_ports, android.R.layout.simple_spinner_item));
        motorPortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


       // set up Left Motor Spinner
        Spinner leftMotorSpinner = (Spinner) findViewById(R.id.leftmotor_spinner);
        leftMotorSpinner.setAdapter(motorPortAdapter);
        leftMotorSpinner.setOnItemSelectedListener(new OnMotorPortSelectedListener(Motor.LEFT));
        leftMotorSpinner.setSelection(mMotorPortList.indexOf(Motor.LEFT.getPort()));


//
        // set up Right Motor Spinner
        Spinner rightMotorSpinner = (Spinner) findViewById(R.id.rightmotor_spinner);
        rightMotorSpinner.setAdapter(motorPortAdapter);
        rightMotorSpinner.setOnItemSelectedListener(new OnMotorPortSelectedListener(Motor.RIGHT));
        rightMotorSpinner.setSelection(mMotorPortList.indexOf(Motor.RIGHT.getPort()));




        if (isUsingSensors) {

        mSensorPortList = new ArrayList<Integer>();
        mSensorPortList.add(0);
        mSensorPortList.add(1);
        mSensorPortList.add(2);
        mSensorPortList.add(3);
        mSensorPortList.add(-1);



        ArrayAdapter<CharSequence> sensorPortAdapter =
                ArrayAdapter.createFromResource(this, R.array.sensor_ports, android.R.layout.simple_spinner_item);
        sensorPortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


/*

            // set up Arm Motor Spinner
            Spinner armMotorSpinner = (Spinner) findViewById(R.id.armmotor_spinner);
            armMotorSpinner.setAdapter(motorPortAdapter);
            armMotorSpinner.setOnItemSelectedListener(new OnMotorPortSelectedListener(Motor.ARM));
            armMotorSpinner.setSelection(mMotorPortList.indexOf(Motor.ARM.getPort()));
*/



        // set up color sensor spinner
        Spinner colorSensorSpinner = (Spinner) findViewById(R.id.color_spinner);
        colorSensorSpinner.setAdapter(sensorPortAdapter);
        colorSensorSpinner.setOnItemSelectedListener(new OnSensorPortSelectedListener(Sensor.LIGHT));
        colorSensorSpinner.setSelection(mSensorPortList.indexOf(Sensor.LIGHT.getPort()));

        // set up sound sensor spinner
        Spinner soundSensorSpinner = (Spinner) findViewById(R.id.sound_spinner);
        soundSensorSpinner.setAdapter(sensorPortAdapter);
        soundSensorSpinner.setOnItemSelectedListener(new OnSensorPortSelectedListener(Sensor.SOUND));
        soundSensorSpinner.setSelection(mSensorPortList.indexOf(Sensor.SOUND.getPort()));

        // set up ultrasonic sensor spinner
        Spinner ultrasonicSensorSpinner = (Spinner) findViewById(R.id.ultrasonic_spinner);
        ultrasonicSensorSpinner.setAdapter(sensorPortAdapter);
        ultrasonicSensorSpinner.setOnItemSelectedListener(new OnSensorPortSelectedListener(Sensor.ULTRASONIC));
        ultrasonicSensorSpinner.setSelection(mSensorPortList.indexOf(Sensor.ULTRASONIC.getPort()));


        // set up touch sensor spinner
        Spinner touchSensorSpinner = (Spinner) findViewById(R.id.switch_spinner);
        touchSensorSpinner.setAdapter(sensorPortAdapter);
        touchSensorSpinner.setOnItemSelectedListener(new OnSensorPortSelectedListener(Sensor.SWITCH));
        touchSensorSpinner.setSelection(mSensorPortList.indexOf(Sensor.SWITCH.getPort()));;


        // set up infrared sensor spinner
            Spinner infraredSensorSpinner = (Spinner)findViewById(R.id.infrared_spinner);
            infraredSensorSpinner.setAdapter(sensorPortAdapter);
            infraredSensorSpinner.setOnItemSelectedListener(new OnSensorPortSelectedListener(Sensor.INFRARED));
            infraredSensorSpinner.setSelection(mSensorPortList.indexOf(Sensor.INFRARED.getPort()));

        }

        //set up done button
        Button doneButton = (Button) findViewById(R.id.doneButton);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int unusedSensors = 0;
                Set<Integer> sensorPortSet = new HashSet<Integer>();
                if (isUsingSensors) {
                Sensor[] sensors = new Sensor[]{Sensor.LIGHT, Sensor.SOUND, Sensor.ULTRASONIC, Sensor.SWITCH};
                for (Sensor sensor : sensors) {
                    if (sensor.getPort()==-1) unusedSensors++;
                 else sensorPortSet.add(sensor.getPort());
                }

                }

                if (Motor.LEFT.getPort() == Motor.RIGHT.getPort()) {
                    Toast.makeText(getApplicationContext(), "Error. You cannot map two motors to the same port.", Toast.LENGTH_SHORT).show();

                } else if (isUsingSensors && sensorPortSet.size()+unusedSensors<4) {
                    Toast.makeText(getApplicationContext(), "You cannot map more than one sensor to the same port.", Toast.LENGTH_SHORT).show();

                } else {
                    setResult(RESULT_OK);
                    finish();
                }

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public void onBackPressed() {
            Intent intent = new Intent();
            setResult(Activity.RESULT_CANCELED, intent);
        super.onBackPressed();
        finish();
    }

    /**
     * Inner class listening to changes to spinners. Each spinner has its own ID mapping to a port type
     */
    class OnMotorPortSelectedListener implements AdapterView.OnItemSelectedListener {

        private Motor mMotor;

        public OnMotorPortSelectedListener (Motor motor) {
        mMotor = motor;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
            Object selection = parent.getItemAtPosition(position);
            Log.d(TAG, "Selected item: "+selection.toString());
            SharedPreferences sharedPreferences = getSharedPreferences(MY_PREFS,Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            switch (mMotor) {
                case LEFT:
//                    Motor.LEFT.setPort(mMotorPortMappings.get(selection.toString()));
                    Motor.LEFT.setPort(mMotorPortList.get(position));
                    editor.putInt("MOTOR_LEFT",mMotorPortList.get(position));
                    break;
                case RIGHT:
                    Motor.RIGHT.setPort(mMotorPortList.get(position));
                    editor.putInt("MOTOR_RIGHT",mMotorPortList.get(position));
                    break;
/*                case ARM:
                    Motor.ARM.setPort(mMotorPortList.get(position));
                    editor.putInt("MOTOR_ARM",mMotorPortList.get(position));
                    break;*/
                default:
                    break;

            }
            editor.commit();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    }


    class OnSensorPortSelectedListener implements AdapterView.OnItemSelectedListener {

        private Sensor mSensor;

        public OnSensorPortSelectedListener (Sensor sensor) {
            mSensor = sensor;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
            Object selection = parent.getItemAtPosition(position);
            Log.d(TAG, "Selected item: "+selection.toString());
            SharedPreferences sharedPreferences = getSharedPreferences(MY_PREFS,Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            switch (mSensor) {
                case SOUND:
                    Sensor.SOUND.setPort(mSensorPortList.get(position));
                    editor.putInt("SENSOR_SOUND",mSensorPortList.get(position));
                    break;
                case LIGHT:
                    Sensor.LIGHT.setPort(mSensorPortList.get(position));
                    editor.putInt("SENSOR_LIGHT",mSensorPortList.get(position));
                    break;
                case ULTRASONIC:
                    Sensor.ULTRASONIC.setPort(mSensorPortList.get(position));
                    editor.putInt("SENSOR_ULTRASONIC",mSensorPortList.get(position));
                    break;
                case SWITCH:
                    Sensor.SWITCH.setPort(mSensorPortList.get(position));
                    editor.putInt("SENSOR_SWITCH",mSensorPortList.get(position));
                    break;
                case INFRARED:
                    Sensor.INFRARED.setPort(mSensorPortList.get(position));
                    editor.putInt("SENSOR_INFRARED",mSensorPortList.get(position));
                default:
                    break;

            }
            editor.commit();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    }


}
