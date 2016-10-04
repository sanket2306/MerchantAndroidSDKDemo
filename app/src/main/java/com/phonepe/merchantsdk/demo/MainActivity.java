package com.phonepe.merchantsdk.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.phonepe.android.sdk.api.PhonePe;
import com.phonepe.android.sdk.api.PhonePeResult;
import com.phonepe.android.sdk.api.TransactionCompleteListener;
import com.phonepe.android.sdk.domain.DataListenerContract;
import com.phonepe.android.sdk.domain.builders.CreditRequestBuilder;
import com.phonepe.android.sdk.domain.builders.DebitRequestBuilder;
import com.phonepe.android.sdk.domain.builders.OrderInfoBuilder;
import com.phonepe.android.sdk.domain.builders.SignUpRequestBuilder;
import com.phonepe.android.sdk.domain.builders.UserInfoBuilder;
import com.phonepe.android.sdk.models.APIError;
import com.phonepe.android.sdk.models.api.CreditRequest;
import com.phonepe.android.sdk.models.api.DebitRequest;
import com.phonepe.android.sdk.models.api.OrderInfo;
import com.phonepe.android.sdk.models.api.SignUpRequest;
import com.phonepe.android.sdk.models.api.UserInfo;
import com.phonepe.android.sdk.models.enums.CreditType;
import com.phonepe.android.sdk.models.enums.PayInstrumentOption;
import com.phonepe.android.sdk.models.enums.WalletState;
import com.phonepe.android.sdk.models.networking.response.DebitSuggestion;
import com.phonepe.android.sdk.models.networking.response.TransactionStatus;
import com.phonepe.android.sdk.utils.CheckSumUtils;
import com.phonepe.merchantsdk.demo.utils.CacheUtils;
import com.phonepe.merchantsdk.demo.utils.Constants;

