package com.hacktx.android.activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.hacktx.android.R;
import com.hacktx.android.network.UserStateStore;
import com.hacktx.android.services.BeaconService;

public class WelcomeActivity extends BaseActivity {

    private FloatingActionButton floatingActionButton;
    private int page = 0;
    private int cardOriginalHeight = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        setupStatusBar();
        setupFab();
        setupCard();

        if (mConfigManager.needLocationPerms()) {
            checkLocationPermission();
        }
    }

    private void setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            setTranslucentStatusFlag(true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTranslucentStatusFlag(false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    public void onBackPressed() {
        if (page == 1) {
            hideHiddenPage();
            page--;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Stopping BeaconService due to lack of permissions...");
                    stopService(new Intent(this, BeaconService.class));
                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Starting BeaconService since permission granted...");
                    startService(new Intent(this, BeaconService.class));
                }
            }
        }
    }

    private void setupFab() {
        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (page == 0) {
                    showHiddenPage();
                    page++;
                } else {
                    EditText email = (EditText) findViewById(R.id.sign_in_card_email);
                    if (!email.getText().toString().trim().isEmpty()) {
                        UserStateStore.setUserEmail(WelcomeActivity.this, email.getText().toString().trim());
                    }

                    UserStateStore.setFirstLaunch(WelcomeActivity.this, false);
                    startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                }
            }
        });
    }

    private void setupCard() {
        final View card = findViewById(R.id.sign_in_card);
        final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
        final int bigMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, getResources().getDisplayMetrics());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) card.getLayoutParams();
            p.setMargins(margin, bigMargin, margin, margin);
            card.requestLayout();
        }

        final EditText emailField = (EditText) findViewById(R.id.sign_in_card_email);
        emailField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    Display display = getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);

                    if (cardOriginalHeight == -1) {
                        cardOriginalHeight = card.getLayoutParams().height;
                    }

                    ValueAnimator va = ValueAnimator.ofInt(cardOriginalHeight, size.y - 200);
                    va.setDuration(500);
                    va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        public void onAnimationUpdate(ValueAnimator animation) {
                            card.getLayoutParams().height = (Integer) animation.getAnimatedValue();
                            card.requestLayout();
                        }
                    });
                    va.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            View expandedContent = findViewById(R.id.sign_in_card_expanded_content);
                            expandedContent.animate().alpha(1.0f);
                            super.onAnimationEnd(animation);
                        }
                    });
                    va.start();
                }
            }
        });

        emailField.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(emailField.getWindowToken(), 0);
                    floatingActionButton.performClick();
                    return true;
                }
                return false;
            }
        });
    }

    private void checkLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!mConfigManager.grantedLocationPerms()) {
                int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

                if (result == PackageManager.PERMISSION_DENIED) {
                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(this, R.style.DialogStyle);
                    builder.setTitle(R.string.welcome_location_perm_dialog_title);
                    builder.setMessage(R.string.welcome_location_perm_dialog_text);
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(WelcomeActivity.this,
                                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                        }
                    });
                    builder.show();
                }
            }
        }
    }

    private void showHiddenPage() {
        floatingActionButton.setImageResource(R.drawable.ic_check);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View hiddenView = findViewById(R.id.sign_in_view);

            int centerX = (floatingActionButton.getLeft() + floatingActionButton.getRight()) / 2;
            int centerY = (floatingActionButton.getTop() + floatingActionButton.getBottom()) / 2;

            int startRadius = 0;
            int endRadius = Math.max(hiddenView.getWidth(), hiddenView.getHeight());

            Animator anim =
                    ViewAnimationUtils.createCircularReveal(hiddenView, centerX, centerY, startRadius, endRadius);
            hiddenView.setVisibility(View.VISIBLE);
            anim.start();
        } else {
            findViewById(R.id.sign_in_view).setVisibility(View.VISIBLE);
        }
    }

    public void hideHiddenPage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final View viewToHide = findViewById(R.id.sign_in_view);

            int centerX = (floatingActionButton.getLeft() + floatingActionButton.getRight()) / 2;
            int centerY = (floatingActionButton.getTop() + floatingActionButton.getBottom()) / 2;

            int startRadius = Math.max(viewToHide.getWidth(), viewToHide.getHeight());
            int endRadius = 0;

            Animator anim =
                    ViewAnimationUtils.createCircularReveal(viewToHide, centerX, centerY, startRadius, endRadius);
            anim.start();
            anim.addListener(new AnimatorListenerAdapter() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onAnimationEnd(Animator animation) {
                    viewToHide.setVisibility(View.INVISIBLE);
                    floatingActionButton.setImageResource(R.drawable.ic_arrow_right);
                    resetSignInCardSize();
                    super.onAnimationEnd(animation);
                }
            });
        } else {
            findViewById(R.id.sign_in_view).setVisibility(View.INVISIBLE);
            floatingActionButton.setImageResource(R.drawable.ic_arrow_right);
            resetSignInCardSize();
        }
    }

    private void setTranslucentStatusFlag(boolean on) {
        if (Build.VERSION.SDK_INT >= 19) {
            Window win = getWindow();
            WindowManager.LayoutParams winParams = win.getAttributes();
            final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            if (on) {
                winParams.flags |= bits;
            } else {
                winParams.flags &= ~bits;
            }
            win.setAttributes(winParams);
        }
    }

    private void resetSignInCardSize() {
        if (cardOriginalHeight != -1) {
            View card = findViewById(R.id.sign_in_card);
            card.getLayoutParams().height = cardOriginalHeight;
            card.requestLayout();

            View expandedContent = findViewById(R.id.sign_in_card_expanded_content);
            expandedContent.animate().alpha(0.0f);
        }
    }
}
