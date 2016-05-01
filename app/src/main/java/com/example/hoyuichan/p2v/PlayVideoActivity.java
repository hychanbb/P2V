package com.example.hoyuichan.p2v;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.MediaController;
import android.widget.VideoView;

import com.example.hoyuichan.p2v.MultiplePhotoSelection.CustomGalleryActivity;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Math.random;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;

public class PlayVideoActivity extends Activity {
    boolean combineThreadDone;
    VideoView videoview;
    String chosenMusicPath;
    String chosenMusicName ;
    int  chosenEffect;
    int chosenTemplate;
    String[] allPath;
    File makevideo;
    File combine;
    ArrayList<opencv_core.Mat> photos = new ArrayList<opencv_core.Mat>();
    private int transitionFrameDuration;
    private int mainFrameDuration;
    ArrayList<Photo> myPhotos = new ArrayList<Photo>();
    Thread sort_photo_thread;
    Thread make_video_thread;

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

        myPhotos = CustomGalleryActivity.getMyPhotos();

        sort_photo_thread = new Thread(sort_photo_worker);
        sort_photo_thread.start();

        make_video_thread = new Thread(make_video_worker);
        make_video_thread.start();

        while(true){
            if (combineThreadDone == false){
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else {
                playVideoOnView(combine);
                break;
            }
        }
    }

    private Runnable make_video_worker = new Runnable() {
        public  void run() {
            try {
                sort_photo_thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
                for (int i = 0; i < myPhotos.size(); i++) {
                    //captured_frame = converter.convert(photos.get(i));
                    captured_frame = converter.convert(imread(myPhotos.get(i).getPhotoPath()));
                    for (int j = 0; j < mainFrameDuration; j++) {
                        recorder.record(captured_frame);
                    }
                }
                recorder.stop();
                recorder.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread combine_thread = new Thread(combine_worker);
            combine_thread.start();
        }
    };

    private Runnable combine_worker = new Runnable() {
        File audio;
        public void run() {
            combine = new File("/sdcard/P2V/combine.mp4");
            audio = new File(chosenMusicPath);
            FFmpegFrameGrabber grabber1 = new FFmpegFrameGrabber(makevideo.getAbsolutePath());
            FFmpegFrameGrabber grabber2 = new FFmpegFrameGrabber(audio.getAbsolutePath());
            Frame video_frame = null;
            Frame audio_frame = null;
            FFmpegFrameRecorder recorder;
            try {
                grabber1.start();
                grabber2.start();
                final int totalFrame = grabber1.getLengthInFrames();
                int addedFrame = 0;
                recorder = new FFmpegFrameRecorder(combine.getAbsolutePath(), grabber1.getImageWidth(), grabber1.getImageHeight(), grabber2.getAudioChannels());
                recorder.setFrameRate(grabber1.getFrameRate() * 2);
                //recorder.setSampleRate(grabber2.getSampleRate());
                //recorder.setFormat("mp4");
                recorder.setSampleRate(grabber2.getSampleRate());
                //recorder.setVideoBitrate(192000); // set 太底會濛
                recorder.start();
                //(video_frame = grabber1.grabFrame(true,true,true,false)
                //while(((video_frame = grabber1.grabFrame())!= null) && ((audio_frame = grabber2.grabFrame())!= null)){
                while (addedFrame < totalFrame) {
                    audio_frame = grabber2.grabFrame();
                    video_frame = grabber1.grabFrame();
                    recorder.record(video_frame);
                    recorder.record(audio_frame);
                    addedFrame++;
                }
                grabber1.stop();
                grabber2.stop();
                recorder.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
            combineThreadDone = true;
        }
    };

    private Runnable sort_photo_worker = new Runnable() {
        public void run() {
            sortByLevelOfSmile(myPhotos);
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

    private ArrayList<Photo> sortByLevelOfSmile (ArrayList<Photo> photos){
        Collections.sort(photos);
        return photos;
    }
}
