package richadx;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.duong.mylibrary.R;
import com.facebook.ads.Ad;
import com.facebook.ads.AdChoicesView;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;

import inter.OnErrorLoadAd;

import static com.google.android.gms.ads.AdSize.MEDIUM_RECTANGLE;

/**
 * Created by d on 1/24/2018.
 */
/*


 */
public class RichNativeAd {
    OnErrorLoadAd onErrorLoadAd;
    Context mContext;
    View mViewContainer;
    String idAd;

    public RichNativeAd(Context mContext, View mViewContainer, String idAd) {
        this.mContext = mContext;
        this.mViewContainer = mViewContainer;
        this.idAd = idAd;
    }

    public void setOnErrorLoadAd(OnErrorLoadAd onErrorLoadAd) {
        this.onErrorLoadAd = onErrorLoadAd;
    }

    private AdView adViewPlayer;

    public void show() {
        adViewPlayer = new AdView(mContext);
        adViewPlayer.setAdSize(MEDIUM_RECTANGLE);
        adViewPlayer.setAdUnitId(idAd); // richadx
        final LinearLayout nativeAdContainer = (LinearLayout) mViewContainer;
        try {
            AdRequest.Builder adRequestBuilderHeader = new AdRequest.Builder();
            nativeAdContainer.removeAllViews();
            nativeAdContainer.addView(adViewPlayer);
            adViewPlayer.loadAd(adRequestBuilderHeader.build());
            adViewPlayer.setAdListener(
                    new com.google.android.gms.ads.AdListener() {
                        @Override
                        public void onAdFailedToLoad(int i) {
                            super.onAdFailedToLoad(i);
                            if (onErrorLoadAd != null)
                                onErrorLoadAd.onMyError();
                        }
                    });

        } catch (Exception e) {
        }

    }
}
