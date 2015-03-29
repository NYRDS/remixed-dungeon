package com.watabou.noosa;

import com.nyrds.android.google.util.IabHelper;
import com.nyrds.android.google.util.IabResult;
import com.nyrds.android.google.util.Inventory;
import com.nyrds.android.google.util.Purchase;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;

public class GameWithGoogleIap extends Game {

	protected static final String SKU_LEVEL_1 = "supporter_level_1";
	protected static final String SKU_LEVEL_2 = "supporter_level_2";
	protected static final String SKU_LEVEL_3 = "supporter_level_3";

	// The helper object
	IabHelper mHelper;

	// (arbitrary) request code for the purchase flow
	static final int RC_REQUEST = 10001;

	public GameWithGoogleIap(Class<? extends Scene> c) {
		super(c);
		instance(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*
		 * base64EncodedPublicKey should be YOUR APPLICATION'S PUBLIC KEY (that
		 * you got from the Google Play developer console). This is not your
		 * developer public key, it's the *app-specific* public key.
		 * 
		 * Instead of just storing the entire literal string here embedded in
		 * the program, construct the key at runtime from pieces or use bit
		 * manipulation (for example, XOR with some other string) to hide the
		 * actual key. The key itself is not secret information, but we don't
		 * want to make it easy for an attacker to replace the public key with
		 * one of their own and then fake messages from the server.
		 */
		String base64EncodedPublicKey = "putkey here but do not commit it";
		// Create the helper, passing it our context and the public key to
		// verify signatures with
		Log.d("GAME", "Creating IAB helper.");
		mHelper = new IabHelper(this, base64EncodedPublicKey);

		// enable debug logging (for a production application, you should set
		// this to false).
		mHelper.enableDebugLogging(true);

		// Start setup. This is asynchronous and the specified listener
		// will be called once setup completes.
		Log.d("GAME", "Starting setup.");

		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			@Override
			public void onIabSetupFinished(IabResult result) {
				Log.d("GAME", "Setup finished.");

				if (!result.isSuccess()) {
					// Oh noes, there was a problem.
					complain("Problem setting up in-app billing: " + result);
					return;
				}

				// Have we been disposed of in the meantime? If so, quit.
				if (mHelper == null)
					return;

				// IAB is fully set up. Now, let's get an inventory of stuff we
				// own.
				Log.d("GAME", "Setup successful. Querying inventory.");
				mHelper.queryInventoryAsync(mGotInventoryListener);
			}
		});
	}

	// Listener that's called when we finish querying the items and
	// subscriptions we own
	IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
		@Override
		public void onQueryInventoryFinished(IabResult result,
				Inventory inventory) {
			Log.d("GAME", "Query inventory finished.");

			// Have we been disposed of in the meantime? If so, quit.
			if (mHelper == null)
				return;

			// Is it a failure?
			if (result.isFailure()) {
				complain("Failed to query inventory: " + result);
				return;
			}

			Log.d("GAME", "Query inventory was successful.");

			/*
			 * Check for items we own. Notice that for each purchase, we check
			 * the developer payload to see if it's correct! See
			 * verifyDeveloperPayload().
			 */

			// Do we have the premium upgrade?
			Purchase premiumPurchase = inventory.getPurchase(SKU_LEVEL_1);
			boolean mIsPremium = (premiumPurchase != null && verifyDeveloperPayload(premiumPurchase));
			Log.d("GAME", "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));

			Log.d("GAME", "Initial inventory query finished; enabling main UI.");
		}
	};
	protected boolean mIsPremium;

	// User clicked the "Upgrade to Premium" button.
	public void doPurchase() {
		Log.d("GAME",
				"Upgrade button clicked; launching purchase flow for upgrade.");

		/*
		 * TODO: for security, generate your payload here for verification. See
		 * the comments on verifyDeveloperPayload() for more info. Since this is
		 * a SAMPLE, we just use an empty string, but on a production app you
		 * should carefully generate this.
		 */
		String payload = "";

		// mHelper.launchPurchaseFlow(this, SKU_LEVEL_1,
		// RC_REQUEST,mPurchaseFinishedListener, payload);
		mHelper.launchPurchaseFlow(this, "android.test.purchased", RC_REQUEST,
				mPurchaseFinishedListener, payload);
	}

	/** Verifies the developer payload of a purchase. */
	boolean verifyDeveloperPayload(Purchase p) {
		String payload = p.getDeveloperPayload();
		Log.d("GAME", payload);

		return true;
	}

	// Callback for when a purchase is finished
	IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
		public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
			Log.d("GAME", "Purchase finished: " + result + ", purchase: "
					+ purchase);

			// if we were disposed of in the meantime, quit.
			if (mHelper == null)
				return;

			if (result.isFailure()) {
				complain("Error purchasing: " + result);
				return;
			}

			if (!verifyDeveloperPayload(purchase)) {
				complain("Error purchasing. Authenticity verification failed.");
				return;
			}

			Log.d("GAME", "Purchase successful.");

			if (purchase.getSku().equals(SKU_LEVEL_1)) {
				// bought the premium upgrade!
				Log.d("GAME",
						"Purchase is premium upgrade. Congratulating user.");
				alert("Thank you for upgrading to premium!");
				mIsPremium = true;

			}
		}
	};

	void complain(String message) {
		Log.e("GAME", "**** IAP Error: " + message);
		alert("Error: " + message);
	}

	void alert(final String message) {
		instance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				AlertDialog.Builder bld = new AlertDialog.Builder(instance());
				bld.setMessage(message);
				bld.setNeutralButton("OK", null);
				Log.d("GAME", "Showing alert dialog: " + message);
				bld.create().show();
			}
		});
	}
}
