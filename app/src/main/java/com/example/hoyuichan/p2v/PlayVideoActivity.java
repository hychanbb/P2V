package com.example.hoyuichan.p2v;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Math.random;
import static org.bytedeco.javacpp.opencv_core.addWeighted;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.resize;

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
    ArrayList<opencv_core.Mat> imagesWithEffect1 = new ArrayList<opencv_core.Mat>();
    ArrayList<opencv_core.Mat> imagesWithEffect2 = new ArrayList<opencv_core.Mat>();
    ArrayList<opencv_core.Mat> imagesWithEffect3 = new ArrayList<opencv_core.Mat>();
    ArrayList<opencv_core.Mat> images = new ArrayList<opencv_core.Mat>();

    ArrayList<opencv_core.Mat> zooming = new ArrayList<opencv_core.Mat>();
    int zoomWidth, zoomHeight, zoomStartingColumn, zoomStartingRow;
    ArrayList<opencv_core.Mat> multipleInOne = new ArrayList<opencv_core.Mat>();

    private int transitionFrameDuration;
    private int mainFrameDuration;
    ArrayList<Photo> myPhotos = new ArrayList<Photo>();
    Thread sort_photo_thread, add_template_thread, add_effect_thread, make_video_thread;

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

        add_template_thread = new Thread(add_template_worker);
        add_template_thread.start();

        add_effect_thread = new Thread(add_effect_worker);
        add_effect_thread.start();

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

    private Runnable add_template_worker = new Runnable() {
        public void run() {
            try {
                sort_photo_thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            switch(chosenTemplate){
                case 0: break;
                case 1: myPhotos.add(0, new Photo("/sdcard/P2V/template/christmas1_1.jpg"));
                        myPhotos.add(myPhotos.size()/2, new Photo("sdcard/P2V/template/christmas1_2.jpg"));
                        myPhotos.add(myPhotos.size(), new Photo("sdcard/P2V/template/christmas1_3.jpg"));
                        break;
                case 2: myPhotos.add(0, new Photo("/sdcard/P2V/template/wedding1_1.jpg"));
                        myPhotos.add(myPhotos.size()/3, new Photo("/sdcard/P2V/template/wedding1_2.jpg"));
                        myPhotos.add(myPhotos.size()*2/3, new Photo("/sdcard/P2V/template/wedding1_3.jpg"));
                        myPhotos.add(myPhotos.size(), new Photo("/sdcard/P2V/template/wedding1_4.jpg"));
                        break;
                case 3: myPhotos.add(0, new Photo("/sdcard/P2V/template/love2_1.jpg"));
                        myPhotos.add(myPhotos.size()/2, new Photo("sdcard/P2V/template/love2_2.jpg"));
                        myPhotos.add(myPhotos.size(), new Photo("sdcard/P2V/template/love2_3.jpg"));
                        break;
            }
            for (int i=0; i<myPhotos.size(); i++){
                images.add(imread(myPhotos.get(i).getPhotoPath()));
                imagesWithEffect1.add(imread(myPhotos.get(i).getPhotoPath()));
                imagesWithEffect2.add(imread(myPhotos.get(i).getPhotoPath()));
                imagesWithEffect3.add(imread(myPhotos.get(i).getPhotoPath()));
            }
        }
    };

    private Runnable add_effect_worker = new Runnable() {
        public void run() {
            try {
                add_template_thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            switch(chosenEffect){
                case 0: break;
                case 1: {
                    usingZooming();
                    break;
                }
                case 2: {
                    usingMultipleInOne();
                    break;
                }
            }
        }
    };

    private Runnable make_video_worker = new Runnable() {
        public  void run() {
            try {
                add_effect_thread.join();
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
                transitionFrameDuration = 4;
                mainFrameDuration = 80;
                recorder.start();

                switch (chosenEffect){
                    case 0: {
                        for (int i = 0; i < myPhotos.size(); i++) {
                            captured_frame = converter.convert(imread(myPhotos.get(i).getPhotoPath()));
                            for (int j = 0; j < mainFrameDuration; j++) {
                                recorder.record(captured_frame);
                            }
                        }
                        break;
                    }
                    case 1: {
                        for (int i = 0; i < zooming.size(); i++) {
                            captured_frame = converter.convert(zooming.get(i));
                            for (int j = 0; j < transitionFrameDuration; j++) {
                                recorder.record(captured_frame);
                            }
                        }
                        break;
                    }
                    case 2: {
                        for (int i=0; i<multipleInOne.size(); i++) {
                            captured_frame = converter.convert(multipleInOne.get(i));
                            for (int j = 0; j < mainFrameDuration; j++) {
                                recorder.record(captured_frame);
                            }
                        }
                        break;
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

    private int checkPhotoType(int width, int height){
        if (width > height)
            return 1;
        else if (width < height)
            return 2;
        else
            return 3;
    }

    private opencv_core.Mat zoom (opencv_core.Mat image, opencv_core.Mat background, int typeOfPhoto){
        resize(background, background, new opencv_core.Size(960, 720));
        opencv_core.Mat tempBlackGround = background.clone();
        opencv_core.Mat imageCopy = image.clone();
        resize(imageCopy, imageCopy, new opencv_core.Size(zoomWidth, zoomHeight));
        imageCopy.copyTo(tempBlackGround.colRange(zoomStartingColumn, zoomStartingColumn + zoomWidth).rowRange(zoomStartingRow, zoomStartingRow + zoomHeight));
        changeZoom(typeOfPhoto);
        return tempBlackGround;
    }

    private opencv_core.Mat customZoom(opencv_core.Mat image, opencv_core.Mat background, int width, int height, int startingColumn, int startingRow){
        resize(background, background, new opencv_core.Size(960, 720));
        opencv_core.Mat tempBlackGround = background.clone();
        opencv_core.Mat imageCopy = image.clone();
        resize(imageCopy, imageCopy, new opencv_core.Size(width, height));
        imageCopy.copyTo(tempBlackGround.colRange(startingColumn, startingColumn + width).rowRange(startingRow, startingRow + height));
        return tempBlackGround;
    }

    private void changeZoom(int typeOfPhoto){
        switch (typeOfPhoto){
            case 1: {
                zoomWidth+=8;
                zoomHeight+=6;
                zoomStartingColumn-=4;
                zoomStartingRow-=3;
                break;
            }
            case 2: {
                zoomWidth+=4;
                zoomHeight+=6;
                zoomStartingColumn-=2;
                zoomStartingRow-=3;
                break;
            }
            case 3: {
                zoomWidth+=3;
                zoomHeight+=3;
                zoomStartingColumn-=1;
                zoomStartingRow-=1;
                break;
            }
        }
    }

    private void setZoom(int typeOfPhoto){
        switch (typeOfPhoto){
            case 1: {
                zoomWidth = 640;
                zoomHeight = 480;
                zoomStartingColumn = 160;
                zoomStartingRow = 120;
                break;
            }
            case 2: {
                zoomWidth = 360;
                zoomHeight = 480;
                zoomStartingColumn = 300;
                zoomStartingRow = 120;
                break;
            }
            case 3: {
                zoomWidth = 600;
                zoomHeight = 600;
                zoomStartingColumn = 180;
                zoomStartingRow = 60;
                break;
            }
        }
    }

    private void usingZooming(){
        opencv_core.Mat black = imread("sdcard/P2V/template/black.jpg");
        for (int i=0; i<images.size(); i++){
            int typeOfPhoto = checkPhotoType(images.get(i).cols(), images.get(i).rows());
            setZoom(typeOfPhoto);

            for (int k=0; k<=40; k++){
                opencv_core.Mat temp = images.get(i).clone();
                if (k==37 && i!=images.size()-1){
                    int typeOfPhoto2 = checkPhotoType(images.get(i+1).cols(), images.get(i+1).rows());
                    switch (typeOfPhoto2){
                        case 1: {
                            addWeighted(zoom(images.get(i), black, typeOfPhoto), 0.8, customZoom(images.get(i+1), black, 640, 480, 160, 120), 0.2, 0.0, temp);
                            break;
                        }
                        case 2: {
                            addWeighted(zoom(images.get(i), black, typeOfPhoto), 0.8, customZoom(images.get(i+1), black, 360, 480, 300, 120), 0.2, 0.0, temp);
                            break;
                        }
                        case 3: {
                            addWeighted(zoom(images.get(i), black, typeOfPhoto), 0.8, customZoom(images.get(i+1), black, 600, 600, 180, 60), 0.2, 0.0, temp);
                            break;
                        }
                    }
                    zooming.add(temp);
                }
                else if (k==38 && i!=images.size()-1){
                    int typeOfPhoto2 = checkPhotoType(images.get(i+1).cols(), images.get(i+1).rows());
                    switch (typeOfPhoto2){
                        case 1: {
                            addWeighted(zoom(images.get(i), black, typeOfPhoto), 0.6, customZoom(images.get(i+1), black, 648, 486, 156, 117), 0.4, 0.0, temp);
                            break;
                        }
                        case 2: {
                            addWeighted(zoom(images.get(i), black, typeOfPhoto), 0.6, customZoom(images.get(i+1), black, 364, 486, 298, 117), 0.4, 0.0, temp);
                            break;
                        }
                        case 3: {
                            addWeighted(zoom(images.get(i), black, typeOfPhoto), 0.6, customZoom(images.get(i+1), black, 603, 603, 179, 59), 0.4, 0.0, temp);
                            break;
                        }
                    }
                    zooming.add(temp);
                }
                else if (k==39 && i!=images.size()-1){
                    int typeOfPhoto2 = checkPhotoType(images.get(i+1).cols(), images.get(i+1).rows());
                    switch (typeOfPhoto2){
                        case 1: {
                            addWeighted(zoom(images.get(i), black, typeOfPhoto), 0.4, customZoom(images.get(i+1), black, 656, 492, 152, 114), 0.6, 0.0, temp);
                            break;
                        }
                        case 2: {
                            addWeighted(zoom(images.get(i), black, typeOfPhoto), 0.4, customZoom(images.get(i+1), black, 368, 492, 296, 114), 0.6, 0.0, temp);
                            break;
                        }
                        case 3: {
                            addWeighted(zoom(images.get(i), black, typeOfPhoto), 0.4, customZoom(images.get(i+1), black, 606, 606, 178, 58), 0.6, 0.0, temp);
                            break;
                        }
                    }
                    zooming.add(temp);
                }
                else if (k==40 && i!=images.size()-1){
                    int typeOfPhoto2 = checkPhotoType(images.get(i+1).cols(), images.get(i+1).rows());
                    switch (typeOfPhoto2){
                        case 1: {
                            addWeighted(zoom(images.get(i), black, typeOfPhoto), 0.2, customZoom(images.get(i+1), black, 664, 498, 148, 111), 0.8, 0.0, temp);
                            break;
                        }
                        case 2: {
                            addWeighted(zoom(images.get(i), black, typeOfPhoto), 0.2, customZoom(images.get(i+1), black, 372, 498, 294, 111), 0.8, 0.0, temp);
                            break;
                        }
                        case 3: {
                            addWeighted(zoom(images.get(i), black, typeOfPhoto), 0.2, customZoom(images.get(i+1), black, 609, 609, 177, 57), 0.8, 0.0, temp);
                            break;
                        }
                    }
                    zooming.add(temp);
                }
                else {
                    if (k>=0 && k<4 && i!=0){
                        zoom(images.get(i), black, typeOfPhoto);
                    }
                    else {
                        zooming.add(zoom(images.get(i), black, typeOfPhoto));
                    }
                }
            }
        }
    }

    private void usingMultipleInOne(){
        opencv_core.Mat black = imread("sdcard/P2V/template/black.jpg");
        int count = images.size();
        for (int i=0; i<images.size();){
            int randomNumber = (int) (random()*100) % 8;
            if (count < 4){
                multipleInOne.add(addBackground(images.get(i++), black));
                count--;
            }
            else {
                switch (randomNumber) {
                    case 0: {
                        if (count >= 4) {
                            multipleInOne.add(combine_style_1a(images.get(i++), images.get(i++), images.get(i++), images.get(i++)));
                            count -= 4;
                        }
                        break;
                    }
                    case 1: {
                        if (count >= 4) {
                            multipleInOne.add(combine_style_1b(images.get(i++), images.get(i++), images.get(i++), images.get(i++)));
                            count -= 4;
                        }
                        break;
                    }
                    case 2: {
                        if (count >= 5) {
                            multipleInOne.add(combine_style_2a(images.get(i++), images.get(i++), images.get(i++), images.get(i++), images.get(i++)));
                            count -= 5;
                        }
                        break;
                    }
                    case 3: {
                        if (count >= 5) {
                            multipleInOne.add(combine_style_2b(images.get(i++), images.get(i++), images.get(i++), images.get(i++), images.get(i++)));
                            count -= 5;
                        }
                        break;
                    }
                    case 4: {
                        if (count >= 5) {
                            multipleInOne.add(combine_style_3a(images.get(i++), images.get(i++), images.get(i++), images.get(i++), images.get(i++)));
                            count -= 5;
                        }
                        break;
                    }
                    case 5: {
                        if (count >= 5) {
                            multipleInOne.add(combine_style_3b(images.get(i++), images.get(i++), images.get(i++), images.get(i++), images.get(i++)));
                            count -= 5;
                        }
                        break;
                    }
                    case 6: {
                        if (count >= 4) {
                            multipleInOne.add(combine_style_4(images.get(i++), images.get(i++), images.get(i++), images.get(i++)));
                            count -= 4;
                        }
                        break;
                    }
                    case 7: {
                        if (count >= 9) {
                            multipleInOne.add(combine_style_5(images.get(i++), images.get(i++), images.get(i++), images.get(i++), images.get(i++), images.get(i++), images.get(i++), images.get(i++), images.get(i++)));
                            count -= 9;
                        }
                        break;
                    }
                }
            }
        }
    }

    private opencv_core.Mat addBackground ( opencv_core.Mat image , opencv_core.Mat background) {
        resize(background, background, new opencv_core.Size(960, 720));
        opencv_core.Mat tempBlackGround = background.clone();
        if (image.cols() < image.rows()){
            resize(image, image, new opencv_core.Size(320, 480));
            image.copyTo(tempBlackGround.rowRange(0, 480).colRange(240, 560));
        }
        else {
            resize(image, image, new opencv_core.Size(640, 480));
            image.copyTo(tempBlackGround.rowRange(0, 480).colRange(80, 720));
        }
        return tempBlackGround;
    }

    private opencv_core.Mat combine_style_1a (opencv_core.Mat a, opencv_core.Mat b, opencv_core.Mat c, opencv_core.Mat d){
        opencv_core.Mat black = imread("/sdcard/Download/black.jpg");
        opencv_core.Mat temp = black.clone();
        resize(temp, temp, new opencv_core.Size(960, 720));
        resize(a, a, new opencv_core.Size(310, 230));
        resize(b, b, new opencv_core.Size(310, 230));
        resize(c, c, new opencv_core.Size(310, 230));
        resize(d, d, new opencv_core.Size(630, 710));
        a.copyTo(temp.rowRange(5, 235).colRange(5, 315));
        b.copyTo(temp.rowRange(245, 475).colRange(5, 315));
        c.copyTo(temp.rowRange(485, 715).colRange(5, 315));
        d.copyTo(temp.rowRange(5, 715).colRange(325, 955));
        imwrite("/sdcard/DCIM/Camera//" + "testinginging.jpg", black);
        return temp;
    }


    private opencv_core.Mat combine_style_1b (opencv_core.Mat a, opencv_core.Mat b, opencv_core.Mat c, opencv_core.Mat d){
        opencv_core.Mat black = imread("/sdcard/Download/black.jpg");
        opencv_core.Mat temp = black.clone();
        resize(temp, temp, new opencv_core.Size(960, 720));
        resize(a, a, new opencv_core.Size(310, 230));
        resize(b, b, new opencv_core.Size(310, 230));
        resize(c, c, new opencv_core.Size(310, 230));
        resize(d, d, new opencv_core.Size(630, 710));
        a.copyTo(temp.rowRange(5, 235).colRange(645, 955));
        b.copyTo(temp.rowRange(245, 475).colRange(645, 955));
        c.copyTo(temp.rowRange(485, 715).colRange(645, 955));
        d.copyTo(temp.rowRange(5, 715).colRange(5, 635));
        imwrite("/sdcard/DCIM/Camera//"+"testinginging.jpg", black);
        return temp;
    }

    private opencv_core.Mat combine_style_2a (opencv_core.Mat a, opencv_core.Mat b, opencv_core.Mat c , opencv_core.Mat d, opencv_core.Mat e){
        opencv_core.Mat black = imread("/sdcard/Download/black.jpg");
        opencv_core.Mat temp = black.clone();
        resize(temp, temp, new opencv_core.Size(960, 720));
        resize(a, a, new opencv_core.Size(310, 230));
        resize(b, b, new opencv_core.Size(310, 230));
        resize(c, c, new opencv_core.Size(310, 230));
        resize(d, d, new opencv_core.Size(630, 350));
        resize(e, e, new opencv_core.Size(630, 350));
        a.copyTo(temp.rowRange(5, 235).colRange(5, 315));
        b.copyTo(temp.rowRange(245, 475).colRange(5, 315));
        c.copyTo(temp.rowRange(485, 715).colRange(5, 315));
        d.copyTo(temp.rowRange(5, 355).colRange(325, 955));
        e.copyTo(temp.rowRange(365, 715).colRange(325, 955));
        imwrite("/sdcard/DCIM/Camera//"+"testinginging.jpg", black);
        return temp;
    }


    private opencv_core.Mat combine_style_2b (opencv_core.Mat a, opencv_core.Mat b, opencv_core.Mat c , opencv_core.Mat d, opencv_core.Mat e){
        opencv_core.Mat black = imread("/sdcard/Download/black.jpg");
        opencv_core.Mat temp = black.clone();
        resize(temp, temp, new opencv_core.Size(960, 720));
        resize(a, a, new opencv_core.Size(310, 230));
        resize(b, b, new opencv_core.Size(310, 230));
        resize(c, c, new opencv_core.Size(310, 230));
        resize(d, d, new opencv_core.Size(630, 350));
        resize(e, e, new opencv_core.Size(630, 350));
        a.copyTo(temp.rowRange(5, 235).colRange(645, 955));
        b.copyTo(temp.rowRange(245, 475).colRange(645, 955));
        c.copyTo(temp.rowRange(485, 715).colRange(645, 955));
        d.copyTo(temp.rowRange(5, 355).colRange(5, 635));
        e.copyTo(temp.rowRange(365, 715).colRange(5, 635));
        imwrite("/sdcard/DCIM/Camera//" + "testinginging.jpg", black);
        return temp;
    }

    private opencv_core.Mat combine_style_3a (opencv_core.Mat a, opencv_core.Mat b, opencv_core.Mat c , opencv_core.Mat d, opencv_core.Mat e){
        opencv_core.Mat black = imread("/sdcard/Download/black.jpg");
        opencv_core.Mat temp = black.clone();
        resize(temp, temp, new opencv_core.Size(960, 720));
        resize(a, a, new opencv_core.Size(450, 370));
        resize(b, b, new opencv_core.Size(450, 370));
        resize(c, c, new opencv_core.Size(300, 250));
        resize(d, d, new opencv_core.Size(300, 250));
        resize(e, e, new opencv_core.Size(300, 250));
        a.copyTo(temp.rowRange(20, 390).colRange(5, 455));
        b.copyTo(temp.rowRange(20, 390).colRange(485, 935));
        c.copyTo(temp.rowRange(420, 670).colRange(5, 305));
        d.copyTo(temp.rowRange(420, 670).colRange(325, 625));
        e.copyTo(temp.rowRange(420, 670).colRange(645, 945));
        imwrite("/sdcard/DCIM/Camera//" + "testinginging.jpg", black);
        return temp;
    }


    private opencv_core.Mat combine_style_3b (opencv_core.Mat a, opencv_core.Mat b, opencv_core.Mat c , opencv_core.Mat d, opencv_core.Mat e){
        opencv_core.Mat black = imread("/sdcard/Download/black.jpg");
        opencv_core.Mat temp = black.clone();
        resize(temp, temp, new opencv_core.Size(960, 720));
        resize(a, a, new opencv_core.Size(450, 370));
        resize(b, b, new opencv_core.Size(450, 370));
        resize(c, c, new opencv_core.Size(300, 250));
        resize(d, d, new opencv_core.Size(300, 250));
        resize(e, e, new opencv_core.Size(300, 250));
        a.copyTo(temp.rowRange(305, 675).colRange(5, 455));
        b.copyTo(temp.rowRange(305, 675).colRange(485, 935));
        c.copyTo(temp.rowRange(20, 270).colRange(5, 305));
        d.copyTo(temp.rowRange(20, 270).colRange(325, 625));
        e.copyTo(temp.rowRange(20, 270).colRange(645, 945));
        imwrite("/sdcard/DCIM/Camera//"+"testinginging.jpg", black);
        return temp;
    }

    private opencv_core.Mat combine_style_4 (opencv_core.Mat a, opencv_core.Mat b, opencv_core.Mat c, opencv_core.Mat d){
        opencv_core.Mat black = imread("/sdcard/Download/black.jpg");
        opencv_core.Mat temp = black.clone();
        resize(temp, temp, new opencv_core.Size(960, 720));
        resize(a, a, new opencv_core.Size(470, 350));
        resize(b, b, new opencv_core.Size(470, 350));
        resize(c, c, new opencv_core.Size(470, 350));
        resize(d, d, new opencv_core.Size(470, 350));
        a.copyTo(temp.rowRange(5, 355).colRange(5, 475));
        b.copyTo(temp.rowRange(5, 355).colRange(485, 955));
        c.copyTo(temp.rowRange(365, 715).colRange(5, 475));
        d.copyTo(temp.rowRange(365, 715).colRange(485, 955));
        imwrite("/sdcard/DCIM/Camera//"+"testinginging.jpg", black);
        return temp;
    }

    private opencv_core.Mat combine_style_5 (opencv_core.Mat a, opencv_core.Mat b, opencv_core.Mat c, opencv_core.Mat d, opencv_core.Mat e, opencv_core.Mat f, opencv_core.Mat g, opencv_core.Mat h,opencv_core.Mat i){
        opencv_core.Mat black = imread("/sdcard/Download/black.jpg");
        opencv_core.Mat temp = black.clone();
        resize(temp, temp, new opencv_core.Size(960, 720));
        resize(a, a, new opencv_core.Size(310, 230));
        resize(b, b, new opencv_core.Size(310, 230));
        resize(c, c, new opencv_core.Size(310, 230));
        resize(d, d, new opencv_core.Size(310, 230));
        resize(e, e, new opencv_core.Size(310, 230));
        resize(f, f, new opencv_core.Size(310, 230));
        resize(g, g, new opencv_core.Size(310, 230));
        resize(h, h, new opencv_core.Size(310, 230));
        resize(i, i, new opencv_core.Size(310, 230));
        a.copyTo(temp.rowRange(5, 235).colRange(5, 315));
        b.copyTo(temp.rowRange(5, 235).colRange(325, 635));
        c.copyTo(temp.rowRange(5, 235).colRange(645, 955));
        d.copyTo(temp.rowRange(245, 475).colRange(5, 315));
        e.copyTo(temp.rowRange(245, 475).colRange(325, 635));
        f.copyTo(temp.rowRange(245, 475).colRange(645, 955));
        g.copyTo(temp.rowRange(485,715).colRange(5 ,315));
        h.copyTo(temp.rowRange(485,715).colRange(325, 635));
        i.copyTo(temp.rowRange(485,715).colRange(645, 955));
        imwrite("/sdcard/DCIM/Camera//"+"testinginging.jpg", black);
        return temp;
    }
}
