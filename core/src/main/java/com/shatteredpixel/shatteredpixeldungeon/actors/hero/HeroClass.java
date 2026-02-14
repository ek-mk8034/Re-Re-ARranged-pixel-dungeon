/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.QuickSlot;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.adventurer.Root;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.adventurer.Sprout;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.adventurer.TreasureMap;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.archer.DashAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.archer.Hunt;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.archer.Snipe;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cleric.AscendedForm;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cleric.PowerOfMany;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cleric.Trinity;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.duelist.Challenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.duelist.ElementalStrike;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.duelist.Feint;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.gunner.FirstAidKit;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.gunner.ReinforcedArmor;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.gunner.Riot;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.NaturesPower;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.SpectralBlades;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.SpiritHawk;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.knight.HolyShield;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.knight.StimPack;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.knight.UnstableAnkh;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage.ElementalBlast;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage.WarpBeacon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage.WildMagic;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.medic.AngelWing;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.medic.GammaRayEmmit;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.medic.HealingGenerator;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.DeathMark;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.ShadowClone;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.SmokeBomb;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.samurai.Abil_Kunai;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.samurai.Awake;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.samurai.ShadowBlade;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.Endure;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.HeroicLeap;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.Shockwave;
import com.shatteredpixel.shatteredpixeldungeon.items.ArrowBag;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.BulletBelt;
import com.shatteredpixel.shatteredpixeldungeon.items.GammaRayGun;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KingsCrown;
import com.shatteredpixel.shatteredpixeldungeon.items.KnightsShield;
import com.shatteredpixel.shatteredpixeldungeon.items.Sheath;
import com.shatteredpixel.shatteredpixeldungeon.items.Teleporter;
import com.shatteredpixel.shatteredpixeldungeon.items.TengusMask;
import com.shatteredpixel.shatteredpixeldungeon.items.Waterskin;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClothArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.PlateArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.AlchemistsToolkit;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.MedicKit;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.VelvetPouch;
import com.shatteredpixel.shatteredpixeldungeon.items.changer.OldAmulet;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHaste;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfInvisibility;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLiquidFlame;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfMindVision;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfParalyticGas;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfPurity;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfHoneyedHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfAccuracy;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfHaste;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfMight;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfIdentify;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfLullaby;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRage;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRetribution;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.HandyBarricade;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.WildEnergy;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfEnchantment;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.bow.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Cudgel;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Dagger;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Gloves;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Machete;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Rapier;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Saber;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Scalpel;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Shovel;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WornKatana;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WornShortsword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.alchemy.TacticalShield;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.bow.WornShortBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.AR.AR_T1;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.quick.PocketKnife;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingKnife;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingSpike;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingStone;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.DeviceCompat;

public enum HeroClass {

	WARRIOR( HeroSubClass.BERSERKER, HeroSubClass.GLADIATOR, HeroSubClass.VETERAN ),
	MAGE( HeroSubClass.BATTLEMAGE, HeroSubClass.WARLOCK, HeroSubClass.WIZARD ),
	ROGUE( HeroSubClass.ASSASSIN, HeroSubClass.FREERUNNER, HeroSubClass.CHASER ),
	HUNTRESS( HeroSubClass.SNIPER, HeroSubClass.WARDEN, HeroSubClass.FIGHTER ),
	DUELIST( HeroSubClass.CHAMPION, HeroSubClass.MONK, HeroSubClass.FENCER ),
	CLERIC( HeroSubClass.PRIEST, HeroSubClass.PALADIN, HeroSubClass.ENCHANTER ),
	GUNNER( HeroSubClass.OUTLAW, HeroSubClass.GUNSLINGER, HeroSubClass.SPECIALIST ),
	SAMURAI( HeroSubClass.SLASHER, HeroSubClass.MASTER, HeroSubClass.SLAYER ),
	ADVENTURER( HeroSubClass.ENGINEER, HeroSubClass.EXPLORER, HeroSubClass.RESEARCHER ),
	KNIGHT( HeroSubClass.DEATHKNIGHT, HeroSubClass.HORSEMAN, HeroSubClass.CRUSADER),
	MEDIC( HeroSubClass.SAVIOR, HeroSubClass.THERAPIST, HeroSubClass.MEDICALOFFICER ),
	ARCHER( HeroSubClass.BOWMASTER, HeroSubClass.JUGGLER, HeroSubClass.SHARPSHOOTER );

