package nest.wy.com.nestscroll.nest;

import android.view.View;

/**
 * Created by wangyong on 18-11-6.
 */

public interface HeightChangeListener {
    /**
     * 通知子view高度需要调整
     * @param height
     * @return 子view高度将会调整，则返回true，不会调整则返回false。
     */
    public boolean onHeightChanged(View view, int height);
}
