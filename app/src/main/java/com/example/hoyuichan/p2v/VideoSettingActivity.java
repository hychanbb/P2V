package com.example.hoyuichan.p2v;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

/**
 * Created by OnzzZ on 29/4/2016.
 */
public class VideoSettingActivity extends Activity {

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
    }

    public void setFont(int id){
        Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fff_Tusj.ttf");
        TextView myText = (TextView) findViewById(id);
        myText.setTypeface(myTypeface);
    }
}
