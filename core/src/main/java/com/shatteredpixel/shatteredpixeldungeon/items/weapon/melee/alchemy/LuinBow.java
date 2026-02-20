package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.alchemy;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Evolution;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.alchemy.AlchemyWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfDisintegration;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 6티어 합성 활: 파멸궁
 * - GreatBow + Evolution + WandOfDisintegration
 * - 공속 1.1배(딜레이 0.909배)
 * - 관통 광선: 지나간 '단단한 지형' + '추가로 관통한 적' 만큼 4%씩 누적 증뎀
 *
 * BowWeapon의 Arrow 시스템을 그대로 활용하되,
 * Arrow.onThrow 를 광선 처리로 교체한다.
 */
public class RuinBow extends BowWeapon implements AlchemyWeapon {

    {
        tier = 6;
        image = ItemSpriteSheet.RUIN_BOW;
    }

    /**
     * 공속 1.1배 더 빠르게:
     * BowWeapon.Arrow.delayFactor()가 arrowFrom.delayFactor(owner)를 쓰므로
     * 여기서 delayFactor를 낮춰주면 된다. :contentReference[oaicite:3]{index=3}
     */
    @Override
    public float delayFactor(Char owner) {
        return super.delayFactor(owner) * (1f / 1.1f);
    }

    /**
     * 파멸궁은 일반 화살이 아니라 '광선 화살'을 발사한다.
     */
    @Override
    public Arrow knockArrow() {
        RuinRayArrow a = new RuinRayArrow();
        a.reset(this);
        return a;
    }

    /**
     * 벽 뒤도 조준 가능하게: WandOfDisintegration처럼 targetingPos를 dst 그대로 반환.
     * (BowWeapon 기본은 Arrow.targetingPos에 위임) 
     */
    @Override
    public int targetingPos(Hero user, int dst) {
        return dst;
    }

    /**
     * 합성(청사진) 레시피
     */
    @Override
    public ArrayList<Class<? extends Item>> weaponRecipe() {
        return new ArrayList<>(Arrays.asList(GreatBow.class, Evolution.class, WandOfDisintegration.class));
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

    /**
     * 파멸궁 전용 "광선 화살"
     *
     * 핵심:
     * - throwPos/targetingPos를 dst로 고정해 벽/지형에 막히지 않게 만들고,
     * - onThrow에서 Ballistica.WONT_STOP 빔을 직접 처리한다.
     */
    public static class RuinRayArrow extends BowWeapon.Arrow {

        /**
         * WandOfDisintegration은 collisionProperties=Ballistica.WONT_STOP. :contentReference[oaicite:6]{index=6}
         * MissileWeapon 쪽 throwPos 로직을 우회하려고 dst를 그대로 반환.
         */
        @Override
        public int throwPos(Hero user, int dst) {
            return dst;
        }

        @Override
        public int targetingPos(Hero user, int dst) {
            return dst;
        }

        /**
         * 광선 처리:
         * - 벽/지형/적을 모두 관통
         * - 지나간 solid 타일 수 + (첫 타격 이후 추가로 관통한 적 수) 만큼 4%씩 누적 증뎀
         * - WandOfDisintegration의 "terrainPassed/bonus" 느낌만 가져와 하향(반타작) :contentReference[oaicite:7]{index=7}
         */
        @Override
        protected void onThrow(int cell) {

            if (!(curUser instanceof Hero)) {
                // 안전장치: 영웅이 아니면 기존 로직으로
                super.onThrow(cell);
                return;
            }

            Hero hero = (Hero) curUser;

            // 탄환 체크는 BowWeapon.execute에서 이미 하지만,
            // 다른 경로로 호출될 수 있어 안전장치 추가.
            if (useBullet && Dungeon.bullet <= 0) {
                GLog.w(Messages.get(this, "no_arrow"));
                return;
            }

            Ballistica beam = new Ballistica(hero.pos, cell, Ballistica.WONT_STOP);

            int maxDist = Math.min(distance(), beam.dist);

            // 관통 카운트(하향 버전): solid 타일 + 추가 적 관통
            int solidPassed = 0;
            int enemiesHit = 0;

            // 마지막으로 맞춘 적(Sharpshooter 훅 등에 사용)
            Char lastEnemy = null;

            for (int c : beam.subPath(1, maxDist)) {

                if (Dungeon.level.solid[c]) {
                    solidPassed++;
                }

                Char ch = Actor.findChar(c);
                if (ch != null && ch != hero) {

                    // undiscovered passive mob 보호 (wand 로직과 동일 취지) :contentReference[oaicite:8]{index=8}
                    if (ch instanceof Mob
                            && ((Mob) ch).state == ((Mob) ch).PASSIVE
                            && !(Dungeon.level.mapped[c] || Dungeon.level.visited[c])) {
                        continue;
                    }

                    enemiesHit++;
                    lastEnemy = ch;

                    float mult = damageMultiplier(solidPassed, enemiesHit);

                    int dmg = damageRoll(hero);
                    dmg = Math.round(dmg * mult);

                    // BowWeapon.Arrow이므로 enchant/augment/기타 proc 경로를 최대한 태우기 위해 proc 호출
                    dmg = proc(hero, ch, dmg);

                    ch.damage(dmg, this);

                    // kill/Sharpshooter 트리거는 적마다 처리하는 게 자연스러움
                    // (BowWeapon.Arrow는 맨 마지막에 한 번 호출하지만, 광선은 다단히트라 여기서 처리)
                    com.shatteredpixel.shatteredpixeldungeon.actors.buffs.SharpShooterBuff.rangedLethal(ch, isBurst, this);
                }
            }

            // 사운드: wand 느낌으로 바꾸고 싶으면 여기만 교체
            Sample.INSTANCE.play(Assets.Sounds.HIT_MAGIC, 1, Random.Float(0.95f, 1.05f));

            // ✅ 탄환 소모/피로/버프 처리 등은 BowWeapon.Arrow.onShoot()를 그대로 재사용 :contentReference[oaicite:9]{index=9}
            onShoot();
        }

        /**
         * 광선 사거리(하향): wand는 buffedLvl*2 + 6. :contentReference[oaicite:10]{index=10}
         * 파멸궁은 살짝 줄여서 +4로.
         */
        private int distance() {
            return buffedLvl() * 2 + 4;
        }

        /**
         * “관통할수록 강해짐” 하향 버전.
         * - 관통 스택 = solidPassed + max(0, enemiesHit-1)
         * - 스택 1당 +8%
         * - 과도 폭주 방지용 상한(예: 2.0배) 적용
         */
        private float damageMultiplier(int solidPassed, int enemiesHit) {
            int stacks = solidPassed + Math.max(0, enemiesHit - 1);
            float mult = 1f + 0.08f * stacks;
            return Math.min(mult, 2.0f);
        }

        /**
         * 광선이니까 “화살 드랍/꽂힘”은 제거 (원하면 다시 살릴 수 있음)
         */
        @Override
        public void dropArrow(int cell) {
            // no-op
        }
    }
}