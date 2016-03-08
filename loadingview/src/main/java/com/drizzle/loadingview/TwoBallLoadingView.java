package com.drizzle.loadingview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
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

	private Paint mArcPaint;
	private Paint mROWPaint;
	private RectF mRectF;

	private Path mPath;

	/**
	 * 大球半径和小球半径,大球半径为view可用尺寸较小边的1/2,小球半径为大球的1/3
	 */
	private int mParentBallRadius;
	private int mChildBallRadius;

	private int mDefaultRadius;

	/**
	 * 动画状态
	 */
	private int LOADING_STATE = 1;

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

	private boolean ISANIM = false;
	private boolean FINISHWITHWHAT = true;

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
		initAnimation();
		mDefaultRadius = DensityUtils.dip2px(context, 40);
	}

	private void initPaint() {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setColor(mBackColor);
		mPaint.setStyle(Paint.Style.FILL);
		mArcPaint = new Paint();
		mArcPaint.setAntiAlias(true);
		mArcPaint.setStyle(Paint.Style.STROKE);
		mArcPaint.setColor(mBackColor);
		mArcPaint.setStrokeJoin(Paint.Join.ROUND);
		mArcPaint.setStrokeCap(Paint.Cap.ROUND);
		mROWPaint = new Paint();
		mROWPaint.setAntiAlias(true);
		mROWPaint.setStyle(Paint.Style.STROKE);
		mROWPaint.setColor(mBackColor);
		mROWPaint.setStrokeJoin(Paint.Join.ROUND);
		mROWPaint.setStrokeCap(Paint.Cap.ROUND);
		mPath = new Path();
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
			mChildBallRadius = (int) (mParentBallRadius * 0.2);
		} else {
			mParentBallRadius = viewWidth / 2;
			mChildBallRadius = (int) (mParentBallRadius * 0.2);
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
				drawRotate(canvas, rotateProgress);
				break;
			case LOADING_MERGE:
				drawChildArc(canvas, mergeProgress, mChildBallRadius);
				break;
			case LOADING_FINISH:
				//圆弧渐变为原半径的1/4
				drawChildArc(canvas, 100, mChildBallRadius * (100 - finishProgress) * 3 / 400 + mChildBallRadius / 4);
				drawRightOrWrong(canvas, finishProgress, FINISHWITHWHAT);
				break;
			default:
				break;
		}
	}

	/**
	 * 画大球
	 */
	private void drawParentBall(Canvas canvas, int progress) {
		canvas.drawCircle((getMeasuredWidth() + getPaddingLeft() - getPaddingRight()) / 2,
			(getMeasuredHeight() + getPaddingTop() - getPaddingBottom()) / 2, progress * mParentBallRadius / 100,
			mPaint);
	}

	/**
	 * 画小球
	 */
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

	/**
	 * 画圆弧
	 */
	private void drawChildArc(Canvas canvas, int progress, int strokeWidth) {
		mArcPaint.setStrokeWidth(strokeWidth * 2);
		int centerX = (getMeasuredWidth() + getPaddingLeft() - getPaddingRight()) / 2;
		int centerY = (getMeasuredHeight() + getPaddingTop() - getPaddingBottom()) / 2;
		mRectF =
			new RectF(centerX - mParentBallRadius + mChildBallRadius, centerY - mParentBallRadius + mChildBallRadius,
				centerX + mParentBallRadius - mChildBallRadius, centerY + mParentBallRadius - mChildBallRadius);
		progress++;
		canvas.drawArc(mRectF, 180, 180 * progress / 100, false, mArcPaint);
		canvas.drawArc(mRectF, 0, 180 * progress / 100, false, mArcPaint);
	}

	/**
	 * 小球旋转
	 */
	private void drawRotate(Canvas canvas, int progress) {
		canvas.rotate(progress * 180 / 100, (getMeasuredWidth() + getPaddingLeft() - getPaddingRight()) / 2,
			(getMeasuredHeight() + getPaddingTop() - getPaddingBottom()) / 2);
		canvas.drawCircle(getPaddingLeft() + mChildBallRadius,
			(getMeasuredHeight() + getPaddingTop() - getPaddingBottom()) / 2, mChildBallRadius, mPaint);
		canvas.drawCircle(getMeasuredWidth() - getPaddingRight() - mChildBallRadius,
			(getMeasuredHeight() + getPaddingTop() - getPaddingBottom()) / 2, mChildBallRadius, mPaint);
	}

	/**
	 * 画对号和错号
	 */
	private void drawRightOrWrong(Canvas canvas, int progress, boolean right) {
		mPath.reset();
		mROWPaint.setStrokeWidth(mChildBallRadius / 3);
		int cx = (int) (mParentBallRadius * 0.75 / 1.414);
		int centerX = (getMeasuredWidth() + getPaddingLeft() - getPaddingRight()) / 2;
		int centerY = (getMeasuredHeight() + getPaddingTop() - getPaddingBottom()) / 2;
		if (right) {
			int lineLengthX, lineLengthY;
			if (progress <= 30) {
				lineLengthX = cx * progress / 30;
				lineLengthY = (int) (mParentBallRadius * 0.75 - cx) * progress / 30;
				mPath.moveTo(centerX - cx, centerY + cx);
				mPath.lineTo(centerX - lineLengthX, centerY + cx + lineLengthY);
			} else {
				lineLengthX = cx * (progress - 30) / 70;
				lineLengthY = (int) (mParentBallRadius * 0.75 + cx) * (progress - 30) / 70;
				mPath.moveTo(centerX - cx, centerY + cx);
				mPath.lineTo(centerX, (float) (centerY + mParentBallRadius * 0.75));
				mPath.lineTo(centerX + lineLengthX, (float) (centerY + mParentBallRadius * 0.75 - lineLengthY));
			}
		} else {
			int lineLength;
			if (progress <= 50) {
				lineLength = cx * 2 * progress / 50;
				mPath.moveTo(centerX + cx, centerY - cx);
				mPath.lineTo(centerX + cx - lineLength, centerY - cx + lineLength);
			} else {
				lineLength = cx * 2 * (progress - 50) / 50;
				mPath.moveTo(centerX + cx, centerY - cx);
				mPath.lineTo(centerX - cx, centerY + cx);
				mPath.moveTo(centerX - cx, centerY - cx);
				mPath.lineTo(centerX - cx + lineLength, centerY - cx + lineLength);
			}
		}
		canvas.drawPath(mPath, mROWPaint);
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

	private void initAnimation() {
		mLoadingAnimation.setDuration(500);
		mLoadingAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
		mLoadingAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override public void onAnimationStart(Animation animation) {
				if (LOADING_STATE == LOADING_START) {
					if (mOnLoadingListener != null) {
						mOnLoadingListener.onLoadingStart();
					}
				}
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
						ISANIM = false;
						if (mOnLoadingListener != null) {
							mOnLoadingListener.onLoadingEnd();
						}
						break;
					default:
						break;
				}
			}

			@Override public void onAnimationRepeat(Animation animation) {

			}
		});
		mLoadingAnimationRepeat.setDuration(500);
		mLoadingAnimationRepeat.setInterpolator(new AccelerateDecelerateInterpolator());
		mLoadingAnimationRepeat.setRepeatCount(-1);
		mLoadingAnimationRepeat.setAnimationListener(new Animation.AnimationListener() {
			@Override public void onAnimationStart(Animation animation) {

			}

			@Override public void onAnimationEnd(Animation animation) {
				LOADING_STATE++;
				TwoBallLoadingView.this.startAnimation(mLoadingAnimation);
				rotateProgress = 0;
			}

			@Override public void onAnimationRepeat(Animation animation) {
				if (!ISANIM) {
					TwoBallLoadingView.this.clearAnimation();
				}
			}
		});
	}

	public void startLoading() {
		if (ISANIM) {
			return;
		} else {
			ISANIM = true;
			this.startAnimation(mLoadingAnimation);
		}
	}

	public void stop(boolean right) {
		if (ISANIM) {
			FINISHWITHWHAT = right ? true : false;
			ISANIM = false;
		} else {
			return;
		}
	}

	private OnLoadingListener mOnLoadingListener = null;

	public void setOnLoadingListener(OnLoadingListener onLoadingListener) {
		mOnLoadingListener = onLoadingListener;
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
