package cn.bahamut.vessage.activities.tim;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;

import java.io.File;
import java.io.IOException;

import cn.bahamut.common.StringHelper;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.main.UserSetting;

public class TextImagePreviewActivity extends AppCompatActivity {

    private ImageView backgroundImageView;
    private TextView contentTextView;
    private RecyclerView backgroundListView;
    private View contentContainer;
    private SeekBar fontSeek;

    private int selectedBackgroundIndex = 0;
    private float fontSize = 0;
    static TextImagePreviewActivity instance;

    private float getCachedFontSize() {
        String key = UserSetting.generateUserSettingKey(TIMConstants.cachedFontSizeKey);
        return UserSetting.getUserSettingPreferences().getFloat(key, fontSize);
    }

    private int getSelectedTIMStyleIndex() {
        String key = UserSetting.generateUserSettingKey(TIMConstants.selectedStyleIndexKey);
        return UserSetting.getUserSettingPreferences().getInt(key, 0);
    }

    private void setCachedFontSize(float newSize) {
        String key = UserSetting.generateUserSettingKey(TIMConstants.cachedFontSizeKey);
        UserSetting.getUserSettingPreferences().edit().putFloat(key, newSize);
    }

    private void setSelectedTIMStyleIndex(int newIndex) {
        String key = UserSetting.generateUserSettingKey(TIMConstants.selectedStyleIndexKey);
        UserSetting.getUserSettingPreferences().edit().putInt(key, newIndex);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.tim_activity_text_image_preview);

        setTitle(R.string.tim_title);

        backgroundImageView = (ImageView) findViewById(R.id.bcg_img_view);
        contentTextView = (TextView) findViewById(R.id.text_content);
        backgroundListView = (RecyclerView) findViewById(R.id.select_bcg_list);
        contentContainer = findViewById(R.id.content_container);
        findViewById(R.id.font_btn).setOnClickListener(onClickViews);
        fontSeek = (SeekBar) findViewById(R.id.font_size_seek);

        fontSize = contentTextView.getTextSize();
        fontSize = getCachedFontSize();
        selectedBackgroundIndex = getSelectedTIMStyleIndex();

        float fontProgress = fontSize / (TIMConstants.maxFontSize - TIMConstants.minFontSize) * 100;
        fontSeek.setProgress((int) fontProgress);

        String textContent = getIntent().getStringExtra("textContent");
        if (StringHelper.isStringNullOrWhiteSpace(textContent)) {
            contentTextView.setText(R.string.tim_default_text);
        } else {
            contentTextView.setText(textContent);
        }

        contentTextView.setTextSize(getCachedFontSize());

        TIMConstants.TIMStyle style = TIMConstants.getStyleOfIndex(getSelectedTIMStyleIndex());
        backgroundImageView.setImageResource(style.backgroundResId);


        contentTextView.setTextColor(style.getColor());

        backgroundListView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true));
        backgroundListView.setAdapter(new Adapter());

        fontSeek.setOnSeekBarChangeListener(onSeekbarChange);
        backgroundListView.scrollToPosition(selectedBackgroundIndex);

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
            try {
                File tmpViewShotFile = File.createTempFile("tim_tmp", ".png");
                Bitmap bitmap = captureContentImage();
                ImageHelper.storeBitmap2PNG(this, bitmap, tmpViewShotFile, 1);
                Intent intent = new Intent(this, TextImageSaveAndShareActivity.class);
                intent.setData(Uri.fromFile(tmpViewShotFile));
                startActivity(intent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private Bitmap captureContentImage() {
        return ImageHelper.convertViewToBitmap(contentContainer);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        RoundedImageView imageView;
        int pos = 0;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (RoundedImageView) itemView.findViewById(R.id.imageView);
            imageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            setSelectedBackground(pos);
        }
    }

    private void setSelectedBackground(int pos) {
        setSelectedTIMStyleIndex(pos);
        selectedBackgroundIndex = pos;
        TIMConstants.TIMStyle style = TIMConstants.getStyleOfIndex(pos);
        backgroundImageView.setImageResource(style.backgroundResId);
        contentTextView.setTextColor(style.getColor());
        backgroundListView.getAdapter().notifyDataSetChanged();
    }

    class Adapter extends RecyclerView.Adapter<ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = TextImagePreviewActivity.this.getLayoutInflater().inflate(R.layout.tim_bcg_item, null);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.pos = position;
            holder.imageView.setImageResource(TIMConstants.getStyleOfIndex(position).backgroundResId);
            if (position == selectedBackgroundIndex) {
                holder.imageView.setBorderColor(Color.BLUE);
            } else {
                holder.imageView.setBorderColor(Color.DKGRAY);
            }
        }

        @Override
        public int getItemCount() {
            return TIMConstants.getStyleCount();
        }
    }

    private SeekBar.OnSeekBarChangeListener onSeekbarChange = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            fontSize = TIMConstants.minFontSize + (progress / 100f) * (TIMConstants.maxFontSize - TIMConstants.minFontSize);
            contentTextView.setTextSize(fontSize);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            setCachedFontSize(fontSize);
        }
    };

    private View.OnClickListener onClickViews = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.font_btn:
                    break;
                default:
                    break;
            }
        }
    };
}
