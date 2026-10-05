# TO-DO Features

Planned foundry features, implementation checklists, and optional enhancements.
Focus on getting the core systems working reliably before tackling visual polish.
Check off implementation tasks after they have been built and tested.
Open design questions remain provisional until agreed.

## Foundry mold - casting endpoint

The mold receives molten material and creates finished items. One block supports
manual player crafting, villager operation, and automation. All three should use
the same recipe validation and material-consumption rules.

### Agreed manual crafting behavior

- Use a nine-slot (3x3) grid with slots the player can enable or disable, similar to the vanilla crafter.
- Disabled slots remain empty and do not receive virtual metal ingredients.
- Real items, such as sticks, can occupy enabled slots.
- Empty enabled slots represent crafting ingredients supplied by the molten material, such as iron ingots or nuggets, as required by the selected recipe.
- Ghost images communicate these virtual metal ingredients; they are not real, extractable items.
- Find supported recipes that match the exact grid arrangement, molten material, and real ingredients.
- Update the selected recipe's output preview and availability automatically when the pattern, ingredients, or available material changes.
- No Pour button is required.
- Taking a valid output consumes the required real ingredients and exact amount of molten material, gives the finished item, and plays a hammer/anvil sound.
- Invalid patterns or insufficient resources cannot produce an item or consume material.

Target example: an iron sword.

```text
[disabled] [virtual iron ingot] [disabled]
[disabled] [virtual iron ingot] [disabled] -> [iron sword preview]
[disabled] [real stick]         [disabled]
```

The stick is supplied by the player or automation. The two ingot representations
come from molten iron. Recipe matching determines the result; the number of
enabled slots alone is not enough.

### Agreed manual recipe selection

Use the vanilla stonecutter's selectable results as the UI blueprint. The same
molten material and grid arrangement can support multiple recipes because the
virtual metal ingredients may represent different forms of that material.

- Select the recipe automatically when only one compatible recipe exists.
- Present selectable result previews when multiple compatible recipes exist.
- Selecting a result chooses the target recipe and updates the ghost ingredients and required molten-material amount.
- Keep the chosen recipe selected while it remains compatible with the configured grid and ingredients.
- Insufficient material makes the selected output unavailable; it must not switch automatically to a cheaper recipe.
- Revalidate the selection when the grid, real ingredients, or molten-material type changes, and clear it if the recipe no longer matches.
- Manual selection and item-frame selection should feed the same target-recipe and casting logic.

Example with all nine slots enabled and no real items in the grid:

| Selected result | Virtual ingredients | Required metal |
| --- | --- | --- |
| Iron ingot | Nine iron nuggets | One ingot's worth |
| Iron block | Nine iron ingots | One block's worth |

Exact mB costs remain part of the material-accounting design below.

### Agreed endpoint pickup and fluid recovery

- Deliberately picking up a mold discards the molten fluid stored inside it; the dropped mold item does not carry that fluid.
- Apply the same pickup rule to future foundry endpoint blocks.
- This is an explicit exception to the general material-preservation rule for stored endpoint fluid on player pickup.
- A mold that remains placed continues to retain its fluid through save/quit and reload.
- Plan bucket recovery through the UI: clicking the tank display with a bucket should let the player remove stored fluid before picking up the block.
- The fluid-discard exception does not authorize deleting real ingredient items or finished output items.
- Behavior for removal other than deliberate player pickup remains to be specified.

### Implementation checklist

- [x] Add the mold block, item, model, and block entity. Temporary vanilla-anvil appearance verified in game.
- [x] Receive compatible molten material through the NeoForge fluid-handler capability. Molten copper accepted; water supplied through Create pipes rejected.
- [x] Verify existing channels connect to the mold and deliver material correctly.
- [x] Save and restore the fluid reservoir. Fluid contents verified unchanged after save/quit and reload.
- [x] Add the empty-mold item drop and pickaxe mining support. Survival mining, item drop, and replacement verified; the replaced mold has an empty reservoir.
- [ ] Choose and implement a supported first casting recipe using the currently available material.
- [ ] Define exact fluid costs and output counts consistent with the melting system.
- [ ] Implement one shared casting operation for players, villagers, and machines.
- [ ] Validate resources and output space before committing a cast; failed operations leave resources intact.
- [x] Add the enabled/disabled grid configuration and save/load support. Slot-state data verified unchanged after save/quit and reload.
- [x] Reject real-item insertion into disabled slots. Hopper input verified: items remain in the hopper when all slots are disabled and enter the enabled slot when one is available.
- [x] Verify real-ingredient inventory persistence and occupied-slot protection during loading. Ingredient count, slot position, and grid configuration survive save/reload; loading leaves occupied slots enabled.
- [ ] Verify that `setSlotDisabled()` rejects disabling an occupied slot through a player interaction once the menu is available.
- [x] Drop real ingredients when the mold is mined. Survival mining returns the mold item and exact ingredient count; the replaced mold has an empty ingredient inventory.
- [ ] Add the manual menu and screen with a configurable 3x3 grid.
- [ ] Find all compatible target recipes, including recipes using different virtual forms of the same molten metal.
- [ ] Add a stonecutter-style result selector and automatically select an unambiguous recipe.
- [ ] Display virtual metal ingredients and update the recipe/output preview automatically.
- [ ] Update ghost ingredients and the displayed fluid cost when the selected result changes.
- [ ] Retain a compatible selection through material shortages and clear selections that no longer match.
- [ ] Commit a manual cast when the player takes the output, including ingredient remainders where applicable.
- [ ] Play the hammer/anvil sound only after a successful cast.
- [ ] Save and restore all owned material, real ingredients, grid configuration, selected recipe, and any finished output through world save/load.
- [ ] Add bucket extraction through the UI tank display for fluid recovery before endpoint pickup.
- [ ] Preserve real ingredients and finished items when the mold is removed, and preserve resources during interrupted casts except for the agreed endpoint-fluid pickup discard. Real-ingredient drops are implemented and verified for survival mining; finished output and interrupted casting remain to implement and test.
- [ ] Verify multiplayer interactions cannot duplicate output or spend the same resources twice.
- [ ] Add molten iron support and verify the iron-sword example when that material is available.
- [ ] Verify the full-grid iron-ingot/iron-block ambiguity, selection changes, exact consumption, and behavior when only the cheaper result is affordable.

