package com.nyrds.market;

import android.app.Activity;

import com.google.firebase.FirebaseApp;
import com.nyrds.platform.EventCollectorGooglePlay;
import com.nyrds.platform.storage.FileSystem;
import com.nyrds.platform.storage.FileSystemAndroid;
import com.nyrds.platform.events.EventCollector;
import com.nyrds.platform.app.RemixedDungeonApp;
import com.nyrds.platform.storage.Preferences;
import com.nyrds.platform.storage.PreferencesAndroid;
import com.nyrds.platform.util.CrypterAndroid;
import com.nyrds.util.Crypter;

public class MarketApp {
    static public void init(RemixedDungeonApp app) {
        Preferences.init(new PreferencesAndroid(RemixedDungeonApp.getContext().getSharedPreferences("com.watabou.pixeldungeon.RemixedDungeon", Activity.MODE_PRIVATE)));
        FileSystem.init(new FileSystemAndroid());
        Crypter.init(new CrypterAndroid());
        FirebaseApp.initializeApp(app);
        EventCollector.init(new EventCollectorGooglePlay());
    }
}
