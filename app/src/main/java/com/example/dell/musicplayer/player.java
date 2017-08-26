package com.example.dell.musicplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;

import static java.lang.Math.abs;

public class player extends AppCompatActivity implements View.OnClickListener,SensorEventListener{
    static MediaPlayer mp;
    ArrayList<File> mysongs;
    int position;
    SeekBar sb;
    Button btPlay, btFB,btFF, btPv, btNxt;
    Uri u;
    TextView t;
    Thread UpdateSeekBar;
    String string;
    SensorManager sm;
    Sensor sensor1,sensor2;
    int max,curr,val,direction=0,d=0;
    AudioManager am;
    int flag=0;
    static double div,result,r;
    String set;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        sm=(SensorManager)getSystemService(SENSOR_SERVICE);
        sensor1=sm.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sensor2=sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        am=(AudioManager)getSystemService(AUDIO_SERVICE);
        max=am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int current=am.getStreamVolume(AudioManager.STREAM_MUSIC);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        if(toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("Player");

        }

        btPlay= (Button) findViewById(R.id.btPLay);
        btFB= (Button) findViewById(R.id.btFB);
        btFF= (Button) findViewById(R.id.btFF);
        btNxt= (Button) findViewById(R.id.btNxt);
        btPv= (Button) findViewById(R.id.btPv);

        btPlay.setOnClickListener(this);
        btFB.setOnClickListener(this);
        btFF.setOnClickListener(this);
        btNxt.setOnClickListener(this);
        btPv.setOnClickListener(this);

