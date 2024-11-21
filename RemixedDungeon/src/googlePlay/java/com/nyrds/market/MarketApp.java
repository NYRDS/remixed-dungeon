package com.nyrds.market;

import com.google.firebase.FirebaseApp;
import com.nyrds.platform.EventCollectorGooglePlay;
import com.nyrds.platform.storage.FileSystem;
import com.nyrds.platform.storage.FileSystemAndroid;
import com.nyrds.platform.events.EventCollector;
import com.nyrds.platform.app.RemixedDungeonApp;
import com.nyrds.platform.util.CrypterAndroid;
import com.nyrds.util.Crypter;

public class MarketApp {
    static public void init(RemixedDungeonApp app) {
        FileSystem.init(new FileSystemAndroid());
        Crypter.init(new CrypterAndroid());
        FirebaseApp.initializeApp(app);
        EventCollector.init(new EventCollectorGooglePlay());
    }
}
