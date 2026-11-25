---
--- TomeOfMastery: choose a subclass (was items/TomeOfMastery.java). The
--- class->ways table mirrors the java switch; the completion (detach, fx,
--- subclass set) is MasteryItem.choose, driven by the java WndChooseWay.
---
local RPD     = require "scripts/lib/commonClasses"
local itemLib = require "scripts/lib/item"

local AC_READ = "TomeOfMastery_ACRead"

-- classes that never get the generic mastery paths
local noMastery = {
    NECROMANCER = true,
    GNOLL       = true,
    DOCTOR      = true,
    PRIEST      = true,
}

local waysByClass = {
    WARRIOR  = { "GLADIATOR",  "BERSERKER" },
    MAGE     = { "BATTLEMAGE", "WARLOCK" },
    ROGUE    = { "FREERUNNER", "ASSASSIN" },
    HUNTRESS = { "SNIPER",     "WARDEN" },
    ELF      = { "SCOUT",      "SHAMAN" },
}

return itemLib.init{
    desc = function()
        return {
            image     = 82, -- ItemSpriteSheet.MASTERY
            imageFile = "items.png",
            name      = "TomeOfMastery_Name",
            info      = "TomeOfMastery_Info",
            price     = 0,
        }
    end,

    actions = function(self, item)
        return { AC_READ }
    end,

    execute = function(self, item, chr, action)
        if action ~= AC_READ then
            return
        end

        if chr:hasBuff("Blindness") then
            RPD.glogw(RPD.textById("TomeOfMastery_Blinded"))
            return
        end

        if chr:getSubClass():name() ~= "NONE" then
            RPD.glogw(RPD.textById("TomeOfMastery_WayAlreadyChosen"))
            return
        end

        local heroClass = chr:getHeroClass():name()
        if noMastery[heroClass] or not waysByClass[heroClass] then
            RPD.glogw(RPD.textById("TomeOfMastery_WayAlreadyChosen"))
            return
        end

        local ways = waysByClass[heroClass]
        local wnd = luajava.newInstance(RPD.Objects.Ui.WndChooseWay,
            chr, item, chr:getSubClassByName(ways[1]), chr:getSubClassByName(ways[2]))
        RPD.GameScene:show(wnd)
    end,

    onPickUp = function(self, item, chr)
        if not noMastery[chr:getHeroClass():name()] then
            RPD.Badges:validateMastery(chr:getHeroClass())
        end
    end,
}
