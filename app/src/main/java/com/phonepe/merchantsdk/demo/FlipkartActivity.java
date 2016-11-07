package com.phonepe.merchantsdk.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.phonepe.android.sdk.api.PhonePe;
import com.phonepe.android.sdk.base.enums.CreditType;
import com.phonepe.android.sdk.base.enums.ErrorCode;
import com.phonepe.android.sdk.base.listeners.AccountDetailsListener;
import com.phonepe.android.sdk.base.listeners.TransactionCompleteListener;
import com.phonepe.android.sdk.base.models.CreditRequest;
import com.phonepe.android.sdk.base.models.DebitRequest;
import com.phonepe.android.sdk.base.models.ErrorInfo;
import com.phonepe.android.sdk.base.models.OrderInfo;
import com.phonepe.android.sdk.base.models.PayInstrumentOption;
import com.phonepe.android.sdk.domain.builders.CreditRequestBuilder;
import com.phonepe.android.sdk.domain.builders.DebitRequestBuilder;
import com.phonepe.android.sdk.domain.builders.OrderInfoBuilder;
import com.phonepe.android.sdk.domain.builders.SignUpRequestBuilder;
import com.phonepe.android.sdk.domain.builders.UserInfoBuilder;
import com.phonepe.android.sdk.utils.CheckSumUtils;
import com.phonepe.merchantsdk.demo.utils.AppUtils;
import com.phonepe.merchantsdk.demo.utils.CacheUtils;
import com.phonepe.merchantsdk.demo.utils.Constants;

