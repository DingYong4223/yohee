package com.fula.helper;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.DimenRes;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 类描述：横向的分割，注：当RecyclerView为竖向的时候使用横向的分割
 * for example:
 * <pre>
 *   <code>
 *     recyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this)
 *        .size(20) //分割线宽度px
 *        .margin(50,50)//margin(left,right)
 *        .color(Color.BLACK)
 *        .build());
 *   </code>
 * </pre>
 */
public class HorizontalDividerItemDecoration extends BaseDividerItemDecoration {
    private MarginProvider mMarginProvider;
    public HorizontalDividerItemDecoration(Builder builder) {
        super(builder);
        mMarginProvider = builder.mMarginProvider;
    }

    @Override
    protected Rect getDividerBound(int position, RecyclerView parent, View child) {
        Rect bounds = new Rect(0, 0, 0, 0);
        int transitionX = (int) ViewCompat.getTranslationX(child);
        int transitionY = (int) ViewCompat.getTranslationY(child);
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
        bounds.left = parent.getPaddingLeft() +
                mMarginProvider.dividerLeftMargin(position, parent) + transitionX;
        bounds.right = parent.getWidth() - parent.getPaddingRight() -
                mMarginProvider.dividerRightMargin(position, parent) + transitionX;

        int dividerSize = getDividerSize(position, parent);
        if (mDividerType == DividerType.DRAWABLE) {
            // set top and bottom position of divider
            if (mPositionInsideItem) {
                bounds.bottom = child.getBottom() + params.topMargin + transitionY;
                bounds.top = bounds.bottom - dividerSize;
            } else {
                bounds.top = child.getBottom() + params.topMargin + transitionY;
                bounds.bottom = bounds.top + dividerSize;
            }
        } else {
            // set center point of divider
            if (mPositionInsideItem) {
                bounds.top = child.getBottom() + params.topMargin - dividerSize / 2 + transitionY;
            } else {
                bounds.top = child.getBottom() + params.topMargin + dividerSize / 2 + transitionY;
            }
            bounds.bottom = bounds.top;
        }

        return bounds;
    }

    @Override
    protected void setItemOffsets(Rect outRect, int position, RecyclerView parent) {
        if (mPositionInsideItem) {
            outRect.set(0, 0, 0, 0);
        } else {
            outRect.set(0, 0, 0, getDividerSize(position, parent));
        }
    }

    private int getDividerSize(int position, RecyclerView parent) {
        if (mPaintProvider != null) {
            return (int) mPaintProvider.dividerPaint(position, parent).getStrokeWidth();
        } else if (mSizeProvider != null) {
            return mSizeProvider.dividerSize(position, parent);
        } else if (mDrawableProvider != null) {
            Drawable drawable = mDrawableProvider.drawableProvider(position, parent);
            return drawable.getIntrinsicHeight();
        }
        throw new RuntimeException("failed to get size");
    }

    public interface MarginProvider {

        int dividerLeftMargin(int position, RecyclerView parent);

        int dividerRightMargin(int position, RecyclerView parent);
    }

    public static class Builder extends BaseDividerItemDecoration.Builder<Builder> {

        private MarginProvider mMarginProvider = new MarginProvider() {
            @Override
            public int dividerLeftMargin(int position, RecyclerView parent) {
                return 0;
            }

            @Override
            public int dividerRightMargin(int position, RecyclerView parent) {
                return 0;
            }
        };

        public Builder(Context context) {
            super(context);
        }

        public Builder margin(final int leftMargin, final int rightMargin) {
            return marginProvider(new MarginProvider() {
                @Override
                public int dividerLeftMargin(int position, RecyclerView parent) {
                    return leftMargin;
                }

                @Override
                public int dividerRightMargin(int position, RecyclerView parent) {
                    return rightMargin;
                }
            });
        }

        public Builder margin(int horizontalMargin) {
            return margin(horizontalMargin, horizontalMargin);
        }

        public Builder marginResId(@DimenRes int leftMarginId, @DimenRes int rightMarginId) {
            return margin(mResources.getDimensionPixelSize(leftMarginId),
                    mResources.getDimensionPixelSize(rightMarginId));
        }

        public Builder marginResId(@DimenRes int horizontalMarginId) {
            return marginResId(horizontalMarginId, horizontalMarginId);
        }

        public Builder marginProvider(MarginProvider provider) {
            mMarginProvider = provider;
            return this;
        }

        public HorizontalDividerItemDecoration build() {
            checkBuilderParams();
            return new HorizontalDividerItemDecoration(this);
        }
    }
}
