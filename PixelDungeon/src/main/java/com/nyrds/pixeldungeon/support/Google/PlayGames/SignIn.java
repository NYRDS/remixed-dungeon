package com.nyrds.pixeldungeon.support.Google.PlayGames;

import android.content.Intent;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.watabou.noosa.Game;

import java.util.concurrent.Executor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static android.app.Activity.RESULT_OK;

public class SignIn implements OnCompleteListener, Executor {

    private static final int RC_SIGN_IN = 0x1234;

    @Nullable
    private GoogleSignInAccount signedInAccount;

    private void signInSilently() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(Game.instance(),
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);

        signInClient.silentSignIn().addOnCompleteListener(this,
                task -> {
                    if (task.isSuccessful()) {
                        // The signed in account is stored in the task's result.
                        signedInAccount = task.getResult();

                    } else {
                        startSignInIntent();
                    }
                });
    }

    private void startSignInIntent() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(Game.instance(),
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        Intent intent = signInClient.getSignInIntent();
        Game.instance().startActivityForResult(intent, RC_SIGN_IN);
    }


    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {

            }
            return true;
        }
        return false;
    }

    @Override
    public void onComplete(@NonNull Task task) {

    }

    @Override
    public void execute(Runnable command) {
        command.run();
    }
}
