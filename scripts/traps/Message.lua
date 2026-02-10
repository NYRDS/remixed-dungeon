--
-- User: mike
-- Date: 04.11.2017
-- Time: 22:26
-- This file is part of Remixed Pixel Dungeon.
--
local RPD = require "scripts/lib/commonClasses"

local trap = require"scripts/lib/trap"

return trap.init(
    function (cell, char, data)
        local wnd = RPD.new(RPD.Objects.Ui.WndMessage,data)
        RPD.GameScene:show(wnd)
    end
)
