package com.nyrds.market;

import android.app.Activity;
import android.content.Context;

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
import com.nyrds.util.UserKey;

public class MarketApp {
    static public void init(RemixedDungeonApp app) {
        Preferences.init(new PreferencesAndroid(app.getSharedPreferences("com.watabou.pixeldungeon.RemixedDungeon", Context.MODE_PRIVATE)));
        Crypter.init(new CrypterAndroid());
        UserKey.someValue();
        FileSystem.init(new FileSystemAndroid());
        FirebaseApp.initializeApp(app);
        EventCollector.init(new EventCollectorGooglePlay());


    }
}
