local RPD = require "scripts/lib/commonClasses"

local spell = require "scripts/lib/spell"

return spell.init{
    desc  = function ()
        return {
            image         = 1,
            imageFile     = "spellsicons/plaguedoctor.png",
            name          = "CorpseExplosion_Name",
            info          = "CorpseExplosion_Info",
            magicAffinity = "PlagueDoctor",
            targetingType = "cell",
            level         = 3,
            castTime      = 2,
            spellCost     = 10,
            cooldown      = 12
        }
    end,

    castOnCell = function(self, spell, caster, cell)
        local level = RPD.Dungeon.level
        local heap = level:getHeap(cell)
        
        if heap and heap:isCarcass() then
            -- Consume the corpse
            heap:pickUp()
            
            -- Get all chars in the explosion radius (3x3 area)
            local affectedChars = {}
            RPD.forCellsAround(cell, function(targetCell)
                local target = RPD.Actor:findChar(targetCell)
                if target and target ~= caster then
                    table.insert(affectedChars, target)
                end
            end)
            
            -- Apply poison damage to all affected chars
            for _, target in ipairs(affectedChars) do
                local damage = math.random(caster:skillLevel() * 2, caster:skillLevel() * 5)
                
                -- Apply poison effect
                RPD.affectBuff(target, "Poison", 5 + caster:skillLevel()):setSource(caster)
                
                -- Deal damage
                target:damage(damage, caster)
                
                -- Visual effect
                target:getSprite():emitter():burst(RPD.Sfx.PoisonParticle.SPLASH, 6)
            end
            
            -- Visual explosion effect
            RPD.Sfx.CellEmitter:center(cell):burst(RPD.Sfx.PoisonParticle.SPLASH, 15)
            
            -- Sound effect
            RPD.playSound("snd_blast.mp3")
            
            return true
        else
            RPD.glogn("CorpseExplosion_NoCorpse")
            return false
        end
    end
}