package com.nyrds.pixeldungeon.ai;

public interface AiState {
    boolean act(boolean enemyInFOV, boolean justAlerted);

    String status();
}
