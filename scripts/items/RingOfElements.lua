---
--- RingOfElements: random-gated full resistance set + the shared control-buff
--- duration scaler consumed kind-side via CharUtils.durationFactor
--- (was rings/RingOfElements.java).
---
local ring = require "scripts/lib/ring"

return ring.init{
    desc = function()
        return {
            buff = "Resistance",
            price = 80,
        }
    end,
    resistances = ring.elementsResistances,
    durationFactor = ring.elementsDurationFactor,
}
