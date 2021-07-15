package com.nyrds.pixeldungeon.support;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.SnapshotsClient;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.tasks.Task;
import com.nyrds.pixeldungeon.items.common.Library;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.app.RemixedDungeonApp;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.storage.FileSystem;
import com.nyrds.platform.storage.Preferences;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.ModError;
import com.nyrds.util.Unzip;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Rankings;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by mike on 09.05.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class PlayGames {
	private static final int RC_SIGN_IN          = 42353;
	private static final int RC_SHOW_BADGES      = 67584;
	private static final int RC_SHOW_LEADERBOARD = 96543;
	private static final int RC_VIDEO_OVERLAY    = 9011;


	private static final String PROGRESS = "Progress";

	private ArrayList<String> mSavedGamesNames;

	private GoogleSignInAccount signedInAccount;

	private final GoogleSignInOptions signInOptions;

	private boolean connecting = false;

	public PlayGames() {
		signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
						.requestScopes(Games.SCOPE_GAMES_SNAPSHOTS)
						.build();
	}


	public void connectExplicit() {
		if(isConnected() || connecting) {
			return;
		}

		Preferences.INSTANCE.put(Preferences.KEY_USE_PLAY_GAMES, true);

		connecting = true;

		Intent intent = GoogleSignIn.getClient(Game.instance(), signInOptions)
				.getSignInIntent();
		Game.instance().startActivityForResult(intent, RC_SIGN_IN);
	}

	public void connect() {

		if(isConnected() || connecting) {
			return;
		}

		connecting = true;

		GoogleSignInOptions signInOptions =
				new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
						.requestScopes(Games.SCOPE_GAMES_SNAPSHOTS)
						.build();

		GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(Game.instance());
		if (GoogleSignIn.hasPermissions(account, signInOptions.getScopeArray())) {
			signedInAccount = account;
			onConnected();
		} else {
			GoogleSignIn.getClient(Game.instance(), signInOptions)
					.silentSignIn()
					.addOnCompleteListener(
							Game.instance().serviceExecutor,
							task -> {
								if (task.isSuccessful()) {
									 signedInAccount = task.getResult();
									Preferences.INSTANCE.put(Preferences.KEY_PLAY_GAMES_CONNECT_FAILURES, 0);
									 onConnected();
								} else {
								    connecting = false;
									int failCount = Preferences.INSTANCE.getInt(Preferences.KEY_PLAY_GAMES_CONNECT_FAILURES, 0);
									failCount++;
									Preferences.INSTANCE.put(Preferences.KEY_PLAY_GAMES_CONNECT_FAILURES, failCount);
									if(failCount > 5) {
										Preferences.INSTANCE.put(Preferences.KEY_USE_PLAY_GAMES, false);
									}
                                }
							});
		}
	}

	public void disconnect() {
		Preferences.INSTANCE.put(Preferences.KEY_USE_PLAY_GAMES, false);

		GoogleSignInClient signInClient = GoogleSignIn.getClient(Game.instance(),
				GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
		signInClient.signOut().addOnCompleteListener(Game.instance().serviceExecutor,
				task -> {
					signedInAccount = null;
					// at this point, the user is signed out.
				});
	}

	public void unlockAchievement(String achievementCode) {
		if (isConnected()) {
			Games.getAchievementsClient(Game.instance(),signedInAccount).unlock(achievementCode);
		}
	}

	public static boolean usable() {
		return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1
				&& RemixedDungeonApp.checkOwnSignature();
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

	private Task<SnapshotMetadata> writeSnapshot(Snapshot snapshot,
												 byte[] data) {

		// Set the data payload for the snapshot
		snapshot.getSnapshotContents().writeBytes(data);

		// Create the change operation
		SnapshotMetadataChange metadataChange = new SnapshotMetadataChange.Builder()
				.build();

		SnapshotsClient snapshotsClient =
				Games.getSnapshotsClient(Game.instance(), signedInAccount);

		// Commit the operation
		return snapshotsClient.commitAndClose(snapshot, metadataChange);
	}


	private void writeToSnapshot(String snapshotId, byte[] content) {

		SnapshotsClient snapshotsClient =
				Games.getSnapshotsClient(Game.instance(), signedInAccount);

		snapshotsClient.open(snapshotId, true, SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED)
				.addOnFailureListener( e -> EventCollector.logException(e))
				.addOnSuccessListener(snapshotDataOrConflict -> {
					if(snapshotDataOrConflict.isConflict()) {
						EventCollector.logException(snapshotDataOrConflict.getConflict().getConflictId());
						//Just remove conflicting snapshot and try again
						snapshotsClient.delete(snapshotDataOrConflict.getConflict().getConflictingSnapshot().getMetadata())
								.addOnCompleteListener(
										task -> PlayGames.this.writeToSnapshot(snapshotId, content)
								);
						return;
					}
					writeSnapshot(snapshotDataOrConflict.getData(),content);
				});
	}

	public void unpackSnapshotTo(String snapshotId, File readTo, IResult result) {
		// Get the SnapshotsClient from the signed in account.
		SnapshotsClient snapshotsClient =
				Games.getSnapshotsClient(Game.instance(), signedInAccount);

		// Open the saved game using its name.
		snapshotsClient.open(snapshotId, true, SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED)
				.addOnFailureListener(EventCollector::logException)
				.continueWith(task -> {
					Snapshot snapshot = task.getResult().getData();

					// Opening the snapshot was a success and any conflicts have been resolved.
					try {
						// Extract the raw data from the snapshot.
						return snapshot.getSnapshotContents().readFully();
					} catch (IOException e) {
						EventCollector.logException(e);
					}
					result.status(false);
					return null;
				})
				.addOnCompleteListener(task -> {
					try {
						result.status(
								Unzip.unzipStream(
										new ByteArrayInputStream(
												task.getResult()),
										readTo.getAbsolutePath(),
										null));
					} catch (Exception e) {
						EventCollector.logException(e);
						result.status(false);
					}
				});
	}

	public boolean haveSnapshot(String snapshotId) {
		if (mSavedGamesNames == null) {
			return false;
		}

		return mSavedGamesNames.contains(snapshotId);
	}

	public boolean isConnected() {
		return signedInAccount != null && !signedInAccount.isExpired();
	}

	private void onConnected() {
		connecting = false;
		loadSnapshots(null);
	}

	public void loadSnapshots(@Nullable final Runnable doneCallback) {
		if (isConnected()) {
			Games.getSnapshotsClient(Game.instance(), signedInAccount)
					.load(false)
                    .addOnSuccessListener(
					snapshotMetadataBufferAnnotatedData -> {

						mSavedGamesNames = new ArrayList<>();
						for (SnapshotMetadata m : snapshotMetadataBufferAnnotatedData.get()) {
							mSavedGamesNames.add(m.getUniqueName());
						}
						if (doneCallback!=null) {
							doneCallback.run();
						}
					}
			)
			.addOnFailureListener(e -> {
                EventCollector.logException(e, "Play Games load");
            });
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

	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode != RC_SIGN_IN) {
			return false;
		}

		GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

		if (result.isSuccess()) {
			signedInAccount = result.getSignInAccount();
			onConnected();
		} else {

			Status status = result.getStatus();

			if(status.hasResolution()) {
				try {
					status.getResolution().send();
				} catch (PendingIntent.CanceledException e) {
					EventCollector.logException(e);
				}
				return true;
			}

			String message = status.getStatusMessage();
			if (message == null || message.isEmpty()) {
				message = status.toString();
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

		Game.instance().serviceExecutor.execute(() -> {
			try {
				boolean res = packFilesToSnapshot(PlayGames.PROGRESS, FileSystem.getInternalStorageFile(Utils.EMPTY_STRING), pathname -> {
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
});
					resultCallback.status(res);
				} catch (Exception e) {
					ModError.doReport("Error while uploading save to cloud: %s", e);
			}
		});
	}

	public void restoreProgress(final IResult resultCallback) {
		if (!isConnected()) {
			resultCallback.status(false);
			return;
		}

		Game.instance().serviceExecutor.execute(() -> {
			unpackSnapshotTo(PROGRESS, FileSystem.getInternalStorageFile(Utils.EMPTY_STRING),
					resultCallback);
		});
	}


	public void showBadges() {
		if (isConnected()) {
			Games.getAchievementsClient(Game.instance(), signedInAccount)
					.getAchievementsIntent()
					.addOnSuccessListener(
							intent -> Game.instance().startActivityForResult(intent, RC_SHOW_BADGES)
					);
		}
	}

	private static int[] boards = {R.string.leaderboard_easy_mode,
			R.string.leaderboard_normal_with_saves,
			R.string.leaderboard_normal,
			R.string.leaderboard_expert};

	public  void submitScores(int level, int scores) {
		if (isConnected()) {
            Games.getLeaderboardsClient(Game.instance(),signedInAccount).
					submitScore(StringsManager.getVar(boards[level]), scores);
		}
	}

	public void showLeaderboard() {
		if (isConnected()) {
			Games.getLeaderboardsClient(Game.instance(),signedInAccount)
					.getAllLeaderboardsIntent()
					.addOnSuccessListener(
							intent -> Game.instance().startActivityForResult(intent, RC_SHOW_LEADERBOARD)
					);
		}
	}

	public void showVideoOverlay() {
		if(isConnected()) {
			Games.getVideosClient(Game.instance(), signedInAccount)
					.getCaptureOverlayIntent()
					.addOnSuccessListener(intent -> Game.instance().startActivityForResult(intent, RC_VIDEO_OVERLAY));
		}
	}


	public interface IResult {
		void status(boolean status);
	}
}
