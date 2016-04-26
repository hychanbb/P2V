package com.example.hoyuichan.p2v;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.util.ArrayList;

public class SelectMusicActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_music);

        ArrayList<String> musicPath  = getMusicPath ("mp3");
        System.out.println("Printing " + musicPath);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_select_music, menu);
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
