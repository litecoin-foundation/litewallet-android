package com.breadwallet.presenter.fragments;

import android.app.Fragment;
import android.os.Bundle;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.breadwallet.R;
import com.breadwallet.presenter.customviews.BRButton;
import com.breadwallet.tools.animation.BRAnimator;

public class FragmentGreetings extends Fragment {
    private static final String TAG = FragmentGreetings.class.getName();

    private BRButton ok;
    private ConstraintLayout mainLayout;
    private RelativeLayout background;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.

        View rootView = inflater.inflate(R.layout.fragment_greetings, container, false);
        ok = (BRButton) rootView.findViewById(R.id.ok);
        mainLayout = (ConstraintLayout) rootView.findViewById(R.id.signal_layout);
        background = (RelativeLayout) rootView.findViewById(R.id.layout);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ViewTreeObserver observer = mainLayout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(observer.isAlive()) {
                    observer.removeOnGlobalLayoutListener(this);
                }
                BRAnimator.animateBackgroundDim(background, false);
                BRAnimator.animateSignalSlide(mainLayout, false, null);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        BRAnimator.animateBackgroundDim(background, true);
        BRAnimator.animateSignalSlide(mainLayout, true, new BRAnimator.OnSlideAnimationEnd() {
            @Override
            public void onAnimationEnd() {
                if (getActivity() != null)
                    getActivity().getFragmentManager().popBackStack();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

}