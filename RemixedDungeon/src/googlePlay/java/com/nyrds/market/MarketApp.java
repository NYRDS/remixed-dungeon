package com.nyrds.market;

import com.google.firebase.FirebaseApp;
import com.nyrds.platform.EventCollectorGooglePlay;
import com.nyrds.platform.FileSystem;
import com.nyrds.platform.storage.FileSystemAndroid;
import com.nyrds.util.events.EventCollector;
import com.nyrds.platform.app.RemixedDungeonApp;

public class MarketApp {
    static public void init(RemixedDungeonApp app) {
        FileSystem.init(new FileSystemAndroid());
        FirebaseApp.initializeApp(app);
        EventCollector.init(new EventCollectorGooglePlay());
    }
}
