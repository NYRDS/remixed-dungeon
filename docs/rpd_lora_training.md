# Training a "Remixed Dungeon style" LoRA

Plan + state for training a style LoRA on the game's own pixel art, so
generated images (icons, promo, banners) inherit the game's flat colors,
bold outlines and chunky pixel grid instead of drifting amber/grey.
Started 2026-11-09 after the AI icon experiments (see the icon-pipeline
notes) kept drifting from the family style.

All work lives in `/media/mike/hdd_maxi1/play/ai/rpd_lora/` (local disk;
ComfyUI + RTX 4090 run on this same box).

## Two tracks

| | Track A: z_image (fast) | Track B: qwen-image-2.1 (main) |
|---|---|---|
| Base | `z_image_turbo_bf16` (6B, local) | `Qwen/Qwen-Image-2.1` (31GB, **downloaded 2026-11-10**) |
| TE / VAE | `qwen_3_4b` / flux `ae` (local) | `qwen3vl_8b` / 64ch **RGBA** VAE (in the download) |
| Trainer | musubi-tuner (`zimage_train_network.py`) | ai-toolkit (explicit 2.1 support) |
| Why | all pieces local, training tonight | native transparency + the engine we used for icon probes |
| Status | training 2026-11-10 (see below) | chained to auto-start after Track A |

Track A first (zero download), Track B when the base lands. The z-image
Turbo model is distilled - musubi's docs call turbo-direct training
unstable; conservative LR + the fallback `zimage_turbo_training_adapter_v2`
(ostris, 324MB, `--base_weights`) if the first samples smear.

