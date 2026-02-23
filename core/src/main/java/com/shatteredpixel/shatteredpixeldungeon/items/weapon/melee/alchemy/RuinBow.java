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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BowMasterSkill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.GreaterHaste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.SharpShooterBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Evolution;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfDisintegration;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.bow.BowWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.alchemy.ObsidianBow;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Arrays;

public class RuinBow extends BowWeapon implements AlchemyWeapon {

    {
        tier = 7;
        image = ItemSpriteSheet.RUINBOW;
    }

    @Override
    public float delayFactor(Char owner) {
        // 1.1x faster
        return super.delayFactor(owner) * (1f / 1.1f);
    }

    @Override
    public int targetingPos(Hero user, int dst) {
        return dst;
    }

    @Override
    public ArrayList<Class<? extends Item>> weaponRecipe() {
        return new ArrayList<>(Arrays.asList(
                ObsidianBow.class,
                WandOfDisintegration.class,
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
        RuinRayArrow a = new RuinRayArrow();
        a.reset(this);
        return a;
    }

    public static class RuinRayArrow extends BowWeapon.Arrow {

        // 이번 발사에서 소모될 탄약(관통 기반)
        private int ammoCost = 1;

        // 현재 타겟에 적용할 관통 배율(관통 기반)
        private float shotMult = 1f;

        @Override
        public int throwPos(Hero user, int dst) {
            return dst;
        }

        @Override
        public int targetingPos(Hero user, int dst) {
            return dst;
        }

        /**
         * - 각 타겟에 대해 curUser.shoot(target, this)를 호출해서
         *   기본 활의 명중/회피 계산을 그대로 탄다.
         * - 관통 배율은 shoot()의 데미지 흐름에서 proc()가 호출되므로,
         *   proc()에서 마지막에 배율을 곱해준다.
         */
        @Override
        public int proc(Char attacker, Char defender, int damage) {
            damage = super.proc(attacker, defender, damage);
            return Math.round(damage * shotMult);
        }

        @Override
        protected void onThrow(int cell) {

            if (!(curUser instanceof Hero)) {
                super.onThrow(cell);
                return;
            }

            Hero hero = (Hero) curUser;

            // 붙어있어도 발사 허용 (dist==1 OK). 자기 자신(0)만 무시.
            Ballistica pre = new Ballistica(hero.pos, cell, Ballistica.PROJECTILE);
            if (pre.dist <= 0) return;

            // 빔 트레이스
            Ballistica beam = new Ballistica(hero.pos, cell, Ballistica.WONT_STOP);

            // “목표 지점(cell)”까지만 처리하도록 clamp
            int endDist = endDistToCell(beam, cell);

            // ─────────────────────────────────────────────
            // 1) Dry-run: 탄약 비용 계산(관통 기반)
            //   - solid은 “연속 구간”을 1회 관통으로 계산
            //   - 적은 두 번째부터 관통 1회로 계산 (enemiesSeen-1)
            // ─────────────────────────────────────────────
            int solidBlocks = 0;
            int enemiesSeen = 0;
            boolean wasSolid = false;

            for (int c : beam.subPath(1, endDist)) {

                boolean isSolid = Dungeon.level.solid[c];
                if (isSolid && !wasSolid) {
                    solidBlocks++;               // 장애물 “덩어리” 단위 카운트
                }
                wasSolid = isSolid;

                Char ch = Actor.findChar(c);
                if (ch != null && ch != hero) {

                    // 패시브/미탐색 보호
                    if (ch instanceof Mob
                            && ((Mob) ch).state == ((Mob) ch).PASSIVE
                            && !(Dungeon.level.mapped[c] || Dungeon.level.visited[c])) {
                        continue;
                    }

                    enemiesSeen++;
                }
            }

            int penetrations = solidBlocks + Math.max(0, enemiesSeen - 1);
            ammoCost = ammoCostByPenetrations(penetrations);

            if (useBullet && Dungeon.bullet < ammoCost) {
                GLog.w(Messages.get(BowWeapon.class, "no_arrow"));
                return;
            }

            // ─────────────────────────────────────────────
            // 2) Real pass: 타격(정확도 적용) + 데미지(관통당)
            // ─────────────────────────────────────────────
            solidBlocks = 0;
            enemiesSeen = 0;
            wasSolid = false;

            for (int c : beam.subPath(1, endDist)) {

                boolean isSolid = Dungeon.level.solid[c];
                if (isSolid && !wasSolid) {
                    solidBlocks++;
                }
                wasSolid = isSolid;

                Char ch = Actor.findChar(c);
                if (ch != null && ch != hero) {

                    if (ch instanceof Mob
                            && ((Mob) ch).state == ((Mob) ch).PASSIVE
                            && !(Dungeon.level.mapped[c] || Dungeon.level.visited[c])) {
                        continue;
                    }

                    enemiesSeen++;

                    // 관통당 +10% (cap 2.0) — “칸수”가 아니라 관통(덩어리/대상) 기반
                    shotMult = damageMultiplier(solidBlocks, enemiesSeen);

                    // 기존 활 정확도/회피 적용(필중 제거)
                    boolean hit = curUser.shoot(ch, this);

                    // 기존 활 훅 유지
                    SharpShooterBuff.rangedLethal(ch, isBurst, this);

                    if (hit && !ch.isAlive() && isBurst && Dungeon.hero.hasTalent(Talent.HURRICANE)) {
                        Buff.affect(Dungeon.hero, GreaterHaste.class)
                                .set(1 + Dungeon.hero.pointsInTalent(Talent.HURRICANE));
                    }
                }
            }

            Sample.INSTANCE.play(Assets.Sounds.HIT_MAGIC, 1, Random.Float(0.95f, 1.05f));

            // 빔: 화살 드랍/핀 없음
            onShoot();
        }

        @Override
        public void dropArrow(int cell) {
            // 빔이라 드랍 없음
        }

        /**
         * 목표 cell까지만 가도록 endDist를 찾아낸다.
         * Ballistica가 어떤 이유로든 맵 끝까지 뻗는 케이스를 강제 clamp.
         */
        private int endDistToCell(Ballistica beam, int targetCell) {
            int end = beam.dist;

            // BowWeapon에서 trajectory.path.get(...) 를 쓰는 걸 보면,
            // path는 List<Integer> 형태임.
            if (beam.path != null) {
                int max = Math.min(beam.dist, beam.path.size() - 1);
                for (int i = 1; i <= max; i++) {
                    if (beam.path.get(i) == targetCell) {
                        end = i;
                        break;
                    }
                }
            }
            return end;
        }

        /**
         * 데미지 배율: 관통 스택당 +10%, cap 2.0
         * stacks = solidBlocks + max(0, enemiesSeen-1)
         */
        private float damageMultiplier(int solidBlocks, int enemiesSeen) {
            int stacks = solidBlocks + Math.max(0, enemiesSeen - 1);
            float mult = 1f + 0.10f * stacks;
            return Math.min(mult, 2.0f);
        }

        /**
         * 탄약 소모(관통 기반, cap 20)
         * p=0 -> 1
         * p>=1 -> p*(p+3)/2  (1->3, 2->7, 3->12 ...)
         */
        private int ammoCostByPenetrations(int p) {
            int cost = (p <= 0) ? 1 : (p * (p + 5)) / 2;
            return Math.min(25, cost);
        }

        /**
         * BowWeapon.Arrow.onShoot() 오버라이드:
         * - 1발이 아니라 ammoCost만큼 소모
         * - 스펙터 애로우 환급 제거
         * - 나머지(피로/보우마스터/관통샷 해제)는 유지
         */
        @Override
        public void onShoot() {

            if (useBullet) {
                Dungeon.bullet -= ammoCost;
                if (Dungeon.bullet < 0) Dungeon.bullet = 0;
            }

            if (Dungeon.hero.buff(PenetrationShotBuff.class) != null) {
                Dungeon.hero.buff(PenetrationShotBuff.class).detach();
            }

            if (Dungeon.hero.subClass != HeroSubClass.BOWMASTER) {
                Buff.affect(Dungeon.hero, BowFatigue.class).countUp(1);
            }

            if (Dungeon.hero.subClass == HeroSubClass.BOWMASTER) {
                Buff.affect(Dungeon.hero, BowMasterSkill.class).shoot();
            }

            // Spectre Arrow 환급 없음

            updateQuickslot();
        }

        @Override
        public void throwSound() {
            Sample.INSTANCE.play(Assets.Sounds.HIT_MAGIC, 1, Random.Float(0.87f, 1.15f));
        }
    }
}