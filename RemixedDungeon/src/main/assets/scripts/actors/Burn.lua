--
-- User: mike
-- Date: 06.11.2017
-- Time: 23:57
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"
local actor = require "scripts/lib/actor"

return actor.init({
    act = function()
        local levelSize = RPD.Dungeon.level:getLength()
        local cell = math.random(levelSize)-1
        if not RPD.Dungeon.level.solid[cell] then
            RPD.placeBlob( RPD.Blobs.Fire, cell, 10)
        end
        return true
    end,
    actionTime = function()
        return 1
    end,
    activate = function()
       local wnd = RPD.new(RPD.Objects.Ui.WndStory,"It gonna be hot here...")
       RPD.GameScene:show(wnd)
    end
})