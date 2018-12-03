package com.nyrds.pixeldungeon.support;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.nyrds.pixeldungeon.ml.EventCollector;
import com.watabou.noosa.Game;
import com.watabou.noosa.InterstitialPoint;

class AAdsComboProvider implements AdsUtilsCommon.IBannerProvider, AdsUtilsCommon.IInterstitialProvider {

    @Override
    public void displayBanner() {

        final WebView adView = new WebView(Game.instance());

        LinearLayout layout = Game.instance().getLayout();
        int adViewHeight = Math.max(50, layout.getHeight() / 10);

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, adViewHeight);
        adView.setLayoutParams(params);

        adView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {
                AdsUtils.updateBanner(adView);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                AdsUtilsCommon.bannerFailed(AAdsComboProvider.this);
                EventCollector.logException(description);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                Game.instance().startActivity(browserIntent);
                return true;
            }
        });

        adView.loadUrl("https://acceptable.a-ads.com/995145");
    }

    @Override
    public void showInterstitial(final InterstitialPoint ret) {

        final AlertDialog.Builder alert = new AlertDialog.Builder(Game.instance());

        WebView adView = new WebView(Game.instance());

        LinearLayout layout = Game.instance().getLayout();
        int adViewHeight = Math.max(250, layout.getHeight() / 2);

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, adViewHeight);
        adView.setLayoutParams(params);

        adView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {
                final AlertDialog dialog = alert.create();

                dialog.setCanceledOnTouchOutside(true);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.dismiss();
                        ret.returnToWork(true);
                    }
                });
                dialog.show();
            }

            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                AdsUtilsCommon.bannerFailed(AAdsComboProvider.this);
                EventCollector.logException("aads_no_interstitial" + description);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                Game.instance().startActivity(browserIntent);
                return true;
            }

        });

        alert.setView(adView);
        adView.loadUrl("https://acceptable.a-ads.com/995145");
    }
}