	private HeroSubClass[] subClasses;

	HeroClass( HeroSubClass...subClasses ) {
		this.subClasses = subClasses;
	}

	public void initHero( Hero hero ) {

		hero.heroClass = this;
		Talent.initClassTalents(hero);

		final boolean oneSlot = Dungeon.isChallenged(Challenges.ONE_SLOT_PACK);

		// 기본 갑옷은 장비 슬롯(armor)이라서 허용
		Item i = new ClothArmor().identify();
		if (!Challenges.isItemBlocked(i)) hero.belongings.armor = (ClothArmor)i;

		// --------------------------------------------------
		// 기본 지급 아이템 (ONE_SLOT_PACK에서는 인벤 지급 스킵)
		// --------------------------------------------------
		Waterskin waterskin = null;

		if (!oneSlot) {

			i = new Food();
			if (!Challenges.isItemBlocked(i)) i.collect();

			new VelvetPouch().collect();
			Dungeon.LimitedDrops.VELVET_POUCH.drop();

			waterskin = new Waterskin();
			waterskin.collect();

			new ScrollOfIdentify().identify();

		} else {
			// “식별됨(지식)”만 주는 용도라면 유지 가능 (원치 않으면 지워도 됨)
			new ScrollOfIdentify().identify();
		}

		// 디버그/테스트 시드
		if (DeviceCompat.isDebug() || SPDSettings.customSeed().contains("test")) {

		    new RingOfMight().identify().upgrade(10).collect();
		    new RingOfEnergy().identify().upgrade(20).collect();
		    new RingOfHaste().identify().upgrade(100).collect();
		    new RingOfAccuracy().identify().upgrade(100).collect();
		    new AlchemistsToolkit().identify().upgrade(10).collect();
		    new ElixirOfHoneyedHealing().identify().quantity(500).collect();
		    new PlateArmor().identify().upgrade(100).collect();
		    new TacticalShield().identify().upgrade(100).collect();
		    new Teleporter().collect();

		    new TengusMask().collect();
		    new KingsCrown().collect();
		    new OldAmulet().collect();

		    //new HandyBarricade().identify().quantity(20).collect();
		    new WildEnergy().identify().quantity(20).collect();
		    new StoneOfEnchantment().identify().quantity(20).collect();
		}

		// --------------------------------------------------
		// 클래스별 초기화
		// --------------------------------------------------
		switch (this) {
			case WARRIOR:    initWarrior(hero, oneSlot); break;
			case MAGE:       initMage(hero, oneSlot); break;
			case ROGUE:      initRogue(hero, oneSlot); break;
			case HUNTRESS:   initHuntress(hero, oneSlot); break;
			case DUELIST:    initDuelist(hero, oneSlot); break;
			case CLERIC:     initCleric(hero, oneSlot); break;
			case GUNNER:     initGunner(hero, oneSlot); break;
			case SAMURAI:    initSamurai(hero, oneSlot); break;
			case ADVENTURER: initAdventurer(hero, oneSlot); break;
			case KNIGHT:     initKnight(hero, oneSlot); break;
			case MEDIC:      initMedic(hero, oneSlot); break;
			case ARCHER:     initArcher(hero, oneSlot); break;
		}

		// 물통 퀵슬롯 (ONE_SLOT_PACK에서는 인벤 자체가 제한이니 스킵)
		if (!oneSlot && SPDSettings.quickslotWaterskin() && waterskin != null) {
			for (int s = 0; s < QuickSlot.SIZE; s++) {
				if (Dungeon.quickslot.getItem(s) == null) {
					Dungeon.quickslot.setSlot(s, waterskin);
					break;
				}
			}
		}

		// --------------------------------------------------
		// ONE_SLOT_PACK: 시작 인벤을 "첫칸 1개"로 강제
		// --------------------------------------------------
		if (oneSlot) {
			applyOneSlotPackStarter(hero);
		}

		// 마지막으로 장비 규칙 강제(artifact/ring 슬롯에 남는 것 방지)
		if (oneSlot) {
			hero.belongings.enforceOneSlotPackEquipmentRule();
		}
	}

