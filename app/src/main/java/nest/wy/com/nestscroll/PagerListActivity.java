package nest.wy.com.nestscroll;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import nest.wy.com.nestscroll.nest.HeightChangeListener;
import nest.wy.com.nestscroll.nest.NestScrollView;
import nest.wy.com.nestscroll.view.PagerItemView;

/**
 * Created by wangyong on 18-11-14.
 */

public class PagerListActivity extends Activity {
    private static final String TAG = "WY.PagerListActivity";
    private NestScrollView mNestScrollView;
    private ViewPager mViewPager;
    private List<PagerItemView> mViewsList;
    private MyPagerAdapter mPagerAdapter;

    private int mChildHeight;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager_list);

        mNestScrollView = findViewById(R.id.sv);
        mViewPager = findViewById(R.id.vp);
        mViewsList = new ArrayList<>();

        init();
    }

    private void init() {
        addPage();
        mPagerAdapter = new MyPagerAdapter(mViewsList);
        mViewPager.setAdapter(mPagerAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (LogUtil.enable()) {
                    LogUtil.i(TAG, "onPageSelected -> position = " + position);
                }
                mNestScrollView.setNestChildView(mViewsList.get(position).getNestChildView());
                mViewsList.get(position).notifyHeightListener();
                mViewsList.get(position).scrollToTop();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mNestScrollView.setTabView(findViewById(R.id.tab));
        mNestScrollView.setNestChildView(mViewsList.get(0).getNestChildView());
    }

    private HeightChangeListener mHeightChangeListener = new HeightChangeListener() {
        @Override
        public boolean onHeightChanged(View view, int height) {
            if (mChildHeight == height) {
                return false;
            }
            setHeight(height);
            return true;
        }
    };

    private void setHeight(int height) {
        ViewGroup.LayoutParams params = mViewPager.getLayoutParams();
        params.height = height;
        mViewPager.setLayoutParams(params);
    }

    private void addPage() {
        for (int i = 0; i < 5; i++) {
            PagerItemView view = new PagerItemView(this);
            view.setHeightChangeListener(mHeightChangeListener);
            mViewsList.add(view);
        }
    }


    class MyPagerAdapter extends PagerAdapter {
        private List<PagerItemView> views;

        public MyPagerAdapter(List<PagerItemView> views) {
            this.views = views;
        }

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            // return super.instantiateItem(container, position);
            View view = views.get(position);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            //super.destroyItem(container, position, object);
            container.removeView(views.get(position));
        }

        @Override
        public CharSequence getPageTitle(int position) {
            //return super.getPageTitle(position);
            return "标题" + position;
        }

    }
}
