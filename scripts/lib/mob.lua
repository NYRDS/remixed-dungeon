--
-- User: mike
-- Date: 23.11.2017
-- Time: 21:00
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"

local quest = require"scripts/lib/quest"

local serpent = require "scripts/lib/serpent"

local mob = {}

local knownMobs = {}
setmetatable(knownMobs, { __mode = 'vk' })

mob.__index = mob

mob.init = function(desc)
    local ret = {}

    for k,v in pairs(desc) do
        ret[k] = v
    end

    setmetatable(ret, mob)

    ret.data = {}

    return ret
end

local onDieCallbacks = {}

mob.installOnDieCallback = function(callback)
    onDieCallbacks[callback] = true
end

mob.saveData = function (self, _)
    return serpent.dump(self.data or {})
end

mob.loadData = function (self, _, str)
    local _,data = serpent.load(str)
    self.data = data or {}
end

mob.storeData = function(self, data)
    knownMobs[self].data = data or {}
end

mob.restoreData = function(self)
    return knownMobs[self].data or {}
end

mob.onDie = function(self,mob,cause)
    quest.mobDied(mob, cause)

    for k, _ in pairs(onDieCallbacks) do
        k(mob,cause)
    end

    return not not (self.die and self.die(mob, cause))
end

mob.onInteract = function(self,mob,chr)
    if not self.interact then
        return false
    end

    -- nil = handled (legacy NPC scripts return nothing); a script may return
    -- false to decline the interact (angry RatKing falls through to attack)
    local handled = self.interact(mob, chr)
    if handled == nil then
        return true
    end
    return not not handled
end

mob.onMove = function(self,mob,cell)
    return not not (self.move and self.move(mob, cell))
end

mob.onAct = function(self,mob)
    return not not (self.act and self.act(mob))
end

-- called from Char.destroy: side cleanup on removal (journal entries etc.)
mob.onDestroy = function(self,mob)
    return not not (self.destroy and self.destroy(mob))
end

-- called from Char.getDescription: nil lets the json classDesc answer
mob.description = function(self,mob)
    if not self.desc then
        return nil
    end
    return self.desc(mob)
end

-- called from Mob.getCloser during the java AI step: return true when the
-- script took the step itself (e.g. Succubus blink, incl. its spend refund),
-- false to fall through to the regular pathfind
mob.onGetCloser = function(self,mob,target,ignorePets)
    if not self.getCloser then
        return false
    end
    return not not self.getCloser(mob, target, ignorePets)
end

-- called from Mob.zap before the base damage flow: return true when the
-- script took the zap entirely (hit rolls, effects, death reports), false
-- for the regular damage zap
mob.onZap = function(self,mob,enemy)
    if not self.zap then
        return false
    end
    return not not self.zap(mob, enemy)
end

-- called from CustomMob.speed: return the (possibly adjusted) speed,
-- base is the buff/armor-computed value
mob.onSpeed = function(self,mob,base)
    if not self.speed then
        return base
    end
    return self.speed(mob, base)
end

-- called from Char.add before any buff attach: return true when the script
-- handled the buff entirely (e.g. elemental Burning-heal/Frost-damage
-- reactions) and the base attach must be skipped
mob.onAddBuff = function(self,mob,buff)
    if not self.addBuff then
        return false
    end
    return not not self.addBuff(mob, buff)
end

mob.onScoreItemAction = function(self, mob, item, action)
    if not self.scoreItemAction then
        return 0
    end
    return self.scoreItemAction(mob, item, action) or 0
end

mob.onDamage = function(self,mob,dmg,src)
    return not not (self.damage and self.damage(mob, dmg, src))
end

mob.onSpawn = function(self,mob,level)
    return not not (self.spawn and self.spawn(mob,level))
end

mob.onDefenceProc = function(self,mob, enemy, damage)
    if not self.defenceProc then
        return damage
    end
    return self.defenceProc(mob, enemy, damage)
end

mob.onAttackProc = function(self,mob, enemy, damage)
    if not self.attackProc then
        return damage
    end
    return self.attackProc(mob, enemy, damage)
end

mob.onZapProc = function(self,mob, enemy, damage)
    if not self.zapProc then
        return damage
    end
    return self.zapProc(mob, enemy, damage)
end

-- called from CustomMob.notice (after the sprite alert): boss intro yells etc.
mob.onNotice = function(self,mob)
    if not self.notice then
        return
    end
    return self.notice(mob)
end

-- called from CustomMob.canAttack: return true/false to replace the
-- range+LOS check entirely (ray attacks, pumped Goo reach), nil to keep it
mob.onCanAttack = function(self,mob,enemy)
    if not self.canAttack then
        return nil
    end
    return not not self.canAttack(mob, enemy)
end

-- called from CustomMob.doAttack before the base attack: return true when
-- the script took the attack (incl. its own spend), false/nil for the regular one
mob.onDoAttack = function(self,mob,enemy)
    if not self.doAttack then
        return false
    end
    return not not self.doAttack(mob, enemy)
end

-- nil keeps the java roll from the json dmg range
mob.onAttackSkill = function(self,mob,target)
    if not self.attackSkill then
        return nil
    end
    return self.attackSkill(mob, target)
end

mob.onDefenseSkill = function(self,mob,enemy)
    if not self.defenseSkill then
        return nil
    end
    return self.defenseSkill(mob, enemy)
end

mob.onDr = function(self,mob)
    if not self.dr then
        return nil
    end
    return self.dr(mob)
end

mob.onDamageRoll = function(self,mob)
    if not self.damageRoll then
        return nil
    end
    return self.damageRoll(mob)
end

-- called from CustomMob.add for npc-profile mobs before the blanket buff
-- veto: return true to let this buff attach anyway (RatKing when angered)
mob.onAllowBuff = function(self,mob,buff)
    if not self.allowBuff then
        return false
    end
    return not not self.allowBuff(mob, buff)
end

-- called from CustomMob.damage before the base flow: return true when the
-- script consumed the hit entirely (RatKing anger gate) and no hp is lost
mob.onBlockDamage = function(self,mob,dmg,src)
    if not self.blockDamage then
        return false
    end
    return not not self.blockDamage(mob, dmg, src)
end


mob.onZapMiss = function(self,mob, enemy)
    if not self.zapMiss then
        return
    end
    return self.zapMiss(mob, enemy)
end


mob.fillStats = function(self,mob)
    knownMobs[mob] = self
    return not not (self.stats and self.stats(mob))
end

mob.onSelectCell = function(self, mob)
    return not not (self.selectCell and self.selectCell(mob))
end

mob.actionsList = function(self, mob, hero)
    if not self.actions then
        return {}
    end
    return self.actions(mob, hero)
end

mob.executeAction = function(self, mob, hero, action)
    if not self.execute then
        return
    end
    return self.execute(mob, hero, action)
end

mob.priceSell = function(self, mob, item, defaultPrice)
    if not self.priceForSell then
        return defaultPrice
    end
    return self.priceForSell(mob, item)
end

mob.priceBuy = function(self, mob, item, defaultPrice)
    if not self.priceForBuy then
        return defaultPrice
    end
    return self.priceForBuy(mob, item)
end


mob._buyMode = function(self, mob, chr, defaultMode)
    if not self.buyMode then
        return defaultMode
    end
    return self.buyMode(mob, chr)
end

mob._sellMode = function(self, mob, chr, defaultMode)
    if not self.sellMode then
        return defaultMode
    end
    return self.sellMode(mob, chr)
end


return mob
