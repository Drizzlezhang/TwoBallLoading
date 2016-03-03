package com.drizzle.loadingview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by drizzle on 16/3/3.
 */
public class TwoBallLoadingView extends View {

	private int mBackColor;

	private Paint mPaint;

	/**
	 * 大球半径和小球半径,大球半径为view可用尺寸较小边的1/2,小球半径为大球的1/3
	 */
	private int mParentBallRadius;
	private int mChildBallRadius;

	private int mDefaultRadius;

	/**
	 * 动画状态
	 */
	private int LOADING_STATE = 2;

	private static final int LOADING_START = 1;
	private static final int LOADING_SPLIT = 2;
	private static final int LOADING_ROTATE = 3;
	private static final int LOADING_MERGE = 4;
	private static final int LOADING_FINISH = 5;

	private int startProgress = 0;
	private int splitProgress = 0;
	private int rotateProgress = 0;
	private int mergeProgress = 0;
	private int finishProgress = 0;

	final LoadingAnimation mLoadingAnimation = new LoadingAnimation(this);
	final LoadingAnimation mLoadingAnimationRepeat = new LoadingAnimation(this);

	public TwoBallLoadingView(Context context) {
		this(context, null);
	}

	public TwoBallLoadingView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TwoBallLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		TypedArray array =
			context.getTheme().obtainStyledAttributes(attrs, R.styleable.TwoBallLoadingView, defStyleAttr, 0);
		mBackColor = array.getColor(R.styleable.TwoBallLoadingView_loading_back, Color.BLUE);
		array.recycle();
		initPaint();
		mDefaultRadius = DensityUtils.dip2px(context, 40);
	}

	private void initPaint() {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setColor(mBackColor);
		mPaint.setStyle(Paint.Style.FILL);
	}

	@Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		int width, height;
		if (widthMode == MeasureSpec.EXACTLY) {
			width = widthSize;
		} else {
			width = getPaddingLeft() + getPaddingRight() + mDefaultRadius * 2;
		}
		if (heightMode == MeasureSpec.EXACTLY) {
			height = heightSize;
		} else {
			height = getPaddingBottom() + getPaddingTop() + mDefaultRadius * 2;
		}
		int viewWidth = width - getPaddingRight() - getPaddingLeft();
		int viewHeight = height - getPaddingTop() - getPaddingBottom();
		//比较View可用空间的长和宽,以较短一边为标准设置半径
		if (viewWidth >= viewHeight) {
			mParentBallRadius = viewHeight / 2;
			mChildBallRadius = (int) (mParentBallRadius * 0.4);
		} else {
			mParentBallRadius = viewWidth / 2;
			mChildBallRadius = (int) (mParentBallRadius * 0.4);
		}
		setMeasuredDimension(width, height);
	}

	@Override protected void onDraw(Canvas canvas) {
		switch (LOADING_STATE) {
			case LOADING_START:
				drawParentBall(canvas, startProgress);
				break;
			case LOADING_SPLIT:
				drawBornBall(canvas, splitProgress);
				break;
			case LOADING_ROTATE:
				canvas.rotate(rotateProgress * 180 / 100,
					(getMeasuredWidth() + getPaddingLeft() - getPaddingRight()) / 2,
					(getMeasuredHeight() + getPaddingTop() - getPaddingBottom()) / 2);
				canvas.drawCircle(getPaddingLeft() + mChildBallRadius,
					(getMeasuredHeight() + getPaddingTop() - getPaddingBottom()) / 2, mChildBallRadius, mPaint);
				canvas.drawCircle(getMeasuredWidth() - getPaddingRight() - mChildBallRadius,
					(getMeasuredHeight() + getPaddingTop() - getPaddingBottom()) / 2, mChildBallRadius, mPaint);
				break;
			case LOADING_MERGE:
				drawBornBall(canvas, 100 - mergeProgress);
				break;
			case LOADING_FINISH:
				drawParentBall(canvas, 100 - finishProgress);
				break;
			default:
				break;
		}
	}

	private void drawParentBall(Canvas canvas, int progress) {
		canvas.drawCircle((getMeasuredWidth() + getPaddingLeft() - getPaddingRight()) / 2,
			(getMeasuredHeight() + getPaddingTop() - getPaddingBottom()) / 2, progress * mParentBallRadius / 100,
			mPaint);
	}

	private void drawBornBall(Canvas canvas, int progress) {
		int leftCenterX = (getMeasuredWidth() + getPaddingLeft() - getPaddingRight()) / 2
			- ((getMeasuredWidth() - getPaddingLeft() - getPaddingRight()) / 2 - mChildBallRadius) * progress / 100;
		int rightCenterX = (getMeasuredWidth() + getPaddingLeft() - getPaddingRight()) / 2
			+ ((getMeasuredWidth() - getPaddingLeft() - getPaddingRight()) / 2 - mChildBallRadius) * progress / 100;
		int centerY = (getMeasuredHeight() + getPaddingTop() - getPaddingBottom()) / 2;
		int changingRadius = mParentBallRadius - (mParentBallRadius - mChildBallRadius) * progress / 100;
		canvas.drawCircle(leftCenterX, centerY, changingRadius, mPaint);
		canvas.drawCircle(rightCenterX, centerY, changingRadius, mPaint);
	}

	private void setProgress(int progress) {
		switch (LOADING_STATE) {
			case LOADING_START:
				startProgress = progress;
				break;
			case LOADING_SPLIT:
				splitProgress = progress;
				break;
			case LOADING_ROTATE:
				rotateProgress = progress;
				break;
			case LOADING_MERGE:
				mergeProgress = progress;
				break;
			case LOADING_FINISH:
				finishProgress = progress;
				break;
			default:
				break;
		}
	}

	public void startLoading() {
		mLoadingAnimation.setDuration(500);
		mLoadingAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
		mLoadingAnimationRepeat.setDuration(500);
		mLoadingAnimationRepeat.setInterpolator(new AccelerateDecelerateInterpolator());
		mLoadingAnimationRepeat.setRepeatCount(5);
		mLoadingAnimationRepeat.setAnimationListener(new Animation.AnimationListener() {
			@Override public void onAnimationStart(Animation animation) {

			}

			@Override public void onAnimationEnd(Animation animation) {
				LOADING_STATE++;
				TwoBallLoadingView.this.startAnimation(mLoadingAnimation);
				rotateProgress = 0;
			}

			@Override public void onAnimationRepeat(Animation animation) {

			}
		});
		mLoadingAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override public void onAnimationStart(Animation animation) {

			}

			@Override public void onAnimationEnd(Animation animation) {
				switch (LOADING_STATE) {
					case LOADING_START:
						LOADING_STATE++;
						TwoBallLoadingView.this.clearAnimation();
						TwoBallLoadingView.this.startAnimation(mLoadingAnimation);
						startProgress = 0;
						break;
					case LOADING_SPLIT:
						LOADING_STATE++;
						TwoBallLoadingView.this.clearAnimation();
						TwoBallLoadingView.this.startAnimation(mLoadingAnimationRepeat);
						splitProgress = 0;
						break;
					case LOADING_MERGE:
						LOADING_STATE++;
						TwoBallLoadingView.this.clearAnimation();
						TwoBallLoadingView.this.startAnimation(mLoadingAnimation);
						mergeProgress = 0;
						break;
					case LOADING_FINISH:
						LOADING_STATE = LOADING_START;
						TwoBallLoadingView.this.clearAnimation();
						finishProgress = 0;
						break;
					default:
						break;
				}
			}

			@Override public void onAnimationRepeat(Animation animation) {
			}
		});
		this.startAnimation(mLoadingAnimation);
	}

	public void stop() {

	}

	private class LoadingAnimation extends Animation {
		private TwoBallLoadingView mTwoBallLoadingView;

		public LoadingAnimation(TwoBallLoadingView twoBallLoadingView) {
			mTwoBallLoadingView = twoBallLoadingView;
		}

		@Override protected void applyTransformation(float interpolatedTime, Transformation t) {
			int progress = (int) (100 * interpolatedTime);
			mTwoBallLoadingView.setProgress(progress);
			mTwoBallLoadingView.requestLayout();
		}
	}
}
