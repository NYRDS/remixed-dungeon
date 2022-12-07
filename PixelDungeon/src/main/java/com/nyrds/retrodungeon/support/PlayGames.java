package com.nyrds.retrodungeon.support;

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
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotContents;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.games.snapshot.Snapshots;
import com.nyrds.android.util.FileSystem;
import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.android.util.Unzip;
import com.nyrds.retrodungeon.items.common.Library;
import com.nyrds.retrodungeon.ml.EventCollector;
import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Preferences;
import com.watabou.pixeldungeon.Rankings;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static android.app.Activity.RESULT_OK;

/**
 * Created by mike on 09.05.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class PlayGames implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
	private static final int RC_SIGN_IN          = 42353;
	private static final int RC_SHOW_BADGES      = 67584;
	private static final int RC_SHOW_LEADERBOARD = 96543;

	public static final String PROGRESS = "Progress";

	private GoogleApiClient   googleApiClient;
	private Activity          activity;
	private ArrayList<String> mSavedGamesNames;

	private static PlayGames playGames;


	private PlayGames(Activity ctx) {
		activity = ctx;

		googleApiClient = new GoogleApiClient.Builder(activity)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(Games.API).addScope(Games.SCOPE_GAMES)
				.addApi(Drive.API).addScope(Drive.SCOPE_APPFOLDER)
				.build();
	}

	public static void init(Activity context) {
		playGames = new PlayGames(context);
		Log.i("Play Games", "init");
	}

	public static void unlockAchievement(String achievementCode) {
		//TODO store it locally if not connected
		if (isConnected()) {
			Games.Achievements.unlock(playGames.googleApiClient, achievementCode);
		}
	}

	public static void connect() {
		Preferences.INSTANCE.put(Preferences.KEY_USE_PLAY_GAMES, true);
		if (!isConnected()) {
			Log.i("Play Games", "connect");
			playGames.googleApiClient.connect();
		}
	}

	public static void disconnect() {
		Preferences.INSTANCE.put(Preferences.KEY_USE_PLAY_GAMES, false);
		if (isConnected()) {
			Log.i("Play Games", "disconnect");
			Games.signOut(playGames.googleApiClient);
			playGames.googleApiClient.disconnect();
		}
	}

	public static OutputStream streamToSnapshot(final String snapshotId) {
		return new ByteArrayOutputStream() {
			@Override
			public void close() throws IOException {
				super.close();
				writeToSnapshot(snapshotId, toByteArray());
			}
		};
	}

	public static void writeToSnapshot(String snapshotId, byte[] content) {
		Log.i("Play Games", "Streaming to " + snapshotId);
		PendingResult<Snapshots.OpenSnapshotResult> result = Games.Snapshots.open(playGames.googleApiClient, snapshotId, true, Snapshots.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED);

		Snapshots.OpenSnapshotResult openResult = result.await(5, TimeUnit.SECONDS);
		Snapshot snapshot = openResult.getSnapshot();

		if (openResult.getStatus().isSuccess() && snapshot != null) {
			SnapshotContents contents = snapshot.getSnapshotContents();
			contents.writeBytes(content);

			PendingResult<Snapshots.CommitSnapshotResult> pendingResult = Games.Snapshots.commitAndClose(playGames.googleApiClient, snapshot, SnapshotMetadataChange.EMPTY_CHANGE);
			pendingResult.setResultCallback(new ResultCallback<Snapshots.CommitSnapshotResult>() {
				@Override
				public void onResult(@NonNull Snapshots.CommitSnapshotResult commitSnapshotResult) {
					if (commitSnapshotResult.getStatus().isSuccess()) {
						Log.i("Play Games", "commit ok");
					} else {
						Log.e("Play Games", "commit" + commitSnapshotResult.getStatus().getStatusMessage());
					}
				}
			});
		}
	}

	public static InputStream streamFromSnapshot(String snapshotId) throws IOException {
		return new ByteArrayInputStream(readFromSnapshot(snapshotId));
	}

	public static byte[] readFromSnapshot(String snapshotId) throws IOException {
		PendingResult<Snapshots.OpenSnapshotResult> result = Games.Snapshots.open(playGames.googleApiClient, snapshotId, false);

		Snapshots.OpenSnapshotResult openResult = result.await(5, TimeUnit.SECONDS);
		Snapshot snapshot = openResult.getSnapshot();

		if (openResult.getStatus().isSuccess() && snapshot != null) {
			return snapshot.getSnapshotContents().readFully();
		} else {
			throw new IOException("snapshot timeout");
		}
	}

	public static boolean haveSnapshot(String snapshotId) {
		if (playGames.mSavedGamesNames == null) {
			return false;
		}

		return playGames.mSavedGamesNames.contains(snapshotId);
	}

	public static boolean copyFileToCloud(String id) {
		if (!isConnected()) {
			return false;
		}

		try {
			Game.showWindow(id);

			FileSystem.copyStream(FileSystem.getInputStream(id),
					PlayGames.streamToSnapshot(id));
		} catch (FileNotFoundException e) {
			// no file yet
			return false;
		} finally {
			Game.hideWindow();
		}
		return true;
	}

	public static boolean copyFileFromCloud(String id) {
		if (!isConnected()) {
			return false;
		}

		if (!haveSnapshot(id)) {
			return false;
		}

		try {
			Game.showWindow(id);
			FileSystem.copyStream(PlayGames.streamFromSnapshot(id),
					FileSystem.getOutputStream(id));
			return true;
		} catch (Exception e) { // should not happen
			throw new TrackedRuntimeException(e);
		} finally {
			Game.hideWindow();
		}
	}

	public static boolean isConnected() {
		return playGames != null && playGames.googleApiClient.isConnected();
	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {
		Log.i("Play Games", "onConnected");
		loadSnapshots(null);
	}

	public static void loadSnapshots(@Nullable final Runnable doneCallback) {
		if (isConnected()) {
			Games.Snapshots.load(playGames.googleApiClient, false).setResultCallback(new ResultCallback<Snapshots.LoadSnapshotsResult>() {
				@Override
				public void onResult(@NonNull Snapshots.LoadSnapshotsResult result) {
					if (result.getStatus().isSuccess()) {
						Log.i("Play Games", "load ok!");

						playGames.mSavedGamesNames = new ArrayList<>();
						for (SnapshotMetadata m : result.getSnapshots()) {
							playGames.mSavedGamesNames.add(m.getUniqueName());
						}

						restoreProgress();
					} else {
						Log.e("Play Games", "load " + result.getStatus().getStatusMessage());
					}
					if (doneCallback != null) {
						doneCallback.run();
					}
				}
			}, 3, TimeUnit.SECONDS);
		}
	}

	public static boolean packFilesToSnapshot(String id, File dir, FileFilter filter) {
		OutputStream out = PlayGames.streamToSnapshot(id);
		try {
			FileSystem.zipFolderTo(out, dir, 0, filter);
		} catch (IOException e) {
			EventCollector.logException(e);
			return false;
		}
		return true;
	}

	public static boolean unpackSnapshotTo(String id, File dir) {
		try {
			Unzip.unzip(PlayGames.streamFromSnapshot(id), dir.getAbsolutePath());
		} catch (IOException e) {
			EventCollector.logException(e);
			return false;
		}
		return true;
	}

	@Override
	public void onConnectionSuspended(int i) {
		Log.i("Play Games", "onConnectionSuspended");
		googleApiClient.connect();
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult result) {
		Log.i("Play Games", "onConnectionFailed: " + result.getErrorMessage());

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
			if (resultCode == RESULT_OK) {
				playGames.googleApiClient.connect();
			} else {
				Log.e("Play Games", String.format("%d", resultCode));
				Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode,
						playGames.activity, requestCode);
				if (dialog != null) {
					dialog.show();
				}
			}
			return true;
		}
		return false;
	}

	public static void backupProgress() {
		if (!isConnected()) {
			return;
		}

		Game.instance().executor.execute(new Runnable() {
			@Override
			public void run() {
				PlayGames.packFilesToSnapshot(PlayGames.PROGRESS, FileSystem.getInternalStorageFile(""), new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						String filename = pathname.getName();
						if (filename.equals(Badges.BADGES_FILE)) {
							return true;
						}

						if (filename.equals(Library.getLibraryFile())) {
							return true;
						}

						if (filename.equals(Rankings.RANKINGS_FILE)) {
							return true;
						}

						if (filename.startsWith("game_") && filename.endsWith(".dat")) {
							return true;
						}
						return false;
					}
				});

			}
		});
	}

	public static void restoreProgress() {
		if (!isConnected()) {
			return;
		}

		Game.instance().executor.execute(new Runnable() {
			@Override
			public void run() {
				PlayGames.unpackSnapshotTo(PROGRESS, FileSystem.getInternalStorageFile(""));
			}
		});
	}


	public static void showBadges() {
		if (isConnected()) {
			playGames.activity.startActivityForResult(
					Games.Achievements.getAchievementsIntent(playGames.googleApiClient),
					RC_SHOW_BADGES
			);
		}
	}

	static int[] boards = {R.string.leaderboard_easy_mode,
			R.string.leaderboard_normal_with_saves,
			R.string.leaderboard_normal,
			R.string.leaderboard_expert};

	public static void submitScores(int level, int scores) {
		if (isConnected()) {
			Games.Leaderboards.submitScore(playGames.googleApiClient, Game.getVar(boards[level]),
					scores);
		}
	}

	public static void showLeaderboard() {
		if (isConnected()) {
			playGames.activity.startActivityForResult(
					Games.Leaderboards.getAllLeaderboardsIntent(playGames.googleApiClient),
					RC_SHOW_LEADERBOARD
			);
		}
	}
}
