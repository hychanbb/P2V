package com.example.hoyuichan.p2v;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by OnzzZ on 29/4/2016.
 */
public class VideoSettingActivity extends Activity {
     MediaPlayer mp = new MediaPlayer();
     ArrayList<String> musicPath = getMusicPath("mp3");
     ArrayList<String> musicFileName = new ArrayList<String>();
     String musicChoosenPath = null;
     String musicChoosenName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_videosetting);

        Intent intent = getIntent();
        String[] s = intent.getStringArrayExtra("all_path");

        int[] id = {R.id.templateText, R.id.effectText, R.id.musicText};
        for (int i=0; i<id.length; i++){
            setFont(id[i]);
        }
        // building ArrayList of musicFileName
        for (int i =0 ; i< musicPath.size() ; i++){
            File f = new File(musicPath.get(i));
            musicFileName.add(f.getName());
        }


        findViewById(R.id.music).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                String[] musicInList = musicFileName.toArray(new String[0]);
                final AlertDialog.Builder musicTemplateBuilder = new AlertDialog.Builder(VideoSettingActivity.this);
                LayoutInflater musicTemplateInflater = getLayoutInflater();
                musicTemplateBuilder.setTitle("Choose Music");
                musicTemplateBuilder.setSingleChoiceItems(musicInList, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        System.out.println("ON9 Malca 有樣睇 : " + position);
                        musicChoosenPath = musicPath.get(position);
                        musicChoosenName = musicFileName.get(position);
                        if (mp.isPlaying()) {
                            try {
                                mp.stop();
                                mp.release();
                                mp = new MediaPlayer();
                                mp.setDataSource(musicChoosenPath);
                                mp.prepare();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        else{
                            try {
                                mp = new MediaPlayer();
                                mp.setDataSource(musicChoosenPath);
                                mp.prepare();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        mp.start();
                    }
                });
                musicTemplateBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        System.out.println("fk you Malca : " + position);
                        Button p1_button = (Button) findViewById(R.id.music);
                        p1_button.setText(musicChoosenName);
                        if (mp.isPlaying()) {
                            mp.stop();
                        }
                    }
                });
                musicTemplateBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mp.isPlaying()) {
                            mp.stop();
                        }
                    }
                });
                musicTemplateBuilder.show();
            }
        });
    }


    public void setFont(int id){
        Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fff_Tusj.ttf");
        TextView myText = (TextView) findViewById(id);
        myText.setTypeface(myTypeface);
    }





    private ArrayList<String> searchMusic(File file, String type){
        ArrayList<String> musicSubPath = new ArrayList<String>();
        File[] files = file.listFiles();

        for (int i =0; i < files.length; i++) {
            String filePath = files[i].getAbsolutePath();
            String fileName = files[i].getName();
            //System.out.println("fileName : " + fileName);
            //System.out.println("filePath : " + filePath);
            if (fileName.endsWith(type)) {
                //System.out.println(fileName + "it is a song ");
                musicSubPath.add(filePath);
            }
        }
        return musicSubPath;
    }

    public ArrayList<String> getMusicPath (String type){
        ArrayList<String> musicPath = new ArrayList<String>();
        File sdCard = Environment.getExternalStorageDirectory();
        File[] files = sdCard.listFiles();
        for (int i =0; i < files.length; i++) {
            musicPath.addAll(searchMusic(files[i], type));
        }
        return musicPath;
    }
}
