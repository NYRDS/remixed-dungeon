--
-- User: mike
-- Date: 05.11.2017
-- Time: 14:46
-- This file is part of Remixed Pixel Dungeon.
--
GameScene = luajava.bindClass("com.watabou.pixeldungeon.scenes.GameScene")

Blob = luajava.bindClass("com.watabou.pixeldungeon.actors.blobs.Blob")
Fire = luajava.bindClass("com.watabou.pixeldungeon.actors.blobs.Fire")

Dungeon = luajava.bindClass("com.watabou.pixeldungeon.Dungeon")

Actor = luajava.bindClass("com.watabou.pixeldungeon.actors.Actor")

local data
function setData(_data)
    data = _data
end

function trap(cell, char)

    local x = Dungeon.level:cellX(cell)
    local y = Dungeon.level:cellY(cell)

    local levelTwistActorProxy =  luajava.createProxy("com.nyrds.pixeldungeon.mechanics.actors.IScriptedActor", {
        act = function()
            GameScene:add( Blob:seed( Dungeon.level:cell(x,y),1, Fire ) );
            return true
        end,
        actionTime = function()
            return 1
        end
    })

    local levelTwistActor = luajava.newInstance("com.nyrds.pixeldungeon.mechanics.actors.ScriptedActor",levelTwistActorProxy)

    Actor:add(levelTwistActor)
end
