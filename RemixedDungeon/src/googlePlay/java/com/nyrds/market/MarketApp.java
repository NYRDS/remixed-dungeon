package com.nyrds.market;

import com.google.firebase.FirebaseApp;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.app.RemixedDungeonApp;

public class MarketApp {
    static public void init(RemixedDungeonApp app) {
        FirebaseApp.initializeApp(app);
        EventCollector.init();
    }
}
