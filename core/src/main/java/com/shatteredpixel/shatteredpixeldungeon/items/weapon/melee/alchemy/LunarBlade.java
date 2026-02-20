package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.alchemy;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Evolution;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.UpgradeDust;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.LargeKatana;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.NormalKatana;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.Arrays;

public class LunarBlade extends MeleeWeapon implements AlchemyWeapon {

    private int moonCharge = 0;
    private static final String MOON_CHARGE = "moonCharge";

    private static final int TRIGGER_HITS = 5;
    private static final float FROST_DURATION = 12f;
    private static final float CHILL_DURATION_BOSS = 10f;

    {
        tier = 6;
        image = ItemSpriteSheet.LUNAR_KATANA;

        hitSound = Assets.Sounds.HIT_SLASH;
        hitSoundPitch = 1.25f;
    }

    @Override
    public int min(int lvl) {
        return 1;
    }

    @Override
    public int max(int lvl) {
        return 5 * (tier + 2) + lvl * (tier + 2);
    }

    @Override
    protected void duelistAbility(Hero hero, Integer target) {
        NormalKatana.flashSlashAbility(hero, target, 0.4f, this);
    }

    @Override
    public String abilityInfo() {
        if (levelKnown) {
            return Messages.get(this, "ability_desc", Messages.decimalFormat("#.##", 0.4f));
        } else {
            return Messages.get(this, "typical_ability_desc", Messages.decimalFormat("#.##", 0.4f));
        }
    }

    @Override
    public int proc(Char attacker, Char defender, int damage) {

        damage = super.proc(attacker, defender, damage);

        if (damage > 0 && attacker instanceof Hero) {

            moonCharge++;

            if (moonCharge >= TRIGGER_HITS) {
                moonCharge = 0;

                if (defender.properties().contains(Char.Property.BOSS)) {
                    Buff.affect(defender, Chill.class, CHILL_DURATION_BOSS);
                } else {
                    // 타격으로 바로 깨지는 문제 방지: WandOfFrost 방식으로 1틱 지연 적용
                    if (defender.buff(Frost.class) == null) {
                        delayFreeze(defender);
                    }
                }
            }
        }

        return damage;
    }

    private void delayFreeze(final Char target) {
        new FlavourBuff() {
            { actPriority = VFX_PRIO; }

            @Override
            public boolean act() {
                Buff.affect(target, Frost.class, FROST_DURATION);
                return super.act();
            }
        }.attachTo(target);
    }

    @Override
    public ArrayList<Class<? extends Item>> weaponRecipe() {
        return new ArrayList<>(Arrays.asList(
                LargeKatana.class,
                UpgradeDust.class,
                Evolution.class 
        ));
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

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(MOON_CHARGE, moonCharge);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        moonCharge = bundle.getInt(MOON_CHARGE);
    }
}