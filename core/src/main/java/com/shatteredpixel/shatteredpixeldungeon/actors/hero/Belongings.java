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

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LostInventory;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.KindofMisc;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ShardOfOblivion;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Iterator;

public class Belongings implements Iterable<Item> {

	private Hero owner;

	public static class Backpack extends Bag {
		{
			image = ItemSpriteSheet.BACKPACK;
		}
		public int capacity(){
			int cap = super.capacity();
			for (Item item : items){
				if (item instanceof Bag){
					cap++;
				}
			}
			if (Dungeon.hero != null && Dungeon.hero.belongings.secondWep != null){
				//secondary weapons still occupy an inv. slot
				cap--;
			}
			return cap;
		}
	}

	public Backpack backpack;

	public Belongings( Hero owner ) {
		this.owner = owner;

		backpack = new Backpack();
		backpack.owner = owner;
	}

	public KindOfWeapon weapon = null;
	public Armor armor = null;

	// NOTE: ONE_SLOT_PACK에서는 artifact/ring 슬롯이 "사용 불가"가 되어야 함.
	public Artifact artifact = null;
	public KindofMisc misc = null;
	public Ring ring = null;

	//used when thrown weapons temporary become the current weapon
	public KindOfWeapon thrownWeapon = null;

	//used to ensure that the duelist always uses the weapon she's using the ability of
	public KindOfWeapon abilityWeapon = null;

	//used by the champion subclass
	public KindOfWeapon secondWep = null;

	//*** these accessor methods are so that worn items can be affected by various effects/debuffs
	// we still want to access the raw equipped items in cases where effects should be ignored though,
	// such as when equipping something, showing an interface, or dealing with items from a dead hero

	private boolean oneSlotPack(){
		return Dungeon.isChallenged(Challenges.ONE_SLOT_PACK);
	}

	//normally the primary equipped weapon, but can also be a thrown weapon or an ability's weapon
	public KindOfWeapon attackingWeapon(){
		if (thrownWeapon != null) return thrownWeapon;
		if (abilityWeapon != null) return abilityWeapon;
		return weapon();
	}

	//we cache whether belongings are lost to avoid lots of calls to hero.buff(LostInventory.class)
	private boolean lostInvent;
	public void lostInventory( boolean val ){
		lostInvent = val;
	}

	public boolean lostInventory(){
		return lostInvent;
	}

	public KindOfWeapon weapon(){
		if (!lostInventory() || (weapon != null && weapon.keptThroughLostInventory())){
			return weapon;
		} else {
			return null;
		}
	}

	public Armor armor(){
		if (!lostInventory() || (armor != null && armor.keptThroughLostInventory())){
			return armor;
		} else {
			return null;
		}
	}

	/**
	 * ONE_SLOT_PACK: artifact 슬롯은 완전 비활성.
	 * -> 항상 null 처리 (효과/표시/사용 모두 봉인)
	 */
	public Artifact artifact(){
		if (oneSlotPack()) return null;

		if (!lostInventory() || (artifact != null && artifact.keptThroughLostInventory())){
			return artifact;
		} else {
			return null;
		}
	}

	public KindofMisc misc(){
		if (!lostInventory() || (misc != null && misc.keptThroughLostInventory())){
			return misc;
		} else {
			return null;
		}
	}

	/**
	 * ONE_SLOT_PACK: ring 슬롯은 완전 비활성.
	 * -> 항상 null 처리
	 */
	public Ring ring(){
		if (oneSlotPack()) return null;

		if (!lostInventory() || (ring != null && ring.keptThroughLostInventory())){
			return ring;
		} else {
			return null;
		}
	}

	public KindOfWeapon secondWep(){
		if (!lostInventory() || (secondWep != null && secondWep.keptThroughLostInventory())){
			return secondWep;
		} else {
			return null;
		}
	}

	// ***

	private static final String WEAPON		= "weapon";
	private static final String ARMOR		= "armor";
	private static final String ARTIFACT   = "artifact";
	private static final String MISC       = "misc";
	private static final String RING       = "ring";

	private static final String SECOND_WEP = "second_wep";

	public void storeInBundle( Bundle bundle ) {

		backpack.storeInBundle( bundle );

		bundle.put( WEAPON, weapon );
		bundle.put( ARMOR, armor );

		// ONE_SLOT_PACK이면 artifact/ring은 저장해도 로드시 정리되지만,
		// 애초에 룰상 비활성이므로 그대로 저장해도 무방함.
		bundle.put( ARTIFACT, artifact );
		bundle.put( MISC, misc );
		bundle.put( RING, ring );

		bundle.put( SECOND_WEP, secondWep );
	}

