package com.nyrds.pixeldungeon.support;

/**
 * HTML version of PlayGames
 */
public class PlayGames {
    public interface IResult {
        void onResult(boolean success);
    }
    
    public static void unlockAchievement(String achievementId) {
        // In HTML version, Play Games services are not supported
        System.out.println("Play Games achievement unlock not supported in HTML version");
    }
    
    public static void submitScore(String leaderboardId, long score) {
        // In HTML version, Play Games services are not supported
        System.out.println("Play Games score submission not supported in HTML version");
    }
    
    public static void showAchievements() {
        // In HTML version, Play Games services are not supported
        System.out.println("Play Games achievements not supported in HTML version");
    }
    
    public static void showLeaderboards() {
        // In HTML version, Play Games services are not supported
        System.out.println("Play Games leaderboards not supported in HTML version");
    }
}