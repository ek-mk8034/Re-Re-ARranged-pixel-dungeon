package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.alchemy;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
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

/**
 * Lunar Blade (월영도) - Tier 6 Alchemy weapon
 * Recipe: LargeKatana + Evolution + UpgradeDust
 *
 * Mechanic:
 * - Count successful hits globally (not per target).
 * - On the 6th successful hit: apply Frost (normal enemies) or Chill (bosses), then reset.
 */
public class LunarBlade extends MeleeWeapon implements AlchemyWeapon {

    // 0~5, 6번째 성공타에서 발동 후 0으로 리셋
    private int moonCharge = 0;

    private static final String MOON_CHARGE = "moonCharge";

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

    // LargeKatana 계열 듀얼리스트 능력 유지(같은 계열 느낌)
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

    /**
     * 핵심: 6번째 성공타에 상태이상 발동
     * - 보스는 Frost(완전 빙결) 대신 Chill로 다운그레이드
     */
    @Override
    public int proc(Char attacker, Char defender, int damage) {

        damage = super.proc(attacker, defender, damage);

        // “성공한 공격”만 카운트: damage > 0 이면 명중/유효타로 간주
        if (damage > 0 && attacker instanceof Hero) {

            moonCharge++;

            if (moonCharge >= 6) {
                moonCharge = 0;

                if (defender.properties().contains(Char.Property.BOSS)) {
                    // 보스 예외처리: 완전 행동불가(Frost) 대신 감속(Chill)
                    Buff.affect(defender, Chill.class, 3f);
                } else {
                    // 일반 몹: 짧은 Frost
                    Buff.affect(defender, Frost.class, 2f);
                }
            }
        }

        return damage;
    }

    // ─────────────────────────────────────────
    // Alchemy blueprint recipe/hint
    // ─────────────────────────────────────────
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
        // 원하면 strings에 설명 키 추가해서 월광 충전 메커니즘 안내 가능
        // info += "\n\n" + Messages.get(this, "mooncharge_desc");
        return info;
    }

    // ─────────────────────────────────────────
    // Save / Load
    // ─────────────────────────────────────────
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