### Villager workstation concept

The mold acts as a workstation for a line worker, with no trading interface.
An item frame attached to an available mold face specifies the desired output.
The displayed item selects a supported recipe and remains a reference item.
Production still requires the recipe's molten material and real ingredients.

- [ ] Allow an eligible villager to claim and reach the mold as a workstation.
- [ ] Select the target recipe from an attached item frame.
- [ ] Repeat the shared casting operation while the villager is working and resources/output space allow it.
- [ ] Stop when the frame or target item is removed, required resources run out, or output storage is full.
- [ ] Respect the intended daily work/rest schedule, leave for bed, and return to work afterward.
- [ ] Provide line-worker behavior without trades.
- [ ] Verify workstation ownership, frame changes, and save/reload behavior.

### Automation concept

Fluid supply, real-ingredient insertion, finished-item extraction, and triggering
a cast are separate responsibilities. Automation must never extract a ghost
ingredient or an output preview as though it were a stored item.

- [x] Expose the real ingredient inventory through the NeoForge item-handler capability. The current handler is available on all faces; hopper insertion verified.
- [ ] Expose finished-item output through the NeoForge item-handler capability.
- [ ] Define which faces allow fluid input, ingredient insertion, and output extraction.
- [ ] Define how automated equipment selects the recipe and triggers casting.
- [x] Test ordinary hopper ingredient input, including disabled-slot rejection.
- [ ] Test item-pipe ingredient input and automated extraction behavior for ingredients and finished output.
- [ ] Investigate and test Create deployer operation through a block interaction.
- [ ] Investigate and test Create mechanical-arm access, including any required integration.
- [ ] Define and investigate the desired Create mechanical-press interaction.
- [ ] Keep Create integrations optional so the mold also works without Create installed.

### Open design questions

- Fluid ownership: a saved 4,000 mB reservoir is implemented with a recognized-foundry-material filter. Capacity remains provisional.
- Bucket recovery: decide how to handle amounts below a bucket's capacity, bucket compatibility, and inventory space for the recovered fluid item.
- Decide fluid behavior when endpoints are removed by causes other than deliberate player pickup.
- Recipe policy: decide how vanilla/modded crafting recipes are supported and how their virtual metal ingredients and fluid costs are determined.
- Material accounting: choose costs that preserve material through casting and melting, including partial amounts and multi-item outputs.
- Decide whether cooling time is part of casting and what happens if a cast is interrupted.
- Decide the villager work schedule, production rate, eligible villagers, and target selection if multiple frames or recipes apply.
- Decide how an item-frame target configures the grid and receives any required real ingredients.
- Decide where automated finished items wait and what action triggers each automated cast.
- Decide how manual configuration interacts with an active villager or machine operator.

## Basin heat visualization

- [ ] Add a visual indication of basin temperature to the stone basin walls.

The stone should retain its normal appearance when cool, then develop a red glow
that becomes more intense as the basin approaches its tier's maximum temperature.
The current stone tier maximum is 2,800 degrees Fahrenheit.

Possible implementation:

- Reuse the existing straight, inner-corner, and outer-corner wall geometry.
- Enable tinting on selected model faces and choose their color with a client color handler.
- Represent rough heat levels with a small blockstate property, potentially eight stages.
- Derive the visual stage from the basin's authoritative temperature; keep rendering separate from heat and melting logic.
- Consider modest light emission at higher temperatures.
- Later, consider an emissive texture layer for surfaces that remain bright in darkness.

Behavior to verify when implemented:

- Walls change appearance consistently as the basin heats and cools.
- The display reflects temperature even when the basin contains no material.
- All wall shapes retain their existing geometry and connections.
- Appearance remains correct after save/reload and for other players.
- The visual effect does not change melting, storage, or transport behavior.
