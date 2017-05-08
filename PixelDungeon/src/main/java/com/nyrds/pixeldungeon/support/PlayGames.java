package com.nyrds.pixeldungeon.support;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.snapshot.Snapshots;

/**
 * Created by mike on 09.05.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class PlayGames implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
	private GoogleApiClient googleApiClient;

	private static PlayGames playGames;

	private PlayGames(Context ctx) {
		googleApiClient = new GoogleApiClient.Builder(ctx)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(Games.API).addScope(Games.SCOPE_GAMES)
				.addApi(Drive.API).addScope(Drive.SCOPE_APPFOLDER)
				.build();

	}


	public static void init(Context context) {
		playGames =new PlayGames(context);
	}

	public static void saveGame(String slotId) {
		PendingResult<Snapshots.OpenSnapshotResult> result = Games.Snapshots.open(playGames.googleApiClient,slotId,true);
	}

	public static void loadGame(String slotId) {
		PendingResult<Snapshots.LoadSnapshotsResult> result = Games.Snapshots.load(playGames.googleApiClient,false);
	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {

	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

	}
}
