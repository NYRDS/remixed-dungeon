-- caveman: PalaceServants ScriptedActor. Manages neutral→hostile faction behavior
-- for Monk/Warlock/Golem palace servants. Unarmed hero passes peacefully; armed hero
-- or any damage (hero, pet, env) triggers faction-wide hostility.
-- cellClicked on a neutral servant shows a phrase (no swap).

local RPD = require "scripts/lib/commonClasses"
local util = require "scripts/lib/util"
local actor = require "scripts/lib/actor"

local Fraction = luajava.bindClass("com.watabou.pixeldungeon.actors.mobs.Fraction")

local truceBroken = false  -- caveman: one-shot. once true, never re-checks.

-- caveman: servant entity kinds we manage.
local servantKinds = { Monk = true, Warlock = true, Golem = true }

-- caveman: phrases per mob type (string resource keys).
local phrases = {
    Monk    = { "PalaceServant_Monk_1", "PalaceServant_Monk_2", "PalaceServant_Monk_3" },
    Warlock = { "PalaceServant_Warlock_1", "PalaceServant_Warlock_2", "PalaceServant_Warlock_3" },
    Golem   = { "PalaceServant_Golem_1", "PalaceServant_Golem_2", "PalaceServant_Golem_3" },
}

-- caveman: flip ALL neutral servants to hostile (DUNGEON + Hunting).
local function flipAllServants()
    if truceBroken then return end
    truceBroken = true
    local level = RPD.Dungeon.level
    local iter = level.mobs:iterator()
    while iter:hasNext() do
        local m = iter:next()
        if servantKinds[m:getEntityKind()] and m:friendly(RPD.Dungeon.hero) then
            m:setFraction(Fraction.DUNGEON)
            RPD.setAi(m, "Hunting")
        end
    end
    RPD.glog("The palace servants turn on you!")
end

return actor.init({
    act = function()
        return true
    end,

    onStep = function()
        if truceBroken then return end

        local level = RPD.Dungeon.level

        -- caveman: servants turn hostile ONLY if one took damage (hero, pet, env).
        -- Being armed does NOT automatically trigger hostility — only violence does.
        -- (The armed/unarmed distinction controls chess vs boss fight, not servant mood.)
        local iter = level.mobs:iterator()
        while iter:hasNext() do
            local m = iter:next()
            local kind = m:getEntityKind()
            if servantKinds[kind] and m:friendly(RPD.Dungeon.hero) then
                if m:hp() < m:ht() then
                    flipAllServants()
                    return
                end
            end
        end
    end,

    cellClicked = function(cell)
        if truceBroken then return false end  -- caveman: hostile servants = normal combat

        local mob = RPD.Actor:findChar(cell)
        if not mob then return false end

        local kind = mob:getEntityKind()
        if not servantKinds[kind] then return false end
        if not mob:friendly(RPD.Dungeon.hero) then return false end  -- already hostile

        -- caveman: show a random phrase for this servant type.
        -- say() resolves the string resource id by itself (same as other npc scripts).
        local plist = phrases[kind]
        if plist then
            mob:say(plist[math.random(1, #plist)])
        end

        return true  -- caveman: suppress default swap — servant stays put.
    end,

    actionTime = function()
        return 0.1
    end,
})
