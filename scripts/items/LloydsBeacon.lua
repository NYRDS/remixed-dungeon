---
--- LloydsBeacon: marks a spot, returns to it (was items/LloydsBeacon.java).
--- The return position lives in the per-instance lua state; old java saves
--- are read back by loadLegacy (the @Packable returnTo Position field).
---
local RPD     = require "scripts/lib/commonClasses"
local itemLib = require "scripts/lib/item"

local AC_SET    = "LloidsBeacon_ACSet"
local AC_RETURN = "LloidsBeacon_ACReturn"

return itemLib.init{
    desc = function()
        return {
            image     = 85, -- ItemSpriteSheet.BEACON
            imageFile = "items.png",
            name      = "LloidsBeacon_Name",
            info      = "LloidsBeacon_Info",
            price     = 0,
            data      = { levelId = nil, cellId = -1 },
        }
    end,

    actions = function(self, item)
        local ret = { AC_SET }
        if self.data.cellId ~= -1 then
            ret[2] = AC_RETURN
        end
        return ret
    end,

    execute = function(self, item, chr, action)
        if action == AC_SET or action == AC_RETURN then
            if RPD.Dungeon:bossLevel() then
                chr:spend(1) -- TIME_TO_USE
                RPD.glogw(RPD.textById("LloidsBeacon_Preventing"))
                return
            end

            -- was Level.NEIGHBOURS8 sweep: a char in any adjacent cell blocks
            local level = chr:level()
            local w = level:getWidth()
            local offs = { 1, -1, w, -w, 1 + w, 1 - w, -1 + w, -1 - w }
            for i = 1, 8 do
                if RPD.Actor:findChar(chr:getPos() + offs[i]) then
                    RPD.glogw(RPD.textById("LloidsBeacon_Creatures"))
                    return
                end
            end
        end

        if action == AC_SET then
            local cur = RPD.Dungeon:currentPosition()
            self.data.levelId = cur.levelId
            self.data.cellId  = cur.cellId

            chr:doOperate(1) -- TIME_TO_USE
            RPD.playSound("snd_beacon")

            RPD.glog(RPD.textById("LloidsBeacon_Return"))
        elseif action == AC_RETURN then
            local target = RPD.newPosition(self.data.levelId, self.data.cellId)
            self.data.cellId = -1
            self.data.levelId = nil
            chr:teleportTo(target)
        end
    end,

    glowing = function(self, item)
        if self.data.cellId ~= -1 then
            return itemLib.makeGlowing(0xFFFFFF, 1) -- was Glowing(0xFFFFFF) = period 1
        end
        return nil
    end,

    -- the set-beacon suffix on the info text
    info = function(self, item)
        local info = RPD.textById("LloidsBeacon_Info")
        if self.data.cellId ~= -1 then
            info = info .. RPD.format("LloidsBeacon_Set",
                RPD.DungeonGenerator:getLevelDepth(self.data.levelId))
        end
        return info
    end,

    -- restore from a pre-migration java save (the @Packable returnTo field)
    loadLegacy = function(self, item, bundle)
        local ret = bundle:getBundle("returnTo")
        if ret then
            self.data.levelId = ret:optString("levelId", nil)
            self.data.cellId  = ret:optInt("cellId", -1)
        end
    end,
}
