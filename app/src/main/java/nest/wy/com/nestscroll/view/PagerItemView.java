package nest.wy.com.nestscroll.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import nest.wy.com.nestscroll.ConstantsUtil;
import nest.wy.com.nestscroll.R;
import nest.wy.com.nestscroll.nest.HeightChangeListener;
import nest.wy.com.nestscroll.nest.NestListView;

/**
 * Created by wangyong on 18-11-5.
 */

public class PagerItemView extends FrameLayout {
    private NestListView mListView;
    private DataAdapter mAdapter;
    private HeightChangeListener mHeightChangeListener;
    private int mShowNum;
    private View mFooterView;
    private boolean mFooterAdded;

    private int mItemHeight;
    private int mFooterHeight;

    public PagerItemView(@NonNull Context context) {
        this(context, null);
    }

    public PagerItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PagerItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mItemHeight = getResources().getDimensionPixelOffset(R.dimen.view_height_small);
        mFooterHeight = getResources().getDimensionPixelOffset(R.dimen.footer_height);

        mListView = new NestListView(getContext());
        mListView.setVerticalScrollBarEnabled(false);
        addView(mListView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mAdapter = new DataAdapter(getContext(), getMoreData(ConstantsUtil.DEFAULT_SHOW_NUM));
        mListView.setAdapter(mAdapter);
//        mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        addFooter();
    }

    private int getContentHeight() {
        if (mFooterAdded) {
            return mItemHeight * mShowNum + mFooterHeight;
        }
        return mItemHeight * mShowNum;
    }

    private void addFooter() {
        if (mFooterAdded) {
            return;
        }
        mFooterAdded = true;
        if (mFooterView == null) {
            mFooterView = inflate(getContext(), R.layout.footer_layout, null);
            mFooterView.findViewById(R.id.more).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<String> moreData = getMoreData(ConstantsUtil.ADD_NUM);
                    if (moreData == null) {
                        return;
                    }
                    mAdapter.addData(moreData);
                    mAdapter.notifyDataSetChanged();
                    notifyHeightListener();
                    mListView.scrollToTop();
                }
            });
        }
        mListView.addFooterView(mFooterView);
    }

    private void removeFooter() {
        if (!mFooterAdded) {
            return;
        }
        mListView.removeFooterView(mFooterView);
    }

    public void scrollToTop() {
        mListView.scrollToTop();
    }

    public void setHeightChangeListener(HeightChangeListener heightChangeListener) {
        mHeightChangeListener = heightChangeListener;
        mListView.setHeightChangeListener(mHeightChangeListener);
        notifyHeightListener();
    }

    public void notifyHeightListener() {
        if (mHeightChangeListener != null) {
            int maxHeight = mListView.getMaxHeight();
            int height = getContentHeight();
            mListView.setContentHeight(height);
            mHeightChangeListener.onHeightChanged(mListView, height < maxHeight || maxHeight == 0 ? height : maxHeight);
        }
    }

    public NestListView getNestChildView() {
        return mListView;
    }

    private List<String> getMoreData(int num) {
        if (mShowNum >= ConstantsUtil.MAX_SHOWN_NUM) {
            return null;
        }
        if (mShowNum + num >= ConstantsUtil.MAX_SHOWN_NUM) {
            num = ConstantsUtil.MAX_SHOWN_NUM - mShowNum;
            removeFooter();
        }

        List<String> testData = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            testData.add("数据 " + (mShowNum + i));
        }
        mShowNum += num;
        return testData;
    }

    class DataAdapter extends BaseAdapter {
        private Context mContext;
        private List<String> mData;

        public DataAdapter(Context context, List<String> data) {
            mContext = context;
            mData = data;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }

        private void setData(List<String> data) {
            mData.clear();
            mData.addAll(data);
        }

        private void addData(List<String> data) {
            mData.addAll(data);
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public String getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(mContext, R.layout.item_layout, null);
                holder = new ViewHolder();
                holder.content = convertView.findViewById(R.id.content);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.content.setText(getItem(position));
            return convertView;
        }

        class ViewHolder {
            TextView content;
        }
    }
}