	// =========================================================
	// ONE_SLOT_PACK 스타터: 배낭 첫 칸(0번)에 전용 아이템 1개만
	// =========================================================
	private static void applyOneSlotPackStarter(Hero hero) {

		if (hero == null || hero.belongings == null || hero.belongings.backpack == null) return;

		// 1) 배낭 완전 비우기
		hero.belongings.backpack.items.clear();

		// 2) 퀵슬롯 완전 초기화 (QuickSlot 클래스에 clearAll이 없어서 reset 사용)
		Dungeon.quickslot.reset();

		// 3) 클래스 전용 스타터 생성
		Item starter = makeOneSlotStarterItem(hero.heroClass);
		if (starter == null) return;

		starter.identify();
		starter.collect();

		// 배낭 첫 칸 보장
		if (hero.belongings.backpack.items.contains(starter)) {
			hero.belongings.backpack.items.remove(starter);
			hero.belongings.backpack.items.add(0, starter);
		}
	}

	private static Item makeOneSlotStarterItem(HeroClass hc) {

		// Bag류는 절대 지급하지 않음(챌린지에서 가방류 금지)
		switch (hc) {

			case WARRIOR: {
				ThrowingStone stones = new ThrowingStone();
				stones.identify();
				return stones;
			}

			case MAGE: {
				ScrollOfUpgrade sc = new ScrollOfUpgrade();
				sc.identify();
				return sc;
			}

			case ROGUE: {
				ThrowingKnife knives = new ThrowingKnife();
				knives.identify();
				return knives;
			}

			case HUNTRESS: {
				SpiritBow bow = new SpiritBow();
				bow.identify();
				return bow;
			}

			case DUELIST: {
				ThrowingSpike spikes = new ThrowingSpike();
				spikes.quantity(2).identify();
				return spikes;
			}

			case CLERIC: {
				ScrollOfRemoveCurse sc = new ScrollOfRemoveCurse();
				sc.identify();
				return sc;
			}

			case GUNNER: {
				BulletBelt belt = new BulletBelt();
				belt.quantity(5);
				return belt;
			}

			case SAMURAI: {
				return new Sheath();
			}

			case ADVENTURER: {
				Machete machete = new Machete();
				machete.identify();
				return machete;
			}

			case KNIGHT: {
				return new KnightsShield();
			}

			case MEDIC: {
				return new GammaRayGun();
			}

			case ARCHER: {
				PocketKnife knife = new PocketKnife();
				knife.identify();
				return knife;
			}
		}
		return null;
	}

	// =========================================================
	// 클래스별 init (oneSlot에 따라 인벤 지급/퀵슬롯 최소화)
	// =========================================================
	private static void initWarrior( Hero hero, boolean oneSlot ) {
		(hero.belongings.weapon = new WornShortsword()).identify();

		if (!oneSlot) {
			ThrowingStone stones = new ThrowingStone();
			stones.identify().collect();
			Dungeon.quickslot.setSlot(0, stones);
		}

		if (hero.belongings.armor != null){
			hero.belongings.armor.affixSeal(new BrokenSeal());
			Catalog.setSeen(BrokenSeal.class);
		}

		new PotionOfHealing().identify();
		new ScrollOfRage().identify();
	}

	private static void initMage( Hero hero, boolean oneSlot ) {
		MagesStaff staff = new MagesStaff(new WandOfMagicMissile());

		(hero.belongings.weapon = staff).identify();
		hero.belongings.weapon.activate(hero);

		Dungeon.quickslot.setSlot(0, staff);

		new ScrollOfUpgrade().identify();
		new PotionOfLiquidFlame().identify();
	}

	private static void initRogue( Hero hero, boolean oneSlot ) {
		(hero.belongings.weapon = new Dagger()).identify();

		CloakOfShadows cloak = new CloakOfShadows();
		cloak.identify();

		if (oneSlot) {
			// ONE_SLOT_PACK: artifact 슬롯 금지 -> misc 슬롯로 착용
			hero.belongings.misc = cloak;
			hero.belongings.misc.activate(hero);
		} else {
			(hero.belongings.artifact = cloak).identify();
			hero.belongings.artifact.activate(hero);
		}

		if (!oneSlot) {
			ThrowingKnife knives = new ThrowingKnife();
			knives.identify().collect();
			Dungeon.quickslot.setSlot(1, knives);
		}

		Dungeon.quickslot.setSlot(0, cloak);

		new ScrollOfMagicMapping().identify();
		new PotionOfInvisibility().identify();
	}

