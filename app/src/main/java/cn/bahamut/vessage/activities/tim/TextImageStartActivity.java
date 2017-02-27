package cn.bahamut.vessage.activities.tim;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import cn.bahamut.common.StringHelper;
import cn.bahamut.vessage.R;

/**
 * Created by alexchow on 2017/1/6.
 */

public class TextImageStartActivity extends AppCompatActivity {
    public static final int iconId = R.drawable.tim_icon;

    private EditText contentEditText;
    static TextImageStartActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.tim_activity_text_image_editor);
        setTitle(R.string.tim_title);
        contentEditText = (EditText) findViewById(R.id.content_et);
        findViewById(R.id.image_view).setVisibility(View.INVISIBLE);

        setTitle(R.string.tim_title);
        contentEditText.setText(getIntent().getStringExtra("textContent"));
        String contentEditTextHint = getIntent().getStringExtra("textContentHint");
        if (StringHelper.isStringNullOrWhiteSpace(contentEditTextHint) == false) {
            contentEditText.setHint(contentEditTextHint);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(1, 1, 1, R.string.next_step).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            nextStep();
        }
        return super.onOptionsItemSelected(item);
    }

    private void nextStep() {
        Intent intent = new Intent(this, TextImagePreviewActivity.class);
        intent.putExtra("textContent", contentEditText.getText().toString());
        startActivity(intent);
    }
}