	public static boolean bundleRestoring = false;

	public void restoreFromBundle( Bundle bundle ) {
		bundleRestoring = true;
		backpack.clear();
		backpack.restoreFromBundle( bundle );

		weapon = (KindOfWeapon) bundle.get(WEAPON);
		if (weapon() != null)       weapon().activate(owner);

		armor = (Armor)bundle.get( ARMOR );
		if (armor() != null)        armor().activate( owner );

		artifact = (Artifact) bundle.get(ARTIFACT);
		if (artifact != null)       artifact.activate(owner);

		misc = (KindofMisc) bundle.get(MISC);
		if (misc() != null)         misc().activate( owner );

		ring = (Ring) bundle.get(RING);
		if (ring != null)           ring.activate( owner );

		secondWep = (KindOfWeapon) bundle.get(SECOND_WEP);
		if (secondWep() != null)    secondWep().activate(owner);

		// ✅ ONE_SLOT_PACK 규칙 강제 적용(로드 후에도 artifact/ring 슬롯 봉인)
		enforceOneSlotPackEquipmentRule();

		bundleRestoring = false;
	}

	public void clear(){
		backpack.clear();
		weapon = secondWep = null;
		armor = null;
		artifact = null;
		misc = null;
		ring = null;
	}

	public static void preview( GamesInProgress.Info info, Bundle bundle ) {
		if (bundle.contains( ARMOR )){
			Armor armor = ((Armor)bundle.get( ARMOR ));
			if (armor instanceof ClassArmor){
				info.armorTier = 6;
			} else {
				info.armorTier = armor.tier;
			}
		} else {
			info.armorTier = 0;
		}
	}

	//ignores lost inventory debuff
	public ArrayList<Bag> getBags(){
		ArrayList<Bag> result = new ArrayList<>();

		result.add(backpack);

		for (Item i : this){
			if (i instanceof Bag){
				result.add((Bag)i);
			}
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public<T extends Item> T getItem( Class<T> itemClass ) {

		boolean lostInvent = lostInventory();

		for (Item item : this) {
			if (itemClass.isInstance( item )) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					return (T) item;
				}
			}
		}

		return null;
	}

