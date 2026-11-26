---
--- Rotberry.Seed: the WandMaker quest berry - screams the whole level awake
--- when collected, inflammable, and eating it erupts Toxic + Paralytic gas
--- (was plants/RotberrySeed.java). Plants into the Rotberry bush via the
--- java framework (plantKind = "Rotberry"); WandMaker.lua spawns it via
--- RPD.item("Rotberry.Seed").
---
local RPD  = require "scripts/lib/commonClasses"
local seed = require "scripts/lib/seed"

return seed.init{
    desc = function()
        return {
            plantName   = "WandMaker_RotberryName",
            descText    = "WandMaker_RotberryDesc",
            image       = 7,
            alchemyKind = "PotionOfStrength",
        }
    end,
    onEat = function(self, item, chr)
        -- java: Blob.seed(chr.getPos(), 100, ToxicGas/ParalyticGas)
        RPD.placeBlob(RPD.Blobs.ToxicGas, chr:getPos(), 100)
        RPD.placeBlob(RPD.Blobs.ParalyticGas, chr:getPos(), 100)
    end,
    inflammable = function(self, item, cell)
        return true
    end,
    onCollect = function(self, item, container)
        -- java: challengeAllMobs(getOwner(), SND_CHALLENGE) + hero-only warn
        local owner = item:getOwner()
        RPD.CharUtils:challengeAllMobs(owner, "snd_challenge")
        if owner == RPD.Dungeon.hero then
            RPD.glogw(RPD.textById("WandMaker_RotberryInfo"))
        end
    end,
}
