--
-- User: mike
-- Date: 06.11.2017
-- Time: 23:57
-- This file is part of Remixed Pixel Dungeon.
--

require "scripts/commonClasses"

return luajava.createProxy("com.nyrds.pixeldungeon.mechanics.actors.IScriptedActor", {
    act = function()
        local levelSize = Dungeon.level:getLength()
        GameScene:add( Blob:seed(math.random(levelSize)-1,10, Fire ) );
        return true
    end,
    actionTime = function()
        return 1
    end
})
