package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.ArrowItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.bow.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.alchemy.LuinBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.bow.Bow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.bow.BowWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.bow.GreatBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.bow.LongBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.bow.ShortBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.bow.WornShortBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class Juggling extends Buff implements ActionIndicator.Action {
    {
        type = buffType.NEUTRAL;
    }

    Queue<MissileWeapon> weapons = new LinkedList<>();

    @Override
    public int icon() {
        return BuffIndicator.JUGGLING;
    }

    private int maxWeapons(Hero hero) {
        return 3 + hero.pointsInTalent(Talent.SKILLFUL_JUGGLING);
    }

    public boolean isJugglingNow() {
        return !weapons.isEmpty();
    }

    public void juggle(Hero hero, MissileWeapon wep, boolean useTurn) {
        weapons.offer(wep);

        if (weapons.size() > (maxWeapons(hero))) {
            MissileWeapon polled = weapons.poll();
            if (polled != null) {
                if (polled instanceof BowWeapon.Arrow) {
                    ArrowItem ai = new ArrowItem();
                    ai.doPickUp(hero, hero.pos);
                    GLog.i(Messages.get(hero, "you_now_have", ai.name()));
                } else {
                    polled.doPickUp(hero, hero.pos);
                    GLog.i(Messages.get(hero, "you_now_have", polled.name()));
                }
            }
            hero.spend(-1);
        }

        hero.sprite.zap(hero.pos);
        Sample.INSTANCE.play(Assets.Sounds.MISS);

        if (useTurn) {
            hero.spendAndNext(Math.max(0, 1f - hero.pointsInTalent(Talent.SWIFT_JUGGLING)/3f));
        }

        ActionIndicator.setAction(this);
    }

    @Override
    public void detach() {
        // Arrow 투사체는 바닥 드랍 대신 ArrowItem으로 변환 (안정)
        for (MissileWeapon weapon : weapons) {
            if (weapon == null) continue;

            if (weapon instanceof BowWeapon.Arrow) {
                Dungeon.level.drop(new ArrowItem(), target.pos).sprite.drop(target.pos);
            } else {
                Dungeon.level.drop(weapon, target.pos);
            }
        }
        ActionIndicator.clearAction();
        super.detach();
    }

    @Override
    public boolean act() {
        if (weapons.isEmpty()) {
            detach();
        }
        spend(TICK);
        return true;
    }

    @Override
    public String desc() {
        StringBuilder sb = new StringBuilder();
        Iterator<MissileWeapon> iterator = weapons.iterator();
        while (iterator.hasNext()) {
            MissileWeapon weapon = iterator.next();

            sb.append(weapon.name());
            if (iterator.hasNext()) {
                sb.append(", ");
            }
        }
        return Messages.get(this, "desc", sb.toString());
    }

    @Override
    public String actionName() {
        return Messages.get(this, "action_name");
    }

    @Override
    public int actionIcon() {
        return HeroIcon.JUGGLING;
    }

    @Override
    public int indicatorColor() {
        return 0xB3B3B3;
    }

    @Override
    public void doAction() {
        GameScene.selectCell(listener);
    }

    private static final String WEAPONS = "weapons";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(WEAPONS, weapons);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);

        for (Bundlable item : bundle.getCollection(WEAPONS)) {
            if (item != null){
                weapons.add((MissileWeapon) item);
            }
        }
        ActionIndicator.setAction(this);
    }

    private final CellSelector.Listener listener = new CellSelector.Listener() {

        @Override
        public void onSelect(Integer cell) {
            if (cell != null) {
                Ballistica aim = new Ballistica(Dungeon.hero.pos, cell, Ballistica.STOP_TARGET);
                int destination = aim.collisionPos;

                while (!weapons.isEmpty()) {
                    MissileWeapon weapon = weapons.poll();
                    if (weapon != null) {
                        if (weapon.STRReq() <= Dungeon.hero.STR()) {
                            weapon.cast(Dungeon.hero, destination, false, weapons.isEmpty() ? 1 : 0, new Callback() {
                                @Override
                                public void call() {
                                    if (Dungeon.hero.hasTalent(Talent.FANCY_PERFORMANCE)) {
                                        Char ch = Actor.findChar(destination);
                                        if (ch != null && ch.alignment == Char.Alignment.ENEMY) {
                                            Dungeon.level.drop(new Gold(5*Dungeon.hero.pointsInTalent(Talent.FANCY_PERFORMANCE)), destination).sprite.drop();
                                        }
                                    }
                                }
                            });
                        } else {
                            if (weapon instanceof BowWeapon.Arrow) {
                                Dungeon.level.drop(new ArrowItem(), Dungeon.hero.pos).sprite.drop(Dungeon.hero.pos);
                            } else {
                                Dungeon.level.drop(weapon, Dungeon.hero.pos);
                            }

                            if (weapons.isEmpty()) {
                                Dungeon.hero.spendAndNext(1);
                            }
                        }
                    }
                }

                detach();
            }
        }

        @Override
        public String prompt() {
            return Messages.get(SpiritBow.class, "prompt");
        }
    };

    public static BowWeapon getBow() {
        BowWeapon bow;
        if (!(Dungeon.hero.belongings.weapon instanceof BowWeapon)) {
            switch (Dungeon.scalingDepth()/5+1) {
                case 1: default:
                    bow = new WornShortBow();
                    break;
                case 2:
                    bow = new ShortBow();
                    break;
                case 3:
                    bow = new Bow();
                    break;
                case 4:
                    bow = new LongBow();
                    break;
                case 5:
                case 6:
                    bow = new GreatBow();
                    break;
                case 7:
                    bow = new LuinBow();
                    break;
            }
        } else {
            bow = (BowWeapon) Dungeon.hero.belongings.weapon;
        }
        return bow;
    }

    /**
     * ✅ 어떤 킬이든 Mob.die에서 호출됨
     * - 화살통이 있을 때만 발동
     * - 킬 시점에 bullet을 소모하면서 저글링 화살을 장전
     * - 장전된 화살은 useBullet=false로 만들어서 발사 시 추가 소모 방지
     */
    public static void kill() {
        if (Dungeon.hero.subClass != HeroSubClass.JUGGLER) return;
        if (!Dungeon.hero.hasTalent(Talent.HABITUAL_HAND)) return;

        if (Dungeon.bullet <= 0) return;

        Juggling j = Dungeon.hero.buff(Juggling.class);
        if (j != null && j.isJugglingNow()) return;

        int n = Dungeon.hero.pointsInTalent(Talent.HABITUAL_HAND); // 1/2/3
        if (n <= 0) return;

        n = Math.min(n, Dungeon.bullet);
        if (n <= 0) return;

        if (j == null) j = Buff.affect(Dungeon.hero, Juggling.class);

        for (int i = 0; i < n; i++) {
            // ✅ 킬 시점에 탄약 소비
            Dungeon.bullet--;

            BowWeapon.Arrow arrow = getBow().knockArrow();

            // ✅ 발사 시 추가 탄약 소모 방지(킬에서 이미 소비했으므로)
            arrow.useBullet = false;

            // ✅ BowWeapon 쪽에서 useBullet=false여도 드랍/꽂힘 허용하도록 플래그(3번 패치 필요)
            arrow.fromJuggling = true;

            j.juggle(Dungeon.hero, arrow, false);
        }

        Item.updateQuickslot();
    }

    public static float accuracyFactor(Hero hero) {
        if (hero.buff(Juggling.class) != null) {
            return 0.5f + 0.2f*Dungeon.hero.pointsInTalent(Talent.FOCUS_MAINTAIN);
        } else {
            return 1;
        }
    }

    /**
     * TOUR_PERFORMANCE도 동일 컨셉으로: "탄약이 있을 때만" + "발동 시 탄약 1 소비"
     */
    public static void move() {
        if (Dungeon.hero.subClass != HeroSubClass.JUGGLER) return;
        if (!Dungeon.hero.hasTalent(Talent.TOUR_PERFORMANCE)) return;

        if (Dungeon.bullet <= 0) return;

        if (Random.Float() >= 0.01f * Dungeon.hero.pointsInTalent(Talent.TOUR_PERFORMANCE)) return;

        Juggling j = Dungeon.hero.buff(Juggling.class);
        if (j != null && j.isJugglingNow()) return;

        if (j == null) j = Buff.affect(Dungeon.hero, Juggling.class);

        // ✅ 발동 시 탄약 1 소비
        Dungeon.bullet--;

        BowWeapon.Arrow arrow = getBow().knockArrow();
        arrow.useBullet = false;
        arrow.fromJuggling = true;

        j.juggle(Dungeon.hero, arrow, false);

        Item.updateQuickslot();
    }
}
