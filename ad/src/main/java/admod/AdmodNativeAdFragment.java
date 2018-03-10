package admod;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.duong.mylibrary.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;

import inter.OnErrorLoadAd;

/**
 * Created by d on 1/24/2018.
 */
/*


 */
public class AdmodNativeAdFragment extends Fragment {
    private OnErrorLoadAd onErrorLoadAd;

    public void setIdAd(String idAd) {
        this.idAd = idAd;
    }

    private String idAd;

    public void setOnErrorLoadAd(OnErrorLoadAd onErrorLoadAd) {
        this.onErrorLoadAd = onErrorLoadAd;
//        if (onErrorLoadAd != null)
//            onErrorLoadAd.onError();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.admod_native_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showNativeAd();
    }


    NativeExpressAdView mAdView;
    VideoController mVideoController;

    private void showNativeAd() {
        mAdView = getView().findViewById(R.id.adView);
//        mAdView.setAdUnitId(idAd);
//        mAdView.setAdSize(AdSize.MEDIUM_RECTANGLE);
        mAdView.setVideoOptions(new VideoOptions.Builder()
                .setStartMuted(true)
                .build());
        mVideoController = mAdView.getVideoController();
        mVideoController.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
            @Override
            public void onVideoEnd() {
                super.onVideoEnd();
            }
        });
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                if (mVideoController.hasVideoContent()) {
                } else {
                }
            }

            @Override
            public void onAdFailedToLoad(int i) {
                if (onErrorLoadAd != null)
                    onErrorLoadAd.onMyError();
                super.onAdFailedToLoad(i);

            }
        });

        mAdView.loadAd(new AdRequest.Builder().addTestDevice("E3DB9C939E65BA7A72A0F9CC3FDEB9E9").build());
    }
}

