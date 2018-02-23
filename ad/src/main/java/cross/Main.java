package cross;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.duong.mylibrary.R;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by D on 2/23/2018.
 */

public class Main extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_cross);

        CrossAd.init(this);

        for (CrossAd.VNCross vnCross : CrossAd.getCrossAd(this)) {
            Log.e("hihi", vnCross.toString());
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_cross, new CrossNativeAdFragment()).commitAllowingStateLoss();
        startActivity(new Intent(this, CrossFullscreenAdFragment.class));
    }


}
