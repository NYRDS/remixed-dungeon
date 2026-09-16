--[[
  BurningFist - Yog's burning fist (batch 17d-2, was mobs/guts/BurningFist.java).
  Ranged striker: any enemy on a clean ballistica ray is attackable; the hit
  lands through the base zap flow and the victim sprite flashes (java gated
  the flash in attack(), which only ever runs adjacent - the zapProc hook
  fires exactly on ranged hits, so the flash finally works). Trails fire in
  all 9 cells around itself every turn.
]]
local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

return mob.init{
    canAttack = function(self, enemy)
        return RPD.Ballistica:cast(self:getPos(), enemy:getPos(), false, true)
                == enemy:getPos()
    end,

    zapProc = function(self, enemy, damage)
        if enemy ~= nil then
            enemy:getSprite():flash()
        end
        return damage
    end,

    act = function(self)
        local level = RPD.Dungeon.level
        local w = level:getWidth()
        local dirs = { 0, -1, 1, -w, w, -w - 1, -w + 1, w - 1, w + 1 }
        for i = 1, 9 do
            RPD.placeBlob(RPD.Blobs.Fire, self:getPos() + dirs[i], 2)
        end
    end,

    damage = function(self, dmg, src)
        -- being hurt draws every mob in the halls to the fist
        local mobs = RPD.Dungeon.level:getMobs()
        for i = 1, #mobs do
            mobs[i]:beckon(self:getPos())
        end
    end,
}
