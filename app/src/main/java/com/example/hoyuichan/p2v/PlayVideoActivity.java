package com.example.hoyuichan.p2v;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.MediaController;
import android.widget.VideoView;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.io.File;
import java.util.ArrayList;

import static org.bytedeco.javacpp.opencv_imgcodecs.imread;

public class PlayVideoActivity extends Activity {
    boolean makevideoThreadDone;
    VideoView videoview;
    String chosenMusicPath;
    String chosenMusicName ;
    int  chosenEffect;
    int chosenTemplate;
    String[] allPath;
    File makevideo;
    ArrayList<opencv_core.Mat> photos = new ArrayList<opencv_core.Mat>();
    private int transitionFrameDuration;
    private int mainFrameDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video);
        Intent intent = getIntent();
        chosenMusicPath = intent.getStringExtra("chosenMusicPath");
        chosenMusicName = intent.getStringExtra("chosenMusicName");
        chosenEffect = intent.getIntExtra("chosenEffect", 0);
        chosenTemplate = intent.getIntExtra("chosenTemplate", 0);
        allPath = intent.getStringArrayExtra("allPath");

        for (int i=0; i<allPath.length; i++){
            photos.add(imread(allPath[i]));
            System.out.println("OK");
        }

        Thread make_video_thread = new Thread(make_video_worker);
        make_video_thread.start();

        while(true){
            if (makevideoThreadDone == false){
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else {
                playVideoOnView(makevideo);
                break;
            }
        }
    }

    private Runnable make_video_worker = new Runnable() {
        public  void run() {
            OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
            makevideo = new File("/sdcard/P2V/makevideo.mp4");
            if (!makevideo.exists()){
                makevideo.getParentFile().mkdirs();
            }
            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(makevideo.getAbsolutePath(), 640, 480,1);
            Frame captured_frame;
            try {
                recorder.setFrameRate(20);
                transitionFrameDuration = 1;
                mainFrameDuration = 20;
                recorder.start();
                for (int i = 0; i < photos.size(); i++) {
                    captured_frame = converter.convert(photos.get(i));
                    for (int j = 0; j < mainFrameDuration; j++) {
                        recorder.record(captured_frame);
                    }
                }
                recorder.stop();
                recorder.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            makevideoThreadDone = true;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_play_video, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void playVideoOnView(File video) {
        videoview = (VideoView)findViewById(R.id.video01);
        videoview.setVideoPath(video.getAbsolutePath());
        videoview.setMediaController(new MediaController(PlayVideoActivity.this));
        DisplayMetrics displaymetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int h = displaymetrics.heightPixels;
        int w = displaymetrics.widthPixels;
        videoview.setMinimumHeight(h * 3);
        videoview.setMinimumWidth(w * 4);
        videoview.start();
    }
}
