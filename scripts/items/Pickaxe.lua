---
--- Pickaxe: the troll blacksmith's mining pick (was quest/Pickaxe.java).
--- Vanilla mining: a WALL_DECO neighbour in the caves turns into DarkGold.
---
local RPD     = require "scripts/lib/commonClasses"
local itemLib = require "scripts/lib/item"

local action_mine = "Pickaxe_ACMine"
local requiredStr = 14

return itemLib.init{
    desc  = function (self, item)
        return {
            image         = 101,
            imageFile     = "items.png",
            name          = "Pickaxe_Name",
            info          = "Pickaxe_Info",
            stackable     = false,
            defaultAction = action_mine,
            equipable     = RPD.Slots.weapon,
            price         = 20,
            upgradable    = false,
            identified    = true,
            data          = { bloodStained = false },
        }
    end,

    actions = function(self, item, hero)
        return { action_mine }
    end,

    getBoolean = function(self, item, key)
        return self.data[key]
    end,

    execute = function(self, item, owner, action)
        if action ~= action_mine then
            return
        end

        local level = owner:level()
        local levelKind = level:levelKind()
        if levelKind ~= "CavesLevel" and levelKind ~= "CavesBossLevel" then
            RPD.glogw(RPD.textById("Pickaxe_NoVein"))
            return
        end

        local mined = false
        local tryMine = function(cell)
            if mined then
                return
            end
            if level:get(cell) == RPD.Terrain.WALL_DECO then
                owner:spend(2) -- TIME_TO_MINE
                owner:playAttack(cell)

                RPD.Sfx.CellEmitter:center(cell):burst(RPD.Sfx.Speck:factory(RPD.Sfx.Speck.STAR), 7)
                RPD.playSound("snd_evoke")

                level:set(cell, RPD.Terrain.WALL)
                RPD.GameScene:updateMap()

                owner:collectAnimated(RPD.item("DarkGold"))
                owner:hunger():satisfy(-40) -- -Hunger.STARVING / 10

                mined = true
            end
        end
        RPD.forCellsAround(owner:getPos(), tryMine)

        if not mined then
            RPD.glogw(RPD.textById("Pickaxe_NoVein"))
        end
    end,

    attackProc = function(self, item, attacker, defender, damage)
        if not self.data.bloodStained
                and defender:getEntityKind() == "Bat"
                and defender:hp() <= damage then
            self.data.bloodStained = true
            RPD.QuickSlot:refresh(attacker)
        end
        return damage
    end,

    glowing = function(self, item)
        if self.data.bloodStained then
            return itemLib.makeGlowing(0x550000, 1)
        end
        return nil
    end,

    slot = function(self, item, belongings)
        return RPD.Slots.weapon
    end,

    typicalSTR = function(self, item)
        return requiredStr
    end,

    statsRequirementsSatisfied = function(self, item)
        return item:getOwner():effectiveSTR() >= requiredStr
    end,
}
