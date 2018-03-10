/*
 * Copyright (c) 2016-present, Facebook, Inc. All rights reserved.
 *
 * You are hereby granted a non-exclusive, worldwide, royalty-free license to
 * use, copy, modify, and distribute this software in source code or binary
 * form for use in connection with the web services and APIs provided by
 * Facebook.
 *
 * As with any software that integrates with the Facebook platform, your use of
 * this software is subject to the Facebook Developer Principles and Policies
 * [http://developers.facebook.com/policy/]. This copyright notice shall be
 * included in all copies or substantial portions of the software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package facebook;

import android.content.Context;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

import inter.OnErrorLoadAd;

public class FacebookNativeAd {
    private NativeAd nativeAd;
    OnErrorLoadAd onErrorLoadAd;
    Context mContext;
    View mViewContainer;
    String idAd;

    public FacebookNativeAd(Context mContext, View mViewContainer, String idAd) {
        this.mContext = mContext;
        this.mViewContainer = mViewContainer;
        this.idAd = idAd;
    }

    public void setIdAd(String idAd) {
        this.idAd = idAd;
    }


    LayoutInflater inflater;

    private void show() {
        nativeAd = new NativeAd(mContext, idAd);
        nativeAd.setAdListener(new AdListener() {

            @Override
            public void onAdLoaded(Ad ad) {
                LinearLayout nativeAdContainer = (LinearLayout) mViewContainer;
                View adView = inflater.inflate(R.layout.facebook_native_ad_layout_300, nativeAdContainer, false);
                nativeAdContainer.removeAllViews();
                nativeAdContainer.addView(adView);

                // Create native UI using the ad metadata.
                ImageView nativeAdIcon = adView.findViewById(R.id.native_ad_icon);
                TextView nativeAdTitle = adView.findViewById(R.id.native_ad_title);
                MediaView nativeAdMedia = adView.findViewById(R.id.native_ad_media);
                TextView nativeAdSocialContext = adView.findViewById(R.id.native_ad_social_context);
                TextView nativeAdBody = adView.findViewById(R.id.native_ad_body);
                Button nativeAdCallToAction = adView.findViewById(R.id.native_ad_call_to_action);

                // Set the Text.
                nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
                nativeAdTitle.setText(nativeAd.getAdTitle());
                nativeAdBody.setText(nativeAd.getAdBody());
                nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
                // Download and display the ad icon.
                NativeAd.Image adIcon = nativeAd.getAdIcon();
                NativeAd.downloadAndDisplayImage(adIcon, nativeAdIcon);
                // Download and display the cover image.
                nativeAdMedia.setNativeAd(nativeAd);
                // Add the AdChoices icon
                LinearLayout adChoicesContainer = nativeAdContainer.findViewById(R.id.ad_choices_container);
                AdChoicesView adChoicesView = new AdChoicesView(mContext, nativeAd, true);
                adChoicesContainer.addView(adChoicesView);
                // Register the Title and CTA button to listen for clicks.
                List<View> clickableViews = new ArrayList<>();
                clickableViews.add(nativeAdIcon);
                clickableViews.add(nativeAdTitle);
                clickableViews.add(nativeAdMedia);
                clickableViews.add(nativeAdSocialContext);
                clickableViews.add(nativeAdBody);
                clickableViews.add(nativeAdCallToAction);
                nativeAd.registerViewForInteraction(nativeAdContainer, clickableViews);
            }

            @Override
            public void onError(Ad ad, AdError error) {
                if (onErrorLoadAd != null)
                    onErrorLoadAd.onMyError();
            }

            @Override
            public void onAdClicked(Ad ad) {

            }

            @Override
            public void onLoggingImpression(Ad ad) {

            }

        });
        nativeAd.loadAd();
    }



    public void setOnErrorLoadAd(OnErrorLoadAd onErrorLoadAd) {
        this.onErrorLoadAd = onErrorLoadAd;

    }

}
