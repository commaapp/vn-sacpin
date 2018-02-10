package core;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.duong.mylibrary.R;

/**
 * Created by d on 1/24/2018.
done ca-app-pub-9912310468706838/4525571555
 float ca-app-pub-9912310468706838/9545608728
home ca-app-pub-9912310468706838/1439053842
settings ca-app-pub-9912310468706838/5100286624

 */

public class AdFragmentManager extends Fragment {
    private int facebook = 1;
    private int richadx = 2;
    private int apota = 3;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.ad_fragment_manager, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }
}
