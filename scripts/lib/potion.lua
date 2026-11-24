---
--- Shared wrapper for data potions: Potion.java (the kind-parameterized java
--- framework) reads the desc table once per kind and dispatches the optional
--- hooks. self = the per-kind script table, item = the Potion instance.
--- Identification (random per-run color image + known set) stays java-side.
---
--- desc(self)                        -> { labelIndex, price (basePrice) }
--- apply(self, item, chr)            -> drink effect; absent = base shatter
--- shatter(self, item, cell)         -> thrown-break effect; absent = base splash
--- moistenArrow(self, item, arrow, owner)     -> arrow kind string it becomes
--- moistenScroll(self, item, scroll, owner)   -> replacement scroll kind string
--- moistenRottenFood(self, item, food, owner) -> truthy = flies away untouched
---
--- Shared effect helpers below keep the per-kind scripts one-liners.
---

local RPD  = require "scripts/lib/commonClasses"
local item = require "scripts/lib/item"

local potion = {}

potion.__index = potion
setmetatable(potion, item)

potion.desc = function(self)
    return {}
end

potion.init = function(desc)
    setmetatable(desc, potion)
    return desc
end

---
--- effect helpers (mirror the per-potion java classes they replace)
---

-- Splash the potion and mark it known; returns the char standing on the cell.
function potion.splashIdentify(self, item, cell)
    item:setKnown()
    item:splash(cell)
    RPD.playSound("snd_shatter") -- Assets.SND_SHATTER
    return RPD.Actor:findChar(cell)
end

-- Standard drink: identify, then run an "applyTo(chr)" lambda.
function potion.drinkIdentify(self, item, chr, applyTo)
    item:setKnown()
    applyTo(chr)
end

-- The canonical potion-of-healing effect (was PotionOfHealing.heal).
function potion.heal(self, item, target, portion)
    target:heal(math.floor(target:ht() * portion), item)
    RPD.removeBuff(target, "Poison")
    RPD.removeBuff(target, "Cripple")
    RPD.removeBuff(target, "Weakness")
    RPD.removeBuff(target, "Bleeding")
end

return potion
