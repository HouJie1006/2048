package com.example.fairhand.a2048.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.example.fairhand.a2048.R;

/**
 * @author FairHand
 * @date 2018/10/10
 * 自定义更新Dialog
 */
public class CustomUpdateDialog extends Dialog {
    
    
    private static String vision;
    private static String size;
    private static String log;
    private static View.OnClickListener onCloseButtonListener;
    private static View.OnClickListener onUpdateButtonListener;
    private static View.OnClickListener onIgnoreButtonListener;
    private static View.OnClickListener onInstallButtonListener;
    
    private Button updateButton;
    private Button installButton;
    private NumberProgressBar progressBar;
    private LinearLayout closeLayout;
    private TextView ignoreUpdate;
    
    private CustomUpdateDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_custom_update);
        init();
    }
    
    private void init() {
        TextView newVersion = findViewById(R.id.tv_new_version);
        TextView versionSize = findViewById(R.id.tv_version_size);
        TextView updateLog = findViewById(R.id.tv_version_update_log);
        ImageView closeUpdate = findViewById(R.id.iv_close_update);
        ignoreUpdate = findViewById(R.id.tv_ignore_update);
        progressBar = findViewById(R.id.number_progress_bar);
        updateButton = findViewById(R.id.btn_update);
        installButton = findViewById(R.id.btn_install);
        closeLayout = findViewById(R.id.ll_close_update);
        if (!TextUtils.isEmpty(vision)) {
            newVersion.setText(vision);
        }
        if (!TextUtils.isEmpty(size)) {
            versionSize.setText(size);
        }
        if (!TextUtils.isEmpty(log)) {
            updateLog.setText(log);
        }
        if (onCloseButtonListener != null) {
            closeUpdate.setOnClickListener(onCloseButtonListener);
        }
        if (onUpdateButtonListener != null) {
            updateButton.setOnClickListener(onUpdateButtonListener);
        }
        if (onIgnoreButtonListener != null) {
            ignoreUpdate.setOnClickListener(onIgnoreButtonListener);
        }
        if (onInstallButtonListener != null) {
            installButton.setOnClickListener(onInstallButtonListener);
        }
    }
    
    /**
     * 设置进度条进度
     */
    public void setProgressPercent(int percent) {
        progressBar.setProgress(percent);
    }
    
    /**
     * 开始更新，隐藏升级按钮，显示下载进度条
     */
    public void startUpdate() {
        updateButton.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        closeLayout.setVisibility(View.GONE);
        ignoreUpdate.setVisibility(View.GONE);
    }
    
    /**
     * 更新完成，隐藏下载进度条，显示安装按钮
     */
    public void updateComplete() {
        progressBar.setVisibility(View.INVISIBLE);
        installButton.setVisibility(View.VISIBLE);
    }
    
    public static class Builder {
        private Context context;
        private int themeResId;
        
        public Builder(Context context, int themeResId) {
            this.context = context;
            this.themeResId = themeResId;
        }
        
        public Builder setCloseButtonListener(
                View.OnClickListener onCloseButtonListener) {
            CustomUpdateDialog.onCloseButtonListener = onCloseButtonListener;
            return this;
        }
        
        public Builder setUpdateButtonListener(
                View.OnClickListener onUpdateButtonListener) {
            CustomUpdateDialog.onUpdateButtonListener = onUpdateButtonListener;
            return this;
        }
        
        public Builder setInstallButtonListener(
                View.OnClickListener onInstallButtonListener) {
            CustomUpdateDialog.onInstallButtonListener = onInstallButtonListener;
            return this;
        }
        
        public Builder setIgnoreButtonListener(
                View.OnClickListener onIgnoreButtonListener) {
            CustomUpdateDialog.onIgnoreButtonListener = onIgnoreButtonListener;
            return this;
        }
        
        public Builder setVersionName(String versionName) {
            CustomUpdateDialog.vision = versionName;
            return this;
        }
        
        public Builder setVersionSize(String versionSize) {
            CustomUpdateDialog.size = versionSize;
            return this;
        }
        
        public Builder setVersionLog(String versionLog) {
            CustomUpdateDialog.log = versionLog;
            return this;
        }
        
        public CustomUpdateDialog build() {
            return new CustomUpdateDialog(context, themeResId);
        }
        
    }
    
}
