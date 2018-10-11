package com.example.fairhand.a2048.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.example.fairhand.a2048.R;

/**
 * Created by FairHand on 2018/10/6.<br />
 * 游戏结束对话框
 */
public class CustomOverDialog extends BaseDialog {
    
    private String finalScore;
    private String title;
    
    private View.OnClickListener onShareClickListener;
    private View.OnClickListener onGoOnClickListener;
    
    public CustomOverDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }
    
    @Override
    protected int setView() {
        return R.layout.dialog_custom_over;
    }
    
    @Override
    protected void initData() {
        init();
    }
    
    private void init() {
        TextView title = findViewById(R.id.tv_custom_title);
        TextView finalScore = findViewById(R.id.tv_final_score);
        TextView share = findViewById(R.id.tv_share);
        TextView goOn = findViewById(R.id.tv_go_on);
        if (onShareClickListener != null) {
            share.setOnClickListener(onShareClickListener);
        }
        if (onGoOnClickListener != null) {
            goOn.setOnClickListener(onGoOnClickListener);
        }
        if (!TextUtils.isEmpty(this.finalScore)) {
            finalScore.setText(this.finalScore);
        }
        if (!TextUtils.isEmpty(this.title)) {
            title.setText(this.title);
        }
    }
    
    public CustomOverDialog setOnShareClickListener(
            View.OnClickListener onShareClickListener) {
        this.onShareClickListener = onShareClickListener;
        return this;
    }
    
    public CustomOverDialog setOnGoOnClickListener(
            View.OnClickListener onGoOnClickListener) {
        this.onGoOnClickListener = onGoOnClickListener;
        return this;
    }
    
    /**
     * 设置最终得分
     */
    public CustomOverDialog setFinalScore(String finalScore) {
        this.finalScore = finalScore;
        return this;
    }
    
    /**
     * 设置标题
     */
    public CustomOverDialog setTitle(String title) {
        this.title = title;
        return this;
    }
    
}
