--[[
    mob lua migration batch 16b: replaces java Hedgehog
    (HallsLevel spawns it once at depth 23; each talk speeds it up,
    the 4th talk drops a Pasty, then it hurries away)
]]

local RPD = require "scripts/lib/commonClasses"

local mob = require "scripts/lib/mob"

return mob.init({
    interact = function(self, chr)
        local data   = mob.restoreData(self)
        local action = data.action or 0
        local speed  = data.speed

        if action <= 3 then
            self:say("Hedgehog_Info" .. (action + 1))

            if action == 3 then
                RPD.Dungeon.level:drop(RPD.item("Pasty"), self:getPos())
            end
        else
            self:say("Hedgehog_ImLate")
            action = 4
            speed  = 3
        end

        data.action = action + 1
        data.speed  = (speed or 0.5) + 0.5
        mob.storeData(self, data)

        return true
    end,

    speed = function(self, base)
        local data = mob.restoreData(self)
        return data.speed or base
    end
})
