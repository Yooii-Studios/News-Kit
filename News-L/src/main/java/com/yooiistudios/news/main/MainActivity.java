package com.yooiistudios.news.main;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yooiistudios.news.R;
import com.yooiistudios.news.detail.NLDetailActivity;
import com.yooiistudios.news.store.NLStoreActivity;

import java.io.IOException;


public class MainActivity extends Activity {
    private ImageView mTopNewsImageView;
    private TextView mTopNewsTitle;


    public static Bitmap sBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTopNewsImageView = (ImageView)findViewById(R.id.topNewsImageView);
        mTopNewsTitle = (TextView)findViewById(R.id.topNewsTitle);

        try {
            sBitmap = BitmapFactory.decodeStream(getResources().getAssets()
                    .open("lone_pine_sunset.jpg"));
            mTopNewsImageView.setImageBitmap(sBitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mTopNewsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                ActivityOptions options2 = ActivityOptions.
//                        makeSceneTransitionAnimation(MainActivity.this,
//                                Pair.create(mTopNewsImageView, "topImage"),
//                                Pair.create(mTopNewsTitle, "topTitle"));
//                ActivityOptions options2 = ActivityOptions.
//                        makeSceneTransitionAnimation(MainActivity.this,
//                                mTopNewsImageView, "");

                ActivityOptions activityOptions =
                        ActivityOptions.makeSceneTransitionAnimation(
                                MainActivity.this,
                                new Pair<View, String>(mTopNewsImageView,
                                        "topImage"),
                                new Pair<View, String>(mTopNewsTitle,
                                        "topTitle")
                        );
                ActivityOptions activityOptions2 = ActivityOptions.
                        makeSceneTransitionAnimation(MainActivity.this,
                                mTopNewsTitle, "topTitle");

                startActivity(new Intent(MainActivity.this,
                        NLDetailActivity.class), activityOptions.toBundle());
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_store) {
            startActivity(new Intent(MainActivity.this, NLStoreActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
