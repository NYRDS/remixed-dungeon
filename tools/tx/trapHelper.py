codes = ["TOXIC_TRAP","FIRE_TRAP","PARALYTIC_TRAP","POISON_TRAP","ALARM_TRAP","LIGHTNING_TRAP","GRIPPING_TRAP","SUMMONING_TRAP"]

for code in codes:
    print(f'''
				case Terrain.{code}:
					map[i] = Terrain.EMPTY;
					putLevelObject(Trap.makeSimpleTrap(i, {code}, false));
				break;

				case Terrain.SECRET_{code}:
					map[i] = Terrain.EMPTY;
					putLevelObject(Trap.makeSimpleTrap(i, {code}, true));
				break;
				
''')