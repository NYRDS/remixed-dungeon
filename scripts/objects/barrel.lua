---
--- Barrel, data-served (batch 21). Burns once into a LiquidFlame seed; the
--- burn-down animation comes from the def ("burn"), removal rides its
--- completion hook.
---

local RPD = require "scripts/lib/commonClasses"

local object = require "scripts/lib/object"


return object.init{

    stepOn = function(self, object, hero)
        return true
    end,

    pushable = function(self, object, hero)
        return true
    end,

    nonPassable = function(self, object, ch)
        return ch:valid()
    end,

    affectItems = function(self, object)
        return true
    end,

    affectLevelObjects = function(self, object)
        return true
    end,

    flammable = function(self, object)
        return true
    end,

    burn = function(self, object)
        local st = self:restoreData()
        if st.burned then
            return
        end
        st.burned = true
        self:storeData(st)

        object:playObjectAnim("burn", "onBurned")
        RPD.playSound("snd_explosion")
        RPD.placeBlob(RPD.Blobs.LiquidFlame, object:getPos(), 10)
    end,

    onBurned = function(self, object)
        object:remove()
    end,

    image = function(self, object, level)
        if RPD.ModdingBase:isHalloweenEvent() then
            return 0
        end
        return 8
    end,

    name = function(self, object, level)
        if RPD.ModdingBase:isHalloweenEvent() then
            return RPD.textById("Barrel_Pumpkin_Name")
        end
        return RPD.textById("Barrel_Name")
    end,

    info = function(self, object, level)
        if RPD.ModdingBase:isHalloweenEvent() then
            return RPD.textById("Barrel_Pumpkin_Desc")
        end
        return RPD.textById("Barrel_Desc")
    end
}
