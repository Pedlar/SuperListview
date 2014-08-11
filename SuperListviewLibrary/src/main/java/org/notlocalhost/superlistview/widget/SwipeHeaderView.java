package org.notlocalhost.superlistview.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.LayerDrawable;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;

import org.notlocalhost.superlistview.R;

/**
 * Custom header view for showing a pull down heading above the list.
 */
public final class SwipeHeaderView {
    public static final int FLAG_SLIDE_IN = 0x100000;
    public static final int FLAG_EXPAND = 0x20000;
    public static final int FLAG_ANIMATE_ARROW = 0x30000;

    private final Paint mPaint = new Paint();
    private float mTriggerPercentage;
    private View mParent;
    private View mRefreshHeader;
    private Rect mBounds = new Rect();
    private int mHeaderOffset;

    private ProgressBar mProgressSpinner;
    private TextView mProgressSpinnerTextView;
    private ImageView mProgressArrow;

    private SwipeHeaderType mHeaderType = SwipeHeaderType.SPINNER;

    private int mFlags = FLAG_EXPAND;

    private ValueAnimator mSpinAnimation;
    private boolean mHasAnimation = false;
    private Interpolator mInterpolator;

    private boolean mAnimationRunning = false;

    private float spinAmount = 1.0f;

    private void initAnimation() {
        mHasAnimation = true;

        if (mInterpolator == null) {
            mInterpolator = new LinearInterpolator();
        }

        if (mSpinAnimation == null) {
            mSpinAnimation = ValueAnimator.ofFloat(0.1f, spinAmount);
        } else {
            mSpinAnimation.end();
        }

        mSpinAnimation.setRepeatMode(AlphaAnimation.RESTART);
        mSpinAnimation.setRepeatCount(Animation.INFINITE);
        mSpinAnimation.setDuration(4000);
        mSpinAnimation.setInterpolator(mInterpolator);
    }

    public void start() {
        mAnimationRunning = true;

        if(hasSpinnerAndNotNull(mHeaderType)) {
            if(mProgressSpinner.getIndeterminateDrawable() != null &&
                    mProgressSpinner.getIndeterminateDrawable() instanceof LayerDrawable) {
                // TODO STUB! (Will eval at later time)
            }
            if(mProgressSpinner.getProgressDrawable() instanceof Animatable) {
                // TODO STUB! (Will eval at later time)
            }
        }
        ViewCompat.postInvalidateOnAnimation(mParent);
    }

    public void stop() {
        mAnimationRunning = false;
        if(hasSpinnerAndNotNull(mHeaderType)) {
            mProgressSpinner.setIndeterminate(false);
        }
    }

    public static enum SwipeHeaderType {
        SPINNER(true, false, false),
        SPINNER_WITH_TEXT(true, true, false),
        ARROW(false, false, true),
        DEFAULT(false, false, false);
        SwipeHeaderType(boolean spinner, boolean text, boolean arrow) {
            this.isSpinner = spinner;
            this.isText = text;
            this.isArrow = arrow;
        }
        private boolean isSpinner;
        private boolean isText;
        private boolean isArrow;

        protected boolean isSpinner() {
            return this.isSpinner;
        }
        protected boolean isText() {
            return this.isText;
        }
        protected boolean isArrow() {
            return this.isArrow;
        }
    }

    private boolean hasSpinnerAndNotNull(SwipeHeaderType type) {
        return type.isSpinner() && mProgressSpinner != null;
    }

    private boolean hasTextAndNotNull(SwipeHeaderType type) {
        return type.isText() && mProgressSpinnerTextView != null;
    }

    private boolean hasArrowAndNotNull(SwipeHeaderType type) {
        return type.isArrow() && mProgressArrow != null;
    }

