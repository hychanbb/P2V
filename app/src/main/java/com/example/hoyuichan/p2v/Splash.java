package com.example.hoyuichan.p2v;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Splash extends Activity {

    /** Duration of wait **/
    private final int SPLASH_DISPLAY_LENGTH =3000;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);
        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/

                saveTemplateImage();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                /* Create an Intent that will start the Menu-Activity. */
                        Intent mainIntent = new Intent(Splash.this, MainActivity.class);
                        Splash.this.startActivity(mainIntent);
                        Splash.this.finish();
                    }
                }, SPLASH_DISPLAY_LENGTH);


    }


    private void saveTemplateImage(){
        String[] templateFileName = {"christmas1_1.jpg", "christmas1_2.jpg", "christmas1_3.jpg", "wedding1_1.jpg", "wedding1_2.jpg", "wedding1_3.jpg",
                "wedding1_4.jpg", "love2_1.jpg", "love2_2.jpg", "love2_3.jpg", "black.jpg"};
        int[] resourceID = {R.drawable.christmas1_1, R.drawable.christmas1_2, R.drawable.christmas1_3, R.drawable.wedding1_1, R.drawable.wedding1_2,
                R.drawable.wedding1_3, R.drawable.wedding1_4, R.drawable.love2_1, R.drawable.love2_2, R.drawable.love2_3, R.drawable.black};

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize =1;
        for (int i=0; i<templateFileName.length; i++) {
            Bitmap bmp = BitmapFactory.decodeResource(getResources(), resourceID[i], options);
            File f = new File("/sdcard/P2V/template/", templateFileName[i]);
            if (!f.exists()) {
                f.getParentFile().mkdirs();
            }
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(f);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();
                bmp.recycle();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Path is here :  " + f.getAbsolutePath());
        }
    }

}
