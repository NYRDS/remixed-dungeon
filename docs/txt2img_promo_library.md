# txt2img promo library — game elements reference

Drop-in prompt blocks for announce and promo images (Qwen 3 / Qwen-Image).
Companion to the worked example in `release_32.4.beta.7.md` — same section
style, same params, same style vocabulary. Goal: any combination of blocks
below yields images that look like one campaign.

Sources for descriptions: game strings (`strings_all.xml`), hero/mob sprites
(`assets/hero*/`, `assets/spritesDesc/`), region tilesets. In-game English
names are used verbatim so promo art matches what players see in game.

## How to compose a prompt

1. Paste `STYLE`, `CONSTRAINTS` and the negative prompt verbatim (below).
2. Pick one `SCENE` block (Locations).
3. Pick `CAST` lines — one hero, 1–4 mobs / NPCs / props (Heroes / Mobs /
   Dungeon & Town NPCs / Elements).
4. Pick a `LIGHTING` preset and a `COMPOSITION` preset.
5. Params are fixed — see Params. For quick iteration use the short-variant
   trick: one sentence per block, comma-separated.

Never paraphrase the shared vocabulary — reusing the exact phrases is what
keeps the images consistent across releases.

## Shared blocks (paste verbatim)

### STYLE

```
STYLE: crisp 16-bit pixel art, visible pixel clusters, deliberate dithering on
stone and in shadows, clean readable silhouettes, consistent pixel grid, sharp
edges, inspired by 1990s dungeon crawler key art.
```

### CONSTRAINTS

```
CONSTRAINTS: no text, no letters, no numbers, no watermark, no signature, no
photorealism, no 3D render, no anti-aliasing, no smooth gradients.
```

For the in-image-text variant (Qwen-Image renders text well) replace with:
`CONSTRAINTS: no watermark, no signature; <text instruction here, bold pixel
font, amber on dark stone>`.

### Negative prompt

```
blurry, lowres, jpeg artifacts, text, watermark, signature, logo, letters,
numbers, photorealistic, 3d render, smooth gradients, antialiasing, deformed
hands, extra limbs, oversaturated, neon colors
```

### Params (Qwen-Image / Qwen3-Image)

- Resolution: 1664×832 (2:1 banner, crop to 1024×500 for Play), 1328×1328
  square for feed posts, 832×1216 portrait for story/reels format.
- Steps 30–40, CFG ~4.0 (higher burns the pixel art), sampler euler;
  pixel-art LoRA optional at weight ≤0.6.
- Post-process: upscale 2× nearest neighbor, then downscale — never AI
  upscalers, they smooth pixels.

## Lighting presets (pick one per image)

```
LIGHTING (torchlit — default): dramatic torchlight; warm amber and orange
highlights on characters and wall texture, cool teal and dark blue shadows,
strong rim light, soft bloom around flames, light floor fog catching the glow.
```

- `magic` — cool blue-green caster glow as the only strong light source, deep
  indigo shadows, faint glowing motes in the air.
- `ember` — red-orange underlight from lava cracks below, floating embers,
  black-red shadows, heat shimmer.
- `frost` — pale blue-white ambient light, breath mist, ice catching prismatic
  glints, long soft shadows.
- `ghostly` — desaturated green-grey light, wispy trails, light sources
  seemingly without a flame.
- `holy` — warm gold beams from high windows, dust motes in the light,
  soft and clean shadows.
- `heartbeat` — dim pulsing red light that brightens toward the walls' center,
  wet highlights on organic surfaces.

## Composition presets

- `banner` — main cast on one third, threat/door on the opposite third, depth
  toward the middle; top quarter dark, uncluttered, kept empty for logo.
- `square` — cast centered in a ring or line, scene filling the frame edges.
- `portrait` — single hero full body, slightly off-center, environment blurred
  into dithered backdrop.
- `boss-spotlight` — low angle from behind the hero's shoulder, boss looming
  large and lit from its own light source.

## Locations — SCENE blocks

### Town (hub)

```
SCENE: a cozy small fantasy town square in light snow, timber-and-plaster
houses with steep snow-capped roofs, a church spire, an inn with a glowing
sign, market stalls, hanging lanterns casting warm pools of light, the dark
opening of a cellar stairwell in the ground hinting at the dungeon below.
PALETTE: warm browns and cream plaster, snow white, lantern amber, one deep
blue twilight sky.
```

