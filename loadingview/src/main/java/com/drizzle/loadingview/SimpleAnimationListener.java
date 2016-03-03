package com.drizzle.loadingview;

import android.view.animation.Animation;

/**
 * Created by drizzle on 16/3/3.
 */
public interface SimpleAnimationListener extends Animation.AnimationListener {
	@Override void onAnimationStart(Animation animation);

	@Override void onAnimationEnd(Animation animation);

	@Override void onAnimationRepeat(Animation animation);
}
