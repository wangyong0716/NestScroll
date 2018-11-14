package nest.wy.com.nestscroll;

import android.util.Log;

/**
 * Created by wangyong on 18-11-14.
 */

public class LogUtil {
    private static final boolean DEBUG = true;

    public static boolean enable() {
        return DEBUG;
    }

    public static void i(String tag, String msg) {
        Log.i(tag, msg);
    }

}
