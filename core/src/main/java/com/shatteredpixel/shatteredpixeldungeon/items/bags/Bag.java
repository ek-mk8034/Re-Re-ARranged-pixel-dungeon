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

package com.shatteredpixel.shatteredpixeldungeon.items.bags;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LostInventory;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KingsCrown;
import com.shatteredpixel.shatteredpixeldungeon.items.TengusMask;
import com.shatteredpixel.shatteredpixeldungeon.items.changer.OldAmulet;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndQuickBag;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.Iterator;

public class Bag extends Item implements Iterable<Item> {

	public static final String AC_OPEN	= "OPEN";

	{
		image = 11;

		defaultAction = AC_OPEN;

		unique = true;
	}

	public Char owner;

	public ArrayList<Item> items = new ArrayList<>();

	public int capacity(){
		return 20; // default container size
	}

	//if an item is being quick-used from the bag, the bag should take on its targeting properties
	public Item quickUseItem;

	@Override
	public int targetingPos(Hero user, int dst) {
		if (quickUseItem != null){
			return quickUseItem.targetingPos(user, dst);
		} else {
			return super.targetingPos(user, dst);
		}
	}

	@Override
	public void execute( Hero hero, String action ) {
		quickUseItem = null;

		super.execute( hero, action );

		if (action.equals( AC_OPEN ) && !items.isEmpty()) {

			GameScene.show( new WndQuickBag( this ) );

		}
	}

	@Override
	public boolean collect( Bag container ) {

		grabItems(container);

		//if there are any quickslot placeholders that match items in this bag, assign them
		for (Item item : items) {
			Dungeon.quickslot.replacePlaceholder(item);
		}

		if (super.collect( container )) {

			owner = container.owner;

			Badges.validateAllBagsBought( this );

			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onDetach( ) {
		this.owner = null;
		for (Item item : items) {
			Dungeon.quickslot.clearItem(item);
		}
		updateQuickslot();
	}

	public void grabItems(){
		if (owner != null && owner instanceof Hero && this != ((Hero) owner).belongings.backpack) {
			grabItems(((Hero) owner).belongings.backpack);
		}
	}

	public void grabItems( Bag container ){
		for (Item item : container.items.toArray( new Item[0] )) {
			if (canHold( item )) {
				int slot = Dungeon.quickslot.getSlot(item);
				item.detachAll(container);
				if (!item.collect(this)) {
					item.collect(container);
				}
				if (slot != -1) {
					Dungeon.quickslot.setSlot(slot, item);
				}
			}
		}
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	public void clear() {
		items.clear();
	}

	public void resurrect() {
		for (Item item : items.toArray(new Item[0])){
			if (!item.unique) items.remove(item);
		}
	}

	private static final String ITEMS	= "inventory";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( ITEMS, items );
	}

	//temp variable so that bags can load contents even with lost inventory debuff
	private boolean loading;

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );

		loading = true;
		for (Bundlable item : bundle.getCollection( ITEMS )) {
			if (item != null){
				if (!((Item)item).collect( this )){
					//force-add the item if necessary, such as if its item category changed after an update
					items.add((Item) item);
				}
			}
		}
		loading = false;
	}

	public boolean contains( Item item ) {
		for (Item i : items) {
			if (i == item) {
				return true;
			} else if (i instanceof Bag && ((Bag)i).contains( item )) {
				return true;
			}
		}
		return false;
	}

	// =========================================================
	// ONE_SLOT_PACK helpers
	// =========================================================
	private boolean isOneSlotChallengeActive() {
		// owner가 null인 타이밍(로드 등)에서도 챌린지 상태만으로 막고 싶어서
		// Dungeon.isChallenged만 사용
		return Dungeon.isChallenged(Challenges.ONE_SLOT_PACK);
	}

	private boolean isOneSlotExempt(Item item) {
		// 11층/21층 보스 아이템 + 사원 OldAmulet은 1칸 제한과 무관하게 허용
		return item instanceof TengusMask
				|| item instanceof KingsCrown
				|| item instanceof OldAmulet;
	}

	private int countNonExemptItems() {
		int c = 0;
		for (Item it : items) {
			if (it != null && !isOneSlotExempt(it)) c++;
		}
		return c;
	}

	public boolean canHold( Item item ){
		if (!loading && owner != null && owner.buff(LostInventory.class) != null
				&& !item.keptThroughLostInventory()){
			return false;
		}

		// ---------------------------------------------------------
		// ONE_SLOT_PACK rules
		// - No bags can be carried at all (VelvetPouch, SeedPouch, etc.)
		// - Only ONE non-exempt item may exist in the backpack
		// - Exempt items (Tengu mask, King crown, OldAmulet) always allowed
		// - Stacking into the single allowed non-exempt item is allowed
		// ---------------------------------------------------------
		if (!loading && isOneSlotChallengeActive()) {

			// 1) 가방류 아이템(= Bag 상속)은 절대 소지 불가
			if (item instanceof Bag) return false;

			// 2) 예외 아이템은 제한 무시
			if (isOneSlotExempt(item)) return true;

			// 3) 일반 아이템은 딱 1개만 허용
			//    (단, 같은 스택에 합쳐지는 건 허용)
			if (countNonExemptItems() >= 1) {

				// 같은 스택 합치기만 허용
				if (item.stackable) {
					for (Item i : items) {
						if (i != null && !isOneSlotExempt(i) && item.isSimilar(i)) {
							return true;
						}
					}
				}

				return false;
			}

			// 일반 아이템이 아직 0개면 아래 기본 로직으로 계속 진행
		}

		// ---------------------------------------------------------
		// Default logic
		// ---------------------------------------------------------
		if (items.contains(item) || item instanceof Bag || items.size() < capacity()){
			return true;
		} else if (item.stackable) {
			for (Item i : items) {
				if (item.isSimilar( i )) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Iterator<Item> iterator() {
		return new ItemIterator();
	}

	private class ItemIterator implements Iterator<Item> {

		private int index = 0;
		private Iterator<Item> nested = null;

		@Override
		public boolean hasNext() {
			if (nested != null) {
				return nested.hasNext() || index < items.size();
			} else {
				return index < items.size();
			}
		}

		@Override
		public Item next() {
			if (nested != null && nested.hasNext()) {

				return nested.next();

			} else {

				nested = null;

				Item item = items.get( index++ );
				if (item instanceof Bag) {
					nested = ((Bag)item).iterator();
				}

				return item;
			}
		}

		@Override
		public void remove() {
			if (nested != null) {
				nested.remove();
			} else {
				items.remove( index );
			}
		}
	}
}