Props: inn hearth interior (holy preset), library interior with towering
shelves, fortune teller tent with a black cat, shop counter with potions.
Summer variant (tileset exists): swap snow for green grass and flower boxes.

### Sewers (depths 1–5)

```
SCENE: a dungeon sewer level of grey-green cobblestone and brick slick with
slime, dark still water pools reflecting torchlight, moss in wall joints, a
leaking wall crack trickling water, an iron-barred drain in the floor,
arched wooden doors with iron hinges.
PALETTE: stone grey, sewage green, teal water, moss accents; no pure black,
no neon.
```

### Prison (depths 6–10)

```
SCENE: an abandoned underground prison of olive stone ruins, rusted iron bars
and cell doors, hanging chains, crumbling narrow cells, shackles set in the
walls, drifting dust in torch beams.
PALETTE: olive drab, rusted brown, cold grey stone, dim amber torchlight.
```

### Caves (depths 11–15)

```
SCENE: rough natural caverns of brown rock, glittering ore veins in the
walls, patches of softly glowing fluorescent moss and mushrooms, freezing
pools of pale water, uneven rocky floor, a rickety wooden bridge over a
chasm.
PALETTE: earth brown, moss green, glinting gold ore, pale cyan glow.
```

### Dwarven Metropolis (depths 16–20)

```
SCENE: grand halls of white marble with emerald green trim, thick red carpets,
tall columns, heroic dwarf statues, high flower beds in bloom, ornate golden
doors, a throne hall with a giant chessboard floor.
PALETTE: marble white, emerald green, royal gold, deep carpet red.
```

### Demon Halls (depths 21–25)

```
SCENE: demon-haunted halls of dark red stone, hexagonal floor tiles veined
with glowing golden cracks of cold lava, dark red brick walls with gothic
windows and golden trim, demonic statues, ember-colored moss and fungi.
PALETTE: blood red, burnished gold, ember orange, cold lava black.
```

### The Guts (depths 26–30)

```
SCENE: a cavern inside a living god, walls of pulsing pink and red flesh
laced with veins, pools of weird slimy liquid, giant gnashing teeth growing
from the floor, fleshy tendrils, dried shrubs of sinew, a faint rhythmic glow
as if the whole place breathes.
PALETTE: flesh pink and deep red, bile green, bruised purple shadows.
```

### Spider Lair (side branch, ~depths 6–10)

```
SCENE: dungeon tunnels of packed earth and old brown brick laced with sheets
of white spider silk, web curtains in corners, hanging cocoons faintly
showing movement inside, egg clusters, roots breaking through the walls.
PALETTE: earth brown and rust orange, silk white, pale egg cream.
```

### Necropolis (side branch, ~depths 8–12)

```
SCENE: an ancient necropolis crypt of near-black stone, skulls glowing in
niches — green, orange, cyan, purple — a faint necromantic glow from deep
archways, cold blue mist and teal water over the floor.
PALETTE: crypt grey-black with necro-glow skull accents of green, orange,
cyan and purple; midnight blue mist.
```

### Ice Caves (side branch, ~depths 15–19)

```
SCENE: caverns of snow-capped deep blue ice bricks, jutting ice spikes and
stalagmites, frozen torches in their brackets, statues of gnolls caught
mid-run encased in ice, frost-covered wooden bridges, kobold machinery
half-buried in ice, crisp star-like glints.
PALETTE: glacier blue, white frost and snow, pale steel, warm accent torch
points.
```

### Shadow Lord realm (single arena)

```
SCENE: a flat arena of vivid purple brickwork with magenta accents, pillars
and statues around the edge, the floor cracking outward into an absolute
dark void beyond the walls, thin purple lightning in the blackness.
PALETTE: royal purple, magenta pink, one pale character highlight, true
dark only outside the arena.
```

### Portal room (safe, between regions)

```
SCENE: a small safe chamber of clean marble, a swirling ring portal of
blue-violet energy in an arch of carved stone, runes glowing along the arch,
three faded destination murals on the walls.
PALETTE: marble white, arcane violet-blue, gold rune accents.
```

### Final level (depth 32, true ending)

```
SCENE: the last hall of the Demon Halls, dark red stone veined with cold
lava, and on a raised carved pedestal the Amulet of Yendor radiating warm
golden light into the darkness.
PALETTE: blood red and cold lava black against one strong amulet-gold glow.
```

