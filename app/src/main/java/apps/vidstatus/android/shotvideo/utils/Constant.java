package apps.vidstatus.android.shotvideo.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;

import apps.vidstatus.android.shotvideo.BuildConfig;
import apps.vidstatus.android.shotvideo.R;
import es.dmoral.toasty.Toasty;

public class Constant {

    public static String FOLDERPATH = Environment.getExternalStorageDirectory() + "/VideoStatus";
    public static String BASEURL = "https://www.videostatusmarket.com/designgallery/xstatusnew/";
    public static String SOCIALCATBASEURL = "https://www.videostatusmarket.com/iPhone_app/fullscreenvideostatus/";
    public static String NAV_SELECTED_ITEM = "Story Status";
    public static String BOTTOM_SELECTED_ITEM = "Latest";
    public static int pagerPosition = 0;
    public static boolean isCategory = false;

    public static String PlayURL = "";

    public static void expand(final View v) {
        int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) v.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        v.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
        final int targetHeight = v.getMeasuredHeight();

        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? LinearLayout.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration((int) (targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration((int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void whatsappShareVideo(Activity activity, String url, File file) {
        if (!file.exists()) {
            Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
            whatsappIntent.setType("text/plain");
            whatsappIntent.setPackage("com.whatsapp");
            whatsappIntent.putExtra(Intent.EXTRA_SUBJECT, "Hey this is the video from " + activity.getResources().getString(R.string.app_name) + " app ");
            whatsappIntent.putExtra(Intent.EXTRA_TEXT, url);
            try {
                activity.startActivity(whatsappIntent);
            } catch (android.content.ActivityNotFoundException ex) {
                Toasty.error(activity, "WhatsApp have not been installed.", Toast.LENGTH_SHORT, true).show();
            }
        } else {
            Uri uri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".provider", file);
            Intent videoshare = new Intent(Intent.ACTION_SEND);
            videoshare.setType("*/*");
            videoshare.setPackage("com.whatsapp");
            videoshare.putExtra(Intent.EXTRA_TEXT, "Hey this is the video from " + activity.getResources().getString(R.string.app_name) + " app ");
            videoshare.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            videoshare.putExtra(Intent.EXTRA_STREAM, uri);
            activity.startActivity(videoshare);
        }
    }

    public static void shareVideo(Activity activity, String url, File file) {
        if (!file.exists()) {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Hey this is the video from " + activity.getResources().getString(R.string.app_name) + " app ");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, url);
            activity.startActivity(Intent.createChooser(sharingIntent, "Share Video"));
        } else {
            Uri uri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".provider", file);
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("*/*");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, "Hey this is the video from " + activity.getResources().getString(R.string.app_name) + " app ");
            sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
            activity.startActivity(Intent.createChooser(sharingIntent, "Share Video"));
        }
    }

}
