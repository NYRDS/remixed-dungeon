---
--- Shared wrapper for data rings: Ring.java (the kind-parameterized java
--- framework) reads the desc table once per kind and dispatches the optional
--- hooks. self = the per-kind script table, item = the Ring instance.
--- Identification (random per-run gem image + known set) stays java-side.
--- The equipped buff is KindRingBuff (java, runtime-only, kind
--- "artifactBuff<Buff>"); its bonus queries route to the hooks below.
---
--- desc(self)                        -> { buff, price, upgradable, identified,
---                                        image, imageFile }
--- random(self, item)                -> truthy = handled (default 1d(1..3) lvl roll)
--- afterEquip(self, item, chr)       -> post-equip effect
--- doPickUp(self, item, chr)         -> truthy = fully handled; nil = default collect
---   (Haggler/Thorns: item:identify() + badge + validateItemLevelAcquired, return nil)
--- regenerationBonus(self,item,buff,chr) / stealthBonus / attackSkillBonus /
--- defenceSkillBonus / hasteLevel    -> bonus numbers (buff:level() is authoritative)
--- resistances(self, item, buff, chr) -> table of resisted buff kind strings
--- durationFactor(self, item, buff)   -> duration scaler (Elements)
--- defenceProc(self, item, buff, defender, enemy, damage) -> damage
--- icon / name / info / onHeroDeath   -> buff display + death hooks
---

local RPD  = require "scripts/lib/commonClasses"
local item = require "scripts/lib/item"

local ring = {}

ring.__index = ring
setmetatable(ring, item)

ring.desc = function(self)
    return {}
end

ring.init = function(desc)
    setmetatable(desc, ring)
    return desc
end

---
--- effect helpers (mirror the per-ring java classes they replace)
---

-- RingOfElements style resistances: full set with a level-gated random miss.
-- The FULL set from the old java class, in its original order.
ring.elementsFullSet = { "Burning", "ToxicGas", "Poison", "Electricity",
                         "Warlock", "Eye", "BurningFist", "LiquidFlame" }

function ring.elementsResistances(self, item, buff, chr)
    -- old java: Random.Int(level() + 3) >= 3 (level 0 -> 3/5, level +3 -> always)
    local upper = buff:level() + 2
    if upper < 0 then
        return {} -- old java Int(negative+n) was never >= 3 either
    end
    -- old java: Random.Int(level() + 3) >= 3, level 0 -> never, level +1 -> 1/4
    if RPD.Random:Int(upper + 1) >= 3 then
        return ring.elementsFullSet
    end
    return {}
end

-- Old durationFactor: level<0 ? 1 : (2 + 0.5*level)/(2 + level)
function ring.elementsDurationFactor(self, item, buff)
    local lvl = buff:level()
    if lvl < 0 then
        return 1
    end
    return (2 + 0.5 * lvl) / (2 + lvl)
end

return ring
