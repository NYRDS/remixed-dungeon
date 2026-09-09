# 32.4.beta.7 — pets and minions round

Announce draft + promo picture txt2img prompt.
Source: beta-6 tester feedback round (see MINION_FEEDBACK_BETA6_FINDINGS.md).

## Announce (EN)

🐾 Pets & minions — the big round, straight from your beta feedback:

- 🚪 Doors close behind you again. Heaps and carcasses can still wedge a door open — that's a tactic, kept on purpose.
- 🗺️ No more permanently lit map spots after descending — pets no longer punch holes into the fog of war where you can't see.
- 👥 No more duplicated pets/statues after level transitions and reloads.
- 🕷️ Exploding spiders and Moongrace no longer clone your pets into a free minion army — a hostile hit leaves a feral copy that is NOT on your side.
- 💪 Pet strength now persists in saves, and heavy armor slows any carrier — pets included.
- 📋 Standing orders survive burning, gas and traps — damage over time no longer cancels them.
- 🛡️ New "stay here" order: order a pet onto its own cell and it guards that post — engages enemies in reach, then returns.
- 👣 Order a pet onto your own cell — that now means "follow me", not "attack me".
- 💀 Pet deaths are announced in the log — no more silent losses.
- ⚔️ Pets fight at full strength even when you're not watching the fight.
- 🤝 Pets are always friendly to you — the game will no longer offer to steal from your own pet.

Also: pet windows now fit any screen and scroll properly, desktop window works in landscape, mob strength made explicit (minor rebalance), new translations: Arabic, Hebrew, Dutch, Vietnamese.

As usual — beta: old saves keep working, but strange pets from old saves may vanish. Report anything odd.

## Announce (RU)

🐾 Питомцы и миньоны — большая серия правок по вашим отзывам с беты:

- 🚪 Двери снова закрываются за вами. Хлам и трупы по-прежнему могут подпирать дверь — это тактика, мы оставили её специально.
- 🗺️ Карта больше не «подсвечивается» навсегда после спуска — питомцы больше не прожигают туман войны там, где вы не видите.
- 👥 Питомцы и статуи больше не дублируются при переходах между этажами и перезагрузках.
- 🕷️ Взрывающиеся пауки и благолунка больше не клонируют ваших питомцев в бесплатную армию — вражеский удар оставляет одичавшую копию, которая НЕ на вашей стороне.
- 💪 Сила питомцев сохраняется в сейвах, а тяжёлая броня теперь замедляет любого носителя — питомцев в том числе.
- 📋 Приказы больше не сбрасываются от огня, газа и ловушек — периодический урон их не отменяет.
- 🛡️ Новый приказ «стой здесь»: прикажите питомцу занять его же клетку — он встанет на пост, будет атаковать врагов в пределах досягаемости и возвращаться на место.
- 👣 Приказ на вашу клетку теперь означает «за мной», а не «атакуй игрока».
- 💀 О гибели питомцев теперь сообщается в журнале — они больше не исчезают бесследно.
- ⚔️ Питомцы сражаются в полную силу, даже когда вы не видите бой.
- 🤝 Питомцы всегда дружелюбны к вам — игра больше не предлагает обокрасть собственного питомца.

Плюс: окна питомцев умещаются на любом экране, длинные списки прокручиваются; десктопная версия корректно работает в альбомной ориентации; сила мобов переведена на явные значения (небольшой ребаланс); новые переводы: арабский, иврит, голландский, вьетнамский.

Как обычно, это бета: старые сейвы работают, но странные питомцы из старых сейвов могут пропасть. Сообщайте о странностях.

## Promo picture — txt2img (Qwen 3 / Qwen-Image)

Concept: the release is about the hero commanding a loyal monster squad. Show the
squad holding a dungeon doorway — torch light, one feral clone silhouette in the
dark for the "no more free minion clones" fix. Qwen-Image family takes long
structured natural-language prompts — use the full block below as-is.

### Main prompt

```
A 16-bit pixel art promotional banner for a retro dungeon crawler game, wide
landscape composition.

SCENE: a torchlit medieval dungeon corridor of grey-blue stone brickwork. On the
left third, a heavy arched wooden door with iron hinges stands ajar, warm amber
light spilling through the gap into the corridor. The corridor recedes into deep
teal darkness on the right.

CHARACTERS: in the center-right foreground, a cloaked hero in a hooded cloak
seen from a back three-quarter view, one hand raised in command. Around the
hero, a squad of three loyal monster companions facing the doorway: a giant
brown rat with alert red eyes, a compact moss-covered stone golem, and an
armored skeleton warrior holding a round wooden shield. Beyond the open door,
half-hidden in shadow, a feral snarling duplicate of the rat with glowing eyes —
clearly hostile, clearly not part of the squad.

LIGHTING: dramatic torchlight; warm amber and orange highlights on the
characters and brick texture, cool teal and dark blue shadows, strong rim light
along the hero's hood and shoulders, soft bloom around the torch flames, light
fog near the floor catching the torch glow.

PALETTE: limited saturated retro palette — teal, amber, warm brown, stone grey;
no pure black, no neon.

STYLE: crisp 16-bit pixel art, visible pixel clusters, deliberate dithering on
stone and in shadows, clean readable silhouettes, consistent pixel grid, sharp
edges, inspired by 1990s dungeon crawler key art.

COMPOSITION: squad centered slightly right, door on the left third, strong depth
from foreground characters to the dark corridor; the top quarter of the image is
dark uncluttered stone wall kept empty for logo placement.

CONSTRAINTS: no text, no letters, no numbers, no watermark, no signature, no
photorealism, no 3D render, no anti-aliasing, no smooth gradients.
```

### Negative prompt

```
blurry, lowres, jpeg artifacts, text, watermark, signature, logo, letters,
numbers, photorealistic, 3d render, smooth gradients, antialiasing, deformed
hands, extra limbs, oversaturated, neon colors
```

### Params (Qwen-Image / Qwen3-Image)

- Resolution: 1664×832 landscape (2:1) for the Play feature graphic / channel
  banner — crop to 1024×500; square 1328×1328 for feed posts (squad centered).
- Steps 30–40, CFG ~4.0 (Qwen-Image is tuned for low CFG — high values burn the
  pixel art), sampler euler; pixel-art LoRA optional, weight ≤0.6.
- Post-process: upscale 2× (nearest neighbor), then downscale — keeps pixel
  edges hard; do not use AI upscalers, they smooth pixels.
- Optional in-image text variant — Qwen-Image is best-in-class at text
  rendering, so this actually works: replace the CONSTRAINTS line with
  `CONSTRAINTS: no watermark, no signature; above the door, a small weathered
  stone plaque reads "32.4" in bold pixel font, amber on dark stone.`

### Short variant (quick iterations)

```
16-bit pixel art banner, dungeon hero in a hooded cloak commanding a squad of
three pet monsters (giant rat, stone golem, armored skeleton) holding a torchlit
wooden door, hostile feral rat duplicate lurking in the dark beyond, teal and
amber palette, dithering, crisp pixels, empty dark wall at the top for a logo,
no text, no watermark
```

### Alt concept

Same squad seen from behind marching down a torchlit corridor in single file,
one pet dragging a loot sack, heavy doors closing behind them; feral eyes
watching from a side passage.
