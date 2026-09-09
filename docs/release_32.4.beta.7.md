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

🐾 Питомцы и миньоны — большая серия правок по вашей бете-обратной связи:

- 🚪 Двери снова закрываются за вами. Хлам и трупы по-прежнему могут подпирать дверь — это тактика, оставили специально.
- 🗺️ Карта больше не «подсвечивается» навсегда после спуска — питомцы не прожигают туман войны там, куда вы не видите.
- 👥 Питомцы и статуи больше не дублируются при переходах между этажами и перезагрузках.
- 🕷️ Взрывающиеся пауки и лунная трава больше не клонируют ваших питомцев в бесплатную армию — вражеский удар оставляет одичавшую копию, которая НЕ на вашей стороне.
- 💪 Сила питомцев сохраняется в сейвах, а тяжёлая броня теперь замедляет любого носителя — питомцев в том числе.
- 📋 Приказы не сбрасываются от горения, газа и ловушек — урон со временем больше их не отменяет.
- 🛡️ Новый приказ «стой здесь»: прикажите питомцу на его же клетку — он охраняет пост, атакует врагов в пределах досягаемости и возвращается.
- 👣 Приказ на вашу клетку теперь означает «за мной», а не «атакуй игрока».
- 💀 Смерти питомцев объявляются в логе — больше не пропадают бесследно.
- ⚔️ Питомцы дерутся в полную силу, даже когда вы не видите бой.
- 🤝 Питомцы всегда дружелюбны к вам — игру больше не предложить «обокрасть собственного питомца».

Плюс: окна питомцев влезают в любой экран и прокручиваются, десктопное окно работает в альбомном режиме, сила мобов переведена на явные значения (лёгкий ребаланс), новые переводы: арабский, иврит, голландский, вьетнамский.

Как обычно — это бета: старые сейвы работают, но странные питомцы из старых сейвов могут пропасть. Сообщайте о странностях.

## Promo picture — txt2img

Concept: the release is about the hero commanding a loyal monster squad. Show the
squad holding a dungeon doorway — torch light, one feral clone silhouette in the
dark for the "no more free minion clones" fix.

### Main prompt (SDXL / generic)

```
pixel art game promo illustration, 16-bit retro style, a cloaked dungeon hero
commanding a squad of three loyal pet monsters — a giant rat, a small stone
golem, an armored skeleton — holding a heavy wooden dungeon door, warm torchlight
on stone walls, glowing blue magic runes, ominous shapes lurking in the dark
corridor beyond, dramatic rim lighting, teal shadows and amber highlights,
crisp pixel clusters, subtle dithering, high detail, centered composition with
clear empty space at the top for a logo, no text, no watermark
```

### Negative prompt

```
blurry, smooth gradients, 3d render, photorealistic, antialiasing, text,
letters, numbers, logo, watermark, signature, jpeg artifacts, deformed hands,
extra limbs, oversaturated
```

### Params / variants

- Aspect: 1024×500 landscape (Play feature graphic / channel banner); square
  1024×1024 crop for posts — keep the squad centered, logo space on top.
- Steps 30–40, CFG 6–7, pixel-art LoRA if available; upscale 2× then downscale
  to keep hard pixel edges.
- Midjourney one-liner: `pixel art, 16-bit, dungeon hero with a squad of pet
  monsters holding a torchlit door, teal and amber palette, logo space on top
  --ar 2:1 --style raw`
- Alt concept: same squad seen from behind marching down a corridor, one pet
  dragging loot, doors closing behind them.
