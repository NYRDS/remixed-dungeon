local RPD = require "scripts/lib/commonClasses"
local trap = require "scripts/lib/trap"

-- java AlarmTrap: every mob on the level beckons to the trap cell.
return trap.init(
    function (cell, char, data)
        local mobs = RPD.Dungeon.level:getMobs()
        for i = 1, #mobs do
            if char == nil or mobs[i]:getId() ~= char:getId() then
                mobs[i]:beckon(cell)
            end
        end

        if RPD.Dungeon:isCellVisible(cell) then
            RPD.GLog:w(RPD.textById("AlarmTrap_Desc"), {})
            RPD.Sfx.CellEmitter:center(cell):start(RPD.Sfx.Speck:factory(RPD.Sfx.Speck.SCREAM), 0.3, 3)
        end

        RPD.playSound("snd_alert")
    end
)
