# 1.0.0

## Additions

### Mod

- This mod's seeds and gourd bottles are now added into villager house loot tables
- Bottle gourd patches now spawn as a vegetation feature in vanilla savanna biomes, in a similar manner to pumpkins.
- New structures are added to villages
    - A small pumpkin garden can now be found in taiga villages
    - A drinking well can now be found in desert villages
- Gourd bottles now can be used to drink liquids which would trigger in-game effects.
  Refer to below for further info.
- Gourd bottles now can be used to mix potions and allow multiple potions to be drunk
  at the same time.
    - The proportion of potions inside the mixture is based on actual real-world concept of
      molarity.
    - The length of potions is scaled based on their proportions. For example, mixing
      a 3-minute strength and 3-minute invisibility potions would give a 1.5-minute of both
      when drunk.
    - Different types of potion also modifies the resulting length of each drink.
        - Splash potions averages the durations of all effects added. This averaging effect
          is controlled by the proportions too.
            - The more splash potion is in the mix, the more well-distributed the durations.
        - Lingering potions extends the duration of applied effects per drink depending on
          their proportions in the mix.
            - The more lingering potion is in the mix, the longer the duration of each effects
              per drink.
            - The maximum length can be achieved here is twice the effect's length. This happens
              when 100% of the potions in the mix were all lingering.
    - Instantaneous effects will wait until enough "moles" or amounts of serving has been drunk.
      Like other amounts measured in drinks, this defaults to 250.
        - For instance, the player needs to drink 1000 mBs worth of mixture that has 25%
          instant healing to receive the healing.
        - This stacks with other mixtures of the same effect and amplifier.
- Grown bottle gourds now will rot and dry, turning into gourd bottles filled with seeds
  and surprise!

### API

- A `CHANGELOG.md`. Yay! (This is technically release notes but oh well).
- Configs. Common and client side.
- Added a new recipe loading condition `recicropal:fluid_tag_empty`, accepting a
  property `tag`
- Added a new recipe loading condition `recicropal:debug` for debug recipes.
- A new data-driven drinking system. The player can now drink the contents of gourd
  bottles and trigger effects.
    - The recipe structure can be described as below.
        - `fluid` either a fluid `tag` or `fluid` ID.
            - If `nbt` is provided, it will match for the NBT structured as-is since
              this uses vanilla's `Codec` API.
        - `amount` specifies the amount of drunk fluid.
            - Defaults to allow drinking below the configured drinking amount
              (250 mB by default) if not specified.
        - `results` accepts an array of `drink_result_types` specified below.
    - By default, this mod comes with honey, milk, and potion fluids and by default
      they already have recipes.
    - Currently, there's only a few `drink_result_type`s:
        - `recicropal:finish_item` would trigger the effects of given property `item`
          when it finished being used.
        - `recicropal:finish_item_transfer_nbt`. Similar to above but transfers the NBT from
          fluid to the item. Its only use case would be with potion items, assuming other modders
          actually putting the `Potion` tag to their fluids.
        - `recicropal:heal`, `recicropal:zap`, and `recicropal:set_fire` for debug purposes.
          They might be replaced one day.
- Data-driven filling system for gourd bottles. This allows non-`IFluidHandler` items
  to be used to fill the bottle with configured liquid. The recipe's structure is described
  as follows.
    - `ingredient` accepts items like how vanilla does.
    - A `fluid` property.
        - If `tag` is specified. It will harshly pick any fluid that's available in the game
          with that tag. `fluid` can be used for referring a specific fluid ID instead. Do not
          specify both as it won't make any sense.
        - `amount` which accepts number of amount it should fill.
        - `nbt` which accepts an NBT structured as-is to apply to the fluid.
    - `result` accepts a serialized ItemStack. Specifying its `Count` and `id` is obligatory. Add
      `tag` for NBTs.
    - A special property named `process`. If this exists, it'll use a hardcoded function
      with the ID specified. Only `recicropal:potion` exists currently and should only be used for
      potions.
- Data-driven pouring system for gourd bottles. Similar to filling but in reverse with
  a few additions.
    - `fluid` property behaves like the fluid property for drinking recipes.
    - `amount`. Again, similar to drinking.
- Data-driven mixing system for gourd bottles.
    - Fluids can be mixed into the bottle when they differ. This triggers when the bottle
      itself is tried to be filled as a block and not an item.
    - The recipe structured is described as follows.
        - `fluidIn` and `fluidInside` accepts fluid ingredients similar to drinking recipes.
        - There's no `amount` property as this checks only by fluid ID and NBT.
        - `result` accepts a fluid similar to filling recipes' `fluid` tag except that if no
          `amount` property is specified, it resorts to just adding the `amounts` of both
          amounts of mixed fluids.
        - A special property named `process`. Similar to other aforementioned recipes.
- Potion mixing currently uses built-in potion and mixture fluids.

## Updates

- Gourd bottles now can be emptied in the crafting table.
- Now, only savanna and desert villagers accept trading bottle gourds and water-filled gourd bottles.
- Potions, honey bottles, and milk buckets now can be used to fill gourd bottles.
- Gourd bottle blocks now show the fluid contained graphically.
- Gourd bottle blocks now emit comparator signal.
- Gourd bottles now can only be placed while sneaking.
- Gourd bottles capacity can now be configured.
- Gourd bottle blocks transfer amount can now be configured.

## Fixes

- Fix minor gourd bottle placement issue (I hope).
- Empty gourd bottles now drop as clean items (without NBT).
- Fix gourd bottles not saving when chunk is not updated.
- Fix trellises not dropping the correct amount of items.
- Fix vine crop base plants having wrong hitboxes when they start to attach to the sides.
- Fix buckets disappearing when failed filling bottles.
- Fix gourd bottles becoming unusable when swapping to a different bottle mid-drinking.