package com.yooiistudios.news.store;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.yooiistudios.news.R;
import com.yooiistudios.news.store.iab.SKIabManager;
import com.yooiistudios.news.store.iab.SKIabManagerListener;
import com.yooiistudios.news.store.util.IabResult;
import com.yooiistudios.news.store.util.Inventory;

import butterknife.ButterKnife;

public class NLStoreActivity extends Activity implements SKIabManagerListener {

    private SKIabManager iabManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_test);
        ButterKnife.inject(this);

        initActionBar();
        initIab();
    }

    private void initActionBar() {

    }

    private void initIab() {
        iabManager = new SKIabManager(this, this);
        iabManager.loadWithAllItems();
    }

    private void initUIAfterQuery(Inventory inventory) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (iabManager != null) {
            if (iabManager.getHelper() == null) return;

            // Pass on the activity result to the helper for handling
            if (!iabManager.getHelper().handleActivityResult(requestCode, requestCode, data)) {
                // not handled, so handle it ourselves (here's where you'd
                // perform any handling of activity results not related to in-app
                // billing...
                super.onActivityResult(requestCode, resultCode, data);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.nlstore, menu);
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (iabManager != null) {
            iabManager.dispose();
        }
    }

    /**
     * IAB Listener
     */
    @Override
    public void onIabSetupFinished(IabResult result) {
    }

    @Override
    public void onIabSetupFailed(IabResult result) {
        Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onQueryFinished(Inventory inventory) {
        initUIAfterQuery(inventory);
    }

    @Override
    public void onQueryFailed(IabResult result) {
        Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
