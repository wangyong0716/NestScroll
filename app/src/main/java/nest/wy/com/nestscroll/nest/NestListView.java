package nest.wy.com.nestscroll.nest;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import nest.wy.com.nestscroll.LogUtil;

/**
 * 注：不要设置paddingTop和paddingBottom，设置paddingTop和paddingBottom后判断方法需要改变，为了节省运算，忽略了padding
 */
public class NestListView extends ListView implements AbsListView.OnScrollListener, NestChildView {
    private static final String TAG = "WY.NestListView";
    private boolean mIsTop = true;
    private boolean mIsBottom = false;
    //ListView的最大高度和内容高度，ListView的实际高度是两个中较小的。
    private int mMaxHeight, mContentHeight;
    private ScrollEdgeListener mScrollEdgeListener;
    private HeightChangeListener mHeightChangeListener;

    public NestListView(Context context) {
        this(context, null);
    }

    public NestListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NestListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnScrollListener(this);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (getChildCount() == 0) {
            return;
        }
        boolean isTop = mIsTop, isBottom = mIsBottom;
        if (firstVisibleItem == 0 && getChildAt(0).getTop() == 0) {
            mIsTop = true;
        } else {
            mIsTop = false;
        }
        int lastItem = firstVisibleItem + visibleItemCount;
        if (lastItem == totalItemCount) {
            View lastItemView = getChildAt(getChildCount() - 1);
            if (getBottom() - getTop() == lastItemView.getBottom()) {
                mIsBottom = true;
            }
        } else {
            mIsBottom = false;
        }
        if (LogUtil.enable()) {
            LogUtil.i(TAG, "isTop = " + mIsTop + ", isBottom = " + mIsBottom);
        }
        if (isTop != mIsTop || isBottom != mIsBottom) {
            notifyScrollEdgeListener();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public boolean isAtTop() {
        return mIsTop;
    }

    @Override
    public boolean isAtBottom() {
        return mIsBottom;
    }

    @Override
    public void setMaxHeight(int height) {
        mMaxHeight = height;
        if (mContentHeight == mMaxHeight) {
            return;
        }
        height = mContentHeight < height ? mContentHeight : height;
        if (mHeightChangeListener != null &&
                !mHeightChangeListener.onHeightChanged((View)getParent(), height) && mScrollEdgeListener != null) {
            mScrollEdgeListener.onDataFilled(mContentHeight <= mMaxHeight);
        }
    }

    @Override
    public int getMaxHeight() {
        return mMaxHeight;
    }

    /**
     * 设置ListView的内容的高度,外层view修改内容高度时一定要调用。
     *
     * @param contentHeight
     */
    public void setContentHeight(int contentHeight) {
        mContentHeight = contentHeight;
        if (mScrollEdgeListener != null) {
            mScrollEdgeListener.onDataFilled(mContentHeight <= mMaxHeight);
        }
    }

    /**
     * 外层View设置，NestScrollView调整高度时，需要通过该listener通知外层view
     *
     * @param heightChangeListener
     */
    public void setHeightChangeListener(HeightChangeListener heightChangeListener) {
        mHeightChangeListener = heightChangeListener;
    }

    @Override
    public void setScrollEdgeListener(ScrollEdgeListener scrollEdgeListener) {
        mScrollEdgeListener = scrollEdgeListener;
    }

    @Override
    public void notifyScrollEdgeListener() {
        if (mScrollEdgeListener != null) {
            mScrollEdgeListener.onEdgeChanged(mIsTop, mIsBottom);
        }
    }

    /**
     * 若子view内容没有完全，查看更多时子view高度会调整，查看更多按钮可见，不需要滚动父view
     */
    public void scrollToTop() {
        if (mScrollEdgeListener != null && mContentHeight > mMaxHeight) {
            mScrollEdgeListener.scrollToTop(mContentHeight <= mMaxHeight ? mContentHeight : mMaxHeight);
        }
    }
}
