package cn.bahamut.vessage.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import cn.bahamut.vessage.R;

public class EditPropertyActivity extends AppCompatActivity {

    public static final String KEY_PROPERTY_VALUE = "PROPERTY_VALUE";
    public static final String KEY_PROPERTY_NAME = "PROPERTY_NAME";
    public static final String KEY_REQUEST_ID = "PROPERTY_ID";
    public static final String KEY_PROPERTY_NEW_VALUE = "PROPERTY_NEW_VALUE";
    public static final int RESULT_CODE_SAVED_PROPERTY = 1;

    private EditText mPropertyValueEditView;
    private Button mOKButton;
    private String initPropertyValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_property);
        mPropertyValueEditView = (EditText) findViewById(R.id.edit_text_property);
        mOKButton = (Button) findViewById(R.id.btn_ok);
        mOKButton.setOnClickListener(onClickOK);
        String propertyName = getIntent().getStringExtra(KEY_PROPERTY_NAME);
        initPropertyValue = getIntent().getStringExtra(KEY_PROPERTY_VALUE);
        setTitle(propertyName);
        mPropertyValueEditView.setText(initPropertyValue);
    }

    private View.OnClickListener onClickOK = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mPropertyValueEditView.clearFocus();
            Intent intent = new Intent();
            intent.putExtra(KEY_PROPERTY_VALUE, getIntent().getStringExtra(KEY_PROPERTY_VALUE));
            intent.putExtra(KEY_PROPERTY_NAME, getIntent().getStringExtra(KEY_PROPERTY_NAME));
            intent.putExtra(KEY_PROPERTY_NEW_VALUE, mPropertyValueEditView.getText().toString());
            setResult(RESULT_CODE_SAVED_PROPERTY, intent);
            finishActivity(getIntent().getIntExtra(KEY_REQUEST_ID, 0));
            finish();
        }
    };

    static public void showEditPropertyActivity(Activity context,int requestId, int propertyNameResId, String propertyValue){
        showEditPropertyActivity(context,requestId,context.getResources().getString(propertyNameResId),propertyValue);
    }

    static public void showEditPropertyActivity(Activity context, int requestId, String propertyName, String propertyValue){
        Intent intent = new Intent(context,EditPropertyActivity.class);
        intent.putExtra(KEY_PROPERTY_VALUE,propertyValue);
        intent.putExtra(KEY_PROPERTY_NAME,propertyName);
        intent.putExtra(KEY_REQUEST_ID,requestId);
        context.startActivityForResult(intent,requestId);
    }
}