For the troll fake ending (depth 26) keep the same scene but swap in
`a tarnished fake amulet, paint peeling`.

### Flooded treasury (any region)

```
SCENE: a small stone vault flooded knee-deep with clear water, treasure
chests and gold heaps on a raised dry platform, coins glinting under the
water surface, rings spreading where something big moves beneath.
PALETTE: cold blue water, gold treasure glints, dark stone.
```

Populate with the giant piranha from Mobs → Specials.

## Heroes — CAST blocks

One hero per promo unless showing the lineup. Base form (no subclass) unless
stated; append a subclass tag to vary. Looks verified against the actual
layered sprites (`assets/hero_modern/`: body + head + hair + armor) — keep
hair and armor colors exactly as written.

- **Warrior** — `a stocky ginger-haired human warrior in a white cloth tunic with blue trim, unique short sword in hand, confident stance`; class-armor look: `dark steel plate with orange pauldrons, mid-heroic-leap`
- **Mage** — `an old human mage with white hair and a white beard, in bright blue robes, a knotted wooden wand tipped with glowing blue magic`
- **Rogue** — `a brown-haired human rogue under a dark hooded cloak, face half lost in shadow, daggers at the belt, a glinting shadowy ring on one hand`
- **Huntress** — `a wiry blonde human huntress, hair falling over one eye, in a teal leather suit, a carved wooden boomerang in hand`
- **Elf** — `a slender elf archer with brown hair and pointed ears, in a green hood and forest-green tunic, drawing a long elven wooden bow, a full quiver of arrows at the hip`
- **Necromancer** — `a pale dark-haired human necromancer in a black robe with purple trim, an oversized ornate skeleton key hanging at the belt, faint green wisps curling around the raised hand`
- **Gnoll** — `a hyena-headed gnoll hero with reddish-brown fur and a wicked grin, wearing only a leather strap and pouch, gripping a throwing tomahawk, no armor`
- **Priest** — `a balding priest in light grey and white vestments, serene expression, soft light around the hands`
- **Plague Doctor** — `a plague doctor in a long brown coat, wide-brimmed hat and white beaked mask, a bone saw in one gloved hand, glass vials on the belt`

### Subclass tags (append to the hero line)

- Gladiator: `wearing a bronze gladiator helmet, fighting with a curved gladius mid-combo`
- Berserker: `bare-armed and scarred, roaring with fury, weapon raised, veins highlighted in red`
- Battlemage: `robes worn over a chain shirt, bashing with the wand like a mace amid blue sparks`
- Warlock: `in dark purple robes, drawing in a glowing soul wisp from a slain foe`
- Freerunner: `in light running garb, sprinting full tilt, motion dither streaks behind`
- Assassin: `in tighter black garbs, reversed dagger, dropping from shadow`
- Sniper: `aiming a missile weapon, one eye glinting, a weak point highlighted on the target`
- Warden: `green cloak overgrown with living plants and vines, dewdrops glowing at the boots`
- Scout: `half-melted into high grass shadow, bow ready, only a silhouette`
- Shaman: `elven shaman with a wand-staff mid-cast, spirit energy coiling down the arm`
- Lich: `body become skeletal, tattered robes, pinpoints of green flame in the eye sockets, immense aura, weak frame`
- Witchdoctor: `gnoll in a bone mask, a shimmering mana shield sphere around the body`
- Guardian: `gnoll planted behind a tower shield, immovable as a bastion`

## Mobs — CAST blocks (by region)

In-game names in bold. Compose 1–4 per image; keep bosses alone with a hero.

**Pets:** any mob can become the hero's pet — that mechanic was the 32.4
round, expect it in promos. Take the mob's CAST line and append
`loyal, standing at the hero's side, no hostile pose`. Squads of 2–3 pets
read best (giant rat + stone golem + armored skeleton was the beta.7 banner).

Colors verified against the actual sprite sheets (`assets/*.png` legacy,
`assets/mobs/*.png` fork; frame 0 of the idle animation) — keep them as
written.

### Sewers

