package com.phonepe.merchantsdk.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import com.phonepe.merchantsdk.demo.utils.CacheUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsActivity extends AppCompatActivity {


    @Bind(R.id.id_user_id)
    EditText mUserIdText;

    @Bind(R.id.id_amount)
    EditText mAmountText;


    @OnClick(R.id.id_save)
    void saveUserId() {
        String userId = mUserIdText.getText().toString();
        if (!userId.trim().equals("")) {
            CacheUtils.getInstance(this).saveUserId(userId);
        }

        String amountString = mAmountText.getText().toString();
        if (!amountString.trim().equals("")) {
            Long amount = 2L;
            try {
                amount = Long.parseLong(amountString);
            } catch (NumberFormatException e) {
            }

            CacheUtils.getInstance(this).saveAmountForTransaction(amount);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        mUserIdText.setText(CacheUtils.getInstance(this).getUserId());
        mAmountText.setText(Long.toString(CacheUtils.getInstance(this).getAmountForTransaction()));
    }
}
