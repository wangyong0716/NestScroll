package nest.wy.com.nestscroll.nest;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.OverScroller;
import android.widget.ScrollView;

import java.lang.reflect.Field;

import nest.wy.com.nestscroll.LogUtil;

/**
 * NestChildView为空或invisible，直接滑动，子view无法滚动;
 * NestChildView不为空但是NestTabView为空，子view可以滚动，但是有临界点;
 * NestChildView和NestTabView均不为空，有两个临界点：tab恰好出现，子view钱好恰好消失。
 * 滚动原则：两者都可以滚动的时候，父view优先滚动。
 * 注：不要设置paddingTop和paddingBottom，设置paddingTop和paddingBottom后判断方法需要改变，为了节省运算，忽略了padding
 */

public class NestScrollView extends ScrollView {
    private static final String TAG = "WY.NestScrollView";
    private ScrollController mScrollController;
    private View mTabView;
    private NestChildView mNestChildView;
    private OverScroller mOverScroller;

    private int mLast = -1;

    public NestScrollView(Context context) {
        this(context, null);
    }

    public NestScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NestScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        resetScroller();
        mScrollController = new ScrollController();
    }

    private void resetScroller() {
        mOverScroller = new OverScroller(getContext());
        Class<?> clazz = ScrollView.class;
        try {
            Field field = clazz.getDeclaredField("mScroller");
            field.setAccessible(true);
            field.set(this, mOverScroller);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public void setNestChildView(NestChildView nestChildView) {
        mNestChildView = nestChildView;
        mScrollController.updateNestChildView();
    }

    public void setTabView(View tabView) {
        mTabView = tabView;
    }

    /**
     * 是否允许下拉退出
     * 父view没有触顶，不允许;
     * 父view触顶，子view触顶，允许;
     * 父view触顶，子view没有触顶，滚动点如果在父view上则允许，在子view上则不允许。
     *
     * @param y
     * @return
     */
    protected boolean allowInterceptTouchEvent(int y) {
        if (!mScrollController.isAtTop) {
            return false;
        }
        if (mScrollController.isChildAtTop) {
            return true;
        }
        int nestTop = mScrollController.childTopHeight - getScrollY();
        if (mTabView != null && mTabView.getVisibility() == VISIBLE) {
            nestTop += mTabView.getHeight();
        }
        return y < nestTop;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mScrollController.updateHeight();
        mScrollController.checkEdgeStatus();
        mScrollController.checkScrollStatus();
        if (LogUtil.enable()) {
            LogUtil.i(TAG, "onLayout : controller = " + mScrollController.toString());
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!mScrollController.childValid) {
            return super.onInterceptTouchEvent(ev);
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLast = (int) ev.getY();
                mScrollController.checkScrollStatus();
                break;
            case MotionEvent.ACTION_MOVE:
                int cur = (int) ev.getY();
                if (LogUtil.enable()) {
                    LogUtil.i(TAG, "MOVE : moveUpDis = " + (mLast - cur) + ", controller = " + mScrollController.toString());
                }
                if (cur < mLast && mScrollController.allowScrollUp || cur > mLast && mScrollController.allowScrollDown) {
                    return false;
                }
            default:
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    /**
     * 需要停顿在两个位置：
     * 1,下滑时tab刚好在顶部的位置：如果子view没有滚动到底部，则父view不能继续向下滚动;
     * 2,上滑时子view刚好完全展现：如果子view没有滚动到顶部，则父view不能继续向上滚动;
     * <p>
     * ScrollView默认优先处理惯性滑动，即fling。
     * 滑动到边界的时候要停止Scroller，否则，ScrollView认为惯性滑动仍在继续，会继续处理惯性滑动，接下来的手势滑动会被直接忽略掉。
     *
     * @return
     */
    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY,
                                   int scrollRangeX, int scrollRangeY, int maxOverScrollX,
                                   int maxOverScrollY, boolean isTouchEvent) {
        if (!mScrollController.childValid) {
            return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY,
                    maxOverScrollX, maxOverScrollY, isTouchEvent);
        }
        deltaY = mScrollController.intercept(scrollY, deltaY);
        if (deltaY == 0) {
            mOverScroller.forceFinished(true);
            return true;
        }
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY,
                maxOverScrollX, maxOverScrollY, isTouchEvent);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (t == 0) {
            mScrollController.isAtTop = true;
            mScrollController.isAtBottom = false;
        } else if (t + getHeight() == getChildAt(0).getHeight()) {
            mScrollController.isAtTop = false;
            mScrollController.isAtBottom = true;
        } else {
            mScrollController.isAtTop = false;
            mScrollController.isAtBottom = false;
        }
    }

    class ScrollController {
        boolean isAtTop = true;
        boolean isAtBottom = false;

        //子view内容可以向上滚动，即手势上滑的时候子view是否可以滚动
        //初始值：如果处于页面顶部，且子view内容超过一屏，则为true，否则为false
        boolean allowScrollUp = false;
        //子view内容可以向下滚动，即手势下滑的时候子view是否可以滚动
        //初始值为false
        boolean allowScrollDown = false;

        //子view信息
        boolean isChildAtTop = true;
        boolean isChildAtBottom = false;

        int topContentHeight;
        int childTopHeight;
        boolean childValid = false;

        public ScrollController() {

        }

        ScrollEdgeListener mEdgeListener = new ScrollEdgeListener() {
            @Override
            public void onEdgeChanged(boolean isTop, boolean isBottom) {
                isChildAtTop = isTop;
                isChildAtBottom = isBottom;

                allowScrollUp = (getScrollY() == topContentHeight || isAtBottom) && !isBottom;
                allowScrollDown = (getScrollY() == topContentHeight || isAtTop) && !isTop;
                if (LogUtil.enable()) {
                    LogUtil.i(TAG, "childEdgeChanged :  controller = " + mScrollController.toString());
                }
            }

            @Override
            public void onDataFilled(boolean allShown) {
                if (allShown) {
                    mScrollController.childValid = false;
                }
                if (LogUtil.enable()) {
                    LogUtil.i(TAG, "childDataFilled : allShown = " + allShown);
                }
            }

            @Override
            public void scrollToTop(int newHeight) {
                /**
                 * 子view底部没有展现出来就不需要调整，因为滚动的时候会先显示父view
                 */
                if (getScrollY() > topContentHeight) {
                    smoothScrollTo(0, topContentHeight);
                }
            }
        };

        void checkScrollStatus() {
            allowScrollUp = mNestChildView != null && (getScrollY() == topContentHeight || isAtBottom) && !isChildAtBottom;
            allowScrollDown = mNestChildView != null && (getScrollY() == topContentHeight || isAtTop) && !isChildAtTop;
        }

        void checkEdgeStatus() {
            isAtTop = getScrollY() == 0;
            isAtBottom = getScrollY() + getHeight() == getChildAt(0).getBottom();

            isChildAtTop = mNestChildView != null && mNestChildView.isAtTop();
            isChildAtBottom = mNestChildView != null && mNestChildView.isAtBottom();
        }

        void updateHeight() {
            topContentHeight = 0;
            if (mNestChildView == null || ((View) mNestChildView).getVisibility() != VISIBLE) {
                childValid = false;
                childTopHeight = -1;
                return;
            }
            childTopHeight = getTopDistance((View) mNestChildView);
            if (childTopHeight < 0) {
                mNestChildView = null;
                childValid = false;
                return;
            }
            childValid = true;

            if (mTabView != null && mTabView.getVisibility() == VISIBLE) {
                int tabTopHeight = getTopDistance(mTabView);
                if (tabTopHeight < 0) {
                    mTabView = null;
                    topContentHeight = childTopHeight;
                    resetChildHeight(getHeight());
                    return;
                } else {
                    topContentHeight = tabTopHeight;
                    resetChildHeight(getHeight() - childTopHeight + tabTopHeight);
                    return;
                }
            } else {
                topContentHeight = childTopHeight;
                resetChildHeight(getHeight());
            }
        }

        private int getTopDistance(View view) {
            int topHeight = view.getTop();
            View parent = (View) view.getParent();
            while (parent != NestScrollView.this && parent != null) {
                topHeight += parent.getTop();
                parent = (View) parent.getParent();
            }
            if (parent != NestScrollView.this) {
                return -1;
            }
            return topHeight;
        }

        /**
         * 防止onLayout不断重复执行。每个子view相同的最大高度只需要设置一次。
         *
         * @param height
         */
        void resetChildHeight(int height) {
            if (LogUtil.enable()) {
                LogUtil.i(TAG, "resetChildHeight : height = " + height);
            }
            if (mNestChildView.getMaxHeight() == height) {
                return;
            }
            mNestChildView.setMaxHeight(height);
        }

        void updateNestChildView() {
            if (mNestChildView == null) {
                reset();
                return;
            }

            childValid = ((View) mNestChildView).getVisibility() == VISIBLE;
            checkEdgeStatus();
            checkScrollStatus();
            if (LogUtil.enable()) {
                LogUtil.i(TAG, "updateNestChildView : controller = " + toString());
            }
            mNestChildView.setScrollEdgeListener(mEdgeListener);
        }

        /**
         * 添加viewpager之后，childNestView可能出现在任意位置。
         * 两种情形特殊处理优化体验：
         * 1,父view滑动到底部，ChildView没有滑动到底部，继续上滑交给子view处理;下滑交给父view处理滑动到tab出现再判断是否子view滑动;
         * 2,父view滑动到顶部，ChildView没有滑动到顶部，继续下滑交给子view处理;上滑交给父view处理滑动到ChildView完全展现在判断是否子view滑动;
         * <p>
         * onInterceptTouchEvent()和overScrollBy()不会交叉执行，所以此处没必要重置scrollUp和scrollDown
         *
         * @param scrollY
         * @param deltaY
         * @return
         */
        int intercept(int scrollY, int deltaY) {
            int newScrollY = scrollY + deltaY;
            //临界位置的判断，需要滚动到临界位置
            boolean scrollUp = !isChildAtBottom && scrollY <= topContentHeight && newScrollY > topContentHeight;
            boolean scrollDown = !isChildAtTop && scrollY >= topContentHeight && newScrollY < topContentHeight;
            if (LogUtil.enable()) {
                LogUtil.i(TAG, "intercept : scrollY = " + scrollY + ", deltaY = " + deltaY + ", controller = "
                        + mScrollController.toString());
            }
            if (scrollUp || scrollDown) {
                deltaY = topContentHeight - scrollY;
            }
            return deltaY;
        }

        void reset() {
            childValid = false;
            allowScrollUp = false;
            allowScrollDown = false;
            isChildAtTop = true;
            isChildAtBottom = false;
            childTopHeight = 0;
        }

        @Override
        public String toString() {
            return "ScrollController{" +
                    "isAtTop=" + isAtTop +
                    ", isAtBottom=" + isAtBottom +
                    ", allowScrollUp=" + allowScrollUp +
                    ", allowScrollDown=" + allowScrollDown +
                    ", isChildAtTop=" + isChildAtTop +
                    ", isChildAtBottom=" + isChildAtBottom +
                    ", topContentHeight=" + topContentHeight +
                    ", childValid=" + childValid +
                    '}';
        }
    }
}
