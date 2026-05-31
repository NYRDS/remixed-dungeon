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
                          'freerunner', 'sniper', 'warden', 'necromancer', 'lich', 
                          'gnoll', 'spider', 'rat', 'chaos', 'elf', 'necromancerRobe'];

export const WEAPON_LIST = ['none', 'shortsword', 'longsword', 'dagger', 'mace', 'hammer', 
                           'sword', 'wand', 'boomerang', 'bow', 'crossbow', 'spear', 
                           'glaive', 'battleaxe', 'claymore', 'quarterstaff', 'knuckles',
                           'bonesaw', 'tomahawk', 'halberd', 'kusarigama', 'pickaxe',
                           'royalshield', 'chaosshield'];

export const ACCESSORY_LIST = ['none', 'plaguedoctormask', 'wizardhat', 'nightcap', 'ushanka',
                              'santahat', 'pumpkin', 'fez', 'shades', 'fullfacemask',
                              'pirateset', 'dogemask', 'nekoears', 'rabbitears', 'rudolph',
                              'vampireskull', 'krampushead', 'zombiemask', 'filteredmask',
                              'medicineMask', 'bowknot', 'capotain', 'chaoshelmet'];

// Body type mapping based on ModernHeroSpriteDef.bodyType
export const BODY_TYPE_MAP = {
    'WARLOCK': 'warlock',
    'LICH': 'lich',
    'GNOLL': 'gnoll',
    'HUNTRESS': 'woman'
};

// Layer order matching ModernHeroSpriteDef.java
export const LAYERS_ORDER = [
    'right_back_item', 'left_back_item',
    'body', 'collar', 'head', 'hair', 'armor', 'armor_boots',
    'facial_hair', 'helmet', 'left_hand', 'right_hand',
    'left_hand_armor', 'right_hand_armor', 'accessory',
    'left_hand_item', 'right_hand_item'
];
