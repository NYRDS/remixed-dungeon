---
--- DewVial: collects dew, drinks it back as healing (was items/DewVial.java).
--- A full vial saves its owner from death (CharUtils.autoDrinkDewVial).
--- Volume lives in the per-instance lua state; old java saves are read back
--- by loadLegacy (the @Packable volume field).
---
local RPD     = require "scripts/lib/commonClasses"
local itemLib = require "scripts/lib/item"

local MAX_VOLUME = 10

return itemLib.init{
    desc = function()
        return {
            image         = 0,
            imageFile     = "items/vials.png",
            name          = "DewVial_Name",
            info          = "DewVial_Info",
            price         = 0,
            defaultAction = "Potion_ACDrink",
            data          = { volume = 0 },
        }
    end,

    actions = function(self, item)
        local ret = {}
        if (self.data.volume or 0) > 0 then
            ret[1] = "Potion_ACDrink"
        end
        return ret
    end,

    execute = function(self, item, chr, action)
        if action == "Potion_ACDrink" then
            self:drink(item, chr)
        end
    end,

    -- the drink itself; also the death-save entry via CharUtils bridge
    drink = function(self, item, chr)
        local volume = self.data.volume or 0
        if volume > 0 then
            -- was Math.ceil(Math.pow(volume, POW) / 20 * ht), POW = Math.log10(20)
            -- hardcoded: LuaJ has no math.log10 and log(x,10) is not bit-exact
            local value = math.ceil(math.pow(volume, 1.3010299956639813) / 20 * chr:ht())
            local effect = math.min(chr:ht() - chr:hp(), value)
            if effect > 0 then
                chr:heal(effect, item)
                chr:showStatus_a(RPD.CharSprite.POSITIVE, "%+dHP", effect)
            end

            self.data.volume = 0

            RPD.playSound("snd_drink")
            chr:doOperate(1) -- TIME_TO_DRINK

            RPD.QuickSlot:refresh(chr)
        else
            RPD.glogw(RPD.textById("DewVial_Empty"))
        end
    end,

    isFull = function(self, item)
        return (self.data.volume or 0) >= MAX_VOLUME
    end,

    collectDew = function(self, item, dew)
        RPD.glog(RPD.textById("DewVial_Collected"))
        local volume = (self.data.volume or 0) + dew:quantity()
        if volume >= MAX_VOLUME then
            volume = MAX_VOLUME
            RPD.glogp(RPD.textById("DewVial_Full"))
        end
        self.data.volume = volume

        RPD.QuickSlot:refresh(item:getOwner())
    end,

    fill = function(self, item)
        self.data.volume = MAX_VOLUME
        RPD.QuickSlot:refresh(item:getOwner())
    end,

    image = function(self, item)
        local volume = self.data.volume or 0
        if volume == 0 then
            return 0
        elseif volume < MAX_VOLUME / 2 then
            return 1
        elseif volume < MAX_VOLUME then
            return 2
        end
        return 3
    end,

    glowing = function(self, item)
        if self:isFull(item) then
            return itemLib.makeGlowing(0xFFFFCC, 1) -- was Glowing(0xFFFFCC) = period 1
        end
        return nil
    end,

    status = function(self, item)
        return string.format("%d/%d", self.data.volume or 0, MAX_VOLUME)
    end,

    -- restore from a pre-migration java save (the @Packable volume field)
    loadLegacy = function(self, item, bundle)
        self.data.volume = bundle:optInt("volume", 0)
    end,
}
