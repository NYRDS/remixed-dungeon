---
--- ChaosStaff: the fused chaos wand (was chaos/ChaosStaff.java). The chaos
--- charge grows on every hit the owner takes and discharges into the next zap.
---
local RPD     = require "scripts/lib/commonClasses"
local wand    = require "scripts/lib/wand"
local serpent = require "scripts/lib/serpent"

return wand.init{
    desc  = function (self, item)
        return {
            name      = "ChaosStaff_Name",
            info      = "ChaosStaff_Info",
            image     = 0,
            imageFile = "items/chaosStaff.png",
            data      = { charge = 0 },
        }
    end,

    -- the tiered staff art: level/3 clamped to the 5 frames
    image = function(self, item)
        return math.max(0, math.min(math.floor(item:level() / 3), 4))
    end,

    saveData = function(self)
        return serpent.dump(self.data or {})
    end,

    loadData = function(self, item, str)
        local _, data = serpent.load(str)
        self.data = data or {}
    end,

    ownerTakesDamage = function(self, item, damage)
        self.data.charge = (self.data.charge or 0) + 1
    end,

    zap = function(self, item, cell, victim)
        local charge = self.data.charge or 0
        RPD.ChaosCommon:doChaosMark(cell, 10 + item:level() * 10 + charge)
        self.data.charge = 0

        if math.random() < 0.1 then
            if victim ~= nil and victim:getEntityKind() ~= "Hero" then
                if victim:isBoss() or victim:isNpc() then
                    return
                end

                -- java rolled Random.Int(0, 4) = [0, 4): the heal branch was
                -- unreachable there too, kept verbatim
                local roll = RPD.Random:Int(0, 4)
                if roll == 0 then
                    victim:die(item:getOwner())
                elseif roll == 1 then
                    RPD.Mob:makePet(victim, item:getOwner():getId())
                elseif roll == 2 then
                    local lvl = RPD.Dungeon.level
                    local nextCell = lvl:getEmptyCellNextTo(cell)
                    if lvl:cellValid(nextCell) then
                        local newMob = RPD.MobFactory:mobByName(victim:getEntityKind())
                        newMob:setPos(nextCell)
                        lvl:spawnMob(newMob, 0)
                    end
                elseif roll == 3 then
                    RPD.CharUtils:teleportAway(victim, item:getOwner())
                end
                -- java case 4 (Potion.heal) was unreachable: Random.Int(0, 4)
                -- never returns 4 - the branch died with the class
            end
        end
    end,
}