- **marsupial rat** — `a brown marsupial rat with alert red eyes, the size of a cat`
- **albino rat** — `a white albino rat with abnormally long sharp front teeth`
- **snail / deep snail** — `a giant snail with a tan-brown shell and a green slime trail` (deep snail: glossy dark shell, near water)
- **sewer crab** — `a huge coral-pink crab with a thick plated exoskeleton, claws wide`
- **swarm of flies** — `a buzzing black cloud of flies with glinting red eyes`
- **gnoll scout** — `a hyena-headed humanoid in a brown loincloth, carrying a spear`
- **crazy thief** — `a figure in a green hooded cloak, face lost in black shadow, dagger and loot sack`
- **fetid rat** — `an oversized grey-green rat wrapped in a green miasma cloud`
- **Goo (boss)** — `a huge amorphous blob of black-green gel, dripping, bubbles rising through its simple mocking face`

### Prison

- **skeleton** — `an animate skeleton warrior with rusted weapon and shield, joints loose`
- **gnoll shaman** — `a hyena-headed gnoll in red hooded garb, crackling battle spell in a raised paw, staff in hand`
- **shadow** — `a flitting black silhouette with two pale eyes, edges smoking`
- **vampire bat** — `a scruffy vampire bat baring fangs mid-flight`
- **crazy bandit** — `a ragged bandit with a gleaming poisoned dagger`
- **Tengu (boss)** — `a masked assassin in red-and-black ninja garb and a white long-nosed oni mask, shurikens fanned between clawed fingers, traps at his feet`

### Caves

- **gnoll brute** — `a huge muscled hyena-headed brute in a steel helm, mace in fist`
- **shielded brute** — `the same helm-wearing brute behind a gigantic full-body shield`
- **elder shaman** — `an ancient bent gnoll shaman with cyan face paint, wrapped in a shimmering reflective aura`
- **cave spinner** — `a greenish furry spider gnashing poison fangs, web strands at its legs`
- **fire elemental** — `a wandering figure of living flame, core white-hot, sparks trailing`
- **DM-300 (boss)** — `a colossal rounded machine of grey-bronze metal, glowing yellow eye lights, drill arms and heavy pistons, stones raining from the ceiling around it`

### Dwarven Metropolis

- **dwarf monk** — `a bald dwarf monk in orange-brown robes, bare fists raised, sleeves tied back`
- **senior monk** — `an older monk mid-teleport, afterimage of folded space`
- **dwarf warlock** — `a robed dwarf warlock with a skull-topped staff, eyes glowing`
- **golem** — `a massive stone golem with runes glowing through its rocky seams, earth-spirit light inside`
- **undead dwarf** — `a skeleton dwarf still in courtly robes, a dignified white beard on the skull` — the King's summon
- **King of Dwarves (boss)** — `a white-bearded dwarf king in red and gold royal dress, golden crown, arms raised, summoning a court of skeleton dwarves in courtly robes`

### Demon Halls

- **succubus** — `a blue-haired gothic demon girl, small horns and bat wings, charming smile, heart-shaped charm glowing`
- **evil eye** — `a floating purple orb with one huge bloodshot eye and trailing tentacles`
- **scorpio** — `a huge orange-amber arachnid demon firing serrated spikes from its curled tail`
- **acidic scorpio** — `the same demon in sickly green, acid dripping`
- **Yog-Dzewa (final boss)** — `an Old God of floating organs: a colossal bloodshot white-red eye hovering above a pink exposed brain, flanked by a giant fanged mouth and a beating heart, one burning fist and one rotting fist at its sides, reality dithering apart around it`

### The Guts

- **suspicious rat** — `a rat whose whole body spasms wrongly as it moves, shadow misshapen`
- **pseudo-rat** — `a red-fanged horror in a rat's place: an enormous white-toothed mouth making up most of its red body`
- **gnoll-zombie** — `a hunched rotten gnoll-zombie in tan fur and red rags, more meat pile than humanoid`
- **worm** — `a giant pale segmented worm surging half out of the fleshy floor, mandibles wide`
- **nightmare** — `a red flesh mass with one wide white eye, sprouting twitching tentacles`
- **spirit of pain** — `a small black wisp of living darkness with angry white eyes`
- **treacherous spirit** — `a cloud of living darkness with one gigantic pulsing eye`
- **god's larva** — `a pale translucent grub radiating wrong divinity`
- **mimic amulet** — `a golden amulet with a grinning face and lolling tongue — the trap dressed as the prize`

### Spider Lair