	private static void initHuntress( Hero hero, boolean oneSlot ) {

		(hero.belongings.weapon = new Gloves()).identify();

		if (!oneSlot) {
			SpiritBow bow = new SpiritBow();
			bow.identify().collect();
			Dungeon.quickslot.setSlot(0, bow);
		}

		new PotionOfMindVision().identify();
		new ScrollOfLullaby().identify();
	}

	private static void initDuelist( Hero hero, boolean oneSlot ) {

		(hero.belongings.weapon = new Rapier()).identify();
		hero.belongings.weapon.activate(hero);

		if (!oneSlot) {
			ThrowingSpike spikes = new ThrowingSpike();
			spikes.quantity(2).identify().collect();
			Dungeon.quickslot.setSlot(1, spikes);
		}

		Dungeon.quickslot.setSlot(0, hero.belongings.weapon);

		new PotionOfStrength().identify();
		new ScrollOfMirrorImage().identify();
	}

	private static void initCleric( Hero hero, boolean oneSlot ) {

		(hero.belongings.weapon = new Cudgel()).identify();
		hero.belongings.weapon.activate(hero);

		HolyTome tome = new HolyTome();
		tome.identify();

		if (oneSlot) {
			// ONE_SLOT_PACK: artifact 슬롯 금지 -> misc 슬롯로 착용
			hero.belongings.misc = tome;
			hero.belongings.misc.activate(hero);
		} else {
			(hero.belongings.artifact = tome).identify();
			hero.belongings.artifact.activate(hero);
		}

		Dungeon.quickslot.setSlot(0, tome);

		new PotionOfPurity().identify();
		new ScrollOfRemoveCurse().identify();
	}

	private static void initGunner( Hero hero, boolean oneSlot ) {

		(hero.belongings.weapon = new AR_T1()).identify();
		hero.belongings.weapon.activate(hero);

		if (!oneSlot) {
			BulletBelt bulletBelt = new BulletBelt();
			bulletBelt.quantity(5).collect();

			PocketKnife pocketKnife = new PocketKnife();
			pocketKnife.identify().collect();

			Dungeon.quickslot.setSlot(1, pocketKnife);
			Dungeon.quickslot.setSlot(2, bulletBelt);
		}

		Dungeon.quickslot.setSlot(0, hero.belongings.weapon);

		new PotionOfHaste().identify();
		new ScrollOfTeleportation().identify();
	}

	private static void initSamurai( Hero hero, boolean oneSlot ) {

		WornKatana wornKatana = new WornKatana();
		(hero.belongings.weapon = wornKatana).identify();

		if (!oneSlot) {
			Sheath sheath = new Sheath();
			sheath.collect();

			ThrowingKnife knives = new ThrowingKnife();
			knives.identify().collect();

			Dungeon.quickslot.setSlot(0, sheath);
			Dungeon.quickslot.setSlot(1, knives);
		}

		new ScrollOfRetribution().identify();
		new PotionOfStrength().identify();
	}

	private static void initAdventurer( Hero hero, boolean oneSlot ) {

		Shovel shovel = new Shovel();
		(hero.belongings.weapon = shovel).identify();
		hero.belongings.weapon.activate(hero);

		if (!oneSlot) {
			Machete machete = new Machete();
			machete.identify().collect();

			ThrowingStone stones = new ThrowingStone();
			stones.identify().collect();

			Dungeon.quickslot.setSlot(1, machete);
			Dungeon.quickslot.setSlot(2, stones);
		}

		Dungeon.quickslot.setSlot(0, shovel);

		new ScrollOfMagicMapping().identify();
		new PotionOfPurity().identify();
	}

	private static void initKnight( Hero hero, boolean oneSlot ) {

		Saber saber = new Saber();
		(hero.belongings.weapon = saber).identify();
		hero.belongings.weapon.activate(hero);

		if (!oneSlot) {
			KnightsShield shield = new KnightsShield();
			shield.collect();

			ThrowingStone stones = new ThrowingStone();
			stones.identify().collect();

			Dungeon.quickslot.setSlot(0, stones);
		}

		new ScrollOfRemoveCurse().identify();
		new PotionOfParalyticGas().identify();
	}

