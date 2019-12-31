package com.mrxzm.root.music;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        AdapterView.OnItemClickListener , MediaPlayer.OnCompletionListener,
        SeekBar.OnSeekBarChangeListener {

    private MediaPlayer mediaPlayer = new MediaPlayer();
    private String dirROOT = Environment.getExternalStorageDirectory() + "/netease/cloudmusic/Music/";

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private boolean isHangUp = false;
    private int currentMusic = 0;
    private Timer timer = null;
    private File[] files = null;
    private String[] names = null;

    SeekBar seekBar = null;
    TextView txtName = null;
    TextView txtStartTime = null;
    TextView txtEndTime = null;

    ImageButton btnPlay = null;
    ImageButton btnPrevious = null;
    ImageButton btnNext = null;
    ImageButton btnFf = null;
    ImageButton btnRew = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init btn
        btnPlay = findViewById(R.id.btn_media_play);
        btnNext = findViewById(R.id.btn_media_next);
        btnPrevious = findViewById(R.id.btn_media_previous);
        btnRew = findViewById(R.id.btn_media_rew);
        btnFf = findViewById(R.id.btn_media_ff);
        txtName = findViewById(R.id.txt_music_name);
        txtStartTime = findViewById(R.id.txt_start_time);
        txtEndTime = findViewById(R.id.txt_end_time);
        ListView lvMusicList = findViewById(R.id.music_list);
        this.seekBar = findViewById(R.id.seekBar);

        // event
        btnPlay.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnPrevious.setOnClickListener(this);
        btnRew.setOnClickListener(this);
        btnFf.setOnClickListener(this);
        lvMusicList.setOnItemClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);
        mediaPlayer.setOnCompletionListener(this);

        // power
        if (Build.VERSION.SDK_INT >= 23)
        {
            for (String power : PERMISSIONS_STORAGE)
            {
                if (this.checkSelfPermission(power) != PackageManager.PERMISSION_GRANTED)
                {
                    this.requestPermissions(PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
                    return;
                }
            }
        }

        // file
        files = (new File(dirROOT)).listFiles();
        if (files == null)
        {
            Toast.makeText(this,"文件为空!",Toast.LENGTH_SHORT).show();
            return;
        }
        // music name list
        names = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            names[i] = files[i].getName();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                MainActivity.this, android.R.layout.simple_list_item_1, names);
        lvMusicList.setAdapter(adapter);
        lvMusicList.setOnItemClickListener(this);

        // default
        initMediaPlayer(files[currentMusic]);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permission,
                                           int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                    File musicFile = new File(dirROOT, "music.mp3");
                    initMediaPlayer(musicFile);
                }else {
                    Toast.makeText(this,"拒绝权限不能使用应用",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_media_play)
        {
            // play
            if (mediaPlayer.isPlaying())
            {
                mediaPlayer.pause();
                btnPlay.setImageResource(android.R.drawable.ic_media_play); // play icon
            }
            else
            {
                mediaPlayer.start();
                btnPlay.setImageResource(android.R.drawable.ic_media_pause); // pause icon
            }
        }
        else
        {
            switch (id){
                case R.id.btn_media_next:
                    // next song
                    currentMusic++;
                    if (currentMusic >= files.length){currentMusic = 0;}
                    initMediaPlayer(files[currentMusic]);
                    break;
                case R.id.btn_media_previous:
                    // previous song
                    currentMusic--;
                    if (currentMusic < 0){currentMusic = files.length - 1;}
                    initMediaPlayer(files[currentMusic]);
                    break;
                case R.id.btn_media_rew:
                    // top
                    currentMusic = 0;
                    initMediaPlayer(files[currentMusic]);
                    break;
                case R.id.btn_media_ff:
                    // end
                    currentMusic = files.length - 1;
                    initMediaPlayer(files[currentMusic]);
                    break;
            }
            mediaPlayer.start();
            btnPlay.setImageResource(android.R.drawable.ic_media_pause); // pause icon
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        currentMusic = i;
        initMediaPlayer(files[currentMusic]);
        mediaPlayer.start();
        btnPlay.setImageResource(android.R.drawable.ic_media_pause); // pause icon
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        // next song
        currentMusic++;
        if (currentMusic >= files.length){currentMusic = 0;}
        initMediaPlayer(files[currentMusic]);
        mediaPlayer.start();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (isHangUp)
        {
            mediaPlayer.seekTo(i);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isHangUp = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        isHangUp = false;
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        timer.cancel();
        mediaPlayer.release();
        System.exit(0);
    }

    private void initMediaPlayer(File file)
    {
        mediaPlayer.reset();//重置
        Toast.makeText(this,"即将播放" + names[currentMusic],Toast.LENGTH_SHORT).show();
        if (timer != null){
            timer.cancel();
        }
        txtName.setText("正在播放:" + names[currentMusic]);
        try {
            mediaPlayer.setDataSource(file.getPath());
            mediaPlayer.prepare();
            txtEndTime.setText(calculateTime(mediaPlayer.getDuration() / 1000));
            initTime();

        } catch (IOException e) {
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("error")
                    .setMessage("错误:"+e.getMessage()).create();
            alertDialog.show();
            e.printStackTrace();
        }
    }

    // time
    private void initTime()
    {
        seekBar.setMax(mediaPlayer.getDuration());

        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (mediaPlayer.isPlaying())
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int progress = mediaPlayer.getCurrentPosition();
                            if (!isHangUp)
                            {
                                seekBar.setProgress(progress);
                            }
                            txtStartTime.setText(calculateTime(progress / 1000));
                        }
                    });
                }
            }
        };
        // start up
        timer.schedule(timerTask, 0, 1000);
    }

    //计算播放时间
    public String calculateTime(int time){
        int minute;
        int second;
        if(time > 60){
            minute = time / 60;
            second = time % 60;
            //分钟再0~9
            if(minute >= 0 && minute < 10){
                //判断秒
                if(second >= 0 && second < 10){
                    return "0"+minute+":"+"0"+second;
                }else {
                    return "0"+minute+":"+second;
                }
            }else {
                //分钟大于10再判断秒
                if(second >= 0 && second < 10){
                    return minute+":"+"0"+second;
                }else {
                    return minute+":"+second;
                }
            }
        }else if(time < 60){
            second = time;
            if(second >= 0 && second < 10){
                return "00:"+"0"+second;
            }else {
                return "00:"+ second;
            }
        }
        return null;
    }


}