- **spider soldier** — `a tan-brown soldier spider with venom-green fangs, legs raised`
- **spider worker** — `a worker spider with a swollen orange abdomen, bloated like a living bomb`
- **psi-spider** — `a frail spider with a swollen purple brain-sac, psychic ripple in the air`
- **amber spider** — `a tough spider whose giant brain-sac glows amber as it strikes`
- **spider guard** — `a stocky soldier-caste spider with pale markings, paralyzing venom on its fangs`
- **spider elite guard** — `a reddish elite guard spider with pale white markings, disorienting venom`
- **spider cocoon / egg** — props: `a white silk cocoon with something alive wriggling inside`, `a fragile white egg sac with many legs visible within`
- **Spider Queen (boss)** — `a queen with a red-haired woman's torso and the body of a giant brown spider, court of soldiers below`

### Necropolis

- **zombie** — `a teal-green rotting adventurer zombie, grave clothes and rusted gear`
- **enslaved soul** — `a small dark-blue ghostly orb where a pained face flashes in and out`
- **exploding skull** — `a floating skull wrapped in trapped orange flame, cracking apart`
- **runic skull** — `a skull set in a stone plate, rune-eyes glowing red` (rare variant: blue)
- **death knight** — `a mighty knight in dark steel armor, cold blue eye-glow under the helm, dead grin`
- **dread knight** — `a broken ancient knight in rusted brown-black armor, burning red eye-glow, aura of torment`
- **Lich (boss)** — `a skeletal lich in a green robe, staff crowned with a purple soul orb, standing over a jar of glowing souls`

### Ice Caves

- **kobold** — `a squat brown-furred kobold under a dark engineer hood with round goggles, gadgets on the belt`
- **kobold icemancer** — `an elite kobold in a pale blue-grey hooded robe, zapping a bolt of ice`
- **cold spirit** — `a small spiky wisp of pale white-blue frost, trailing snow`
- **ice elemental** — `a jagged walking figure of clear ice, frost aura cracking the ground`
- **Ice Guardian (boss)** — `a tall automaton of pale green ice and metal, sword arm, reassembling itself from floating shards around one exposed energy core`

### Shadow Lord realm

- **shadow lord (boss)** — `a vast violet-black shadow in the shape of a jagged crystalline star, empty dark core, edges bleeding smoke`
- **maze shadow** — `a lesser purple shadow mimic with too many joints`

### Specials (any region)

- **mimic** — `a treasure chest mid-bite, teeth in the lid, tongue lolling over spilled gold`
- **wraith** — `a hooded vengeful ghost hovering over a disturbed grave`
- **animated statue** — `a grey stone statue mid-step, red glowing eyes, a very real sword in hand`
- **giant piranha** — `a huge carnivorous fish breaking the water surface, teeth first`
- **magic sheep** — `a fluffy white sheep chewing cud with a blank stare, faint magic sparkle`
- **bee** — `a small furious yellow-and-black bee, stinger first`
- **floating crystal** — `a big shining crystal hovering in the air, orange or purple, filled with magic light`
- **golden statue** — `a gilded stone statue mid-step, red glowing eyes, a very real sword in hand` (armored variant: `in rusted plate`)
- **deathling** — `a dark red spiky soul-orb, a fallen hero's spirit bound to serve its summoner`
- **air / water / earth elemental** — `a flying being of living wind`, `a dark teal blob of living water with a faint face`, `a grey clod of earth walking on stone limbs`
- **hedgehog** — `a small round pet hedgehog, spines bristling, snout up`

## Dungeon NPCs — CAST blocks

Friendly faces met inside the dungeon.

- **sad ghost** — `a barely visible shapeless spot of faint light with a sorrowful face, hovering at knee height` — quest giver
- **old wandmaker** — `a hale old gentleman in mage robes with a slightly confused expression, wands strapped across his back, a magic shield shimmer around him`
- **troll blacksmith** — `a tall lean troll whose stone-grey cracked skin resembles rock, tinkering at an anvil with comically small tools`
- **ambitious imp** — `a small imp demon with a merchant's grin, carrying a ledger and a pack of wares`
- **shopkeeper** — `a stout mustached shopkeeper behind a small counter set right in the dungeon, scales and price tags around`
- **rat king** — `a marsupial rat slightly bigger than usual, wearing a tiny green-gold crown, regal pose on a small treasure pile`
- **mirror image** — `a perfect dithered copy of the hero, edges shimmering like liquid glass`
- **caged kobold** — `a brown-furred kobold locked in an iron-barred cage, pleading eyes` — quest captive in the ice caves

## Town NPCs — CAST blocks

For town-square and interiors promos.

