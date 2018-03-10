package admod;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.duong.mylibrary.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import inter.OnErrorLoadAd;

import static com.google.android.gms.ads.AdSize.BANNER;

/**
 * Created by d on 1/24/2018.
 */

public class AdmodBannerAdFragment extends Fragment {
    private OnErrorLoadAd onErrorLoadAd;
    private AdView mAdView;
    public void setOnErrorLoadAd(OnErrorLoadAd onErrorLoadAd) {
        this.onErrorLoadAd = onErrorLoadAd;
    }
    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }
    private AdView adViewPlayer;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.admod_banner_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showNativeAd();
    }
    public void setIdAd(String idAd) {
        this.idAd = idAd;
    }

    private String idAd;
    private void showNativeAd() {
        MobileAds.initialize(getActivity(), idAd);
        mAdView = getView().findViewById(R.id.ad_view);
        AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.setAdListener(new AdListener(){
            @Override
            public void onAdFailedToLoad(int i) {
                if (onErrorLoadAd != null)
                    onErrorLoadAd.onMyError();
            }
        });


        mAdView.loadAd(adRequest);

    }
}
