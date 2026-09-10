// Pure stub. The real client (by Sergey Kiselev, 2020, MIT) was removed:
// its only consumer was the abandoned epic-dungeon multiplayer experiment,
// which is gone from the mod repos as of 2026-09-10.
//
// REMOVAL NOTE (2026-09-10): keep this shell only so luajava.bindClass from
// mods distributed before that date does not crash. Safe to delete entirely
// after a few months — target: end of 2026. Do not extend or rewire it.
//
// REMOVAL NOTE (RU): заглушка для совместимости; можно полностью удалить
// после конца 2026 года, когда старые копии модов перестанут использоваться.

package com.nyrds.pixeldungeon.networking;

import com.nyrds.LuaInterface;

@LuaInterface
public class SClientLua {

    public SClientLua(String f_ip, int f_port) {
    }

    @LuaInterface
    public static SClientLua createNew(String ip, int port) {
        return new SClientLua(ip, port);
    }

    public SClientLua connect() {
        return this;
    }

    public void stop() {
    }

    public void sendMessage(String message) {
    }

    public String receiveMessage() {
        return null;
    }

    public boolean canReceive() {
        return false;
    }
}
