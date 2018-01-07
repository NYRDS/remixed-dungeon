--
-- User: mike
-- Date: 07.01.2018
-- Time: 21:16
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"

local mob = require "scripts/lib/mob"

local kinds = {}
kinds[1] = {}
kinds[1].blob = RPD.Blobs.ParalyticGas
kinds[1].immunity = RPD.Buffs.Paralysis
kinds[1].speck = RPD.Sfx.Speck.PARALYSIS

kinds[2] = {}
kinds[2].blob = RPD.Blobs.ConfusionGas
kinds[2].immunity = RPD.Buffs.Vertigo
kinds[2].speck = RPD.Sfx.Speck.CONFUSION

kinds[3] = {}
kinds[3].blob = RPD.Blobs.ToxicGas
kinds[3].immunity = RPD.Blobs.ToxicGas
kinds[3].speck = RPD.Sfx.Speck.TOXIC


local function makeFetidRat()
    local cloud
    local data

    return {
        stats = function(self)
            data = mob.restoreData(self)
            print("rat kind", tostring(data.kind))
            data.kind = data.kind or math.random(1, 3)
            mob.storeData(self,data)

            self:immunities():add(kinds[data.kind].immunity)
        end,
        move = function(self, cell)
            if cloud == nil then
                cloud = self:getSprite():emitter()
                cloud:pour(RPD.Sfx.Speck:factory(kinds[data.kind].speck), 0.7);
            end
        end,
        defenceProc = function(self, enemy, dmg)
            RPD.placeBlob(kinds[data.kind].blob, self:getPos(), 20);
        end
    }
end

return mob.init(makeFetidRat())