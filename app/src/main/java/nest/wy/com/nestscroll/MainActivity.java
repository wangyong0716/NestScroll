package nest.wy.com.nestscroll;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {
    private TextView mPagerListBtn;
    private TextView mWebBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPagerListBtn = findViewById(R.id.pager_list_btn);
        mWebBtn = findViewById(R.id.web_btn);
        mPagerListBtn.setOnClickListener(mOnClickListener);
        mWebBtn.setOnClickListener(mOnClickListener);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.pager_list_btn:
                    gotoPagerList();
                    break;
                case R.id.web_btn:
                    gotoScrollWeb();
                    break;
                default:
                    break;
            }
        }
    };

    private void gotoPagerList() {
        Intent intent = new Intent();
        intent.setClass(this, PagerListActivity.class);
        startActivity(intent);
    }

    private void gotoScrollWeb() {
        Intent intent = new Intent();
        intent.setClass(this, ScrollWebActivity.class);
        startActivity(intent);
    }
}
