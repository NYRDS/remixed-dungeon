package com.watabou.noosa;

import com.nyrds.android.google.util.IabHelper;
import com.nyrds.android.google.util.IabResult;
import com.nyrds.android.google.util.Inventory;
import com.nyrds.android.google.util.Purchase;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;

abstract public class GameWithGoogleIap extends Game {

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
		String base64EncodedPublicKey = "put key here";
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
			setDonationLevel(0);
			
			Purchase check = inventory.getPurchase(SKU_LEVEL_1);
			if(check != null && verifyDeveloperPayload(check)){
				setDonationLevel(1);
			}
				
			check = inventory.getPurchase(SKU_LEVEL_2);
			if(check != null && verifyDeveloperPayload(check)){
				setDonationLevel(2);
			}
			
			check = inventory.getPurchase(SKU_LEVEL_3);
			if(check != null && verifyDeveloperPayload(check)){
				setDonationLevel(2);
			}
			
		}
	};

	public void doPurchase(String sku) {
		String payload = "";

		mHelper.launchPurchaseFlow(this, sku, RC_REQUEST,
				mPurchaseFinishedListener, payload);
	}

	/** Verifies the developer payload of a purchase. */
	boolean verifyDeveloperPayload(Purchase p) {
		String payload = p.getDeveloperPayload();
		Log.d("GAME", payload);

		return true;
	}

	public abstract void setDonationLevel(int level);

	static public GameWithGoogleIap instance() {
		return (GameWithGoogleIap) Game.instance();
	}

	public static void donate(int level) {
		switch (level) {
		case 1:
			instance().doPurchase(SKU_LEVEL_1);
			break;
		case 2:
			instance().doPurchase(SKU_LEVEL_2);
			break;
		case 3:
			instance().doPurchase(SKU_LEVEL_3);
			break;
		}
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

			Log.d("GAME", "Purchase successful!");

			if (purchase.getSku().equals(SKU_LEVEL_1)) {
				setDonationLevel(1);
			}

			if (purchase.getSku().equals(SKU_LEVEL_2)) {
				setDonationLevel(2);
			}

			if (purchase.getSku().equals(SKU_LEVEL_3)) {
				setDonationLevel(3);
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