	public<T extends Item> ArrayList<T> getAllItems( Class<T> itemClass ) {
		ArrayList<T> result = new ArrayList<>();

		boolean lostInvent = lostInventory();

		for (Item item : this) {
			if (itemClass.isInstance( item )) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					result.add((T) item);
				}
			}
		}

		return result;
	}

	public boolean contains( Item contains ){

		boolean lostInvent = lostInventory();

		for (Item item : this) {
			if (contains == item) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					return true;
				}
			}
		}

		return false;
	}

	public Item getSimilar( Item similar ){

		boolean lostInvent = lostInventory();

		for (Item item : this) {
			if (similar != item && similar.isSimilar(item)) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					return item;
				}
			}
		}

		return null;
	}

	public ArrayList<Item> getAllSimilar( Item similar ){
		ArrayList<Item> result = new ArrayList<>();

		boolean lostInvent = lostInventory();

		for (Item item : this) {
			if (item != similar && similar.isSimilar(item)) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					result.add(item);
				}
			}
		}

		return result;
	}

	//triggers when a run ends, so ignores lost inventory effects
	public void identify() {
		for (Item item : this) {
			item.identify(false);
		}
	}

	public void observe() {
		if (weapon() != null) {
			if (ShardOfOblivion.passiveIDDisabled() && weapon() instanceof Weapon){
				((Weapon) weapon()).setIDReady();
			} else {
				weapon().identify();
				Badges.validateItemLevelAquired(weapon());
			}
		}
		if (secondWep() != null){
			if (ShardOfOblivion.passiveIDDisabled() && secondWep() instanceof Weapon){
				((Weapon) secondWep()).setIDReady();
			} else {
				secondWep().identify();
				Badges.validateItemLevelAquired(secondWep());
			}
		}
		if (armor() != null) {
			if (ShardOfOblivion.passiveIDDisabled()){
				armor().setIDReady();
			} else {
				armor().identify();
				Badges.validateItemLevelAquired(armor());
			}
		}

		// artifact/ring 슬롯은 ONE_SLOT_PACK에서 완전히 비활성이라 accessor로 건드리지 않음
		if (!oneSlotPack()) {
			if (artifact() != null) {
				artifact().identify();
				Badges.validateItemLevelAquired(artifact());
			}
			if (ring() != null) {
				if (ShardOfOblivion.passiveIDDisabled()){
					ring().setIDReady();
				} else {
					ring().identify();
					Badges.validateItemLevelAquired(ring());
				}
			}
		}

		if (misc() != null) {
			if (ShardOfOblivion.passiveIDDisabled() && misc() instanceof Ring){
				((Ring) misc()).setIDReady();
			} else {
				misc().identify();
				Badges.validateItemLevelAquired(misc());
			}
		}

		if (ShardOfOblivion.passiveIDDisabled()){
			GLog.p(Messages.get(ShardOfOblivion.class, "identify_ready_worn"));
		}
		for (Item item : backpack) {
			if (item instanceof EquipableItem || item instanceof Wand) {
				item.cursedKnown = true;
			}
		}
		Item.updateQuickslot();
	}

	public void uncurseEquipped() {
		// ONE_SLOT_PACK에서는 artifact/ring은 무시되고 misc만 의미있음
		if (oneSlotPack()){
			ScrollOfRemoveCurse.uncurse( owner, armor(), weapon(), null, misc(), null, secondWep());
		} else {
			ScrollOfRemoveCurse.uncurse( owner, armor(), weapon(), artifact(), misc(), ring(), secondWep());
		}
	}

	public Item randomUnequipped() {
		if (owner.buff(LostInventory.class) != null) return null;

		return Random.element( backpack.items );
	}

	public int charge( float charge ) {

		int count = 0;

		for (Wand.Charger charger : owner.buffs(Wand.Charger.class)){
			charger.gainCharge(charge);
			count++;
		}

		return count;
	}

	/**
	 * ONE_SLOT_PACK: "misc 슬롯만 허용"
	 * - artifact/ring 슬롯에 뭐가 들어있으면 misc로 이동(비어있을 때)
	 * - misc가 이미 차있으면 backpack으로 이동(collect)
	 * - 최종적으로 artifact/ring은 null이 됨
	 */
	public void enforceOneSlotPackEquipmentRule(){
		if (!oneSlotPack()) return;

		// 1) artifact 처리
		if (artifact != null){
			if (misc == null && artifact instanceof KindofMisc){
				misc = (KindofMisc) artifact;
				misc.activate(owner);
			} else {
				// misc가 이미 있거나 캐스팅 불가면 인벤으로
				artifact.collect();
			}
			artifact = null;
		}

		// 2) ring 처리
		if (ring != null){
			if (misc == null && ring instanceof KindofMisc){
				misc = (KindofMisc) ring;
				misc.activate(owner);
			} else {
				ring.collect();
			}
			ring = null;
		}
	}

	@Override
	public Iterator<Item> iterator() {
		return new ItemIterator();
	}

	private class ItemIterator implements Iterator<Item> {

		private int index = 0;

		private Iterator<Item> backpackIterator = backpack.iterator();

		private Item[] equipped = {weapon, armor, artifact, misc, ring, secondWep};
		private int backpackIndex = equipped.length;

		@Override
		public boolean hasNext() {

			// ONE_SLOT_PACK에서는 artifact/ring 슬롯은 비활성이므로, 여기서도 사실상 null이 되도록
			// enforceOneSlotPackEquipmentRule()가 항상 선행되는 구조임.
			for (int i=index; i < backpackIndex; i++) {
				if (equipped[i] != null) {
					return true;
				}
			}

			return backpackIterator.hasNext();
		}

		@Override
		public Item next() {

			while (index < backpackIndex) {
				Item item = equipped[index++];
				if (item != null) {
					return item;
				}
			}

			return backpackIterator.next();
		}

		@Override
		public void remove() {
			switch (index) {
				case 0:
					equipped[0] = weapon = null;
					break;
				case 1:
					equipped[1] = armor = null;
					break;
				case 2:
					equipped[2] = artifact = null;
					break;
				case 3:
					equipped[3] = misc = null;
					break;
				case 4:
					equipped[4] = ring = null;
					break;
				case 5:
					equipped[5] = secondWep = null;
					break;
				default:
					backpackIterator.remove();
			}
		}
	}
}