	private static void initMedic( Hero hero, boolean oneSlot ) {

		Scalpel scalpel = new Scalpel();
		(hero.belongings.weapon = scalpel).identify();
		hero.belongings.weapon.activate(hero);

		MedicKit kit = new MedicKit();
		kit.identify();

		if (oneSlot) {
			// ONE_SLOT_PACK: artifact 슬롯 금지 -> misc 슬롯로 착용
			hero.belongings.misc = kit;
			hero.belongings.misc.activate(hero);
		} else {
			(hero.belongings.artifact = kit).identify();
			hero.belongings.artifact.activate(hero);
		}

		if (!oneSlot) {
			GammaRayGun gammaRayGun = new GammaRayGun();
			gammaRayGun.collect();
			Dungeon.quickslot.setSlot(0, gammaRayGun);
			Dungeon.quickslot.setSlot(1, kit);
		} else {
			Dungeon.quickslot.setSlot(0, kit);
		}

		new ScrollOfMirrorImage().identify();
		new PotionOfHealing().identify();
	}

	private static void initArcher( Hero hero, boolean oneSlot ) {

		WornShortBow bow = new WornShortBow();
		(hero.belongings.weapon = bow).identify();
		hero.belongings.weapon.activate(hero);

		if (!oneSlot) {
			PocketKnife pocketKnife = new PocketKnife();
			pocketKnife.identify().collect();

			BulletBelt bulletBelt = new BulletBelt();
			bulletBelt.quantity(3).collect();

			ArrowBag arrowBag = new ArrowBag();
			arrowBag.collect();

			Dungeon.quickslot.setSlot(1, pocketKnife);
			Dungeon.quickslot.setSlot(2, arrowBag);
			Dungeon.quickslot.setSlot(3, bulletBelt);
		}

		Dungeon.quickslot.setSlot(0, hero.belongings.weapon);

		new ScrollOfMagicMapping().identify();
		new PotionOfHaste().identify();
	}

	// =========================================================
	// UI/설명/언락 등 기존 코드
	// =========================================================
	public Badges.Badge masteryBadge() {
		switch (this) {
			case WARRIOR:    return Badges.Badge.MASTERY_WARRIOR;
			case MAGE:       return Badges.Badge.MASTERY_MAGE;
			case ROGUE:      return Badges.Badge.MASTERY_ROGUE;
			case HUNTRESS:   return Badges.Badge.MASTERY_HUNTRESS;
			case DUELIST:    return Badges.Badge.MASTERY_DUELIST;
			case CLERIC:     return Badges.Badge.MASTERY_CLERIC;
			case GUNNER:     return Badges.Badge.MASTERY_GUNNER;
			case SAMURAI:    return Badges.Badge.MASTERY_SAMURAI;
			case ADVENTURER: return Badges.Badge.MASTERY_ADVENTURER;
			case KNIGHT:     return Badges.Badge.MASTERY_KNIGHT;
			case MEDIC:      return Badges.Badge.MASTERY_MEDIC;
			case ARCHER:     return Badges.Badge.MASTERY_ARCHER;
		}
		return null;
	}

	public String title() {
		return Messages.get(HeroClass.class, name());
	}

	public String desc(){
		return Messages.get(HeroClass.class, name()+"_desc");
	}

	public String shortDesc(){
		return Messages.get(HeroClass.class, name()+"_desc_short");
	}

	public HeroSubClass[] subClasses() {
		return subClasses;
	}

