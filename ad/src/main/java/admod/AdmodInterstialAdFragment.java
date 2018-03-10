package admod;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.duong.mylibrary.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;

import inter.OnErrorLoadAd;

/**
 * Created by d on 1/24/2018.
 */

public class AdmodInterstialAdFragment extends Fragment {
    PublisherInterstitialAd mPublisherInterstitialAd;
    private OnErrorLoadAd onErrorLoadAd;

    public void setOnErrorLoadAd(OnErrorLoadAd onErrorLoadAd) {
        this.onErrorLoadAd = onErrorLoadAd;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_native_ad_richadx, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initInterstialAd();
        if (mPublisherInterstitialAd.isLoaded())
            mPublisherInterstitialAd.show();
        else requestNewInterstitial();
    }
    public void setIdAd(String idAd) {
        this.idAd = idAd;
    }

    private String idAd;
    private void initInterstialAd() {
        mPublisherInterstitialAd = new PublisherInterstitialAd(getActivity());
        mPublisherInterstitialAd.setAdUnitId(idAd);
        requestNewInterstitial();
    }

    private void requestNewInterstitial() {

        PublisherAdRequest adRequest = new PublisherAdRequest.Builder().build();

        mPublisherInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                if (onErrorLoadAd != null)
                    onErrorLoadAd.onMyError();
            }

            @Override
            public void onAdClosed() {
//                try {
//                    getActivity().finish();
//                }catch (Exception e){}
            }

            @Override
            public void onAdLoaded() {
                mPublisherInterstitialAd.show();
            }
        });
        mPublisherInterstitialAd.loadAd(adRequest);
    }
}