--
-- User: mike
-- Date: 06.11.2017
-- Time: 23:57
-- This file is part of Remixed Pixel Dungeon.
--

require "scripts/commonClasses"

return luajava.createProxy("com.nyrds.pixeldungeon.mechanics.actors.IScriptedActor", {
    act = function()
        local levelSize = RPD.Dungeon.level:getLength()
        RPD.GameScene:add( RPD.Blobs.Blob:seed(math.random(levelSize)-1,10, RPD.Blobs.Fire ) );
        return true
    end,
    actionTime = function()
        return 1
    end
})
