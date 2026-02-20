package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.alchemy;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Evolution;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfDisintegration;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.alchemy.AlchemyWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.bow.BowWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.bow.GreatBow;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class RuinBow extends BowWeapon implements AlchemyWeapon {

    {
        tier = 6;
        image = ItemSpriteSheet.RUINBOW;
    }

    // 1.1x faster => delay * (1/1.1)
    @Override
    public float delayFactor(Char owner) {
        return super.delayFactor(owner) * (1f / 1.1f);
    }

    // ✅ nested class name must be qualified (not "Arrow")
    @Override
    public BowWeapon.Arrow knockArrow() {
        RuinRayArrow a = new RuinRayArrow();
        a.reset(this);
        return a;
    }

    @Override
    public int targetingPos(Hero user, int dst) {
        return dst;
    }

    // ✅ avoid Arrays.asList generic inference issues
    @Override
    public ArrayList<Class<? extends Item>> weaponRecipe() {
        ArrayList<Class<? extends Item>> list = new ArrayList<>();
        list.add(GreatBow.class);
        list.add(WandOfDisintegration.class);
        list.add(Evolution.class);
        return list;
    }

    @Override
    public String discoverHint() {
        return AlchemyWeapon.hintString(weaponRecipe());
    }

    @Override
    public String desc() {
        String info = super.desc();
        info += "\n\n" + AlchemyWeapon.hintString(weaponRecipe());
        return info;
    }

    public static class RuinRayArrow extends BowWeapon.Arrow {

        @Override
        public int throwPos(Hero user, int dst) {
            return dst;
        }

        @Override
        public int targetingPos(Hero user, int dst) {
            return dst;
        }

        @Override
        protected void onThrow(int cell) {

            if (!(curUser instanceof Hero)) {
                super.onThrow(cell);
                return;
            }

            Hero hero = (Hero) curUser;

            Ballistica beam = new Ballistica(hero.pos, cell, Ballistica.WONT_STOP);

            int maxDist = Math.min(distance(), beam.dist);

            int solidPassed = 0;
            int enemiesHit = 0;

            for (int c : beam.subPath(1, maxDist)) {

                if (Dungeon.level.solid[c]) solidPassed++;

                Char ch = Actor.findChar(c);
                if (ch != null && ch != hero) {
                    enemiesHit++;

                    float mult = damageMultiplier(solidPassed, enemiesHit);

                    int dmg = damageRoll(hero);
                    dmg = Math.round(dmg * mult);

                    dmg = proc(hero, ch, dmg);
                    ch.damage(dmg, this);

                    // SPD 계열에서 ranged lethal 훅이 있으면 그대로 유지(너 포크에 있길래 남김)
                    com.shatteredpixel.shatteredpixeldungeon.actors.buffs.SharpShooterBuff
                            .rangedLethal(ch, isBurst, this);
                }
            }

            Sample.INSTANCE.play(Assets.Sounds.HIT_MAGIC, 1f, Random.Float(0.95f, 1.05f));

            onShoot(); // 탄환 소모/피로 등 BowWeapon.Arrow 기본 처리
        }

        // wand: lvl*2 + 6, 우리는 약간 하향
        private int distance() {
            return buffedLvl() * 2 + 4;
        }

        // stack = solidPassed + (enemiesHit-1), +4% per stack, cap 2.0x
        private float damageMultiplier(int solidPassed, int enemiesHit) {
            int stacks = solidPassed + Math.max(0, enemiesHit - 1);
            float mult = 1f + 0.04f * stacks;
            return Math.min(mult, 2.0f);
        }

        @Override
        public void dropArrow(int cell) {
            // no-op (beam)
        }
    }
}