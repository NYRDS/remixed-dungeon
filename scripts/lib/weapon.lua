---
--- Shared wrapper for data weapons: the MeleeWeapon / KindOfBow /
--- MissileWeapon / Arrow / Dart frameworks (kind-parameterized java) read
--- the desc table once per kind and dispatch the optional hooks.
--- self = the per-kind script table, item = the weapon instance.
---
--- MeleeWeapon desc -> { tier, acu, dly, STR, min, max, image, imageFile,
---     anim, range, enchatable, upgradable, twoHanded, reforger,
---     upgradeNoop, chainAttack, puller, visualName, typicalSTR }
--- MissileWeapon desc -> { STR, min, max, dly, image, imageFile,
---     pricePerUnit | price, randomQty(lo,hi), dropsToFloorOnThrow,
---     incendiary, returnsToHand, rerollForbiddenEnchants, notUsableInMelee,
---     upgradable, isFliesStraight, isFliesFastRotating, anim, visualName }
--- Arrow desc  -> { baseMin, baseMax, baseDly, image } (+ attackProc hook)
--- Dart desc   -> { image, STR, min, max } (+ attackProc hook)
---
--- hooks (all optional): attackProc(self,item,attacker,defender,damage),
--- postAttack(self,item,target), ownerTakesDamage/ownerDoesDamage,
--- randomQty(part), saveData/loadData/loadLegacy(bundle)
---

local RPD  = require "scripts/lib/commonClasses"
local item = require "scripts/lib/item"
local serpent = require "scripts/lib/serpent"

local weapon = {}

weapon.__index = weapon
setmetatable(weapon, item)

weapon.desc = function(self)
    return {}
end

weapon.init = function(desc)
    setmetatable(desc, weapon)
    return desc
end

---
--- effect helpers (mirror the per-weapon java classes they replace)
---

-- Chaos weapon charge cycle (ChaosSword/ChaosBlade/ChaosBow): charge grows
-- on owner hits, spends on owner damage taken; crossing the threshold
-- upgrades (with a random enchant) or degrades + strips the enchant.
function weapon.chaosState(self, item)
    self.data = self.data or {}
    if self.data.charge == nil then
        self.data.charge = 0
    end
    return self.data
end

function weapon.chaosChargeForLevel(self, item, factor)
    factor = factor or 5
    return math.floor(factor * (item:level() ^ 1.5))
end

function weapon.chaosOwnerTakesDamage(self, item, damage, minLevel, factor)
    local data = weapon.chaosState(self, item)
    data.charge = data.charge - 1
    if data.charge < 0 then
        data.charge = 0
    end
    if item:level() > minLevel and data.charge == 0 then
        item:degrade()
        item:enchant(nil)
        data.charge = weapon.chaosChargeForLevel(self, item, factor)
        weapon.chaosSelectImage(self, item)
    end
end

function weapon.chaosOwnerDoesDamage(self, item, damage, factor)
    if item:isCursed() then
        return
    end
    if damage > 0 then
        local data = weapon.chaosState(self, item)
        data.charge = data.charge + 1
        if data.charge > weapon.chaosChargeForLevel(self, item, factor) then
            item:upgrade(true)
            weapon.chaosSelectImage(self, item)
            data.charge = 0
        end
    end
end

function weapon.chaosSelectImage(self, item)
    local lvl = item:level()
    local idx = math.floor(lvl / 3)
    if idx < 0 then idx = 0 end
    if idx > 4 then idx = 4 end
    item:setImage(idx)
end

function weapon.chaosSaveData(self)
    return serpent.dump(self.data or {})
end

function weapon.chaosLoadData(self, item, str)
    local _, data = serpent.load(str)
    self.data = data or {}
end

return weapon
