--
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"
local storage = require "scripts/lib/storage"
local itemLib = require "scripts/lib/item"


local action_mine = "Pickaxe_ACMine"
local mineableLevels = {
    ["CavesLevel"] = true,
    ["CavesBossLevel"] = true,
    ["GutsLevel"] = true,
}

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
            equipable = RPD.Slots.weapon,
            price     = 20,
            data = {
                bloodStained = false
            }
        }
    end,

    actions = function(self, item, hero)
        return {action_mine}
    end,

    getBoolean = function(self, key)
        return self.data[key]
    end,


    execute = function(self, item, owner, action)
        local level = owner:level()
        local levelKind = RPD.DungeonGenerator:getLevelKind(level.levelId)

        RPD.debug("Pickaxe: " .. levelKind .. " " .. action .. " " .. tostring(self.data.bloodStained))

        if action == action_mine then

            if owner:effectiveSTR() < requiredStr then
                owner:say("Pickaxe_InsufficientStr")
                return
            end

           --[[
            if not mineableLevels[levelKind] then
                return
            end
]]
            local mined = false
            local tryMine = function(cell)
                if mined then
                    return
                end
                if level:get(cell) == RPD.Terrain.WALL_DECO then
                    level:set(cell, RPD.Terrain.WALL)
                    RPD.GameScene:updateMap( )
                    mined = true

                    owner:playAttack(cell)
                    owner:spend(time_to_mine)
                    owner:hunger():satisfy(-RPD.Buffs.Hunger.STARVING / 10)

                    if levelKind == 'CavesLevel' or levelKind == 'CavesBossLevel' then
                        RPD.Sfx.CellEmitter:center(cell):burst(RPD.Sfx.Speck:factory(RPD.Sfx.Speck.STAR), 7)
                        RPD.playSound( "snd_evoke" )
                        owner:collectAnimated(RPD.item("DarkGold"))
                    end

                    if levelKind == 'GutsLevel' then
                        RPD.Sfx.Wound:hit(cell,math.random()*3.14)
                        RPD.playSound( "snd_hit" )
                        owner:collectAnimated(RPD.item("MysteryMeat"))
                    end
                end
            end

            RPD.forCellsAround(owner:getPos(), tryMine)

            if not mined then
                owner:say("Pickaxe_NothingToMine")
            end

        end
    end,

    attackProc = function(self, item, attacker, defender, damage)
        if defender:getEntityKind() == "Bat" and damage >= defender:hp() then
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


    goodForMelee = function()
        return true
    end,

    getVisualName = function()
        return "Pickaxe"
    end,

    getAttackAnimationClass = function()
        return "sword"  --look at KindOfWeapon.java for possible values
    end,

    slot = function(self, item, belongings)
        return RPD.Slots.weapon
    end,

    blockSlot = function(self, item, belongings)
        return RPD.Slots.leftHand
    end,

    accuracyFactor = function(self, item, user)
        return 1
    end,

    damageRoll = function(self, item, user)
        local lvl = item:level()
        return math.random(2 + lvl, 10 + lvl*2)
    end,

    attackDelayFactor = function(self, item, user)
        return 1 + math.min((requiredStr - user:effectiveSTR()) / 10, 0)
    end,

    typicalSTR = function(self, item)
        return requiredStr
    end,

    statsRequirementsSatisfied = function(self, item)
        return item:getOwner():effectiveSTR() >= requiredStr
    end,

    knownStatsText = function(self, item)
        return ":"..tostring(requiredStr)
    end,

    unknownStatsText = function(self, item)
        return "???"
    end

}
