package richadx;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.duong.mylibrary.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import inter.OnErrorLoadAd;

import static com.google.android.gms.ads.AdSize.BANNER;
import static com.google.android.gms.ads.AdSize.MEDIUM_RECTANGLE;

/**
 * Created by d on 1/24/2018.
 */

public class BannerAdFragment extends Fragment {
    private OnErrorLoadAd onErrorLoadAd;

    public void setOnErrorLoadAd(OnErrorLoadAd onErrorLoadAd) {
        this.onErrorLoadAd = onErrorLoadAd;

    }

    private AdView adViewPlayer;

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
        showNativeAd();
    }
    public void setIdAd(String idAd) {
        this.idAd = idAd;
    }

    private String idAd;
    private void showNativeAd() {
        adViewPlayer = new AdView(getContext());
        adViewPlayer.setAdSize(BANNER);
        adViewPlayer.setAdUnitId(idAd); // richadx
        final LinearLayout nativeAdContainer = getView().findViewById(R.id.ads_in_list);
        try {
            AdRequest.Builder adRequestBuilderHeader = new AdRequest.Builder();

            nativeAdContainer.removeAllViews();
            nativeAdContainer.addView(adViewPlayer);
            adViewPlayer.setAdListener(new com.google.android.gms.ads.AdListener() {
                @Override
                public void onAdFailedToLoad(int i) {
                    super.onAdFailedToLoad(i);
                    if (onErrorLoadAd != null)
                        onErrorLoadAd.onMyError();
//                    loadCrossNative();
                }
            });
            adViewPlayer.loadAd(adRequestBuilderHeader.build());
        } catch (Exception e) {
        }
//            loadCrossNative();

    }
}
