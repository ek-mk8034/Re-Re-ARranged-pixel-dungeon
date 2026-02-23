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
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Evolution;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.bow.BowWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.bow.GreatBow;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

import java.util.ArrayList;
import java.util.Arrays;

public class ObsidianBow extends BowWeapon implements AlchemyWeapon {

    {
        tier = 6;

        image = ItemSpriteSheet.OBSIDIANBOW;
    }

    @Override
    public ArrayList<Class<? extends Item>> weaponRecipe() {
        return new ArrayList<>(Arrays.asList(
                GreatBow.class,
                ObsidianKnife.class,
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
    public Arrow knockArrow() {
        ObsidianArrow a = new ObsidianArrow();
        a.reset(this);
        return a;
    }

    public static class ObsidianArrow extends BowWeapon.Arrow {

        @Override
        public int proc(Char attacker, Char defender, int damage) {

            // 먼저 기본 활/화살 proc (기본 효과, 증강, 기타 훅) 적용
            damage = super.proc(attacker, defender, damage);

            // 출혈: 최종 피해 기준 1.2배
            float bleed = damage * 1.2f;

            // 보스/미니보스 출혈 감소
            if (defender.properties().contains(Char.Property.BOSS)
                    || defender.properties().contains(Char.Property.MINIBOSS)) {
                bleed *= 0.4f; // 최종 damage * 0.44
            }

            Buff.affect(defender, Bleeding.class).set(bleed);

            return damage;
        }

        @Override
        public void throwSound() {
            com.watabou.noosa.audio.Sample.INSTANCE.play(Assets.Sounds.HIT_MAGIC, 1f, 1f);
        }
    }
}