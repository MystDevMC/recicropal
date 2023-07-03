# 1.0.0
## Additions
- A `CHANGELOG.md`. Yay! (This is technically release notes but oh well).
- This mod's seeds and gourd bottles are now added into villager house loot tables
- Bottle gourd patches now spawn as a vegetation feature in vanilla savanna biomes, in a similar manner to pumpkins.
- New structures are added to villages
  - A small pumpkin garden can now be found in taiga villages
  - A drinking well can now be found in desert villages
- A new data-driven drinking system. The player can now drink the contents of gourd
  bottles and trigger effects.
  - By default, there's already recipes for water, honey, milk, and potion. How
    to write these fluids in the recipes would need a documentation
  - Honey, milk, and potion would need other mods since this mod does not come with
    those fluids.
  - Added a new recipe loading condition `recicropal:fluid_tag_empty`, accepting a
    property `tag`
  - Added a few `drink_result_type`s:
    - `recicropal:finish_item` would trigger the effects of given property `item`
      when it finished being used.
    - `recicropal:finish_item_transfer_nbt`. Similar to above but transfers the NBT from
      fluid to the item. Its only use case would be with potion items, assuming other modders
      actually putting the `Potion` tag to their fluids.
    - `recicropal:heal`, `recicropal:zap`, and `recicropal:set_fire` for debug purposes.
      They might be replaced one day.

## Updates
- Gourd bottles now can be emptied in the crafting table.
- Now, only savanna and desert villagers accept trading bottle gourds and water-filled gourd bottles.

## Fixes
- Fix minor gourd bottle placement issue (I hope).
- Empty gourd bottles now drop as clean items (without NBT).