package richadx;

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
public class NativeAdFragment extends Fragment {
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




    private void showNativeAd() {
        adViewPlayer = new AdView(getContext());
        adViewPlayer.setAdSize(MEDIUM_RECTANGLE);
//        adViewPlayer.setAdUnitId("/194427432/ecomobile/Bigfont-Native-banner300x250"); // appotax
        adViewPlayer.setAdUnitId(idAd); // richadx
        final LinearLayout nativeAdContainer = getView().findViewById(R.id.ads_in_list);
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
                                        onErrorLoadAd.onError();
//                    loadCrossNative();
                                }
                            });

                } catch (Exception e) {
//            loadCrossNative();
                }

    }
}
