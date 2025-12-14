import com.nyrds.pixeldungeon.desktop.FactorySpriteGenerator;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.items.armor.ClassArmor;
import com.watabou.pixeldungeon.items.armor.WarriorArmor;
import com.watabou.pixeldungeon.items.armor.MageArmor;
import com.watabou.pixeldungeon.items.armor.RogueArmor;
import com.watabou.pixeldungeon.items.armor.HuntressArmor;
import com.watabou.pixeldungeon.items.armor.ElfArmor;
import com.nyrds.pixeldungeon.items.common.armor.NecromancerArmor;
import com.watabou.pixeldungeon.items.armor.GnollArmor;
import com.watabou.pixeldungeon.sprites.HeroSpriteDef;

public class TestHeroGeneration {
    public static void main(String[] args) {
        try {
            System.out.println("Testing hero class+subclass combination generation...");
            
            // Test forcing sprite creation
            GameScene.setForceAllowSpriteCreation(true);
            
            // Let's test one specific combination to see what happens
            HeroClass testClass = HeroClass.WARRIOR;
            HeroSubClass testSubClass = HeroSubClass.GLADIATOR;
            
            System.out.println("Creating hero with class: " + testClass + " and subclass: " + testSubClass);
            
            // Create a temporary hero instance with this class and subclass combination
            Hero hero = new Hero(2); // Using difficulty 2 which is normal
            hero.setHeroClass(testClass);
            hero.setSubClass(testSubClass);
            
            System.out.println("Hero created successfully");
            
            // Equip the appropriate class armor for this combination
            ClassArmor classArmor = null;
            switch (testClass) {
                case WARRIOR:
                    if (testSubClass == HeroSubClass.GLADIATOR || testSubClass == HeroSubClass.BERSERKER) {
                        classArmor = new WarriorArmor();
                        System.out.println("Equipping WarriorArmor");
                    }
                    break;
                case MAGE:
                    if (testSubClass == HeroSubClass.BATTLEMAGE || testSubClass == HeroSubClass.WARLOCK) {
                        classArmor = new MageArmor();
                        System.out.println("Equipping MageArmor");
                    }
                    break;
                case ROGUE:
                    if (testSubClass == HeroSubClass.FREERUNNER || testSubClass == HeroSubClass.ASSASSIN) {
                        classArmor = new RogueArmor();
                        System.out.println("Equipping RogueArmor");
                    }
                    break;
                case HUNTRESS:
                    if (testSubClass == HeroSubClass.SNIPER || testSubClass == HeroSubClass.WARDEN) {
                        classArmor = new HuntressArmor();
                        System.out.println("Equipping HuntressArmor");
                    }
                    break;
                case ELF:
                    if (testSubClass == HeroSubClass.SCOUT || testSubClass == HeroSubClass.SHAMAN) {
                        classArmor = new ElfArmor();
                        System.out.println("Equipping ElfArmor");
                    }
                    break;
                case NECROMANCER:
                    if (testSubClass == HeroSubClass.LICH) {
                        classArmor = new NecromancerArmor();
                        System.out.println("Equipping NecromancerArmor");
                    }
                    break;
                case GNOLL:
                    if (testSubClass == HeroSubClass.GUARDIAN || testSubClass == HeroSubClass.WITCHDOCTOR) {
                        classArmor = new GnollArmor();
                        System.out.println("Equipping GnollArmor");
                    }
                    break;
            }
            
            if (classArmor != null) {
                // Equip the class armor
                classArmor.upgrade(0); // Ensure it's at +0 to avoid any upgrade visuals
                classArmor.doEquip(hero);
                System.out.println("Class armor equipped successfully");
            } else {
                System.out.println("No class armor found for this combination");
            }
            
            // Generate sprite for this hero class and subclass combination
            HeroSpriteDef heroSprite = (HeroSpriteDef) hero.newSprite();
            System.out.println("HeroSprite created: " + (heroSprite != null));
            
            if (heroSprite != null) {
                // Update the sprite to ensure all layers are properly applied after equipping items
                heroSprite.heroUpdated(hero);
                System.out.println("HeroSprite updated");
                
                // Use the avatar method to get the layered sprite
                com.watabou.noosa.Image avatar = heroSprite.avatar();
                System.out.println("Avatar created: " + (avatar != null));
            }
            
        } catch (Exception e) {
            System.out.println("Exception occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Always reset the flag
            GameScene.setForceAllowSpriteCreation(false);
            System.out.println("Force allow sprite creation reset to false");
        }
    }
}