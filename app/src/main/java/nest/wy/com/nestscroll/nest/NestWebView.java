package nest.wy.com.nestscroll.nest;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebView;

import nest.wy.com.nestscroll.LogUtil;

/**
 * Created by wangyong on 18-11-5.
 * WebView如果没有加载完，则滚动的时候也会遇到mIsBottom=true的情况。
 * 这种情况下，就需要知道webview内容什么时候真正加载完成，如果是自己的页面可以通过js通知;
 * 否则，建议所有的滑动事件交由WebView处理，当用户按返回键时再跳出WebView，由ScrollView处理，mForceIntercept就是这种情况下使用的。
 */

public class NestWebView extends WebView implements NestChildView {
    private static final String TAG = "WY.NestWebView";
    private boolean mIsTop;
    private boolean mIsBottom;
    private int mMaxHeight;
    private ScrollEdgeListener mScrollEdgeListener;
    private HeightChangeListener mHeightChangeListener;
    //控制webview是否能接收到手势事件
    private boolean mForceIntercept = false;

    public NestWebView(Context context) {
        this(context, null);
    }

    public NestWebView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NestWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        boolean isTop = mIsTop, isBottom = mIsBottom;

        if (mForceIntercept) {
            return;
        } else {
            float contentHeight = getContentHeight() * getScale();// webview的高度
            float curBottom = getHeight() + getScrollY();// 当前webview的高度

            if (getScrollY() == 0) {
                mIsTop = true;
            } else {
                mIsTop = false;
            }
            if (Math.abs(contentHeight - curBottom) < 1) {
                mIsBottom = true;
            } else {
                mIsBottom = false;
            }
        }
        if (mIsTop != isTop || mIsBottom != isBottom) {
            notifyScrollEdgeListener();
        }
        if (LogUtil.enable()) {
            LogUtil.i(TAG, "onScrollChanged -> isTop = " + mIsTop + ", isBottom = " + mIsBottom);
        }
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
        if (mHeightChangeListener != null) {
            mHeightChangeListener.onHeightChanged((View) getParent(), height);
        }
    }

    /**
     * 如果不允许webview滚动，传入参数true即可。设置成false后，webview与scrollview嵌套滚动。
     * @param forceIntercept
     */
    public void setForceIntercept(boolean forceIntercept) {
        if (LogUtil.enable()) {
            LogUtil.i(TAG, "setForceIntercept -> forceIntercept = " + forceIntercept);
        }
        mForceIntercept = forceIntercept;
        mIsTop = mForceIntercept;
        mIsBottom = mForceIntercept;
        notifyScrollEdgeListener();
        if (!forceIntercept && mScrollEdgeListener != null) {
            mScrollEdgeListener.scrollToTop(0);
        }
    }

    @Override
    public int getMaxHeight() {
        return mMaxHeight;
    }

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
}
