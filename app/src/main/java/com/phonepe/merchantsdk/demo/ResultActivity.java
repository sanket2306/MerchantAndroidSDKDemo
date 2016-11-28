package com.phonepe.merchantsdk.demo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.phonepe.android.sdk.base.listeners.DataListener;
import com.phonepe.android.sdk.base.models.ErrorInfo;
import com.phonepe.android.sdk.base.networking.response.TransactionStatus;
import com.phonepe.merchantsdk.demo.utils.Constants;
import com.phonepe.android.sdk.api.PhonePe;
import com.phonepe.android.sdk.utils.CheckSumUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ResultActivity extends AppCompatActivity {

    private String mTxnId;
    private boolean mIsCanceled = false;

    private static final String KEY_TXN_ID = "key_txn_id";
    private static final String KEY_IS_CANCELED = "key_was_canceled";

    @Bind(R.id.id_result)
    TextView mTextView;

    @Bind(R.id.id_progressBar)
    ProgressBar mProgressBar;

    @Bind(R.id.id_image_holder)
    ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        ButterKnife.bind(this);
        readBundle(getIntent().getExtras());
        trackTxnStatus(mTxnId, mIsCanceled);

        if(getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    //*********************************************************************
    // Private methods
    //*********************************************************************

    private void readBundle(Bundle bundle) {
        if (bundle != null && bundle.containsKey(KEY_TXN_ID)) {
            mTxnId = bundle.getString(KEY_TXN_ID);
        } else {
            throw new RuntimeException("TransactionId cannot be null");
        }

        if (bundle.containsKey(KEY_IS_CANCELED)) {
            mIsCanceled = bundle.getBoolean(KEY_IS_CANCELED);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void trackTxnStatus(String txnId, boolean wasCanceled) {
        if (wasCanceled) {
            mProgressBar.setVisibility(View.GONE);
            mTextView.setText("Transaction was canceled");
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.cancel_image));
            return;
        }

        mProgressBar.setVisibility(View.VISIBLE);
        mTextView.setText("Fetching status ...");

        String checksum = CheckSumUtils.getCheckSumForTransactionStatus(Constants.MERCHANT_ID, txnId, Constants.SALT, Constants.SALT_KEY_INDEX);
        PhonePe.fetchTransactionStatus(checksum, txnId, new DataListener<TransactionStatus>() {
            @Override
            public void onSuccess(TransactionStatus transactionStatus) {
                mProgressBar.setVisibility(View.GONE);
                if (transactionStatus != null) {
                    mTextView.setText(transactionStatus.getMessage());
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.order_confirm));
                } else {
                    mTextView.setText("Failed to load status of transaction");
                }
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                mProgressBar.setVisibility(View.GONE);
                mTextView.setText("Failed to load status of transaction");
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();

        }

        return true;
    }

    //*********************************************************************
    // Utility methods
    //*********************************************************************

    public static Intent getInstance(Context context, @NonNull String txnId, boolean isCanceled) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TXN_ID, txnId);
        bundle.putBoolean(KEY_IS_CANCELED, isCanceled);
        Intent intent = new Intent(context, ResultActivity.class);
        intent.putExtras(bundle);
        return intent;
    }

    //*********************************************************************
    // End of the class
    //*********************************************************************
}
