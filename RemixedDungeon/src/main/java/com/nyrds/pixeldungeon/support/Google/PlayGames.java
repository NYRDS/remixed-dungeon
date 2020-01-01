package com.nyrds.pixeldungeon.support.Google;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.PendingResult;
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
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by mike on 09.05.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class PlayGames {
	private static final int RC_SIGN_IN          = 42353;
	private static final int RC_SHOW_BADGES      = 67584;
	private static final int RC_SHOW_LEADERBOARD = 96543;

	private static final String PROGRESS = "Progress";

	private ArrayList<String> mSavedGamesNames;

	GoogleSignInAccount signedInAccount;

	GoogleSignInOptions signInOptions;

	public PlayGames(Activity ctx) {

		signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
						.requestScopes(Drive.SCOPE_APPFOLDER)
						.build();

		connect();
	}


	private void startSignInIntent(Activity ctx) {
		GoogleSignInClient signInClient = GoogleSignIn.getClient(ctx,
				signInOptions;
		Intent intent = signInClient.getSignInIntent();
		Game.instance().startActivityForResult(intent, RC_SIGN_IN);
	}

	public void connect() {
		Preferences.INSTANCE.put(Preferences.KEY_USE_PLAY_GAMES, true);

		GoogleSignInOptions signInOptions =
				new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
						.requestScopes(Drive.SCOPE_APPFOLDER)
						.build();

		GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(Game.instance());
		if (GoogleSignIn.hasPermissions(account, signInOptions.getScopeArray())) {
			signedInAccount = account;
			onConnected();
		} else {
			GoogleSignInClient signInClient = GoogleSignIn.getClient(Game.instance(), signInOptions);
			signInClient
					.silentSignIn()
					.addOnCompleteListener(
							Game.instance().executor,
							task -> {
								if (task.isSuccessful()) {
									 signedInAccount = task.getResult();
									 onConnected();
								} else {
									startSignInIntent(Game.instance());
								}
							});
		}
	}

	public void disconnect() {
		Preferences.INSTANCE.put(Preferences.KEY_USE_PLAY_GAMES, false);

		GoogleSignInClient signInClient = GoogleSignIn.getClient(Game.instance(),
				GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
		signInClient.signOut().addOnCompleteListener(Game.instance().executor,
				task -> {
					// at this point, the user is signed out.
				});
	}



	public void unlockAchievement(String achievementCode) {
		//TODO store it locally if not connected
		if (isConnected()) {
			Games.getAchievementsClient(Game.instance(),signedInAccount).unlock(achievementCode);
		}
	}

	public static boolean usable() {
		return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1;
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
			pendingResult.setResultCallback(commitSnapshotResult -> {
				if (commitSnapshotResult.getStatus().isSuccess()) {
				} else {
					EventCollector.logEvent("Play Games", "commit" + commitSnapshotResult.getStatus().getStatusMessage());
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
		if(signedInAccount != null && !signedInAccount.isExpired()) {
			return true;
		}
		return false;
	}

	private void onConnected() {
		loadSnapshots(null);
	}

	public void loadSnapshots(@Nullable final Runnable doneCallback) {
		if (isConnected()) {
			Games.Snapshots.load(googleApiClient, false).setResultCallback(result -> {
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
			}, 3, TimeUnit.SECONDS);
		}
	}

	public boolean packFilesToSnapshot(String id, File dir, FileFilter filter) {
		OutputStream out = streamToSnapshot(id);
		try {
			FileSystem.zipFolderTo(out, dir, 0, filter);
		} catch (Exception e) {
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

	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode != RC_SIGN_IN) {
			return false;
		}

		GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
		if (result.isSuccess()) {
			signedInAccount = result.getSignInAccount();
			onConnected();
		} else {
			String message = result.getStatus().getStatusMessage();
			if (message == null || message.isEmpty()) {
				message = "Something gonna wrong with google play games";
			}
			new AlertDialog.Builder(Game.instance()).setMessage(message)
					.setNeutralButton(android.R.string.ok, null).show();
		}
		return true;

	}

	public void backupProgress(final IResult resultCallback) {
		if (!isConnected()) {
			resultCallback.status(false);
			return;
		}

		Game.instance().executor.execute(() -> {
			try {
				boolean res = packFilesToSnapshot(PlayGames.PROGRESS, FileSystem.getInternalStorageFile(Utils.EMPTY_STRING), new FileFilter() {
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

                            return filename.startsWith("game_") && filename.endsWith(".dat");
                        }
					});
					resultCallback.status(res);
				} catch (Exception e) {
					EventCollector.logException(e);
					Game.toast("Error while uploading save to cloud: %s", e.getMessage());

			}
		});
	}

	public void restoreProgress(final IResult resultCallback) {
		if (!isConnected()) {
			resultCallback.status(false);
			return;
		}

		Game.instance().executor.execute(() -> {
			boolean res = unpackSnapshotTo(PROGRESS, FileSystem.getInternalStorageFile(Utils.EMPTY_STRING));
			resultCallback.status(res);
		});
	}


	public void showBadges() {
		if (isConnected()) {
			Games.getAchievementsClient(Game.instance(),signedInAccount)
					.getAchievementsIntent()
					.addOnSuccessListener(
							intent -> Game.instance().startActivityForResult(intent,RC_SHOW_BADGES)
					);
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
