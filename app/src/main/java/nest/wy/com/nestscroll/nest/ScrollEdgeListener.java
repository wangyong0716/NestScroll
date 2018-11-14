package nest.wy.com.nestscroll.nest;

/**
 * Created by wangyong on 18-11-5.
 */

public interface ScrollEdgeListener {
    /**
     * 子view滑动到或者滑离顶部/底部时候的回调。
     * @param isTop
     * @param isBottom
     */
    void onEdgeChanged(boolean isTop, boolean isBottom);

    /**
     * 重置子view高度后，通知父view，子view是否满屏
     * @param allShown
     */
    void onDataFilled(boolean allShown);

    /**
     * 子view内容加载更多的时候通知父view滚动到临界位置
     * @param newHeight
     */
    void scrollToTop(int newHeight);
}