**2026-11-10 run state:** Track A venv was missing musubi's own deps
(watchdog only installed ai-toolkit's): added `voluptuous` + `easydict`,
upgraded `lycoris-lora` 1.8.3→4.1.3 (musubi needs
`create_network_from_weights`, added in 3.x). `datasets` is NOT in
requirements_base.txt and unused by this train - do not install it (its
pyarrow pin drags a numpy 2 upgrade; numpy lives in the shared
symlinked stack). `run_train.sh` venv path fixed to `rpd_lora/venv`.

**Track A DONE 2026-11-10 03:00:** 1600 steps, 1.5it/s, loss 0.10→0.07,
~20 min wall. Progression: base model painterly-soft amber → family
golds + bold outlines by 800 → **grid-aligned pixel look locks in at
1200** (`output/zimg_rpd/progress_all.png`). Converted LoRA:
`output/zimg_rpd/rpd_style_zimg_comfy.safetensors`. CAVEAT found in the
16px acceptance check (`final_vs_ref.png`): hand-drawn `priest.png`
icons use a light idiom (1px outline, pale fills), and the generated
512px icons drift half-blocks off the 32px grid → BOX ÷32 downscale
aliases to mush. 512px style direction right; 16px-clean needs grid
snap (post-process quantize-to-32px-grid, or more steps) - z_image is
the dev track, judge the real fix on track B.

**Track B fixes before it trained (4 failures):** hub got bumped to
2.4.0 (lycoris 4.x side effect) breaking transformers 5.5.3 → pin back
`huggingface_hub==1.23.0`; yaml `arch: "qwen_image"` (original) →
**`arch: "qwen_image_2"`** (ai-toolkit id, class `QwenImage2Model`);
then meta-tensor crash in `quantize_module`: the vendored
`QwenImage21Transformer2DModel` natively uses comfy's FUSED
`img_mlp.gate_up`, the HF diffusers snapshot ships split
`gate_layer`/`proj`, and `convert_state_dict_on_load` (which fuses
them) only runs on the single-file path - directory loads went through
diffusers `from_pretrained`, leaving 32 `gate_up` linears on meta and
silently dropping the keys. PATCHED (local, in
`ai-toolkit/extensions_built_in/diffusion_models/qwen_image_2/src/transformer.py`):
override `aitk_from_pretrained` to route local-dir shard loads through
`load_from_state_dict`. Verified 0 meta params + fused [24576,4096]
bf16. Note: ai-toolkit's diffusers pin `c9438378` = the PR-13432
tarball we already installed (identical sha256); no native
`QwenImage21` in diffusers - the vendored class is the only support.
Training 2000 steps @1.4it/s in `trainB4.log`, samples every 250 to
`output/rpd_style_v1/samples/`.

## Dataset

202 captioned sprites, 16px game art upscaled x32 NEAREST onto white,
512x512 (`rpd_lora/dataset/`):

- `items.png` all tiles - captions from `ItemSpriteSheet.java` constants
  (nearest named anchor at-or-below the tile index: "a boomerang game
  item icon, ...")
- `spellsIcons/*` all frames - spell names resolved from each spell
  script's `name` strings key through `values/strings_all.xml`
- `levelObjects/candle.png` frames (rites prop)

Caption template: `{name} game item icon, remixed dungeon pixel art
style, chunky pixels, flat solid colors, bold dark outline, white
background`. No trigger word - the style words are the binding.

Builder: `/tmp/priest_icons/build_dataset.py` (transient path - recreate
from this doc if wiped). Extension ideas: mob sprites via
`tools/sprite_tool.py` (grids come from `spritesDesc/*.json`, NOT the
png), tilesets, more levelObjects - only if v1 samples look starved.

## Dataset v1.1 (2026-11-10 full review, Mike-approved rules)

**187 images, TRANSPARENT RGBA** (Mike 2026-11-10: "let make it on
transparent background") - builder
`/tmp/priest_icons/build_dataset_v11.py` (v1 builder retired):

- **items (109):** only tiles referenced by an item class. Names are
  EXACT via item classes, with one overriding rule (Mike): **the four
  run-randomized categories are captioned by APPEARANCE ONLY** - the
  appearance<->function mapping is reshuffled every run, so function
  names would teach a false binding:
  potions by color (Potion_Colors array: turquoise, crimson, azure,
  jade, ... - the game's own color naming for potions.png tiles
  32+i), wands by wood (WAND_HOLLY..WAND_BIRCH; the shared
  WAND_MAGIC_MISSILE tile = "wooden wand"), scrolls by rune
  (SCROLL_KAUNAN -> "kaunan rune scroll"; 16 runes + blank), rings by
  gem (RING_DIAMOND -> "diamond ring", 12 gems). RING_OF_STONE_WALKING
  (distinct tile 133, no gem name) dropped. Multi-claimant constants
  resolve to the class matching the constant; render-borrowers
  (Heap.java borrowing CHEST/TOMB sprites) lose; borrower-only claims
  name the tile from the constant (crystal chest, tomb).
- **other item sheets (50):** bindings inherited from base classes
  (Armor.java holds `imageFile = "items/armor.png"`, subclasses hold
  `image = N`) + lua items: 23 armor tiers (armor.png is ONE ROW 640x16
  - tile x = tile*16, the 16-col grid math drops half of them!), 6
  darts, 4 shields, 4 fish, hooked dagger, kusarigama, gnoll tomahawk,
  tengu liver, toxic gland, rotten organ, bone shard, bone saw, dew
  vial, chaos shield, plague doctor mask + 2 items.png strays (vile
  essence, second pickaxe). Darts/armor are NOT run-randomized -
  function names safe.
- **spells (26):** only frames bound by scripts/spells/*.lua with the
  script's `name` resolved via strings_all.xml. **Deprecated/32px
  sheets and the priest.png draft set are EXCLUDED (Mike: priest icons
  are extra-low-quality drafts).**
- **candle (2):** first two levelObjects frames.
- **alpha binarized at 128** before white-composite - v1's
  white-compositing of semi-alpha sprites (128/131 item images had
  soft-alpha px; elemental_all frame 5 = 587 = a full grey aura) baked
  grey/pale halo blocks into the style; the trained model painted
  halos behind icons. All sources NEAREST x32 onto white 512px.
- **captions carry the visual content** (Mike rule): game name + what
  is actually on the sprite, sometimes metaphorical - e.g. "a bones
  game item icon, a human skull resting on a sprawled skeletal arm,
  like a macabre coat of arms", "a lloyds beacon game item icon, a
  battered brown field radio with an antenna and glowing dials". Map:
  `/tmp/priest_icons/visual_desc.json` (transient; regenerate from
  zoomed contact sheets), builder composes
  `<name> <icon-kind>, <visual>, <style tail>`.
- **transparent background**: main `dataset/` = RGBA masters with true
  alpha (no compositing; qwen-2.1's VAE is RGBA-native, enabled by
  `model_kwargs: {rgba: true}` in the yaml - the dataloader keeps alpha
  end-to-end and samples decode with it). Caption tail says
  "transparent background". `dataset_white/` = binarized white
  composites for the RGB-only z_image track (its toml points there).
- caption tail is neutral ("chunky pixels, flat solid colors,
  transparent background") - no "bold dark outline" claim (the family mixes bold
  items and pale spell idioms; the claim made the model average them).
- Tamahawk.java's class spelling corrected to "tomahawk" in captions.

## Track A results (v1 dataset)

1600 steps done, ~20 min. Base z_image turbo is painterly-soft amber;
family golds + bold outlines arrive by 800, **grid-aligned pixel look
locks in at 1200** (`output/zimg_rpd/progress_all.png`). LoRA:
`output/zimg_rpd/rpd_style_zimg_comfy.safetensors`. Caveat from the
16px check: generated blocks drift half-steps off the 32px grid so
BOX/32 downscale aliases - the hand-drawn priest.png icons (light
idiom) are crisper at 16px. z_image = dev track; the real fix rides
on track B.

## Track B results (v1 dataset)

2000 steps, 1.4it/s, ~25 min. Base qwen-2.1 already renders clean
grid-aligned pixel icons; LoRA peak is **steps 250-500** (family
golds + bold outlines), then overfit: spellsIcons' soft-edge pixels
bled in as halos (grey blob behind the candle, pale glow around the
flask) and the two dataset idioms blended (pale cross). Sheets:
`output/rpd_style_v1_OLDV1DS/progress_b_full.png` +
`b16_check.png` (16px-per-checkpoint comparison vs priest.png).

**ai-toolkit auto-resume trap:** relaunching a job whose
`output/<name>/` still holds `<name>.safetensors` + `optimizer.pt`
loads them and resumes at 2000/2000 - exits 0 in minutes with NO
training. Archive/rename the output dir when the dataset changes
(bit us on the v1.1 relaunch).

## Retrain results (v1.1 dataset, 2026-11-10 evening)

Both tracks retrained clean. Track A (`progress_v11.png`): same style
arc, **no grey halos**, still needs a 32px grid-snap for true 16px use.
Track B (`progress_v11_b.png`): overfit cliff GONE - family golds +
bold outline hold through step 2000, no idiom blend, no halo; minor
stray dark outline bits appear 1250+. **16px BOX downscale reads
crisp** (`b16_check_v11.png`): cross/candle/sun all legible with dark
outline and family colors. Track B is the deliverable; checkpoints
250-1000 all usable, final at
`output/rpd_style_v1/rpd_style_v1.safetensors`.

## Track A recipe (z_image, musubi-tuner)

```
accelerate launch --mixed_precision bf16 src/musubi_tuner/zimage_train_network.py \
  --dit .../models/diffusion_models/z_image_turbo_bf16.safetensors \
  --vae .../models/vae/ae.safetensors \
  --text_encoder .../models/text_encoders/qwen_3_4b.safetensors \
  --fp8_base --fp8_scaled --fp8_llm --gradient_checkpointing \
  --timestep_sampling shift --weighting_scheme none --discrete_flow_shift 2.0 \
  --optimizer_type adamw8bit --learning_rate 5e-5 \
  --network_module networks.lora_zimage --network_dim 32 \
  --max_train_steps 1600 --save_every_n_steps 400 --seed 42 \
  --sample_prompts zimg_samples.txt --sample_every_n_steps 400 \
  --output_dir output/zimg_rpd --output_name rpd_style_zimg
```

- dataset toml: `zimg_dataset.toml` (num_repeats 8 -> 1600 steps = 1 epoch)
- lr 5e-5 = turbo-conservative (doc example 1e-4 assumes the base model)
- caches: `zimage_cache_latents.py` + `zimage_cache_text_encoder_outputs.py`
- ComfyUI must be stopped for training (holds 16GB VRAM); relaunch line:
  `cd .../play/ComfyUI && setsid nohup ./.venv/bin/python main.py --lowvram
  --listen <ygg-addr> --port 8188`
- LoRA -> ComfyUI: `networks/convert_lora.py --input ... --output ...
  --target other`

## Track B recipe (qwen-image-2.1, ai-toolkit)

Config `rpd_style_v1.yaml`: rank 16, lr 1e-4, 2000 steps, bs 1,
`cache_text_embeddings: true`, `quantize: true` + `qtype_te: qfloat8`,
`low_vram: true` (the 24GB example recipe), resolution [512] only,
same 4 icon sample prompts every 250 steps. Local base path
(`base/Qwen-Image-2.1/`, diffusers layout from HF).

## Evaluation

Every 400 steps the sample prompts render the 4 priest icons (cross /
candle / sun / flask). Accept when: family golds instead of amber, dark
outline instead of maroon-grey mush, grid-aligned pixel look, and the
icon reads at 16px after BOX downscale. Compare against the hand-drawn
set in `spellsIcons/priest.png`. Then produce the real icon set through
the existing ref-based pipeline (fibo/qwen edit with our icons as refs)
and check set consistency.

## Gotchas learned while setting this up

- The proxy (192.168.55.75:3128) is injected into every shell - unset
  all proxy vars for direct fetches; pip needs `PIP_CONFIG_FILE=/dev/null`
  (the global pip.conf carries a dead NGC extra-index that stalls it).
- The pipe throttles hard in the evening (~550KB/s, sometimes 66KB/s);
  `aria2c -x8 -s8 -c --file-allocation=none` is the only downloader that
  survives big HF files (plain requests/xet/curl all stall ~500MB).
  `curl -4 -s speed.cloudflare.com/__down?bytes=N` = ground truth for
  "is it the pipe or the route".
- pip installing a **git-pinned diffusers** stalls forever (internal
  clone, no retry) - fetch the commit tarball from codeload.github.com
  and install locally, then neutralize the pin line.
- `pkill/pgrep -f` self-match killed the working shell five times - the
  pattern string appears in your own cmdline. Use exact pids from a
  separate command.
- hf_transfer is deprecated (hub >= 1.x uses Xet; `HF_XET_HIGH_PERFORMANCE`
  is the new knob).
- ComfyUI's venv already holds torch+nvidia+triton (~8GB) - symlink
  those site-packages dirs into a fresh training venv instead of
  downloading torch again on the throttled pipe.
