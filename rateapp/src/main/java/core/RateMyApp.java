package core;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cross.rateapp.R;

import es.dmoral.prefs.Prefs;

/**
 * Created by D on 3/11/2018.
 */

public class RateMyApp {
    Activity mContext;

    public RateMyApp(Activity mContext) {
        this.mContext = mContext;
    }

    public void show() {
        boolean action = Prefs.with(mContext).readBoolean("action");
        if (!action) {
            if (!Prefs.with(mContext).readBoolean("rate", false))
                showDialog();
            else mContext.finish();
        } else mContext.finish();
    }

    private void showDialog() {
        final Dialog dialog1 = new Dialog(mContext);
        dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.dialog_rate_app, null);
        dialog1.setContentView(view);
        dialog1.setCancelable(true);

        final TextView btnRate = (TextView) view.findViewById(R.id.btn_rate);
        TextView btnLater = (TextView) view.findViewById(R.id.btn_later);


        btnRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Prefs.with(mContext).readBoolean("change", false)) {
                    Prefs.with(mContext).writeBoolean("rate", true);
                    dialog1.dismiss();
                    mContext.finish();
                }

            }
        });
        btnLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Prefs.with(mContext).writeBoolean("rate", false);
                dialog1.dismiss();
                mContext.finish();
            }
        });
        RatingBar mRatingBar = (RatingBar) dialog1.findViewById(R.id.mRatingBar);
        LayerDrawable stars = (LayerDrawable) mRatingBar.getProgressDrawable();
        stars.getDrawable(0).setColorFilter(mContext.getResources().getColor(R.color.colorLater), PorterDuff.Mode.SRC_ATOP);

        mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (rating > 3) {
                    Toast.makeText(mContext, mContext.getString(R.string.sms_thank_you_rate), Toast.LENGTH_SHORT).show();
                    Prefs.with(mContext).writeBoolean("rate", true);
                    Intent i = new Intent("android.intent.action.VIEW");
                    i.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + mContext.getPackageName()));
                    mContext.startActivity(i);
                    mContext.finish();
                } else if (rating <= 3) {
                    Prefs.with(mContext).writeBoolean("change", true);
                    btnRate.setBackgroundResource(R.drawable.custom_button_rate);
                } else {
                    btnRate.setBackgroundResource(R.drawable.custom_button_later);
                }
            }
        });

        dialog1.show();
    }

}