- **town shopkeeper** — `a rotund merchant in an apron behind a counter of potions, pouches and travel gear`
- **town guard** — `a town watchman in a green tabard and kettle helm, sword in hand`
- **townsfolk** — `plainly dressed medieval townsfolk going about their day, hoods and wool`
- **innkeeper** — `an aproned innkeeper polishing a frothy mug`
- **bard** — `a bard in purple garb strumming a lute`
- **drunkard** — `a wobbly drunkard in brown clothes clutching a bottle`
- **mercenary** — `a scarred mercenary in a mail shirt, sword at the belt`
- **servant** — `a servant girl with orange hair in a red dress`
- **healer** — `a gentle healer in white robes with a herb pouch, soft glow around the hands`
- **bishop** — `an old bishop in a tall golden mitre and purple-and-gold vestments, candlelit church behind`
- **librarian** — `a fussy librarian in spectacles among towering bookshelves`
- **fortune teller** — `a fortune teller in shawls and a headscarf over a round table of cards, a black cat at her side`
- **plague doctor (town)** — `a town plague doctor in a dark coat and beaked mask, carrying a basket of herbs`
- **scarecrow** — `a straw scarecrow in a patched coat, unnervingly lifelike`

## Elements — props and icons

- **Amulet of Yendor** — `a radiant golden amulet with a glowing gem, resting on a carved stone pedestal, light beams in the dark` — the macguffin; for the fake ending use `a tarnished fake amulet, paint peeling`
- **Potion** — `a small round glass bottle of brightly colored liquid, wax-sealed cork, glinting highlight`
- **Scroll** — `a rolled parchment scroll with a wax seal, faintly smoking runes`
- **Wand** — `a slender knotted wooden wand with a glowing tip`
- **Ring** — `a small golden ring with a cut gemstone`
- **Gold** — `a heap of gold coins with a few glinting items mixed in`
- **Torch** — `a wall torch in an iron bracket, amber flame, soft bloom`
- **Door** — `a heavy arched wooden door with iron hinges, slightly ajar` — canonical: prop it open with `a spilled heap of gear wedging the door open`, the players' favorite tactic
- **Dewdrop / high grass** — `glowing dewdrops on tall green grass tufts`
- **Portal** — `a swirling ring of blue-violet portal energy`
- **Grave** — `a leaning tombstone with fresh dirt`
- **Ladder down** — `a stone stairwell hatch down into darkness`
- **Skeleton key** — `an oversized ornate skeleton key, gold and bone`
- **Tome of Mastery** — `a worn thick leather-bound tome with a rune-embossed cover, faint sense of importance`
- **Black Skull of Mastery ("The Soulbringer")** — `an ancient black skull artifact wreathed in soul wisps, an arcane formula etched into the cranium`
- **Rat King's crown** — `a tiny green-gold crown the size of a thimble`
- **Ankh** — `a small golden ankh glowing with life-saving light`
- **Honeypot** — `a clay pot of golden honey, a fresh comb glowing inside`
- **Keys** — `a ring of iron and golden dungeon keys`
- **Ration / pasty** — `a paper-wrapped travel ration`, `a golden-brown baked pasty, steam rising`
- **Armor progression** — for armor-themed promos: `cloth tunic`, `leather cuirass`, `mail shirt`, `plate harness`, `embroidered mage robe`
- **Arrows / quiver** — `a full quiver of plain wooden arrows, fletching up`

## Worked composition — boss spotlight (template)

```
A 16-bit pixel art promotional image for a retro dungeon crawler game,
landscape.

SCENE: <location block>

CHARACTERS: <one hero block> seen from a back three-quarter view in the near
foreground, <boss block> dominating the far half of the frame.

LIGHTING: <preset>, boss's own light source carving it out of the dark.

PALETTE: <from the location block>.

STYLE / COMPOSITION / CONSTRAINTS: <shared blocks + composition preset>
```

Ideas bank for future announces: all-nine-heroes lineup on the town square
(square preset); three portals side by side — spider lair, necropolis, ice
caves — with a hero choosing ("choose your path" round); door wedged open by
a loot heap with wary pets behind (pets round); amulet pedestal in cold lava
(final stretch); town festival square with heroes among NPCs (release round);
sad ghost and a hero at a grave (quest round); the rat king crowning a pet
rat (light-hearted round); old wandmaker handing a wand to a young hero
(subclass round); flooded treasury with a piranha leap (loot round).
