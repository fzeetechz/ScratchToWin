package com.myapps.onlysratchapp.activity;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.myapps.onlysratchapp.R;
import com.myapps.onlysratchapp.fragments.HomeFragment;
import com.myapps.onlysratchapp.models.AdKeysResponse;
import com.myapps.onlysratchapp.models.ConversionResponse;
import com.myapps.onlysratchapp.models.ReferUserResponse;
import com.myapps.onlysratchapp.models.TransactionResponse;
import com.myapps.onlysratchapp.models.VerifyMobileResponse;
import com.myapps.onlysratchapp.models.VerifyOTPResponse;
import com.myapps.onlysratchapp.models.VerifyUserResponse;
import com.myapps.onlysratchapp.models.WithdrawalFirstResponse;
import com.myapps.onlysratchapp.models.WithdrawalResponse;
import com.myapps.onlysratchapp.utils.BaseUrl;
import com.myapps.onlysratchapp.utils.Constant;
import com.myapps.onlysratchapp.utils.Injection;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener , LoginContract.View{
    private DrawerLayout drawer;
    private MainActivity activity;
    private NavigationView mNav;
    private TextView points_textView;
    static final float END_SCALE = 0.7f;
    private RelativeLayout contentView;
    private LoginContract.Presenter presenter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;
        new LoginPresenter(Injection.provideLoginRepository(this), this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            TextView titleText = toolbar.findViewById(R.id.toolbarText);
            titleText.setText(getResources().getString(R.string.app_name));
            points_textView = toolbar.findViewById(R.id.points_text_in_toolbar);
        } catch (Exception e) {
            e.printStackTrace();
        }
        contentView = findViewById(R.id.contentView);
        drawer = findViewById(R.id.drawer_layout);
        mNav = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        mNav.setNavigationItemSelectedListener(this);
        getSupportFragmentManager().beginTransaction().add(R.id.content_frame, HomeFragment.newInstance()).commit();
        onClick();
        animateNavigationDrawer();

        if (Constant.isNetworkAvailable(activity)) {
            presenter.getAdKeys(this);

        } else {
            Constant.showInternetErrorDialog(activity, getResources().getString(R.string.no_internet_connection));
        }
    }

    private void onClick() {
        checkIsBlocked();
        View header = mNav.getHeaderView(0);
        TextView userNameTextView = header.findViewById(R.id.user_name);
        userNameTextView.setText(Constant.getString(activity, Constant.USER_NAME));
        TextView userEmailTextView = header.findViewById(R.id.user_email);
        userEmailTextView.setText(Constant.getString(activity, Constant.USER_EMAIL));
        CircleImageView imageView = header.findViewById(R.id.user_profile_image);
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent policyintent = new Intent(activity, ReferActivity.class);
                    policyintent.putExtra("type", "Profile");
                    startActivity(policyintent);
                    overridePendingTransition(R.anim.enter, R.anim.exit);
                } catch (Exception e) {
                    Constant.showToastMessage(activity, e.getMessage());
                }
            }
        });
        Glide.with(activity)
                .load(BaseUrl.LOAD_USER_IMAGE + Constant.getString(activity, Constant.USER_IMAGE))
                .centerCrop()
                .placeholder(R.drawable.profile)
                .into(imageView);
        setPointsText();
    }

    public void setPointsText() {
        if (points_textView != null) {
            String userPoints = Constant.getString(activity, Constant.USER_POINTS);
            if (userPoints.equalsIgnoreCase("")) {
                userPoints = "0";
            }
            points_textView.setText(userPoints);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        onClick();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_home:
                break;

            case R.id.nav_leaderboard:
                try {
                    Intent policyintent = new Intent(activity, ReferActivity.class);
                    policyintent.putExtra("type", "LeaderBoard");
                    startActivity(policyintent);
                    overridePendingTransition(R.anim.enter, R.anim.exit);
                } catch (Exception e) {
                    Constant.showToastMessage(activity, e.getMessage());
                }
                break;

            case R.id.nav_change_password:
                try {
                    Intent policyintent = new Intent(activity, ReferActivity.class);
                    policyintent.putExtra("type", "changePassword");
                    startActivity(policyintent);
                    overridePendingTransition(R.anim.enter, R.anim.exit);
                } catch (Exception e) {
                    Constant.showToastMessage(activity, e.getMessage());
                }
                break;
            case R.id.nav_share:
                Constant.referApp(activity, Constant.getString(activity, Constant.USER_REFFER_CODE));
                break;
            case R.id.nav_rate:
                try {
                    Uri uri = Uri.parse("market://details?id=" + getPackageName());
                    Intent gotoMarket = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(gotoMarket);
                } catch (ActivityNotFoundException e) {
                    Uri uri = Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName());
                    Intent gotoMarket = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(gotoMarket);
                }
                break;

            case R.id.nav_policy:
                try {
                    Intent policyintent = new Intent(activity, PolicyActivity.class);
                    policyintent.putExtra("type", "Privacy Policy");
                    startActivity(policyintent);
                    overridePendingTransition(R.anim.enter, R.anim.exit);
                } catch (Exception e) {
                    Constant.showToastMessage(activity, e.getMessage());
                }
                break;

            case R.id.nav_exit:
                showLogoutDialog();
                break;
            case R.id.nav_refer:
                try {
                    Intent policyintent = new Intent(activity, ReferActivity.class);
                    policyintent.putExtra("type", "refer");
                    startActivity(policyintent);
                    overridePendingTransition(R.anim.enter, R.anim.exit);
                } catch (Exception e) {
                    Constant.showToastMessage(activity, e.getMessage());
                }

                break;
            case R.id.nav_wallet:
                try {
                    Intent policyintent = new Intent(activity, ReferActivity.class);
                    policyintent.putExtra("type", "wallet");
                    startActivity(policyintent);
                    overridePendingTransition(R.anim.enter, R.anim.exit);
                } catch (Exception e) {
                    Constant.showToastMessage(activity, e.getMessage());
                }

                break;

           /* case R.id.nav_matrixmlm:
                try {
                    Intent policyintent = new Intent(activity, MatrixMlmViewActivity.class);
                    startActivity(policyintent);
                    overridePendingTransition(R.anim.enter, R.anim.exit);
                } catch (Exception e) {
                    Constant.showToastMessage(activity, e.getMessage());
                }

                break;
            case R.id.nav_binarymlm:
                try {
                    Intent policyintent = new Intent(activity, BinaryMlmViewActivity.class);
                    startActivity(policyintent);
                    overridePendingTransition(R.anim.enter, R.anim.exit);
                } catch (Exception e) {
                    Constant.showToastMessage(activity, e.getMessage());
                }

                break;*/

            case R.id.nav_wallethistory:
                try {
                    Intent policyintent = new Intent(activity, WalletActivity.class);
                    startActivity(policyintent);
                    overridePendingTransition(R.anim.enter, R.anim.exit);
                } catch (Exception e) {
                    Constant.showToastMessage(activity, e.getMessage());
                }

                break;


            case R.id.nav_passbook:
                try {
                    Intent policyintent = new Intent(activity, PassbookActivity.class);
                    startActivity(policyintent);
                    overridePendingTransition(R.anim.enter, R.anim.exit);
                } catch (Exception e) {
                    Constant.showToastMessage(activity, e.getMessage());
                }

                break;

            case R.id.nav_coinshistory:
                try {
                    Intent policyintent = new Intent(activity, CoinsActivity.class);
                    startActivity(policyintent);
                    overridePendingTransition(R.anim.enter, R.anim.exit);
                } catch (Exception e) {
                    Constant.showToastMessage(activity, e.getMessage());
                }

                break;

            case R.id.nav_profile:
                try {
                    Intent policyintent = new Intent(activity, ReferActivity.class);
                    policyintent.putExtra("type", "Profile");
                    startActivity(policyintent);
                    overridePendingTransition(R.anim.enter, R.anim.exit);
                } catch (Exception e) {
                    Constant.showToastMessage(activity, e.getMessage());
                }
                break;
            case R.id.nav_about:
                try {
                    Intent policyintent = new Intent(activity, PolicyActivity.class);
                    policyintent.putExtra("type", "About Us");
                    startActivity(policyintent);
                    overridePendingTransition(R.anim.enter, R.anim.exit);
                } catch (Exception e) {
                    Constant.showToastMessage(activity, e.getMessage());
                }
                break;
            case R.id.nav_contact:
                try {
                    Intent policyintent = new Intent(activity, ReferActivity.class);
                    policyintent.putExtra("type", "contact");
                    startActivity(policyintent);
                    overridePendingTransition(R.anim.enter, R.anim.exit);
                } catch (Exception e) {
                    Constant.showToastMessage(activity, e.getMessage());
                }

                break;
            case R.id.nav_faq:
                try {
                    Intent policyintent = new Intent(activity, PolicyActivity.class);
                    policyintent.putExtra("type", "Instruction");
                    startActivity(policyintent);
                    overridePendingTransition(R.anim.enter, R.anim.exit);
                } catch (Exception e) {
                    Constant.showToastMessage(activity, e.getMessage());
                }
                break;
        }
        animateNavigationDrawer();
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }
        showExitDialog();
    }

    private void animateNavigationDrawer() {
        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

                final float diffScaledOffset = slideOffset * (1 - END_SCALE);
                final float offsetScale = 1 - diffScaledOffset;
                contentView.setScaleX(offsetScale);
                contentView.setScaleY(offsetScale);

                final float xOffset = drawerView.getWidth() * slideOffset;
                final float xOffsetDiff = contentView.getWidth() * diffScaledOffset / 2;
                final float xTranslation = xOffset - xOffsetDiff;
                contentView.setTranslationX(xTranslation);
            }
        });

    }

    private void gotoProfile() {
        if (Constant.getString(activity, Constant.IS_LOGIN).equals("true")) {
            try {
                Intent policyintent = new Intent(activity, ReferActivity.class);
                policyintent.putExtra("type", "Profile");
                startActivity(policyintent);
                overridePendingTransition(R.anim.enter, R.anim.exit);
            } catch (Exception e) {
                Constant.showToastMessage(activity, e.getMessage());
            }
            overridePendingTransition(R.anim.enter, R.anim.exit);
        } else {
            Constant.showToastMessage(activity, getResources().getString(R.string.login_first));
            Constant.GotoNextActivity(activity, LoginActivity.class, "msg");
            overridePendingTransition(R.anim.enter, R.anim.exit);
            finish();
        }
    }


    private void checkIsBlocked() {
        if (Constant.getString(activity, Constant.USER_BLOCKED).equals("true")) {
            Constant.showBlockedDialog(activity, getResources().getString(R.string.you_are_blocked));
            return;
        }
        checkUserInfo();
        setUSerName();
    }

    private void setUSerName() {
        String user_points = Constant.getString(activity, Constant.USER_POINTS);
        if (user_points.equals("")) {
        } else {
        }
    }

    private void showLogoutDialog() {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.show_points_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        ImageView imageView = dialog.findViewById(R.id.points_image);
        TextView title_text = dialog.findViewById(R.id.title_text_points);
        TextView points_text = dialog.findViewById(R.id.points);
        AppCompatButton cancel_btn = dialog.findViewById(R.id.cancel_btn);
        AppCompatButton yes_btn = dialog.findViewById(R.id.add_btn);

        imageView.setImageResource(R.drawable.ic_error);
        title_text.setText(getResources().getString(R.string.logout_text));
        points_text.setVisibility(View.GONE);
        cancel_btn.setVisibility(View.VISIBLE);
        cancel_btn.setText(getResources().getString(R.string.no));
        yes_btn.setText(getResources().getString(R.string.yes));

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        yes_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Constant.setString(MainActivity.this, Constant.USER_REFFER_CODE,"");
                Constant.setString(MainActivity.this, Constant.USER_ID,"");
                Constant.setString(MainActivity.this, Constant.FIRST_NAME,"");
                Constant.setString(MainActivity.this, Constant.USER_NUMBER,"");
                Constant.setString(MainActivity.this, Constant.USER_AMOUNT,"");
                Constant.setString(MainActivity.this, Constant.TOKEN,"");
                Constant.setString(MainActivity.this, Constant.USER_POINTS,"0");
                Constant.setString(MainActivity.this, Constant.USER_AMOUNT,"0");

                Constant.setString(MainActivity.this, Constant.IS_LOGIN, "false");//clear all data
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        dialog.show();
    }

    private void showExitDialog() {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.show_points_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        ImageView imageView = dialog.findViewById(R.id.points_image);
        TextView title_text = dialog.findViewById(R.id.title_text_points);
        TextView points_text = dialog.findViewById(R.id.points);
        AppCompatButton cancel_btn = dialog.findViewById(R.id.cancel_btn);
        AppCompatButton yes_btn = dialog.findViewById(R.id.add_btn);

        imageView.setImageResource(R.drawable.ic_error);
        title_text.setText(getResources().getString(R.string.exit_text));
        points_text.setVisibility(View.GONE);
        cancel_btn.setVisibility(View.VISIBLE);
        cancel_btn.setText(getResources().getString(R.string.no));
        yes_btn.setText(getResources().getString(R.string.yes));

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        yes_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                finish();
            }
        });
        dialog.show();
    }

    private void checkUserInfo() {
        String user_refer_code = Constant.getString(activity, Constant.USER_REFFER_CODE);
        String user_name = Constant.getString(activity, Constant.USER_NAME);
        String user_number = Constant.getString(activity, Constant.USER_NUMBER);
       /* if (user_refer_code.equals("") || user_name.equals("") || user_number.equals("")) {
            showUpdateProfileDialog();
        }*/
    }

    private void showUpdateProfileDialog() {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.show_points_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        ImageView imageView = dialog.findViewById(R.id.points_image);
        TextView title_text = dialog.findViewById(R.id.title_text_points);
        TextView points_text = dialog.findViewById(R.id.points);
        AppCompatButton add_btn = dialog.findViewById(R.id.add_btn);

        imageView.setImageResource(R.drawable.profile);
        title_text.setText(getResources().getString(R.string.incomplite_profile));
        points_text.setVisibility(View.GONE);
        add_btn.setText(getResources().getString(R.string.update_profile));

        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        gotoProfile();
                    }
                }, 1000);
            }
        });
        dialog.show();
    }

    @Override
    public void loginResponse(VerifyMobileResponse loginResponse) {

    }

    @Override
    public void verifyUserResponse(VerifyUserResponse verifyUserResponse) {

    }

    @Override
    public void getReferUserResponse(ReferUserResponse referUserResponse, String Message) {

    }

    @Override
    public void verifyFirstOTPResponse(VerifyOTPResponse verifyOTPResponse) {

    }

    @Override
    public void verifySecondOTPResponse(VerifyOTPResponse verifyOTPResponse) {

    }

    @Override
    public void adKeysResponse(ArrayList<AdKeysResponse> adKeysResponse) {
        String appKey="";
        for (int i=0;i<adKeysResponse.size();i++)
        {
            if (adKeysResponse.get(i).getId().equals("4"))
            {
                appKey=adKeysResponse.get(i).getApikey();
            }

        }
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            String myApiKey = bundle.getString("com.google.android.gms.ads.APPLICATION_ID");
            Log.d("TAG", "Name Found: " + myApiKey);
            ai.metaData.putString("com.google.android.gms.ads.APPLICATION_ID", appKey);//you can replace your key APPLICATION_ID here
            String ApiKey = bundle.getString("com.google.android.gms.ads.APPLICATION_ID");
            Log.d("TAG", "ReNamed Found: " + ApiKey);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("TAG", "Failed to load meta-data, NameNotFound: " + e.getMessage());
        } catch (NullPointerException e) {
            Log.e("TAG", "Failed to load meta-data, NullPointer: " + e.getMessage());
        }
    }

    @Override
    public void withdrawResponse(WithdrawalResponse withdrawalResponse) {

    }

    @Override
    public void withdrawalFirstResponse(WithdrawalFirstResponse withdrawalFirstResponse) {

    }

    @Override
    public void withdrawalSecondResponse(WithdrawalFirstResponse withdrawalFirstResponse) {

    }

    @Override
    public void transactionResponse(ArrayList<TransactionResponse> transactionResponse) {

    }

    @Override
    public void saveConvertEarningResponse(ConversionResponse message) {

    }

    @Override
    public void saveCoinsResponse(String message) {

    }

    @Override
    public void setPresenter(LoginContract.Presenter presenter) {
     this.presenter=presenter;
    }
}