    private void initHeaderLayout(View parent) {
        LayoutInflater inflater = (LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int layoutId = 0;

        switch(mHeaderType) {
            case SPINNER_WITH_TEXT:
                layoutId = R.layout.view_progress_header_spinner_text;
                break;
            case SPINNER:
                layoutId = R.layout.view_progress_header_spinner;
                break;
            case ARROW:
                layoutId = R.layout.view_progress_header_arrow;
                break;
            case DEFAULT:
                layoutId = -1;
                break;
        }

        if(layoutId > 0) {
            mRefreshHeader = inflater.inflate(layoutId, null);

            if(mRefreshHeader != null) {
                switch (mHeaderType) {
                    case SPINNER_WITH_TEXT:
                        mProgressSpinnerTextView = (TextView) mRefreshHeader.findViewById(R.id.view_progress_text);
                    case SPINNER:
                        mProgressSpinner = (ProgressBar) mRefreshHeader.findViewById(R.id.view_progress_spinner);
                        break;
                    case ARROW:
                        mProgressArrow = (ImageView)mRefreshHeader.findViewById(R.id.view_progress_arrow);
                        ViewCompat.setRotation(mProgressArrow, 180.1f);
                        break;
                }
                initAnimation();
            }
        }
    }

    public SwipeHeaderView(View parent) {
        mParent = parent;
        mPaint.setColor(0xFF000000);
        initHeaderLayout(parent);
    }

    /**
     * Update the progress the user has made toward triggering the swipe
     * gesture. and use this value to update the percentage of the trigger that
     * is shown.
     */
    void setTriggerPercentage(float triggerPercentage) {
        mTriggerPercentage = triggerPercentage;
        ViewCompat.postInvalidateOnAnimation(mParent);
        if(hasSpinnerAndNotNull(mHeaderType)) {
            ViewCompat.setRotation(mProgressSpinner, triggerPercentage * 360);
        } else if(hasArrowAndNotNull(mHeaderType)) {
            if(mTriggerPercentage >= .50 && mProgressArrow.getRotation() == 180.1f) {
                mAnimationRunning = true;
            }
        }
    }

    private static final int MAX_LEVEL = (360 * 5);

    void draw(Canvas canvas) {
        if(mRefreshHeader != null) {
            int restoreCount = canvas.save();
            canvas.clipRect(mBounds);

            int layoutHeight = (mFlags & FLAG_EXPAND) == FLAG_EXPAND ? mHeaderOffset
                    : (mFlags & FLAG_SLIDE_IN) == FLAG_SLIDE_IN ? mBounds.height()
                    : 0;

            Rect rect = new Rect();
            rect.set(mBounds.left, mBounds.top, mBounds.right, mHeaderOffset);

            if (mAnimationRunning) {
                float scale = (Float)mSpinAnimation.getAnimatedValue();
                if(hasSpinnerAndNotNull(mHeaderType)) {
                    ViewCompat.setRotation(mProgressSpinner, scale * MAX_LEVEL);
                } else if(hasArrowAndNotNull(mHeaderType)) {
                    Log.d("SwipeView", "getRotation() : " + mProgressArrow.getRotation());
                    if(mProgressArrow.getRotation() <= 0) {
                        ViewCompat.setRotation(mProgressArrow, 0);
                        mAnimationRunning = false;
                        initAnimation();
                    } else if(mProgressArrow.getRotation() > 180) {
                        ViewCompat.setRotation(mProgressArrow, 180);
                        mAnimationRunning = false;
                        initAnimation();
                    } else {
                        Log.d("SwipeView", "setRotation: " + (scale * 180));
                        ViewCompat.setRotation(mProgressArrow, scale * 180);
                    }
                }

                ViewCompat.postInvalidateOnAnimation(mParent);
                ViewCompat.postInvalidateOnAnimation(mRefreshHeader);
            }

            //Measure the view at the exact dimensions (otherwise the text won't center correctly)
            int widthSpec = View.MeasureSpec.makeMeasureSpec(mBounds.width(), View.MeasureSpec.EXACTLY);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(layoutHeight, View.MeasureSpec.EXACTLY);
            mRefreshHeader.measure(widthSpec, heightSpec);

            mRefreshHeader.layout(0, 0, mBounds.width(), layoutHeight);

            canvas.save();
            int translateX = (mFlags & FLAG_EXPAND) == FLAG_EXPAND ? 0
                    : (mFlags & FLAG_SLIDE_IN) == FLAG_SLIDE_IN ? -(mBounds.bottom - rect.bottom)
                    : 0;

            canvas.translate(rect.left, translateX);

            mRefreshHeader.draw(canvas);
            canvas.restore();
            canvas.restoreToCount(restoreCount);
        }
    }

    /**
     * Set the drawing bounds of this SwipeProgressBar.
     */
    void setBounds(int left, int top, int right, int bottom) {
        mBounds.left = left;
        mBounds.top = top;
        mBounds.right = right;
        mBounds.bottom = bottom;
    }

    protected void setHeaderOffset(int offset) {
        mHeaderOffset = offset;
    }

    protected void setHeaderText(String text) {
        if(hasTextAndNotNull(mHeaderType)) {
            mProgressSpinnerTextView.setText(text);
        }
    }

    protected void setHeaderType(SwipeHeaderType type) {
        mHeaderType = type;
        mRefreshHeader.destroyDrawingCache();
        initHeaderLayout(mParent);
    }

    public void setHeaderFlags(int flags) {
        mFlags = flags;
    }

    public boolean allowOverscroll() {
        return mHeaderType.isArrow();
    }
}
