package com.example.hoyuichan.p2v;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by OnzzZ on 29/4/2016.
 */
public class VideoSettingActivity extends Activity {
     MediaPlayer mp = new MediaPlayer();
     ArrayList<String> musicPath ;
     ArrayList<String> musicFileName = new ArrayList<String>();
     String chosenMusicPath = null;
     String chosenMusicName = null;
    String chosenEffectName;
    String chosenTemplateName;
     int  chosenEffect;
     int chosenTemplate;
    String[]  allPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_videosetting);

        Intent intent = getIntent();
        allPath = intent.getStringArrayExtra("allPath");
        musicPath = getMusicPath("mp3");
        int[] id = {R.id.templateText, R.id.effectText, R.id.musicText};
        for (int i=0; i<id.length; i++){
            setFont(id[i]);
        }

        // building ArrayList of musicFileName
        for (int i =0 ; i< musicPath.size() ; i++){
            File f = new File(musicPath.get(i));
            musicFileName.add(f.getName());
        }

        findViewById(R.id.template).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                selectTemplate();
            }
        });

        findViewById(R.id.effect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                selectEffect();
            }
        });
        findViewById(R.id.music).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                selectMusic();
            }
        });

        findViewById(R.id.btn_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                //if( (chosenMusicPath!=null)&&(chosenEffect!=0)&&(chosenTemplate!=0)){
                    Intent intent = new Intent();
                    intent.putExtra("chosenMusicPath", chosenMusicPath);
                System.out.println("chosenMusicPath" + chosenMusicPath);

                    intent.putExtra("chosenMusicName", chosenMusicName);
                System.out.println("chosenMusicName" + chosenMusicName);

                    intent.putExtra("chosenEffect", chosenEffect);
                System.out.println("chosenEffect" + chosenEffect);

                    intent.putExtra("chosenTemplate", chosenTemplate);
                System.out.println("chosenTemplate" + chosenTemplate);

                    intent.putExtra("allPath", allPath);
                System.out.println("printing all paths....");
                for(int i = 0 ; i<allPath.length ; i++) {
                    System.out.println(allPath[i]);
                }
                    intent.setClass(VideoSettingActivity.this, PlayVideoActivity.class);
                    startActivity(intent);
                //}
                /*else {
                    Toast.makeText(VideoSettingActivity.this, "Please make selection on each section ! ", Toast.LENGTH_LONG).show();
                }*/
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
        if(files.length == 0) {
            System.out.println(file.getName() +"has no files");
            return null;
        }
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
    public void selectTemplate(){
        final String[] templateInList = {"No Template", "Chiristmas","Wedding","Romentic"};
        final AlertDialog.Builder templateBuilder = new AlertDialog.Builder(VideoSettingActivity.this);
        templateBuilder.setTitle("Choose Template");
        templateBuilder.setSingleChoiceItems(templateInList, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position) {
                System.out.println("template position : " + position);
                chosenTemplateName = templateInList[position];
                chosenTemplate = position;
            }
        });
        templateBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position) {
                Button button = (Button) findViewById(R.id.template);
                button.setText(chosenTemplateName);

            }
        });
        templateBuilder.setNegativeButton("Cancel", null);
        templateBuilder.show();

    }

    public void selectEffect(){
        final String[] effectInList = {"No Effect", "Zooming","Multiple into One"};
        final AlertDialog.Builder effectBuilder = new AlertDialog.Builder(VideoSettingActivity.this);
        effectBuilder.setTitle("Choose Effect");
        effectBuilder.setSingleChoiceItems(effectInList, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position) {
                System.out.println("effect position : " + position);
                chosenEffectName = effectInList[position];
                chosenEffect = position;
            }
        });
        effectBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position) {
                Button button = (Button) findViewById(R.id.effect);
                button.setText(chosenEffectName);

            }
        });
        effectBuilder.setNegativeButton("Cancel", null);
        effectBuilder.show();
    }
    public void selectMusic (){
        String[] musicInList = musicFileName.toArray(new String[0]);
        final AlertDialog.Builder musicTemplateBuilder = new AlertDialog.Builder(VideoSettingActivity.this);
        musicTemplateBuilder.setTitle("Choose Music");
        musicTemplateBuilder.setSingleChoiceItems(musicInList, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position) {
                System.out.println("ON9 Malca 有樣睇 : " + position);
                chosenMusicPath = musicPath.get(position);
                chosenMusicName = musicFileName.get(position);
                if (mp.isPlaying()) {
                    try {
                        mp.stop();
                        mp.release();
                        mp = new MediaPlayer();
                        mp.setDataSource(chosenMusicPath);
                        mp.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    try {
                        mp = new MediaPlayer();
                        mp.setDataSource(chosenMusicPath);
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
                p1_button.setText(chosenMusicName);
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

    public ArrayList<String> getMusicPath (String type){
        ArrayList<String> musicPath = new ArrayList<String>();
        File sdCard = Environment.getExternalStorageDirectory();
        File[] files = sdCard.listFiles();
        if(files.length == 0){
            System.out.println("ExternalStorageDirectory is null");
            return null;
        }
        else {
                for (int i = 0; i < files.length; i++) {
                    ArrayList<String> temp = searchMusic(files[i], type);
                    if(temp == null){continue;}
                    musicPath.addAll(searchMusic(files[i], type));
            }
            return musicPath;
        }
    }
}
