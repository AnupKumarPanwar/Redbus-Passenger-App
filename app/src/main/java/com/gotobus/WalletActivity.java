package com.gotobus;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.anupkumarpanwar.scratchview.ScratchView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class WalletActivity extends AppCompatActivity {

    String baseUrl;

    SharedPreferences sharedPreferences;
    String PREFS_NAME = "MyApp_Settings";
    String accessToken;

    ArrayList<Cashback> cashbacks;
    Cashback selectedCashback;
    CashbacksAdapter cashbacksAdapter;
    GridView gridView;
    Animator currentAnimator;
    int shortAnimationDuration = 200;
    float startScaleFinal;
    ScratchView scratchView;
    boolean expanded = false;

    TextView amount, credits;

    // Load the high-resolution "zoomed-in" image.
    RelativeLayout expandedImageView;
    //        expandedImageView.setImageResource(imageResId);
    ImageView imageView;

    // Calculate the starting and ending bounds for the zoomed-in image.
    // This step involves lots of math. Yay, math.
    Rect startBounds;
    Rect finalBounds;
    Point globalOffset;
    float startScale;
    View thumbView;

    int totalCredits = 0;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        Window window = this.getWindow();

// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

// finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.wallet_background));

        baseUrl = getResources().getString(R.string.base_url);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        accessToken = sharedPreferences.getString("access_token", null);

        cashbacks = new ArrayList<>();
        cashbacksAdapter = new CashbacksAdapter(getApplicationContext(), cashbacks);
        gridView = findViewById(R.id.grid_view);
        gridView.setAdapter(cashbacksAdapter);
        amount = findViewById(R.id.amount);
        credits = findViewById(R.id.credits);

        expandedImageView = findViewById(
                R.id.large_card);
        imageView = findViewById(R.id.bg_image);

        startBounds = new Rect();
        finalBounds = new Rect();
        globalOffset = new Point();

        final float startScaleFinal = startScale;

        scratchView = findViewById(R.id.scratch_view);
        scratchView.setRevealListener(new ScratchView.IRevealListener() {
            @Override
            public void onRevealed(ScratchView scratchView) {
                for (Cashback cashback : cashbacks) {
                    if (cashback.id.equals(selectedCashback.id)) {
                        if (cashback.status.equals("CREATED")) {
                            cashback.status = "SCRATCHED";
                            cashbacksAdapter.notifyDataSetChanged();
                            totalCredits += Integer.parseInt(cashback.amount);
                            credits.setText(String.valueOf(totalCredits));
                            redeemCard(cashback.id);
                        }
                        break;
                    }
                }
            }

            @Override
            public void onRevealPercentChangedListener(ScratchView scratchView, float percent) {

            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(getApplicationContext(), "Done", Toast.LENGTH_LONG).show();
                selectedCashback = cashbacks.get(position);
                thumbView = view;
                amount.setText("You've won\n₹" + selectedCashback.amount);
                zoomImageFromThumb();
            }
        });

//        GridLayoutManager glm = new GridLayoutManager(getApplicationContext(), 2);
//        recyclerView.setLayoutManager(glm);

//        cashbacks.add(new Cashback("1", "100", "CREATED"));
//        cashbacks.add(new Cashback("2", "100", "CREATED"));
//        cashbacks.add(new Cashback("3", "100", "CREATED"));
//        cashbacks.add(new Cashback("4", "100", "CREATED"));
//        cashbacks.add(new Cashback("5", "100", "CREATED"));

        cashbacksAdapter.notifyDataSetChanged();

        AndroidNetworking.post(baseUrl + "/getCashbacks.php")
                .setOkHttpClient(NetworkCookies.okHttpClient)
                .addHeaders("Authorization", accessToken)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject result = response.getJSONObject("result");
                            boolean success = Boolean.parseBoolean(result.get("success").toString());
                            if (success) {
                                JSONObject data = result.getJSONObject("data");
                                totalCredits = Integer.parseInt(data.get("credits").toString());
                                credits.setText(String.valueOf(totalCredits));

                                JSONArray cashbacksArray = data.getJSONArray("cashbacks");
                                for (int i = 0; i < cashbacksArray.length(); i++) {
                                    JSONObject cashback = cashbacksArray.getJSONObject(i);
                                    String id = cashback.get("id").toString();
                                    String amount = cashback.get("amount").toString();
                                    String status = cashback.get("status").toString();
                                    cashbacks.add(new Cashback(id, amount, status));
                                }
                                cashbacksAdapter.notifyDataSetChanged();

                            } else {
                                String message = result.get("message").toString();
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Log.e("Exception", e.getMessage());
                        }
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });
    }

    private void redeemCard(String id) {
        AndroidNetworking.post(baseUrl + "/redeemScratchCard.php")
                .setOkHttpClient(NetworkCookies.okHttpClient)
                .addHeaders("Authorization", accessToken)
                .addBodyParameter("scratchCardId", id)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject result = response.getJSONObject("result");
                            boolean success = Boolean.parseBoolean(result.get("success").toString());
                            if (success) {

                            } else {
                                String message = result.get("message").toString();
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Log.e("Exception", e.getMessage());
                        }
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void zoomImageFromThumb() {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }

        if (selectedCashback.status.equals("CREATED")) {
            scratchView.mask();
        } else {
            scratchView.reveal();
        }


        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.container)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).

        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f))
                .with(ObjectAnimator.ofFloat(expandedImageView,
                        View.SCALE_Y, startScale, 1f));
        set.setDuration(shortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                currentAnimator = null;
                imageView.setAlpha(0.95f);
                expanded = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                currentAnimator = null;
                imageView.setAlpha(0.95f);
                expanded = true;
            }
        });
        set.start();
        currentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                shrinkBack();
                if (currentAnimator != null) {
                    currentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.Y, startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(shortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        currentAnimator = null;
                        expanded = false;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        currentAnimator = null;
                        expanded = false;
                    }
                });
                imageView.setAlpha(0.0f);
                set.start();
                currentAnimator = set;
            }
        });
    }

    private void shrinkBack() {

        if (currentAnimator != null) {
            currentAnimator.cancel();
        }

        // Animate the four positioning/sizing properties in parallel,
        // back to their original values.
        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator
                .ofFloat(expandedImageView, View.X, startBounds.left))
                .with(ObjectAnimator
                        .ofFloat(expandedImageView,
                                View.Y, startBounds.top))
                .with(ObjectAnimator
                        .ofFloat(expandedImageView,
                                View.SCALE_X, startScaleFinal))
                .with(ObjectAnimator
                        .ofFloat(expandedImageView,
                                View.SCALE_Y, startScaleFinal));
        set.setDuration(shortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                thumbView.setAlpha(1f);
                expandedImageView.setVisibility(View.GONE);
                currentAnimator = null;
                expanded = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                thumbView.setAlpha(1f);
                expandedImageView.setVisibility(View.GONE);
                currentAnimator = null;
                expanded = false;
            }
        });
        imageView.setAlpha(0.0f);
        set.start();
        currentAnimator = set;
    }

    @Override
    public void onBackPressed() {
        if (expanded) {
            shrinkBack();
        } else {
            super.onBackPressed();
        }
    }
}