	public ArmorAbility[] armorAbilities(){
		switch (this) {
			case WARRIOR: default:
				return new ArmorAbility[]{new HeroicLeap(), new Shockwave(), new Endure()};
			case MAGE:
				return new ArmorAbility[]{new ElementalBlast(), new WildMagic(), new WarpBeacon()};
			case ROGUE:
				return new ArmorAbility[]{new SmokeBomb(), new DeathMark(), new ShadowClone()};
			case HUNTRESS:
				return new ArmorAbility[]{new SpectralBlades(), new NaturesPower(), new SpiritHawk()};
			case DUELIST:
				return new ArmorAbility[]{new Challenge(), new ElementalStrike(), new Feint()};
			case CLERIC:
				return new ArmorAbility[]{new AscendedForm(), new Trinity(), new PowerOfMany()};
			case GUNNER:
				return new ArmorAbility[]{new Riot(), new ReinforcedArmor(), new FirstAidKit()};
			case SAMURAI:
				return new ArmorAbility[]{new Awake(), new ShadowBlade(), new Abil_Kunai()};
			case ADVENTURER:
				return new ArmorAbility[]{new Sprout(), new TreasureMap(), new Root()};
			case KNIGHT:
				return new ArmorAbility[]{new HolyShield(), new StimPack(), new UnstableAnkh()};
			case MEDIC:
				return new ArmorAbility[]{new HealingGenerator(), new AngelWing(), new GammaRayEmmit()};
			case ARCHER:
				return new ArmorAbility[]{new DashAbility(), new Hunt(), new Snipe()};
		}
	}

	public String spritesheet() {
		switch (this) {
			case WARRIOR: default: return Assets.Sprites.WARRIOR;
			case MAGE:            return Assets.Sprites.MAGE;
			case ROGUE:           return Assets.Sprites.ROGUE;
			case HUNTRESS:        return Assets.Sprites.HUNTRESS;
			case DUELIST:         return Assets.Sprites.DUELIST;
			case CLERIC:          return Assets.Sprites.CLERIC;
			case GUNNER:          return Assets.Sprites.GUNNER;
			case SAMURAI:         return Assets.Sprites.SAMURAI;
			case ADVENTURER:      return Assets.Sprites.ADVENTURER;
			case KNIGHT:          return Assets.Sprites.KNIGHT;
			case MEDIC:           return Assets.Sprites.MEDIC;
			case ARCHER:          return Assets.Sprites.ARCHER;
		}
	}

	public String splashArt(){
		switch (this) {
			case WARRIOR: default: return Assets.Splashes.WARRIOR;
			case MAGE:            return Assets.Splashes.MAGE;
			case ROGUE:           return Assets.Splashes.ROGUE;
			case HUNTRESS:        return Assets.Splashes.HUNTRESS;
			case DUELIST:         return Assets.Splashes.DUELIST;
			case CLERIC:          return Assets.Splashes.CLERIC;
			case GUNNER:          return Assets.Splashes.GUNNER;
			case SAMURAI:         return Assets.Splashes.SAMURAI;
			case ADVENTURER:      return Assets.Splashes.ADVENTURER;
			case KNIGHT:          return Assets.Splashes.KNIGHT;
			case MEDIC:           return Assets.Splashes.MEDIC;
			case ARCHER:          return Assets.Splashes.ARCHER;
		}
	}

	public boolean isUnlocked(){
		if (DeviceCompat.isDebug()) return true;

		switch (this){
			case WARRIOR: default: return true;
			case MAGE:            return Badges.isUnlocked(Badges.Badge.UNLOCK_MAGE);
			case ROGUE:           return Badges.isUnlocked(Badges.Badge.UNLOCK_ROGUE);
			case HUNTRESS:        return Badges.isUnlocked(Badges.Badge.UNLOCK_HUNTRESS);
			case DUELIST:         return Badges.isUnlocked(Badges.Badge.UNLOCK_DUELIST);
			case CLERIC:          return Badges.isUnlocked(Badges.Badge.UNLOCK_CLERIC);
			case GUNNER:          return Badges.isUnlocked(Badges.Badge.UNLOCK_GUNNER);
			case SAMURAI:         return Badges.isUnlocked(Badges.Badge.UNLOCK_SAMURAI);
			case ADVENTURER:      return Badges.isUnlocked(Badges.Badge.UNLOCK_ADVENTURER);
			case KNIGHT:          return Badges.isUnlocked(Badges.Badge.UNLOCK_KNIGHT);
			case MEDIC:           return Badges.isUnlocked(Badges.Badge.UNLOCK_MEDIC);
			case ARCHER:          return Badges.isUnlocked(Badges.Badge.UNLOCK_ARCHER);
		}
	}

	public String unlockMsg() {
		return shortDesc() + "\n\n" + Messages.get(HeroClass.class, name()+"_unlock");
	}
}