
package com.xinlan.imageeditlibrary.picchooser;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import com.xinlan.imageeditlibrary.BaseActivity;
import com.xinlan.imageeditlibrary.StatusBar.StatusBar;

public class SelectPictureActivity extends BaseActivity {
    @Override
    protected void onCreate(final Bundle b) {
        super.onCreate(b);

        new StatusBar(this); // 沉浸式状态栏 初始化 每个activity和fragement都应该有

        checkInitImageLoader();
        setResult(RESULT_CANCELED);

        // Create new fragment and transaction
        Fragment newFragment = new BucketsFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this
        // fragment,
        // and add the transaction to the back stack
        transaction.replace(android.R.id.content, newFragment);

        // Commit the transaction
        transaction.commit();
    }

    void showBucket(final int bucketId) {
        Bundle b = new Bundle();
        b.putInt("bucket", bucketId);
        Fragment f = new ImagesFragment();
        f.setArguments(b);
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, f).addToBackStack(null).commit();
    }

    void imageSelected(final String imgPath, final String imgTaken, final long imageSize) {
        returnResult(imgPath, imgTaken, imageSize);
    }

    private void returnResult(final String imgPath, final String imageTaken, final long imageSize) {
        Intent result = new Intent();
        result.putExtra("imgPath", imgPath);
        result.putExtra("dateTaken", imageTaken);
        result.putExtra("imageSize", imageSize);
        setResult(RESULT_OK, result);
        finish();
    }
}
