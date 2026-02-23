/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * Re-ReARranged Pixel Dungeon
 * Copyright (C) 2026 Eric Kim (ek-mk8034)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.alchemy;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Evolution;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.UpgradeDust;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Knife;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.AttackIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

import java.util.ArrayList;
import java.util.Arrays;

public class ObsidianKnife extends MeleeWeapon implements AlchemyWeapon {

    {
        image = ItemSpriteSheet.OBSIDIAN_KNIFE; // ItemSpriteSheet에 상수/좌표 추가 필요
        hitSound = Assets.Sounds.HIT_SLASH;
        hitSoundPitch = 1.15f;

        tier = 4;
    }

    // ---- 4티어: 강화 스케일 +2 (레벨당 2씩 증가) ----
    @Override
    public int min(int lvl) {
        return 4 + 2 * lvl;
    }

    @Override
    public int max(int lvl) {
        return 8 + 2 * lvl;
    }

    // ---- 기본 타격 효과: 출혈 = 피해 * 1.1, 보스/미니보스는 추가로 *0.4 ----
    @Override
    public int proc(Char attacker, Char defender, int damage) {

        float bleed = damage * 1.1f;

        if (defender.properties().contains(Char.Property.BOSS)
                || defender.properties().contains(Char.Property.MINIBOSS)) {
            bleed *= 0.4f; // 최종적으로 damage * 0.48
        }

        Buff.affect(defender, Bleeding.class).set(bleed);

        return super.proc(attacker, defender, damage);
    }

    // ---- 듀얼리스트 능력(knife 스타일) ----
    @Override
    protected int baseChargeUse(Hero hero, Char target) {
        return 2;
    }

    @Override
    public String targetingPrompt() {
        // messages key: items.weapon.melee.alchemy.obsidianknife.prompt
        return Messages.get(this, "prompt");
    }

    @Override
    protected void duelistAbility(Hero hero, Integer target) {
        // Knife와 동일 패턴: 50% 무기 피해(무조건 명중) + (HP비례) 출혈
        obsidianKnifeAbility(hero, target, 0.5f, this);
    }

    public static void obsidianKnifeAbility(Hero hero, Integer target, float bleedingAmt, MeleeWeapon wep) {
        if (target == null) {
            return;
        }

        Char enemy = Actor.findChar(target);
        if (enemy == null || enemy == hero || hero.isCharmedBy(enemy) || !Dungeon.level.heroFOV[target]) {
            // messages key: items.weapon.melee.alchemy.obsidianknife.ability_no_target
            GLog.w(Messages.get(wep, "ability_no_target"));
            return;
        }

        hero.belongings.abilityWeapon = wep;
        if (!hero.canAttack(enemy)) {
            // messages key: items.weapon.melee.alchemy.obsidianknife.ability_bad_position
            GLog.w(Messages.get(wep, "ability_bad_position"));
            hero.belongings.abilityWeapon = null;
            return;
        }
        hero.belongings.abilityWeapon = null;

        hero.sprite.attack(enemy.pos, new Callback() {
            @Override
            public void call() {
                wep.beforeAbilityUsed(hero, enemy);
                AttackIndicator.target(enemy);

                // Knife와 동일: 무조건 명중
                if (hero.attack(enemy, 1, 0, Char.INFINITE_ACCURACY)) {
                    Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG);
                }

                Invisibility.dispel();
                hero.spendAndNext(hero.attackDelay());

                float multi = bleedingAmt;
                if (enemy.properties().contains(Char.Property.BOSS) || enemy.properties().contains(Char.Property.MINIBOSS)) {
                    // Knife처럼 강력한 적은 크게 저항
                    multi = 0.05f;
                }

                if (!enemy.isAlive()) {
                    wep.onAbilityKill(hero, enemy);
                } else {
                    Buff.affect(enemy, Bleeding.class).set(enemy.HP * multi);
                }

                wep.afterAbilityUsed(hero);
            }
        });
    }

    @Override
    public String abilityInfo() {
        // messages keys:
        // - ability_desc
        // - typical_ability_desc
        if (levelKnown) {
            return Messages.get(this, "ability_desc",
                    augment.damageFactor(Math.round(min() * 0.5f)),
                    augment.damageFactor(Math.round(max() * 0.5f)));
        } else {
            return Messages.get(this, "typical_ability_desc",
                    Math.round(min(0) * 0.5f),
                    Math.round(max(0) * 0.5f));
        }
    }

    // ---- 청사진(AlchemyWeapon) ----
    // 제작식: Knife + UpgradeDust + Evolution
    @Override
    public ArrayList<Class<? extends Item>> weaponRecipe() {
        return new ArrayList<>(Arrays.asList(
            Knife.class, 
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
}