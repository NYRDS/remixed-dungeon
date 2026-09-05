// Hero Configuration Data

export const HERO_CLASSES = ['WARRIOR', 'MAGE', 'ROGUE', 'HUNTRESS', 'ELF', 'NECROMANCER', 'GNOLL', 'PRIEST', 'DOCTOR'];

export const SUBCLASSES = {
    'WARRIOR': ['NONE', 'GLADIATOR', 'BERSERKER'],
    'MAGE': ['NONE', 'WARLOCK', 'BATTLEMAGE'],
    'ROGUE': ['NONE', 'ASSASSIN', 'FREERUNNER'],
    'HUNTRESS': ['NONE', 'SNIPER', 'WARDEN'],
    'ELF': ['NONE', 'SCOUT', 'SHAMAN'],
    'NECROMANCER': ['NONE', 'LICH'],
    'GNOLL': ['NONE', 'GUARDIAN', 'WITCHDOCTOR'],
    'PRIEST': ['NONE', 'CLERIC', 'PALADIN'],
    'DOCTOR': ['NONE', 'ALCHEMIST', 'TRANSMUTER']
};

export const ARMOR_LIST = ['none', 'cloth', 'leather', 'mail', 'scale', 'plate', 'gothic',
                          'rogue', 'warrior', 'mage', 'huntress', 'scout', 'shaman',
                          'gladiator', 'berserk', 'warlock', 'battlemage', 'assasin',
                          'freerunner', 'sniper', 'warden', 'necromancer', 'lich', 'necromancerRobe',
                          'gnoll', 'spider', 'rat', 'chaos', 'elf',
                          'priest', 'paladin', 'cleric', 'witchdoctor',
                          'alchemist', 'transmuter', 'plaguedoctor'];

export const WEAPON_LIST = ['none', 'shortsword', 'longsword', 'dagger', 'mace', 'hammer',
                           'sword', 'wand', 'bow', 'crossbow', 'spear',
                           'glaive', 'battleaxe', 'claymore', 'quarterstaff', 'knuckles',
                           'bonesaw', 'tomahawk', 'halberd', 'kusarigama', 'pickaxe',
                           'royalshield', 'chaosshield'];

export const ACCESSORY_LIST = ['none', 'plaguedoctormask', 'wizardhat', 'nightcap', 'ushanka',
                              'santahat', 'pumpkin', 'fez', 'shades', 'fullfacemask',
                              'pirateset', 'dogemask', 'nekoears', 'rabbitears', 'rudolph',
                              'vampireskull', 'krampushead', 'zombiemask', 'filteredmask',
                              'medicinemask', 'bowknot', 'capotain', 'chaoshelmet'];

// Body type mapping from hero_modern/spritesDesc/Hero.json "bodyType" (ModernHeroSpriteDef.bodyDescriptor)
export const BODY_TYPE_MAP = {
    'WARLOCK': 'warlock',
    'LICH': 'lich',
    'GNOLL': 'gnoll',
    'HUNTRESS': 'woman'
};

// Armor visual names as used by Armor.getVisualName() -> file names
export const ARMOR_MAP = {
    'cloth': 'ClothArmor',
    'leather': 'LeatherArmor',
    'mail': 'MailArmor',
    'scale': 'ScaleArmor',
    'plate': 'PlateArmor',
    'gothic': 'GothicArmor',
    'rogue': 'RogueArmor',
    'warrior': 'WarriorArmor',
    'mage': 'MageArmor',
    'huntress': 'HuntressArmor',
    'scout': 'ScoutArmor',
    'shaman': 'ShamanArmor',
    'gladiator': 'GladiatorArmor',
    'berserk': 'BerserkArmor',
    'warlock': 'WarlockArmor',
    'battlemage': 'BattleMageArmor',
    'assasin': 'AssasinArmor',
    'freerunner': 'FreeRunnerArmor',
    'sniper': 'SniperArmor',
    'warden': 'WardenArmor',
    'necromancer': 'NecromancerArmor',
    'lich': 'NecromancerArmor',
    'necromancerrobe': 'NecromancerRobe',
    'gnoll': 'GnollArmor',
    'spider': 'SpiderArmor',
    'rat': 'RatArmor',
    'chaos': 'ChaosArmor',
    'elf': 'ElfArmor',
    'priest': 'PriestArmor',
    'paladin': 'PaladinArmor',
    'cleric': 'ClericArmor',
    'witchdoctor': 'WitchdoctorArmor',
    'alchemist': 'AlchemistArmor',
    'transmuter': 'TransmuterArmor',
    'plaguedoctor': 'PlagueDoctorArmor'
};

// Armors with coverHair=true (Armor subclasses)
export const ARMOR_FLAGS = {
    'HuntressArmor': { coverHair: true },
    'ShamanArmor': { coverHair: true },
    'GuardianArmor': { coverHair: true },
    'GladiatorArmor': { coverHair: true }
};

