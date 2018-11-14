package nest.wy.com.nestscroll.nest;

/**
 * Created by wangyong on 18-11-5.
 * <p>
 * 子view的高度由外部设置。
 */

public interface NestChildView {
    public boolean isAtTop();

    public boolean isAtBottom();

    public void setScrollEdgeListener(ScrollEdgeListener scrollEdgeListener);

    public void notifyScrollEdgeListener();

    public void setMaxHeight(int height);

    public int getMaxHeight();
}
