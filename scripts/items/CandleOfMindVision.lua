---
--- CandleOfMindVision: while equipped, grants Mind Vision for its remaining
--- charges; charges drain while carried (was artifacts/CandleOfMindVision.java,
--- an IActingItem; CustomItem implements the spend hook now).
---
local RPD  = require "scripts/lib/commonClasses"
local item = require "scripts/lib/item"

return item.init{
    desc = function()
        return {
            image      = 0,
            imageFile  = "items/artifacts.png",
            name       = "Candle_Name",
            info       = "Candle_Info",
            stackable  = false,
            upgradable = false,
            identified = true,
            price      = 0,
            equipable  = RPD.Slots.artifact,
        }
    end,

    loadData = function(self, item, str)
        local _, data = serpent.load(str)
        self.data = data or { charges = 50, activated = false }
    end,

    saveData = function(self)
        return serpent.dump(self.data or { charges = 50, activated = false })
    end,

    loadLegacy = function(self, item, bundle)
        -- pre-migration java fields: charges (float), activated (bool)
        self.data = {
            charges   = bundle:getFloat("charges"),
            activated = bundle:getBoolean("activated"),
        }
    end,

    activate = function(self, item, hero)
        local data = self.data or { charges = 50 }
        data.activated = true
        RPD.affectBuff(hero, "MindVision", data.charges)
    end,

    deactivate = function(self, item, hero)
        RPD.removeBuff(hero, "MindVision")
        local data = self.data or {}
        data.activated = false
    end,

    spend = function(self, item, hero, time)
        local data = self.data or { charges = 50 }
        if data.activated then
            data.charges = data.charges - time
            if data.charges < 0 then data.charges = 0 end
        end
    end,

    image = function(self, item)
        local data = self.data or { charges = 50, activated = false }
        if data.activated then
            if data.charges >= 40 then return 1
            elseif data.charges >= 25 then return 2
            elseif data.charges >= 10 then return 3
            else return 4 end
        end
        return 0
    end,

    info = function(self, item)
        local data = self.data or { charges = 50, activated = false }
        if data.activated then
            return RPD.textById("Candle_Info2")
        end
        return RPD.textById("Candle_Info1")
    end,
}