import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int CODE_DEBIT = 189;
    private static final int CODE_CREDIT = 190;
    private static final int CODE_REGISTER = 191;

    @Bind(R.id.id_debit_amount)
    TextView mDebitAmountTextView;

    @Bind(R.id.id_credit_amount)
    TextView mCreditAmountTextView;

    @Bind(R.id.id_result)
    TextView resultTextView;

    @Bind(R.id.id_credit_type)
    SwitchCompat mCreditTypeOption;

    @OnClick(R.id.id_install)
    void installPhonePe() {
        PhonePe.installPhone(this);
    }

    @OnClick(R.id.id_register)
    void showRegisterDemo() {
        startRegister();
    }

    @OnClick(R.id.id_debit)
    void showDebitDemo() {
        startDebit();
    }

    @OnClick(R.id.id_credit)
    void showCreditDemo() {
        startCredit();
    }

    //*********************************************************************
    // Cife cycles
    //*********************************************************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);
        ButterKnife.bind(this);
        mDebitAmountTextView.setText("Rs. " + CacheUtils.getInstance(this).getAmountForTransaction());
        mCreditAmountTextView.setText("Rs. " + CacheUtils.getInstance(this).getAmountForTransaction());

        getWalletBalance();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && (requestCode == CODE_DEBIT || requestCode == CODE_CREDIT)) {
            /* Pass it to PhonePe */
            Bundle extras = data.getExtras();
            if (extras != null && extras.containsKey(PhonePeResult.KEY_TRANSACTION_ID)) {
                trackTxnStatus(extras.getString(PhonePeResult.KEY_TRANSACTION_ID));
            }
        } else {
            resultTextView.setVisibility(View.VISIBLE);
            resultTextView.setText("Failed to complete transaction");
        }
    }

    //*********************************************************************
    // Menu related
    //*********************************************************************

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //*********************************************************************
    // Private class
    //*********************************************************************

    private void startRegister() {
        String sampleUSerId = UUID.randomUUID().toString().substring(0, 15);
        final String txnId = UUID.randomUUID().toString().substring(0, 15);

        String checksum = CheckSumUtils.getCheckSumForRegister(Constants.MERCHANT_ID, txnId, Constants.SALT, Constants.SALT_KEY_INDEX);

        SignUpRequest signUpRequest = new SignUpRequestBuilder()
                .setUserId(sampleUSerId)
                .setMobileNumber("9802836600")
                .setEmail("test@test.com")
                .setShortName("NewUser")
                .setTransactionId(txnId)
                .setChecksum(checksum).build();

        PhonePe.initiateRegister(signUpRequest, new TransactionCompleteListener() {
            @Override
            public void onTransactionComplete() {
                Log.v("Main activity", "transaction complete");
                trackTxnStatus(txnId);
            }

            @Override
            public void onTransactionCanceled() {
                Log.v("Main activity", "transaction canceled");
            }
        });
    }

    private void getWalletBalance() {
        resultTextView.setText("Fetching wallet balance ...");
        String userId = CacheUtils.getInstance(this).getUserId();
        String checksum = CheckSumUtils.getCheckSumForDebitSuggest(Constants.MERCHANT_ID, userId, Constants.SALT, Constants.SALT_KEY_INDEX);

        PhonePe.fetchDebitSuggestion(checksum, userId, new DataListenerContract<DebitSuggestion>() {
            @Override
            public void onSuccess(DebitSuggestion debitSuggestion) {
                if (debitSuggestion != null) {
                    if (debitSuggestion.getWalletState().equals(WalletState.UNKNOWN)) {
                        resultTextView.setText("Wallet Balance: Unknown");
                    } else {
                        Long amountInRs = debitSuggestion.getAvailableBalanceInWallet() / 100;
                        resultTextView.setText("Wallet Balance:" + amountInRs + "Rs.");
                    }
                }
            }

            @Override
            public void onFailure(APIError error) {
                resultTextView.setText("Failed to fetch wallet balance ...");
            }
        });
    }

    private void startDebit() {
        Long amount = CacheUtils.getInstance(this).getAmountForTransaction();
        PayInstrumentOption instrumentOption = PayInstrumentOption.ANY;
        final String txnId = UUID.randomUUID().toString().substring(0, 15);
        String userId = CacheUtils.getInstance(this).getUserId();
        String checksum = CheckSumUtils.getCheckSumForPayment(Constants.MERCHANT_ID, txnId, amount * 100, Constants.SALT, Constants.SALT_KEY_INDEX);

        UserInfo userInfo = new UserInfoBuilder()
                .setUserId(userId)
                .setMobileNumber("9902834400")
                .setEmail("rao@gmail.com")
                .setShortName("Pandeshwar")
                .build();

        OrderInfo orderInfo = new OrderInfoBuilder()
                .setOrderId("someOrderId")
                .setMessage("Pay me for this order.")
                .build();

        DebitRequest debitRequest = new DebitRequestBuilder()
                .setTransactionId(txnId)
                .setAmount(amount * 100)
                .setPaymentInstrumentOption(instrumentOption)
                .setOrderInfo(orderInfo)
                .setUserInfo(userInfo)
                .setChecksum(checksum)
                .build();

        PhonePe.initiateDebit(debitRequest, new TransactionCompleteListener() {
            @Override
            public void onTransactionComplete() {
                Log.v("Main activity", "transaction complete");
                trackTxnStatus(txnId);
            }

            @Override
            public void onTransactionCanceled() {
                resultTextView.setText("Transaction canceled");
                Log.v("Main activity", "transaction canceled");
            }
        });
    }

    void startCredit() {
        Long amount = CacheUtils.getInstance(this).getAmountForTransaction();
        final String txnId = UUID.randomUUID().toString().substring(0, 15);
        String userId = CacheUtils.getInstance(this).getUserId();
        CreditType creditType = CreditType.INSTANT;
        if (mCreditTypeOption.isChecked()) {
            creditType = CreditType.DEFERRED;
        }

        String checksum = CheckSumUtils.getCheckSumForPayment(Constants.MERCHANT_ID, txnId, amount * 100, Constants.SALT, Constants.SALT_KEY_INDEX);

        UserInfo userInfo = new UserInfoBuilder()
                .setUserId(userId)
                .setMobileNumber("9823123412")
                .setEmail("test@test.com")
                .setShortName("New User")
                .build();

        OrderInfo orderInfo = new OrderInfoBuilder()
                .setOrderId("someOrderId")
                .setMessage("Pay me for this order.")
                .build();

        CreditRequest creditRequest = new CreditRequestBuilder()
                .setTransactionId(txnId)
                .setAmount(amount * 100)
                .setOrderInfo(orderInfo)
                .setCreditType(creditType)
                .setUserInfo(userInfo)
                .setChecksum(checksum)
                .build();

        PhonePe.initiateCredit(creditRequest, new TransactionCompleteListener() {
            @Override
            public void onTransactionComplete() {
                Log.v("Main activity", "transaction complete");
                trackTxnStatus(txnId);
            }

            @Override
            public void onTransactionCanceled() {
                Log.v("Main activity", "transaction canceled");
            }
        });
    }

    private void trackTxnStatus(String txnId) {
        resultTextView.setVisibility(View.VISIBLE);

        String checksum = CheckSumUtils.getCheckSumForTransactionStatus(Constants.MERCHANT_ID, txnId, Constants.SALT, Constants.SALT_KEY_INDEX);
        PhonePe.fetchTransactionStatus(checksum, txnId, new DataListenerContract<TransactionStatus>() {
            @Override
            public void onSuccess(TransactionStatus transactionStatus) {
                if (transactionStatus != null) {
                    resultTextView.setText(transactionStatus.getMessage());
                } else {
                    resultTextView.setText("Failed to load status of transaction");
                }
            }

            @Override
            public void onFailure(APIError error) {
                resultTextView.setText("Failed to load status of transaction");
            }
        });
    }

    //*********************************************************************
    // End of the class
    //*********************************************************************
}
