--
-- User: mike
-- Date: 28.05.2018
-- Time: 22:35
-- This file is part of Remixed Pixel Dungeon.
--

local spell = {}

spell.__index = spell

function spell.castOnCell(self, spell, chr, cell)
    return true
end

function spell.cast(self, spell, chr)
    return true
end

function spell.defaultDesc()
    return {
        image         = 0,
        imageFile     = "spellsIcons/common.png",
        name          = "custom spell",
        info          = "unconfigured custom spell",
        magicAffinity = "Common",
        targetingType = "self",
        cooldown      = 10,
        spellCost     = 5,
        level         = 1,
        castTime      = 1
    }
end

function spell.desc(self, spell)
    return spell.defaultDesc()
end

function spell.spellDesc(self)
    local ret = spell.defaultDesc()
    local own = self:desc()

    for k,v in pairs(ret) do
        ret[k] = own[k] or v
    end

    return ret
end


spell.init = function(desc)
    setmetatable(desc, spell)

    return desc
end

return spell