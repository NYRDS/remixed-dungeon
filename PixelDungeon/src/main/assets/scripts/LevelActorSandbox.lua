--
-- User: mike
-- Date: 06.11.2017
-- Time: 23:57
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/commonClasses"
print("Loading LevelActorSandbox")
return {
    act = function()
        print("act")
        local levelSize = RPD.Dungeon.level:getLength()
        RPD.GameScene:add( RPD.Blobs.Blob:seed(math.random(levelSize)-1,10, RPD.Blobs.Fire ) )
        return true
    end,
    actionTime = function()
        return 1
    end
}