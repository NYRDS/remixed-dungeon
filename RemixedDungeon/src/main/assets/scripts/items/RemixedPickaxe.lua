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
                        owner:collect(RPD.item("DarkGold"))
                    end

                    if levelKind == 'GutsLevel' then
                        RPD.Sfx.Wound:hit(cell,math.random()*3.14)
                        RPD.playSound( "snd_hit" )
                        owner:collect(RPD.item("MysteryMeat"))
                    end
                end
            end

            RPD.forCellsAround(owner:getPos(), tryMine)

            if not mined then
                owner:say("Pickaxe_NothingToMine")
            end

        end
    end,

    glowing = function(self, item)
        if self.data.bloodStained then
            return itemLib.makeGlowing(0x550000, 1)
        end
        return nil
    end
}
