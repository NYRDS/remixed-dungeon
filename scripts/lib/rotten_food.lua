---
--- Shared implementation for the rotten foods (was the abstract RottenFood
--- java base). The concrete kinds supply their desc/purify/morph targets via
--- rotten_food.init{}.
---
--- Rotten behavior (all preserved from the java base):
---   eat:  "Disgusting!" + 3/4 chance of a gas burst on the eater's cell
---   throw: 3/4 chance the item is consumed by a gas burst instead of landing
---          (the java Food framework calls the molder hook here)
---

local RPD  = require "scripts/lib/commonClasses"
local food = require "scripts/lib/food"

local rotten = {}

rotten.__index = rotten
setmetatable(rotten, food)

rotten.init = function(desc)
    -- merge the kind's own desc (image etc.) over the rotten defaults so the
    -- java side always sees energy/message/price too
    local ownDesc = desc.desc
    desc.desc = function(self)
        local base = rotten.desc(self)
        if ownDesc then
            local own = ownDesc(self)
            for k, v in pairs(own) do
                base[k] = v
            end
        end
        return base
    end
    setmetatable(desc, rotten)
    return desc
end

-- half a meal, and it tastes like it
rotten.desc = function(self)
    return {
        energy  = 60, -- (Hunger.STARVING - Hunger.HUNGRY) / 2
        message = "RottenFood_Message",
        price   = 1,
    }
end

rotten.molder = function(self, item, cell)
    RPD.playSound("snd_rotten_drop") -- Assets.SND_ROTTEN_DROP

    local roll = RPD.Random:Int(4)
    if roll == 0 then
        RPD.placeBlob(RPD.Blobs.ConfusionGas, cell, 150 + 10 * RPD.Dungeon.depth)
        RPD.Sfx.CellEmitter:get(cell):burst(RPD.speckEffectFactory(RPD.Sfx.Speck.CONFUSION, 0), 10)
    elseif roll == 1 then
        RPD.placeBlob(RPD.Blobs.ParalyticGas, cell, 500)
        RPD.Sfx.CellEmitter:get(cell):burst(RPD.speckEffectFactory(RPD.Sfx.Speck.PARALYSIS, 0), 10)
    elseif roll == 2 then
        RPD.placeBlob(RPD.Blobs.ToxicGas, cell, 500)
        RPD.Sfx.CellEmitter:get(cell):burst(RPD.speckEffectFactory(RPD.Sfx.Speck.TOXIC, 0), 10)
    else
        return false
    end

    return true
end

rotten.onExecute = function(self, item, chr, action)
    if action ~= RPD.Actions.eat then
        return
    end

    -- Food.message is a public String FIELD (not a method); the old
    -- item:message() call resolved the field and tried to call it
    RPD.glogw(item.message)
    self:molder(item, chr:getPos())
end

return rotten