        sb=(SeekBar) findViewById(R.id.sb);
        UpdateSeekBar = new Thread(){
            @Override
            public void run() {
                int totalduration=mp.getDuration();
                int currentposition=0;
                while (currentposition<totalduration){
                    try {
                        sleep(500);
                        currentposition=mp.getCurrentPosition();
                        sb.setProgress(currentposition);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //super.run();
            }
        };


        if(mp!=null){
            mp.stop();
            mp.release();
        }

        Intent i = getIntent();
        Bundle b = i.getExtras();
        mysongs = (ArrayList)b.getParcelableArrayList("songlist");
        position = b.getInt("pos",0);
        t = (TextView) findViewById(R.id.textView2);

        t.setText(mysongs.get(position).getName().toString().replace(".mp3","").replace(".wav",""));
        string = mysongs.get(position).getName().toString().replace(".mp3","").replace(".wav","");
        u = Uri.parse(mysongs.get(position).toString());
        mp = MediaPlayer.create(getApplicationContext(), u);
        mp.start();
        notificate();
        sb.setMax(mp.getDuration());
        UpdateSeekBar.start();
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mp.seekTo(seekBar.getProgress());
            }
        });


    }

    @Override
    public void onClick(View v) {
        int id=v.getId();
        switch (id){
            case R.id.btPLay:
                if(mp.isPlaying()){
                    mp.pause();
                    btPlay.setText(">");
                }
                else{
                    mp.start();
                    btPlay.setText("||");
                }
                break;
            case R.id.btFF:
                mp.seekTo(mp.getCurrentPosition()+5000);
                break;
            case R.id.btFB:
                mp.seekTo(mp.getCurrentPosition()-5000);
                break;
            case R.id.btNxt:
                mp.stop();
                mp.release();
                position= (position+1)%mysongs.size();
                u = Uri.parse(mysongs.get(position).toString());
                mp = MediaPlayer.create(getApplicationContext(), u);
                mp.start();
                string = mysongs.get(position).getName().toString().replace(".mp3","").replace(".wav","");
                notificate();
                t.setText(mysongs.get(position).getName().toString().replace(".mp3","").replace(".wav",""));
                sb.setMax(mp.getDuration());
                break;
            case R.id.btPv:
                mp.stop();
                mp.release();
                position= (position-1<0?mysongs.size()-1:position-1);
                u = Uri.parse(mysongs.get(position).toString());
                mp = MediaPlayer.create(getApplicationContext(), u);
                mp.start();
                string = mysongs.get(position).getName().toString().replace(".mp3","").replace(".wav","");
                notificate();
                t.setText(mysongs.get(position).getName().toString().replace(".mp3", "").replace(".wav", ""));
                sb.setMax(mp.getDuration());
                break;

        }
    }
                private void notificate(){
                    NotificationManager notificationmgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                    /*Intent intent = new Intent(this, player.class);
                    PendingIntent pintent = PendingIntent.getActivities(this, (int) System.currentTimeMillis(), new Intent[]{intent}, 0);
                    */

                    Notification notif = new Notification.Builder(this)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(string)
                            .setContentText("Your Current Song")
                            .build();

                    notificationmgr.notify(0, notif);


                }

    @Override
    protected void onPause() {
        sm.unregisterListener(this);
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        sm.registerListener(this,sensor1,SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM);
        sm.registerListener(this,sensor2,SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor s=event.sensor;
        if(s.getType()==Sensor.TYPE_ACCELEROMETER) {
            float[] values = event.values;
            int x = (int) values[0];
            int y = (int) values[1];
            int z = (int) values[2];
            // tv.setText("X=" + x + "\nY=" + y + "\nZ=" + z);


            if(direction==0)
            {
                curr = am.getStreamVolume(AudioManager.STREAM_MUSIC);
            }
            if(d==0) {
                if (y >= 5) {
                    direction = 1;
                    switch (y) {
                        case 5:
                            div = (y - 5) * ((15 - curr) / 4);
                            result = curr + div;
                            r=result;
                            am.setStreamVolume(AudioManager.STREAM_MUSIC, (int) result, 0);
                            break;
                        case 6:
                            div = (y - 5) * ((15 - curr) / 4);
                            result = curr + div;
                            if(result>=r) {
                                am.setStreamVolume(AudioManager.STREAM_MUSIC, (int) result, 0);
                                r=result;
                            }
                            else {
                                d = 1;
                            }
                            break;
                        case 7:
                            div = (y - 5) * ((15 - curr) / 4);
                            result = curr + div;
                            if(result>=r) {
                                am.setStreamVolume(AudioManager.STREAM_MUSIC, (int) result, 0);
                                r=result;
                            }
                            else
                                d=1;
                            break;
                        case 8:
                            div = (y - 5) * ((15 - curr) / 4);
                            result = curr + div;
                            if(result>=r) {
                                am.setStreamVolume(AudioManager.STREAM_MUSIC, (int) result, 0);
                                r=result;
                            }

                            else
                                d=1;
                            break;
                        case 9:
                            div = (y - 5) * ((15 - curr) / 4);
                            result = curr + div;
                            if(result>=r) {
                                am.setStreamVolume(AudioManager.STREAM_MUSIC, 15, 0);
                                r=result;
                            }
                            else
                                d=1;
                            break;
                        default:
                            result=15;
                            am.setStreamVolume(AudioManager.STREAM_MUSIC, 15, 0);
                            r=result;
                    }
                }
                else if(y<=-2)
                {
                    direction = 1;
                    switch (y) {
                        case -2:
                            div = abs(y + 2) * ((curr) / 4);
                            result = curr - div;
                            r=result;
                            am.setStreamVolume(AudioManager.STREAM_MUSIC, (int) result, 0);
                            break;
                        case -3:
                            div = abs(y + 2) * ((curr) / 4);
                            result = curr - div;
                            if(result<=r) {
                                am.setStreamVolume(AudioManager.STREAM_MUSIC, (int) result, 0);
                                r=result;
                            }
                            else {
                                d = 1;
                            }
                            break;
                        case -4:
                            div = abs(y + 2) * ((curr) / 4);
                            result = curr - div;
                            if(result<=r) {
                                am.setStreamVolume(AudioManager.STREAM_MUSIC, (int) result, 0);
                                r=result;
                            }
                            else
                                d=1;
                            break;
                        case -5:
                            div = abs(y + 2) * ((curr) / 4);
                            result = curr - div;
                            if(result<=r) {
                                am.setStreamVolume(AudioManager.STREAM_MUSIC, (int) result, 0);
                                r=result;
                            }

                            else
                                d=1;
                            break;
                        case -6:
                            div = abs(y + 2) * ((curr) / 4);
                            result = curr - div;
                            if(result<=r) {
                                am.setStreamVolume(AudioManager.STREAM_MUSIC, (int) result, 0);
                                r=result;
                            }
                            else
                                d=1;
                            break;
                        default:
                            result=0;
                            am.setStreamVolume(AudioManager.STREAM_MUSIC, (int) result, 0);
                            r=result;
                    }
                }
            }
            if (y <= 4 && y >= -1) {
                direction = 0;
                d=0;
            }
            if (flag == 0) {
                if (x <= -5) {
                    flag = 1;
                    set = "Next";
                    Toast.makeText(this, "" + set, Toast.LENGTH_SHORT).show();
                    mp.stop();
                    mp.release();
                    position= (position+1)%mysongs.size();
                    u = Uri.parse(mysongs.get(position).toString());
                    mp = MediaPlayer.create(getApplicationContext(), u);
                    mp.start();
                    string = mysongs.get(position).getName().toString().replace(".mp3","").replace(".wav","");
                    notificate();
                    t.setText(mysongs.get(position).getName().toString().replace(".mp3","").replace(".wav",""));
                    sb.setMax(mp.getDuration());



                }
                else if (x >= 5) {
                    flag = 1;
                    set = "previous";
                    Toast.makeText(this, "" + set, Toast.LENGTH_SHORT).show();
                    mp.stop();
                    mp.release();
                    position= (position-1<0?mysongs.size()-1:position-1);
                    u = Uri.parse(mysongs.get(position).toString());
                    mp = MediaPlayer.create(getApplicationContext(), u);
                    mp.start();
                    string = mysongs.get(position).getName().toString().replace(".mp3","").replace(".wav","");
                    notificate();
                    t.setText(mysongs.get(position).getName().toString().replace(".mp3", "").replace(".wav", ""));
                    sb.setMax(mp.getDuration());

                }
            } else if (flag == 1) {
                if (x >= -1 && x <= 1) {
                    flag = 0;
                    set = "normal";
                    Toast.makeText(this, "" + set, Toast.LENGTH_SHORT).show();
                }
            }
        }
        else if(s.getType()==Sensor.TYPE_PROXIMITY)
        {
            float [] values=event.values;
            if(values[0]==0) {
                mp.pause();
                set = "pause";
                Toast.makeText(this, "" + set, Toast.LENGTH_SHORT).show();
            }
            else{}

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