import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FlipkartActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final long ITEM_AMOUNT= 420l;

    private String mMobileNo;
    private String mEmail;
    private String mName;

    @Bind(R.id.image01)
    View image01;

    @Bind(R.id.image02)
    View image02;

    @Bind(R.id.image03)
    View image03;

    @Bind(R.id.image04)
    View image04;

    @Bind(R.id.refund01)
    View refund01;

    @Bind(R.id.refund02)
    View refund02;

    @Bind(R.id.clickBlocker)
    View clickBlocker;

    @OnClick(R.id.image01)
    void image01Click() {
        setImageVisibility(image02,image01,image03,image04);
    }

    @OnClick(R.id.image02)
    void image02Click() {
        setImageVisibility(image03,image01,image02,image04);
    }

    @OnClick(R.id.image03)
    void image03Click() {
        setImageVisibility(image04,image01,image02,image03);
    }

    @OnClick(R.id.image04)
    void image04Click() {
        startPayment();
    }

    @OnClick(R.id.refund01)
    void refund01Click() {
        setRefundVisibility(refund02,refund01);
    }

    @OnClick(R.id.refund02)
    void refund02Click() {
        startCredit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getToDefalultHomeState();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flipkart);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ButterKnife.bind(this);
        setDefaults();

        View headerLayout = navigationView.getHeaderView(0);

        ((TextView)(headerLayout.findViewById(R.id.email) )).setText(mEmail);
        ((TextView)(headerLayout.findViewById(R.id.username) )).setText(mName);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 100) {
            setDefaults();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (image04.getVisibility() == View.VISIBLE) {
                setImageVisibility(image03, image01, image02, image04);
            } else if (image03.getVisibility() == View.VISIBLE) {
                setImageVisibility(image02, image01, image03, image04);
            } else if (image02.getVisibility() == View.VISIBLE) {
                setImageVisibility(image01, image02, image03, image04);
                clickBlocker.setVisibility(View.GONE);
            } else {
                super.onBackPressed();
            }
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.drawer_home) {
            getToDefalultHomeState();
        } else if (id == R.id.drawer_refund) {
            setRefundVisibility(refund01,refund02);

        } else if (id == R.id.drawer_account) {
            String userId = CacheUtils.getInstance(this).getUserId();
            //String userId = UUID.randomUUID().toString().substring(0, 15);
            String checksum = CheckSumUtils.getCheckSumForNonTransaction(Constants.MERCHANT_ID, userId, Constants.SALT, Constants.SALT_KEY_INDEX);

            PhonePe.showAccountDetails(checksum, userId, new AccountDetailsListener() {
                @Override
                public void onSignUpClicked() {
                    startLoginRegister(false);
                }

                @Override
                public void onSignInClicked() {
                    startLoginRegister(true);
                }
            });

        }else if(id == R.id.drawer_settings)
        {
            startActivityForResult(new Intent(FlipkartActivity.this, SettingsActivity.class), 100);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    //*********************************************************************
    // Private class
    //*********************************************************************

    private void getToDefalultHomeState()
    {
        refund02.setVisibility(View.GONE);
        refund01.setVisibility(View.GONE);
        setImageVisibility(image01,image02,image03,image04);
        clickBlocker.setVisibility(View.GONE);
    }

    private void setImageVisibility(View visibleImage, View v2, View v3, View v4)
    {
        visibleImage.setVisibility(View.VISIBLE);
        v2.setVisibility(View.GONE);
        v3.setVisibility(View.GONE);
        v4.setVisibility(View.GONE);
        clickBlocker.setVisibility(View.VISIBLE);
    }

    private void setRefundVisibility(View visibleImage, View v2)
    {
        visibleImage.setVisibility(View.VISIBLE);
        v2.setVisibility(View.GONE);
        clickBlocker.setVisibility(View.VISIBLE);
    }

    private void setDefaults() {
        mMobileNo = CacheUtils.getInstance(this).getMobile();
        mEmail = CacheUtils.getInstance(this).getEmail();
        mName = CacheUtils.getInstance(this).getName();
    }

    private void startPayment() {
        Long amount = ITEM_AMOUNT;
        PayInstrumentOption instrumentOption = PayInstrumentOption.ANY;
        final String txnId = UUID.randomUUID().toString().substring(0, 15);
        String userId = CacheUtils.getInstance(this).getUserId();
        String checksum = CheckSumUtils.getCheckSumForPayment(Constants.MERCHANT_ID, txnId, amount * 100, Constants.SALT, Constants.SALT_KEY_INDEX);

        UserInfoBuilder userInfoBuilder = new UserInfoBuilder()
                .setUserId(userId);


        if (!AppUtils.isEmpty(mMobileNo)) {
            userInfoBuilder.setMobileNumber(mMobileNo);
        }

        if (!AppUtils.isEmpty(mEmail)) {
            userInfoBuilder.setEmail(mEmail);
        }

        if (!AppUtils.isEmpty(mName)) {
            userInfoBuilder.setShortName(mName);
        }


        OrderInfo orderInfo = new OrderInfoBuilder()
                .setOrderId(" Order No. OD139924923")
                .setMessage("Payment towards 'Batman : Arkham Origins' (Order No. OD139924923)")
                .build();

        DebitRequest debitRequest = new DebitRequestBuilder()
                .setTransactionId(txnId)
                .setAmount(amount * 100)
                .setPaymentInstrumentOption(instrumentOption)
                .setOrderInfo(orderInfo)
                .setUserInfo(userInfoBuilder.build())
                .setChecksum(checksum)
                .build();

        PhonePe.initiateDebit(debitRequest, new TransactionCompleteListener() {
            @Override
            public void onTransactionComplete() {
                trackTxnStatus(txnId, false);
            }

            @Override
            public void onTransactionFailed(ErrorInfo errorInfo) {
                trackTxnStatus(txnId, errorInfo.getCode() == ErrorCode.ERROR_CANCELED);
            }
        });
    }

    void startCredit() {
        Long amount = ITEM_AMOUNT;
        final String txnId = UUID.randomUUID().toString().substring(0, 15);
        String userId = CacheUtils.getInstance(this).getUserId();

        CreditType creditType = CreditType.INSTANT;
        /*  if (mCreditTypeOption.isChecked()) {
            creditType = CreditType.DEFERRED;
        }*/

        String checksum = CheckSumUtils.getCheckSumForPayment(Constants.MERCHANT_ID, txnId, amount * 100, Constants.SALT, Constants.SALT_KEY_INDEX);

        UserInfoBuilder userInfoBuilder = new UserInfoBuilder()
                .setUserId(userId);

        if (!AppUtils.isEmpty(mMobileNo)) {
            userInfoBuilder.setMobileNumber(mMobileNo);
        }

        if (!AppUtils.isEmpty(mEmail)) {
            userInfoBuilder.setEmail(mEmail);
        }

        if (!AppUtils.isEmpty(mName)) {
            userInfoBuilder.setShortName(mName);
        }

        OrderInfo orderInfo = new OrderInfoBuilder()
                .setOrderId("OD139924923")
                .setMessage("Payment towards 'Batman : Arkham Origins' (Order No. OD139924923)")
                .build();

        CreditRequest creditRequest = new CreditRequestBuilder()
                .setTransactionId(txnId)
                .setAmount(amount * 100)
                .setOrderInfo(orderInfo)
                .setCreditType(creditType)
                .setUserInfo(userInfoBuilder.build())
                .setChecksum(checksum)
                .build();

        PhonePe.initiateCredit(creditRequest, new TransactionCompleteListener() {
            @Override
            public void onTransactionComplete() {
                trackTxnStatus(txnId, false);
            }

            @Override
            public void onTransactionFailed(ErrorInfo errorInfo) {
                trackTxnStatus(txnId, errorInfo.getCode() == ErrorCode.ERROR_CANCELED);
            }
        });
    }

    private void startLoginRegister(boolean isLogin) {
        String sampleUSerId = UUID.randomUUID().toString().substring(0, 15);
        if (isLogin) {
            sampleUSerId = CacheUtils.getInstance(this).getUserId();
        }

        final String txnId = UUID.randomUUID().toString().substring(0, 15);

        String checksum = CheckSumUtils.getCheckSumForRegister(Constants.MERCHANT_ID, txnId, Constants.SALT, Constants.SALT_KEY_INDEX);

        SignUpRequestBuilder signUpRequestBuilder = new SignUpRequestBuilder()
                .setUserId(sampleUSerId)
                .setTransactionId(txnId)
                .setChecksum(checksum);

        if (!AppUtils.isEmpty(mMobileNo)) {
            signUpRequestBuilder.setMobileNumber(mMobileNo);
        }

        if (!AppUtils.isEmpty(mEmail)) {
            signUpRequestBuilder.setEmail(mEmail);
        }

        if (!AppUtils.isEmpty(mName)) {
            signUpRequestBuilder.setShortName(mName);
        }

        PhonePe.initiateRegister(signUpRequestBuilder.build(), new TransactionCompleteListener() {
            @Override
            public void onTransactionComplete() {
                Toast.makeText(FlipkartActivity.this, "Complete", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTransactionFailed(ErrorInfo errorInfo) {
                trackTxnStatus(txnId, errorInfo.getCode() == ErrorCode.ERROR_CANCELED);
            }
        });
    }

    private void trackTxnStatus(final String txnId, final boolean wascanceled) {
        startActivity(ResultActivity.getInstance(this, txnId, wascanceled));
        overridePendingTransition(0, 0);
    }

    //*********************************************************************
    // End of the class
    //*********************************************************************
}
