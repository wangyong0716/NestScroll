package nest.wy.com.nestscroll;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import nest.wy.com.nestscroll.nest.HeightChangeListener;
import nest.wy.com.nestscroll.nest.NestScrollView;
import nest.wy.com.nestscroll.nest.NestWebView;

/**
 * Created by wangyong on 18-11-14.
 */

public class ScrollWebActivity extends Activity{
    private NestScrollView mNestScrollView;
    private View mTabView;
    private NestWebView mNestWebView;

    private int mChildHeight;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        mNestScrollView = findViewById(R.id.nsv);
        mTabView = findViewById(R.id.tab);
        mNestWebView = findViewById(R.id.web);

        init();

    }

    private void init() {
        mNestScrollView.setTabView(mTabView);
        mNestScrollView.setNestChildView(mNestWebView);
        mNestWebView.loadUrl("https://baike.baidu.com/item/Android/60243?fr=aladdin");
        mNestWebView.setHeightChangeListener(new HeightChangeListener() {
            @Override
            public boolean onHeightChanged(View view, int height) {
                if (mChildHeight == height) {
                    return false;
                }
                updateChildHeight(height);
                mChildHeight = height;
                return true;
            }
        });
    }

    private void updateChildHeight(int height) {
        ViewGroup.LayoutParams layoutParams = mNestWebView.getLayoutParams();
        layoutParams.height = height;
        mNestWebView.setLayoutParams(layoutParams);
    }
}
