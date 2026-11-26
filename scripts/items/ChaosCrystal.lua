---
--- ChaosCrystal: chaos-mark thrower + fuser (was chaos/ChaosCrystal.java).
--- Owner damage taken charges it (cap 100); USE marks a cell with a random
--- chaos blob burst (a cursed crystal marks its owner instead); at 50+
--- charges with two identifications it fuses a scroll/bow/weapon/armor/
--- wand/shield into its chaos counterpart (type routing in ItemUtils).
--- identify() bumps a 3-level counter, 2 = identified - the java quirk of
--- reverting to the unknown name at level 3+ is preserved verbatim.
---
local RPD     = require "scripts/lib/commonClasses"
local itemLib = require "scripts/lib/item"

local AC_USE  = "SpiderCharm_Use" -- sic: UsableArtifact's shared constant
local AC_FUSE = "ChaosCrystal_Fuse"

local TIME_TO_USE  = 1
local TIME_TO_FUSE = 10

local FUSE_CHARGE_THRESHOLD = 50
local CHARGE_CAP            = 100

local function fuse(item, chr)
    if not _G.ItemSelectionCallbacks then
        _G.ItemSelectionCallbacks = {}
    end
    local callbackId = #_G.ItemSelectionCallbacks + 1
    _G.ItemSelectionCallbacks[callbackId] = function(selItem, selector)
        if selItem == nil then
            return
        end

        if selItem:quantity() > 1 then
            selItem:detach(selector:getBelongings().backpack)
        else
            selItem:removeItemFrom(selector)
        end

        item:removeItemFrom(selector)
        selector:doOperate(TIME_TO_FUSE)

        local fused = RPD.ItemUtils:chaosFuseResult(selItem)
        if fused ~= nil then
            selector:collect(fused)
        end
    end

    local listener = luajava.newInstance(RPD.Objects.Ui.LuaWndBagListener, callbackId)
    RPD.GameScene:selectItem(chr, listener, RPD.BackpackMode.FUSEABLE, RPD.textById("ChaosCrystal_SelectForFuse"))
    chr:doOperate()
end

return itemLib.init{
    desc = function()
        return {
            image         = 9,
            imageFile     = "items/artifacts.png",
            name          = "ChaosCrystal_Name",
            info          = "ChaosCrystal_Info",
            stackable     = false,
            upgradable    = false,
            identified    = false,
            defaultAction = RPD.Actions.throw,
            price         = 0,
            equipable     = RPD.Slots.artifact,
            data          = { charge = 0, identifyLevel = 0 },
        }
    end,

    identify = function(self, item)
        self.data.identifyLevel = (self.data.identifyLevel or 0) + 1
    end,

    isIdentified = function(self, item)
        return (self.data.identifyLevel or 0) == 2
    end,

    glowing = function(self, item)
        return itemLib.makeGlowing(math.random(0, 0xfffffe), 1)
    end,

    actions = function(self, item, hero)
        local data = self.data
        local lvl  = data.identifyLevel or 0
        local ret  = {}
        if data.charge > 0 and lvl > 0 then
            item:setDefaultAction(AC_USE)
            table.insert(ret, AC_USE)
            if data.charge >= FUSE_CHARGE_THRESHOLD and lvl > 1 then
                table.insert(ret, AC_FUSE)
            end
        end
        return ret
    end,

    execute = function(self, item, chr, action)
        if action == AC_USE then
            item:selectCell(AC_USE, "ChaosCrystal_Prompt")
        elseif action == AC_FUSE then
            fuse(item, chr)
        end
    end,

    cellSelected = function(self, item, action, cell)
        if cell == nil then
            return
        end
        local chr = item:getOwner()
        if item:isCursed() then
            cell = chr:getPos()
        end
        RPD.ChaosCommon:doChaosMark(cell, self.data.charge)
        self.data.charge = 0
        chr:spend(TIME_TO_USE)
    end,

    cellCanceled = function(self, item, action)
        -- the java listener spent its time even on cancel
        item:getOwner():spend(TIME_TO_USE)
    end,

    ownerTakesDamage = function(self, item, damage)
        if damage > 0 then
            self.data.charge = math.min(self.data.charge + 1, CHARGE_CAP)
        end
    end,

    ownerDoesDamage = function(self, item, damage)
        if item:isCursed() and self.data.charge > 0 then
            RPD.ChaosCommon:doChaosMark(item:getOwner():getPos(), self.data.charge)
        end
    end,

    name = function(self, item)
        local lvl = self.data.identifyLevel or 0
        if lvl == 1 then
            return RPD.textById("ChaosCrystal_Name_1")
        elseif lvl == 2 then
            return RPD.textById("ChaosCrystal_Name_2")
        end
        return RPD.textById("ChaosCrystal_Name")
    end,

    info = function(self, item)
        local lvl = self.data.identifyLevel or 0
        if lvl == 1 then
            return RPD.textById("ChaosCrystal_Info_1")
        elseif lvl == 2 then
            return RPD.textById("ChaosCrystal_Info_2")
        end
        return RPD.textById("ChaosCrystal_Info")
    end,

    -- ItemSlot status text ("x/100"), colored by getColor
    getText = function(self, item)
        if (self.data.identifyLevel or 0) > 0 then
            return string.format("%d/100", self.data.charge)
        end
        return nil
    end,

    getColor = function(self, item)
        return 0xe0a0a0
    end,

    loadLegacy = function(self, item, bundle)
        -- pre-migration @Packable fields
        self.data = {
            charge        = bundle:getInt("charge"),
            identifyLevel = bundle:getInt("identifyLevel"),
        }
    end,
}
