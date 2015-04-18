package com.watabou.noosa;

import java.net.InetAddress;
import java.util.ArrayList;

import com.nyrds.android.google.util.IabHelper;
import com.nyrds.android.google.util.IabResult;
import com.nyrds.android.google.util.Inventory;
import com.nyrds.android.google.util.Purchase;

import android.app.AlertDialog;
import android.util.Log;

public abstract class GameWithGoogleIap extends Game {

	protected static final String SKU_LEVEL_1 = "supporter_level_1";
	protected static final String SKU_LEVEL_2 = "supporter_level_2";
	protected static final String SKU_LEVEL_3 = "supporter_level_3";

	// The helper object
	IabHelper mHelper = null;
	Inventory mInventory = null;

	private volatile boolean m_iapReady = false;

	public boolean iapReady() {
		return m_iapReady;
	}

	// (arbitrary) request code for the purchase flow
	static final int RC_REQUEST = (int) (Math.random() * 0xffff);

	public GameWithGoogleIap(Class<? extends Scene> c) {
		super(c);
		instance(this);
	}

	public void initIapPhase2() {
		if (mHelper != null) {
			return;
		}

		String base64EncodedPublicKey = "put your key here";
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

				ArrayList<String> skuList = new ArrayList<String>();

				skuList.add(SKU_LEVEL_1);
				skuList.add(SKU_LEVEL_2);
				skuList.add(SKU_LEVEL_3);

				mHelper.queryInventoryAsync(true, skuList,
						mGotInventoryListener);
			}
		});
	}
	
	public void initIap() {
		new Thread() {
			@Override
			public void run() {
				try {
					InetAddress ipAddr = InetAddress.getByName("google.com");
					if (ipAddr.equals("")) {
						return;
					}
					initIapPhase2();
				} catch (Exception e) {
					return;
				}
			}
		}.start();
		
	}

	private void checkPurchases() {
		setDonationLevel(0);

		Purchase check = mInventory.getPurchase(SKU_LEVEL_1);
		if (check != null && verifyDeveloperPayload(check)) {
			setDonationLevel(1);
		}

		check = mInventory.getPurchase(SKU_LEVEL_2);
		if (check != null && verifyDeveloperPayload(check)) {
			setDonationLevel(2);
		}

		check = mInventory.getPurchase(SKU_LEVEL_3);
		if (check != null && verifyDeveloperPayload(check)) {
			setDonationLevel(3);
		}
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

			mInventory = inventory;
			checkPurchases();
			m_iapReady = true;
		}
	};

	public String getPriceString(int level) {
		if (mInventory == null) {
			return null;
		}

		switch (level) {
		case 1:
			return mInventory.getSkuDetails(SKU_LEVEL_1).getPrice();
		case 2:
			return mInventory.getSkuDetails(SKU_LEVEL_2).getPrice();
		case 3:
			return mInventory.getSkuDetails(SKU_LEVEL_3).getPrice();
		}
		return null;
	}

	public void doPurchase(String sku) {
		if (!m_iapReady) {
			alert("Sorry, we not ready yet");
			return;
		}

		String payload = "";

		m_iapReady = false;
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

	public static GameWithGoogleIap instance() {
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
				m_iapReady = true;
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

			m_iapReady = true;
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
