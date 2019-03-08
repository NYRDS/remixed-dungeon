package com.nyrds.pixeldungeon.support.Google;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;

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
import com.nyrds.android.util.Unzip;
import com.nyrds.pixeldungeon.items.common.Library;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Preferences;
import com.watabou.pixeldungeon.Rankings;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static android.app.Activity.RESULT_OK;

/**
 * Created by mike on 09.05.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

@Deprecated
public class _PlayGames implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
	private static final int RC_SIGN_IN          = 42353;
	private static final int RC_SHOW_BADGES      = 67584;
	private static final int RC_SHOW_LEADERBOARD = 96543;

	private static final String PROGRESS = "Progress";

	private GoogleApiClient   googleApiClient;
	private Activity          activity;
	private ArrayList<String> mSavedGamesNames;

	public _PlayGames(Activity ctx) {
		activity = ctx;


		googleApiClient = new GoogleApiClient.Builder(activity)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(Games.API).addScope(Games.SCOPE_GAMES)
				.addApi(Drive.API).addScope(Drive.SCOPE_APPFOLDER)
				.build();
	}

	public void unlockAchievement(String achievementCode) {
		//TODO store it locally if not connected
		if (isConnected()) {
			Games.Achievements.unlock(googleApiClient, achievementCode);
		}
	}

	public static boolean usable() {
		return true;
	}

	public void connect() {

		if(usable()) {
			Preferences.INSTANCE.put(Preferences.KEY_USE_PLAY_GAMES, true);
			if (!isConnected()) {
				googleApiClient.connect();
			}
		} else {
			disconnect();
		}
	}

	public void disconnect() {
		Preferences.INSTANCE.put(Preferences.KEY_USE_PLAY_GAMES, false);
		if (isConnected()) {
			Games.signOut(googleApiClient);
			googleApiClient.disconnect();
		}
	}

	private OutputStream streamToSnapshot(final String snapshotId) {
		return new ByteArrayOutputStream() {
			@Override
			public void close() throws IOException {
				super.close();
				writeToSnapshot(snapshotId, toByteArray());
			}
		};
	}

	private void writeToSnapshot(String snapshotId, byte[] content) {
		PendingResult<Snapshots.OpenSnapshotResult> result = Games.Snapshots.open(googleApiClient, snapshotId, true, Snapshots.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED);

		Snapshots.OpenSnapshotResult openResult = result.await(5, TimeUnit.SECONDS);
		Snapshot snapshot = openResult.getSnapshot();

		if (openResult.getStatus().isSuccess() && snapshot != null) {
			SnapshotContents contents = snapshot.getSnapshotContents();
			contents.writeBytes(content);

			PendingResult<Snapshots.CommitSnapshotResult> pendingResult = Games.Snapshots.commitAndClose(googleApiClient, snapshot, SnapshotMetadataChange.EMPTY_CHANGE);
			pendingResult.setResultCallback(new ResultCallback<Snapshots.CommitSnapshotResult>() {
				@Override
				public void onResult(@NonNull Snapshots.CommitSnapshotResult commitSnapshotResult) {
					if (commitSnapshotResult.getStatus().isSuccess()) {
					} else {
						EventCollector.logEvent("Play Games", "commit" + commitSnapshotResult.getStatus().getStatusMessage());
					}
				}
			});
		}
	}

	private InputStream streamFromSnapshot(String snapshotId) throws IOException {
		return new ByteArrayInputStream(readFromSnapshot(snapshotId));
	}

	private byte[] readFromSnapshot(String snapshotId) throws IOException {
		PendingResult<Snapshots.OpenSnapshotResult> result = Games.Snapshots.open(googleApiClient, snapshotId, false);

		Snapshots.OpenSnapshotResult openResult = result.await(15, TimeUnit.SECONDS);
		Snapshot snapshot = openResult.getSnapshot();

		if (openResult.getStatus().isSuccess() && snapshot != null) {
			return snapshot.getSnapshotContents().readFully();
		} else {
			throw new IOException("snapshot timeout");
		}
	}

	public boolean haveSnapshot(String snapshotId) {
		if (mSavedGamesNames == null) {
			return false;
		}

		return mSavedGamesNames.contains(snapshotId);
	}

	public boolean isConnected() {
		return googleApiClient.isConnected();
	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {
		loadSnapshots(null);
	}

	public void loadSnapshots(@Nullable final Runnable doneCallback) {
		if (isConnected()) {
			Games.Snapshots.load(googleApiClient, false).setResultCallback(new ResultCallback<Snapshots.LoadSnapshotsResult>() {
				@Override
				public void onResult(@NonNull Snapshots.LoadSnapshotsResult result) {
					if (result.getStatus().isSuccess()) {

						mSavedGamesNames = new ArrayList<>();
						for (SnapshotMetadata m : result.getSnapshots()) {
							mSavedGamesNames.add(m.getUniqueName());
						}
					} else {
						EventCollector.logEvent("Play Games", "load " + result.getStatus().getStatusMessage());
					}
					if (doneCallback != null) {
						doneCallback.run();
					}
				}
			}, 3, TimeUnit.SECONDS);
		}
	}

	public boolean packFilesToSnapshot(String id, File dir, FileFilter filter) {
		OutputStream out = streamToSnapshot(id);
		try {
			FileSystem.zipFolderTo(out, dir, 0, filter);
		} catch (IOException e) {
			EventCollector.logException(e);
			return false;
		}
		return true;
	}

	public boolean unpackSnapshotTo(String id, File dir) {
		try {
			Unzip.unzip(streamFromSnapshot(id), dir.getAbsolutePath());
		} catch (IOException e) {
			EventCollector.logException(e);
			return false;
		}
		return true;
	}

	@Override
	public void onConnectionSuspended(int i) {
		googleApiClient.connect();
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult result) {
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

	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == RC_SIGN_IN) {
			if (resultCode == RESULT_OK) {
				googleApiClient.connect();
			} else {
				Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode,
						activity, requestCode);
				if (dialog != null) {
					dialog.show();
				}
			}
			return true;
		}
		return false;
	}

	public void backupProgress(final IResult resultCallback) {
		if (!isConnected()) {
			resultCallback.status(false);
			return;
		}

		Game.instance().executor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					boolean res = packFilesToSnapshot(_PlayGames.PROGRESS, FileSystem.getInternalStorageFile(""), new FileFilter() {
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
					resultCallback.status(res);
				}catch (Exception e) {
					EventCollector.logException(e);
					Game.toast("Error while uploading save to cloud: %s", e.getMessage());
				}
			}
		});
	}

	public void restoreProgress(final IResult resultCallback) {
		if (!isConnected()) {
			resultCallback.status(false);
			return;
		}

		Game.instance().executor.execute(new Runnable() {
			@Override
			public void run() {
				boolean res = unpackSnapshotTo(PROGRESS, FileSystem.getInternalStorageFile(""));
				resultCallback.status(res);
			}
		});
	}


	public void showBadges() {
		if (isConnected()) {
			activity.startActivityForResult(
					Games.Achievements.getAchievementsIntent(googleApiClient),
					RC_SHOW_BADGES
			);
		}
	}

	private static int[] boards = {R.string.leaderboard_easy_mode,
			R.string.leaderboard_normal_with_saves,
			R.string.leaderboard_normal,
			R.string.leaderboard_expert};

	public  void submitScores(int level, int scores) {
		if (isConnected()) {
			Games.Leaderboards.submitScore(googleApiClient, Game.getVar(boards[level]),
					scores);
		}
	}

	public void showLeaderboard() {
		if (isConnected()) {
			activity.startActivityForResult(
					Games.Leaderboards.getAllLeaderboardsIntent(googleApiClient),
					RC_SHOW_LEADERBOARD
			);
		}
	}

	public interface IResult {
		void status(boolean status);
	}
}
