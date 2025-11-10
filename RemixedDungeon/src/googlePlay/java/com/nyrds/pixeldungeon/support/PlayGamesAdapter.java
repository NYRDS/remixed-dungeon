package com.nyrds.pixeldungeon.support;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.games.GamesSignInClient;
import com.google.android.gms.games.PlayGames;
import com.google.android.gms.games.SnapshotsClient;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.nyrds.pixeldungeon.items.common.Library;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.app.RemixedDungeonApp;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.storage.FileSystem;
import com.nyrds.platform.storage.Preferences;
import com.nyrds.platform.util.StringsManager;
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
 * Updated to Play Games Services v2 (2025)
 * Created by mike on 09.05.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class PlayGamesAdapter {
	private static final int RC_SIGN_IN          = 42353;
	private static final int RC_SHOW_BADGES      = 67584;
	private static final int RC_SHOW_LEADERBOARD = 96543;
	private static final int RC_VIDEO_OVERLAY    = 9011;

	private static final String PROGRESS = "Progress";

	private ArrayList<String> mSavedGamesNames;

	private boolean isAuthenticated = false;

	private boolean connecting = false;

	private GamesSignInClient signInClient;

	public PlayGamesAdapter() {
		// Initialize PlayGamesSdk if not already done elsewhere
		com.google.android.gms.games.PlayGamesSdk.initialize(Game.instance());

		signInClient = PlayGames.getGamesSignInClient(Game.instance());
	}

	public void connectExplicit() {
		if (isAuthenticated || connecting) {
			return;
		}

		Preferences.INSTANCE.put(Preferences.KEY_USE_PLAY_GAMES, true);

		connecting = true;

		signInClient.signIn().addOnCompleteListener(task -> {
			connecting = false;
			if (task.isSuccessful() && task.getResult().isAuthenticated()) {
				isAuthenticated = true;
				onConnected();
			} else {
				connecting = false;

				handleSignInFailure(task.getException());
				int failCount = Preferences.INSTANCE.getInt(Preferences.KEY_PLAY_GAMES_CONNECT_FAILURES, 0);
				failCount++;
				Preferences.INSTANCE.put(Preferences.KEY_PLAY_GAMES_CONNECT_FAILURES, failCount);
				if (failCount > 5) {
					Preferences.INSTANCE.put(Preferences.KEY_USE_PLAY_GAMES, false);
				}
			}
		});
	}

	public void connect() {
		if (isAuthenticated || connecting) {
			return;
		}

		connecting = true;

		signInClient.isAuthenticated().addOnCompleteListener(task -> {
			connecting = false;
			if (task.isSuccessful() && task.getResult().isAuthenticated()) {
				isAuthenticated = true;
				Preferences.INSTANCE.put(Preferences.KEY_PLAY_GAMES_CONNECT_FAILURES, 0);
				onConnected();
			} else {
				int failCount = Preferences.INSTANCE.getInt(Preferences.KEY_PLAY_GAMES_CONNECT_FAILURES, 0);
				failCount++;
				Preferences.INSTANCE.put(Preferences.KEY_PLAY_GAMES_CONNECT_FAILURES, failCount);
				if (failCount > 5) {
					Preferences.INSTANCE.put(Preferences.KEY_USE_PLAY_GAMES, false);
				}
			}
		});
	}

	public void disconnect() {
		Preferences.INSTANCE.put(Preferences.KEY_USE_PLAY_GAMES, false);
		isAuthenticated = false;
		// No signOut in v2
	}

	public void unlockAchievement(String achievementCode) {
		if (isConnected()) {
			PlayGames.getAchievementsClient(Game.instance()).unlock(achievementCode);
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

		SnapshotsClient snapshotsClient = PlayGames.getSnapshotsClient(Game.instance());

		// Commit the operation
		return snapshotsClient.commitAndClose(snapshot, metadataChange);
	}

	private void writeToSnapshot(String snapshotId, byte[] content) {

		SnapshotsClient snapshotsClient = PlayGames.getSnapshotsClient(Game.instance());

		Task<SnapshotsClient.DataOrConflict<Snapshot>> openTask = snapshotsClient.open(snapshotId, true, SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED);

		openTask.continueWithTask(task -> {
			if (!task.isSuccessful()) {
				return Tasks.forException(task.getException());
			}
			SnapshotsClient.DataOrConflict<Snapshot> dataOrConflict = task.getResult();
			if (dataOrConflict.isConflict()) {
				return processSnapshotOpenResult(dataOrConflict, 0);
			} else {
				return Tasks.forResult(dataOrConflict.getData());
			}
		}).addOnSuccessListener(snapshot -> {
			writeSnapshot(snapshot, content);
		}).addOnFailureListener(EventCollector::logException);
	}

	private Task<Snapshot> processSnapshotOpenResult(SnapshotsClient.DataOrConflict<Snapshot> result, int retryCount) {
		if (!result.isConflict()) {
			TaskCompletionSource<Snapshot> source = new TaskCompletionSource<>();
			source.setResult(result.getData());
			return source.getTask();
		}

		SnapshotsClient.SnapshotConflict conflict = result.getConflict();
		Snapshot snapshot = conflict.getSnapshot();
		Snapshot conflictSnapshot = conflict.getConflictingSnapshot();

		Snapshot resolvedSnapshot = snapshot;
		if (snapshot.getMetadata().getLastModifiedTimestamp() < conflictSnapshot.getMetadata().getLastModifiedTimestamp()) {
			resolvedSnapshot = conflictSnapshot;
		}

		SnapshotsClient snapshotsClient = PlayGames.getSnapshotsClient(Game.instance());

		return snapshotsClient.resolveConflict(conflict.getConflictId(), resolvedSnapshot)
				.continueWithTask(task -> {
					if (!task.isSuccessful()) {
						return Tasks.forException(task.getException());
					}
					SnapshotsClient.DataOrConflict<Snapshot> newResult = task.getResult();
					if (retryCount < 10) {
						return processSnapshotOpenResult(newResult, retryCount + 1);
					} else {
						TaskCompletionSource<Snapshot> source = new TaskCompletionSource<>();
						source.setException(new Exception("Could not resolve snapshot conflicts"));
						return source.getTask();
					}
				});
	}

	public void unpackSnapshotTo(String snapshotId, File readTo, IResult result) {
		SnapshotsClient snapshotsClient = PlayGames.getSnapshotsClient(Game.instance());

		Task<SnapshotsClient.DataOrConflict<Snapshot>> openTask = snapshotsClient.open(snapshotId, false, SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED);

		openTask.continueWithTask(task -> {
			if (!task.isSuccessful()) {
				result.status(false);
				return Tasks.forException(task.getException());
			}
			SnapshotsClient.DataOrConflict<Snapshot> dataOrConflict = task.getResult();
			if (dataOrConflict.isConflict()) {
				return processSnapshotOpenResult(dataOrConflict, 0);
			} else {
				return Tasks.forResult(dataOrConflict.getData());
			}
		}).continueWith(task -> {
			if (!task.isSuccessful()) {
				result.status(false);
				return null;
			}
			Snapshot snapshot = task.getResult();

			try {
				byte[] data = snapshot.getSnapshotContents().readFully();
				result.status(Unzip.unzipStream(new ByteArrayInputStream(data), readTo.getAbsolutePath(), null));
			} catch (IOException e) {
				EventCollector.logException(e);
				result.status(false);
			}
			return null;
		}).addOnFailureListener(e -> {
			EventCollector.logException(e);
			result.status(false);
		});
	}

	public boolean haveSnapshot(String snapshotId) {
		if (mSavedGamesNames == null) {
			return false;
		}

		return mSavedGamesNames.contains(snapshotId);
	}

	public boolean isConnected() {
		return isAuthenticated;
	}

	private void onConnected() {
		loadSnapshots(null);
	}

	public void loadSnapshots(@Nullable final Runnable doneCallback) {
		if (isConnected()) {
			SnapshotsClient snapshotsClient = PlayGames.getSnapshotsClient(Game.instance());
			snapshotsClient.load(false)
					.addOnSuccessListener(snapshotMetadataBufferAnnotatedData -> {
						mSavedGamesNames = new ArrayList<>();
						for (SnapshotMetadata m : snapshotMetadataBufferAnnotatedData.get()) {
							mSavedGamesNames.add(m.getUniqueName());
						}
						if (doneCallback != null) {
							doneCallback.run();
						}
					})
					.addOnFailureListener(e -> EventCollector.logException(e, "Play Games load"));
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
		if (requestCode == RC_SIGN_IN) {
			if (resultCode == Activity.RESULT_OK) {
				connect();
				return true;
			} else {
				// Sign-in failed after resolution
				new AlertDialog.Builder(Game.instance())
						.setMessage("Sign-in resolution failed.")
						.setNeutralButton(android.R.string.ok, null)
						.show();
				return true;
			}
		}
		return false;
	}

	public void backupProgress(final IResult resultCallback) {
		if (!isConnected()) {
			resultCallback.status(false);
			return;
		}

		Game.instance().serviceExecutor.execute(() -> {
			boolean res = packFilesToSnapshot(PlayGamesAdapter.PROGRESS, FileSystem.getInternalStorageFile(Utils.EMPTY_STRING), pathname -> {
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
			PlayGames.getAchievementsClient(Game.instance())
					.getAchievementsIntent()
					.addOnSuccessListener(intent -> Game.instance().startActivityForResult(intent, RC_SHOW_BADGES));
		}
	}

	private static int[] boards = {R.string.leaderboard_easy_mode,
			R.string.leaderboard_normal_with_saves,
			R.string.leaderboard_normal,
			R.string.leaderboard_expert};

	public void submitScores(int level, int scores) {
		if (isConnected()) {
			PlayGames.getLeaderboardsClient(Game.instance())
					.submitScore(StringsManager.getVar(boards[level]), scores);
		}
	}

	public void showLeaderboard() {
		if (isConnected()) {
			PlayGames.getLeaderboardsClient(Game.instance())
					.getAllLeaderboardsIntent()
					.addOnSuccessListener(intent -> Game.instance().startActivityForResult(intent, RC_SHOW_LEADERBOARD));
		}
	}

	public void showVideoOverlay() {
		/*
		if (isConnected()) {
			PlayGames.getVideosClient(Game.instance())
					.getCaptureOverlayIntent()
					.addOnSuccessListener(intent -> Game.instance().startActivityForResult(intent, RC_VIDEO_OVERLAY));
		}
		*/
	}

	private void handleSignInFailure(@Nullable Exception e) {
		if(e == null) {
			EventCollector.logException(new Exception("Something gone wrong while signing in"));
			return;
		}

		if (e instanceof ResolvableApiException) {
			try {
				((ResolvableApiException) e).startResolutionForResult(Game.instance(), RC_SIGN_IN);
			} catch (Exception ex) {
				EventCollector.logException(ex);
			}
		} else if (e instanceof ApiException) {
			String message = CommonStatusCodes.getStatusCodeString(((ApiException) e).getStatusCode());
			new AlertDialog.Builder(Game.instance()).setMessage(message)
					.setNeutralButton(android.R.string.ok, null).show();
		} else {
			EventCollector.logException(e);
		}
	}

	public interface IResult {
		void status(boolean status);
	}
}