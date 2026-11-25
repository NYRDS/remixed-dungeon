---
--- Shared wrapper for data wands: Wand.java (the kind-parameterized java
--- framework) reads the desc table once per kind and dispatches the optional
--- hooks. self = the per-kind script table, item = the Wand instance.
--- Identification (random per-run wood + known set), charges, charging loop
--- and damage roll stay java-side.
---
--- desc(self) -> { fx, hitChars, hitObjects, initialCharges, identified,
---                 image, imageFile, price, disenchants }
---   fx: "blue","purple","earth","white","fire","wool","poison","foliage",
---       "slowness","force","coldLight","ice","shadow" (MagicMissile statics)
---       or "deathray","lightning","blink" (java-side special flows)
--- zap(self, item, cell, victim)          -> the zap effect (was onZap)
--- destCell(self, item, src, target)      -> override after base Ballistica
--- affectTarget(self, item)               -> SHAMAN mastery gate (default true)
--- emitter / emitterInterval              -> idle particle name / seconds
---
--- Grid-sweep zaps are java bridges on RPD.ItemUtils (avalancheZap,
--- disintegrationZap, fireboltZap, flockZap, lightningArc, telekinesisZap).
---

local RPD  = require "scripts/lib/commonClasses"
local item = require "scripts/lib/item"

local wand = {}

wand.__index = wand
setmetatable(wand, item)

wand.desc = function(self)
    return {}
end

wand.init = function(desc)
    setmetatable(desc, wand)
    return desc
end

---
--- effect helpers (mirror the per-wand java classes they replace)
---

-- The shared self-kill fail report (Avalanche/Firebolt/Lightning/MagicMissile/
-- Icebolt): owner==hero && dead -> Dungeon.fail(WAND). infoId is the kind's
-- own _Info1 message string.
function wand.selfKillReport(self, item, infoId)
    local owner = item:getOwner()
    if owner ~= nil and owner:getEntityKind() == "Hero" and not owner:isAlive() then
        RPD.Dungeon:fail(RPD.JavaUtils:format(
            RPD.ResultDescriptions:getDescription(RPD.ResultReason.WAND),
            item:name(), RPD.Dungeon.depth))
        RPD.GLog:n(RPD.textById(infoId or "WandOfAvalanche_Info1"))
    end
end

-- single-target damage + sprite burst (MagicMissile/Icebolt/Shadowbolt shape)
function wand.damageBurst(self, item, victim, dmg, color, burst)
    victim:damage(dmg, item)
    victim:getSprite():burst(color, burst)
    wand.selfKillReport(self, item)
    return dmg
end

return wand
