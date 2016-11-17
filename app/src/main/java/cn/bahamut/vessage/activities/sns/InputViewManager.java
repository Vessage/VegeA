package cn.bahamut.vessage.activities.sns;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import cn.bahamut.common.SoftKeyboardStateHelper;
import cn.bahamut.vessage.R;

/**
 * Created by alexchow on 2016/11/17.
 */

public class InputViewManager {

    static public interface InputViewManagerListener{
        void onSendButtonClicked(InputViewManager manager,EditText editText,Object data);
        void onKeyboardOpened(InputViewManager manager);
        void onKeyboardClosed(InputViewManager manager);
    }

    static public class InputViewManagerListenerAdapter implements InputViewManagerListener{

        @Override
        public void onSendButtonClicked(InputViewManager manager, EditText editText, Object data) {

        }

        @Override
        public void onKeyboardOpened(InputViewManager manager) {

        }

        @Override
        public void onKeyboardClosed(InputViewManager manager) {

        }
    }

    private Button sendButton;
    private EditText editText;
    private Activity context;
    private View inputView;

    private InputViewManagerListener listener;

    private Object data;

    private SoftKeyboardStateHelper keyboardStateHelper;

    public InputViewManager(Activity context, int inputViewId){
        this.context = context;
        this.inputView = this.context.findViewById(inputViewId);
        this.editText = (EditText) this.inputView.findViewById(R.id.edit_text);
        this.sendButton = (Button) this.inputView.findViewById(R.id.send_btn);
        this.sendButton.setOnClickListener(onClickSendButton);
        keyboardStateHelper = new SoftKeyboardStateHelper(context.findViewById(android.R.id.content));
        keyboardStateHelper.addSoftKeyboardStateListener(kbStateListener);
    }

    public void onDestroy(){
        keyboardStateHelper.removeSoftKeyboardStateListener(kbStateListener);
    }

    public void hideInputView(){
        inputView.setVisibility(View.INVISIBLE);
    }

    public void showInputView(){
        inputView.setVisibility(View.VISIBLE);
    }

    public InputViewManagerListener getListener() {
        return listener;
    }

    public void setListener(InputViewManagerListener listener) {
        this.listener = listener;
    }

    private SoftKeyboardStateHelper.SoftKeyboardStateListener kbStateListener = new SoftKeyboardStateHelper.SoftKeyboardStateListener() {
        @Override
        public void onSoftKeyboardOpened(int keyboardHeightInPx) {
            if (listener != null){
                listener.onKeyboardOpened(InputViewManager.this);
            }
        }

        @Override
        public void onSoftKeyboardClosed() {
            if (listener != null){
                listener.onKeyboardClosed(InputViewManager.this);
            }
        }
    };

    public void clearEditingText(){
        editText.setText(null);
    }

    public void hideKeyboard(){
        editText.clearFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(),0);
    }

    public void openKeyboard(){
        openKeyboard(null,null);
    }

    public void openKeyboard(String hint,Object data) {
        this.data = data;
        if (hint == null) {
            editText.setHint(R.string.sns_comment_hint);
        } else {
            editText.setHint(hint);
        }
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, 0);
    }

    private View.OnClickListener onClickSendButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onSendButtonClicked(InputViewManager.this, editText, data);
            }
        }
    };
}