// Weapon definitions: visual name, hands/shoulders animation class,
// two-handed (blocks the off-hand slot), held in left hand (shields).
// Sources: KindOfWeapon animation classes, blockSlot overrides, items/ dir.
export const WEAPON_DEFS = {
    'shortsword':   { visual: 'ShortSword',       anim: 'sword',    twoHanded: false },
    'longsword':    { visual: 'Longsword',        anim: 'heavy',    twoHanded: true  },
    'dagger':       { visual: 'Dagger',           anim: 'sword',    twoHanded: false },
    'mace':         { visual: 'Mace',             anim: 'sword',    twoHanded: false },
    'hammer':       { visual: 'Hammer',           anim: 'heavy',    twoHanded: true  },
    'sword':        { visual: 'GoldenSword',      anim: 'sword',    twoHanded: false },
    'wand':         { visual: 'Wand',             anim: 'staff',    twoHanded: false },
    'bow':          { visual: 'CompoundBow',      anim: 'bow',      twoHanded: true  },
    'crossbow':     { visual: 'CompositeCrossbow',anim: 'crossbow', twoHanded: true  },
    'spear':        { visual: 'Spear',            anim: 'spear',    twoHanded: true  },
    'glaive':       { visual: 'Glaive',           anim: 'spear',    twoHanded: true  },
    'battleaxe':    { visual: 'BattleAxe',        anim: 'heavy',    twoHanded: true  },
    'claymore':     { visual: 'Claymore',         anim: 'heavy',    twoHanded: true  },
    'quarterstaff': { visual: 'Quarterstaff',     anim: 'staff',    twoHanded: true  },
    'knuckles':     { visual: 'Knuckles',         anim: 'none',     twoHanded: false },
    'bonesaw':      { visual: 'BoneSaw',          anim: 'sword',    twoHanded: false },
    'tomahawk':     { visual: 'Tomahawk',         anim: 'sword',    twoHanded: false },
    'halberd':      { visual: 'Halberd',          anim: 'spear',    twoHanded: true  },
    'kusarigama':   { visual: 'Kusarigama',       anim: 'sword',    twoHanded: false },
    'pickaxe':      { visual: 'Pickaxe',          anim: 'sword',    twoHanded: false },
    'royalshield':  { visual: 'RoyalShield',      anim: 'none',     twoHanded: false, shield: true },
    'chaosshield':  { visual: 'ChaosShield',      anim: 'none',     twoHanded: false, shield: true }
};

// Accessory layer file names (accessories/ dir) and covering flags (Accessory subclasses)
export const ACCESSORY_MAP = {
    'plaguedoctormask': 'PlagueDoctorMask',
    'wizardhat': 'WizardHat',
    'nightcap': 'Nightcap',
    'ushanka': 'Ushanka',
    'santahat': 'SantaHat',
    'pumpkin': 'Pumpkin',
    'fez': 'Fez',
    'shades': 'Shades',
    'fullfacemask': 'FullFaceMask',
    'pirateset': 'PirateSet',
    'dogemask': 'DogeMask',
    'nekoears': 'NekoEars',
    'rabbitears': 'RabbitEars',
    'rudolph': 'Rudolph',
    'vampireskull': 'VampireSkull',
    'krampushead': 'KrampusHead',
    'zombiemask': 'ZombieMask',
    'filteredmask': 'FilteredMask',
    'medicinemask': 'MedicineMask',
    'bowknot': 'Bowknot',
    'capotain': 'Capotain',
    'chaoshelmet': 'ChaosHelmet'
};

export const ACCESSORY_FLAGS = {
    'GnollCostume': { coverHair: true, coverItems: true },
    'FullFaceMask': { coverHair: true },
    'RabbitEars': { coverHair: true },
    'Rudolph': { coverHair: true },
    'VampireSkull': { coverHair: true },
    'Ushanka': { coverHair: true },
    'DogeMask': { coverHair: true },
    'Pumpkin': { coverHair: true },
    'KrampusHead': { coverHair: true },
    'PirateSet': { coverHair: true, coverItems: true },
    'Nightcap': { coverHair: true },
    'Fez': { coverHair: true },
    'Capotain': { coverHair: true },
    'ZombieMask': { coverHair: true },
    'NekoEars': { coverHair: true },
    'SantaHat': { coverHair: true },
    'ChaosHelmet': { coverHair: true },
    'WizardHat': { coverHair: true }
};

// Merged z-order of both styles, from ModernHeroSpriteDef.layersOrder and
// RetroHeroSpriteDef.layersOrder (body -> collar -> head -> hair -> armor ->
// armor_boots -> facial_hair -> helmet -> death -> hands -> shoulders ->
// accessory -> held items; back items behind everything).
export const LAYERS_ORDER = [
    'right_back_item', 'left_back_item',
    'body', 'collar', 'head', 'hair', 'armor', 'armor_boots',
    'facial_hair', 'helmet', 'death', 'left_hand', 'right_hand',
    'left_hand_armor', 'right_hand_armor', 'accessory',
    'left_hand_item', 'right_hand_item'
];
