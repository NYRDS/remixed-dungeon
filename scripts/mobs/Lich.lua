--[[
  Lich (batch 17d-3, was mobs/necropolis/Lich.java).
  Necropolis boss: jumps instead of walking while the enemy is visible
  (getCloser hook), and may jump right before an attack after taking a hit
  (defenceProc sets timeToJump, doAttack consumes it). Every 5 turns cycles
  the runic skulls: once, up to 4 skulls appear on the level pedestals
  (2 on easy difficulty, 4 on hard) and a live one is activated - RED heals
  the lich, BLUE summons hunting skeletons, GREEN vents toxic gas, PURPLE
  nullifies damage while active (defenceProc). Skulls are pure json - the
  lich drives them and keys each skull's variant by its cell (they never
  move). Killing the lich removes every non-pet mob on the level.
]]
local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"
local WandOfBlink = luajava.bindClass("com.watabou.pixeldungeon.items.wands.WandOfBlink")
local ShadowParticle = luajava.bindClass("com.watabou.pixeldungeon.effects.particles.ShadowParticle")

local SKULL_DELAY = 5

local RED, BLUE, GREEN, PURPLE = 0, 1, 2, 3

-- java Level.adjacent is a raw cell-diff check (rows can "wrap"); port it
-- verbatim instead of a proper chebyshev
local function adjacent(level, a, b)
    local diff = math.abs(a - b)
    local W = level:getWidth()
    return diff == 1 or diff == W or diff == W + 1 or diff == W - 1
end

local function skullsOnLevel()
    local res = {}
    for _, m in pairs(RPD.Dungeon.level:getMobs()) do
        if m:getEntityKind() == "RunicSkull" and m:isAlive() then
            res[#res + 1] = m
        end
    end
    return res
end

local function spawnSkulls(self, data)
    data.skullsSpawned = true
    data.variantByPos = data.variantByPos or {}

    local nSkulls = 3
    local difficulty = RPD.GameLoop:getDifficulty()
    if difficulty == 0 then
        nSkulls = 2
    elseif difficulty > 2 then
        nSkulls = 4
    end

    local pedestals = {}
    for _, obj in pairs(RPD.Dungeon.level:getLevelObjects()) do
        if obj:getEntityKind() == "pedestal" then
            pedestals[#pedestals + 1] = obj:getPos()
        end
    end

    RPD.playSound("snd_cursed")

    -- java shuffled the pedestals and handed makeNewSkull(i) to the i-th
    -- pick: variants ride the random spawn order
    for i = 0, nSkulls - 1 do
        if #pedestals == 0 then
            break
        end
        local pos = table.remove(pedestals, math.random(#pedestals))

        local skull = RPD.MobFactory:mobByName("RunicSkull")
        WandOfBlink:appear(skull, pos)
        data.variantByPos[pos] = i
        RPD.Sfx.CellEmitter:get(pos):burst(ShadowParticle.CURSE, 8)
    end
end

local function useSkull(self, data)
    self:getSprite():zap(self:getPos())

    local alive = #skullsOnLevel()
    local variant = data.variantByPos[data.activatedSkullPos]

    if variant == RED then
        -- java: PotionOfHealing.heal(this, 0.07f * skulls.size())
        self:heal(math.floor(self:ht() * 0.07 * alive), self)
        self:detachBuff("Poison")
        self:detachBuff("Cripple")
        self:detachBuff("Weakness")
        self:detachBuff("Bleeding")
    elseif variant == BLUE then
        for _ = 1, alive do
            local skeleton = RPD.CharUtils:spawnOnNextCell(self, "Skeleton", 999)
            if not skeleton:valid() then
                break
            end
            RPD.setAi(skeleton, "Hunting")
        end
    elseif variant == GREEN then
        RPD.placeBlob(RPD.Blobs.ToxicGas, self:getPos(), 30 * alive)
    end
end

local function jump(self, data)
    data.timeToJump = false

    local level = RPD.Dungeon.level
    local enemy = self:getEnemy()
    local enemyPos = enemy and enemy:getPos() or -1

    for _ = 1, 15 do
        local newPos = math.random(0, level:getLength() - 1)
        if level.fieldOfView[newPos + 1]
                and level.passable[newPos + 1]
                and not adjacent(level, self:getPos(), enemyPos)
                and RPD.Actor:findChar(newPos) == nil then

            self:move(newPos)
            self:spend(1 / self:speed())
            break
        end
    end
end

return mob.init{
    spawn = function(self, level)
        local heroClass = RPD.Dungeon.hero:getHeroClass():name()
        local bag = self:getBelongings()

        if not bag:getItem("SkeletonKey") then
            self:collect(RPD.item("SkeletonKey"))
        end

        if heroClass == "NECROMANCER" then
            if not bag:getItem("BlackSkullOfMastery") then
                self:collect(RPD.item("BlackSkullOfMastery"))
            end
        elseif not bag:getItem("BlackSkull") then
            self:collect(RPD.item("BlackSkull"))
        end
    end,

    act = function(self)
        local data = mob.restoreData(self)
        if data.timeToSkull == nil then
            data.timeToSkull = SKULL_DELAY
        end

        data.timeToSkull = data.timeToSkull - 1
        if data.timeToSkull < 0 then
            data.timeToSkull = SKULL_DELAY

            local skulls = skullsOnLevel()
            if #skulls == 0 and not data.skullsSpawned then
                spawnSkulls(self, data)
                skulls = skullsOnLevel()
            end

            if #skulls > 0 then
                data.activatedSkullPos = skulls[math.random(#skulls)]:getPos()
                useSkull(self, data)
            else
                data.activatedSkullPos = nil
            end
        end
    end,

    getCloser = function(self, target, ignorePets)
        local level = RPD.Dungeon.level
        if level.fieldOfView[target + 1] then
            -- refund doStepTo's pre-charge; jump() spends the real cost
            self:spend(-1 / self:speed())
            jump(self, mob.restoreData(self))
            return true
        end
        return nil
    end,

    doAttack = function(self, enemy)
        local data = mob.restoreData(self)
        if data.timeToJump then
            jump(self, data)
        end
        return nil
    end,

    defenceProc = function(self, enemy, damage)
        local data = mob.restoreData(self)
        if data.activatedSkullPos ~= nil
                and (data.variantByPos or {})[data.activatedSkullPos] == PURPLE then
            return 0
        end

        if math.random(2) == 1 then
            data.timeToJump = true
        end

        return damage
    end,

    die = function(self, cause)
        RPD.CharUtils:validateBossSlain("LICH_SLAIN")

        -- this hook runs before Char.die's destroy(): the lich is still in
        -- the level list, and remove() would quietly eat its own loot - so
        -- java's post-super.die wipe is ported with an explicit self skip
        for _, m in pairs(RPD.Dungeon.level:getMobs()) do
            if m ~= self and not m:isPet() then
                m:remove()
            end
        end
    end,
}
