package com.nyrds.pixeldungeon.support;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.Snapshots;

import static android.app.Activity.RESULT_OK;

/**
 * Created by mike on 09.05.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class PlayGames implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
	private static final int RC_SIGN_IN = 123;

	private GoogleApiClient googleApiClient;
	private Activity        activity;

	private static PlayGames playGames;

	private PlayGames(Activity ctx) {
		activity = ctx;
		googleApiClient = new GoogleApiClient.Builder(activity)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(Games.API).addScope(Games.SCOPE_GAMES)
				.addApi(Drive.API).addScope(Drive.SCOPE_APPFOLDER)
				.build();

		//ConnectionResult connectionResult = googleApiClient.blockingConnect();
		//Log.i("Play Games", connectionResult.toString());

	}


	public static void init(Activity context) {
		playGames = new PlayGames(context);
	}

	public static void onStart() {
		playGames.googleApiClient.connect();
	}

	public static void saveGame(String slotId) {
		PendingResult<Snapshots.OpenSnapshotResult> result = Games.Snapshots.open(playGames.googleApiClient,slotId,true);
		Snapshot snapshot = result.await().getSnapshot();
	}

	public static void loadGame(String slotId) {
		PendingResult<Snapshots.LoadSnapshotsResult> result = Games.Snapshots.load(playGames.googleApiClient,false);
	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {
		Log.i("Play Games", "onConnected");
	}

	@Override
	public void onConnectionSuspended(int i) {
		Log.i("Play Games", "onConnectionSuspended");

	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult result) {
		Log.i("Play Games", "onConnectionFailed: "+ result.getErrorMessage());

		int requestCode = RC_SIGN_IN;

		if (result.hasResolution()) {
			try {
				result.startResolutionForResult(activity, requestCode);

			} catch (IntentSender.SendIntentException e) {
				googleApiClient.connect();
			}
		} else {
			// not resolvable... so show an error message
			int errorCode = result.getErrorCode();
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(errorCode,
					activity, requestCode);
			if (dialog != null) {
				dialog.show();
			}
		}
	}

	public static boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == RC_SIGN_IN) {
			//mSignInClicked = false;
			//mResolvingConnectionFailure = false;
			if (resultCode == RESULT_OK) {
				playGames.googleApiClient.connect();
			} else {
				Log.e("Play Games",String.format("%d",requestCode));
			}
			return true;
		}
		return false;
	}
}
