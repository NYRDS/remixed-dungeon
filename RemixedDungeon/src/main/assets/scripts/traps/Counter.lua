--
-- User: mike
-- Date: 12.11.2017
-- Time: 20:46
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"

local trap = require"scripts/lib/trap"

local storage = require "scripts/lib/storage"

return trap.init(
    function (cell, char, data)
        local counterId = "myLovelyTrapCounter"

        local counter = storage.get(counterId) or 1

        local wnd = RPD.new(RPD.Objects.Ui.WndMessage,"You stepped on me ".. tostring(counter).. " times already")

        counter = counter + 1
        storage.put(counterId, counter)

        RPD.GameScene:show(wnd)
    end
)

