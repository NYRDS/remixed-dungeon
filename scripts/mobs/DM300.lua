--[[
  DM-300 - dwarven metabolian boss (batch 17c-2b, was actors/mobs/DM300.java).
  Leaks toxic gas every turn; eats a trap it steps on (below full hp) to
  heal, roaring; every step rains rocks - shake, particles, snd_rocks,
  deco-pocks on empty tiles - and stuns whoever stands next to the landing
  cell. isBoss json carries the die-flow and the SkeletonKey.
  The move hook fires mid-move (position still the old cell), so the
  trap/rock effects key off the `cell` argument like the java originals.
]]
local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

return mob.init{
    act = function(self)
        RPD.n(RPD.Blobs.ToxicGas, self:getPos(), 30)
    end,

    spawn = function(self, level)
        local data = mob.restoreData(self)
        if data.lootGranted then
            return
        end
        data.lootGranted = true

        if math.random() < 0.5 then
            self:loot(RPD.ItemFactory:itemByName("ChaosCrystal"), 0.333)
        else
            self:loot(RPD.ItemFactory:itemByName("RingOfThorns"):random(), 0.333)
        end
    end,

    move = function(self, cell)
        local level = RPD.Dungeon.level

        local object = level:getTopLevelObject(cell)
        if object ~= nil and object:isTrap() and self:hp() < self:ht() then
            object:reactivate("ToxicTrap", RPD.GameLoop:getDifficulty() + 1)

            local missing = self:ht() - self:hp()
            local heal = missing > 1 and math.random(1, missing - 1) or 1
            self:heal(heal, self, true)

            self:getSprite():emitter():burst(RPD.Sfx.ElmoParticle.FACTORY, 5)

            if RPD.CharUtils:isVisible(self) then
                RPD.glogn(RPD.textById("DM300_Info1"))
            end
        end

        local w = level:getWidth()
        local dirs = { -1, 1, -w, w, -w - 1, -w + 1, w - 1, w + 1 }
        local rockCell = cell + dirs[math.random(8)]

        if RPD.CharUtils:isVisible(self) then
            RPD.Sfx.CellEmitter:get(rockCell):start(
                RPD.Sfx.Speck:factory(RPD.Sfx.Speck.ROCK), 0.07, 10)
            RPD.shakeCamera(3, 0.7)
            RPD.playSound("snd_rocks")

            if level.water[rockCell + 1] then
                RPD.GameScene:ripple(rockCell)
            elseif level.map[rockCell + 1] == RPD.Terrain.EMPTY then
                level:set(rockCell, RPD.Terrain.EMPTY_DECO)
                RPD.GameScene:updateMap(rockCell)
            end
        end

        local ch = RPD.Actor:findChar(rockCell)
        if ch ~= nil and ch:getId() ~= self:getId() then
            RPD.Buffs.Buff:prolong(ch, "Stun", 2)
        end
    end,

    die = function(self, cause)
        RPD.CharUtils:validateBossSlain("BOSS_SLAIN_3")
        self:yell(RPD.textById("DM300_Info2"))
    end,

    notice = function(self)
        self:yell(RPD.textById("DM300_Info3"))
    end,
}
