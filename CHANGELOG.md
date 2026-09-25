# Changelog

## 1.1.2

- Added a Minecraft 26.3 build.
- The potion map's noise generator now ships with the mod instead of borrowing Minecraft's, so every version generates the same map from the same world seed.
- Merged the separate version branches into a single IDE project, so Alchemia now builds for multiple Minecraft versions from one source tree.
- Fixed several alchemist guide bugs and stopped duplicate recipes from being recorded.
- Improved root generation so roots are easier to find in world, and polished the Alchemy Machine menu and block particles.
- Tangleweed, Goldthorn, Good Berry, Ice Fruit, Thornstick and Dragon Pepper now have paths of their own instead of sharing the generic herb wave.
- The cauldron now takes one active ingredient at a time. An Add Ingredient button builds an internal queue that the brew follows, so a full route can still be planned in advance.
- The potion map is now measured in pixels rather than grid cells. Paths are splines sampled along their length, so ingredients curve smoothly instead of stepping cell by cell, and rotation, grinding and erasing move the path by a real distance instead of a whole cell.
- Effects, dead space and the brew's own position all live in that pixel space, so the map reads as a continuous field. Dead space is drawn from a tile mask with level of detail, so zooming out no longer costs a fill per cell.
- Rough rings of dead space now wall the map into layers between the rarity bands, with a few gaps left in each ring so a brew can still walk through without teleporting. Ring count, thickness, gaps and roughness are configurable.
- Dead space now comes in two shapes, chosen per brew base with a "dead_space" field. Water keeps the gated rings. Honey uses a field instead: no walls, just small patches scattered across the whole map that thicken the further they sit from the centre, so difficulty there is navigation rather than gates. Patch size and how much calmer the middle is than the rim are configurable.
- The clearing dead space leaves around an effect is wider now, and its edge is wobbled by the same noise, so effects sit in a rough hollow rather than a cut square.
- The active ingredient now shows its full potential path with an X where it currently ends, while queued ingredients show the committed route. Grinding, rotating or erasing lerps the preview into its new shape.
- The brew marker now travels along the path at a steady speed instead of hopping cell to cell.
- Path previews in the guide, item tooltips and the grinding overlay now draw the curve itself, scaled to fit, with the same X and end markers.
- Paths are drawn in ink now, at three times the resolution before being scaled back down, so curves and the end markers read cleanly instead of stepping. Every path is broken rather than solid: a normal path dashes, a teleport dots, and the part of a path an ingredient cannot reach yet is drawn as a hollow outline that picks up exactly where the reachable part stops.
- A teleport's landing is marked with a small spiral instead of the X that normal paths use.
- Monster drops no longer carve a path of their own. They now lure the brew: adding one drags the path already queued behind it toward that drop's effect, stopping at the previous drop of the same kind so the pull cannot be stacked over and over. Harvested vanilla items keep their fixed bearing.
- Added an ingredient tool slot to the cauldron menu, in its own panel below the brew status. It takes the mortar and pestle, nigredo, albedo or citrinitas, names itself after whatever is in it, and holding its button works the active ingredient - grind, erase or rotate - filling in the path as it goes.

## 1.0.0 (Minecraft 26.2 / NeoForge)

Ported from 1.18.2 Forge.

- Ingredients now travel a multi-cell path instead of a single jump / teleport.
- Per ingredient paths: straight, wave, arc, spiral, and hand-authored routes.
- Crushed ingredient variants removed. Crushing is now a 0-12 step property on the item itself.
- Added the Mortar and Pestle. Grinding extends an ingredient's path.
- Added dead space to the map. Prevents normal ingredients from pathing into it, teleports into it will ruin the potion.
- Crystals now have a teleportation effect.
- Effects now cover a 5x5 area at potency 3 / 2 / 1 by distance.
- Effect count limit replaced with a potency cap of 5, configurable up to 10.
- Over-filling a potion brew now ruins it instead of emptying the cauldron.
- Cauldron interface rebuilt: zoom, pan, ingredient queue, path preview, potion animations, potency panel.
- Added redstone, gunpowder and dragon's breath slots for duration, splash and lingering potions.
- Guide book rebuilt with separate recipe, effect and ingredient pages.
- Brews can now be named and recorded as recipes and can be replayed from the guide.
- Vanilla items work as ingredients. Mob drops have a homeing effect for their vanilla potion counterpart, although harvested items just have a fixed path that points in the direction of their effect regardless of the world seed.
- Added the Magnum Opus crafting chain and the Philosopher's Stone.
- Added advancements and JEI support.
- Worldgen moved to data generation, with ingredients placed in matching biomes.
- Added crystal geodes with growing clusters.
- Redrawn and resized several item textures but these are still programmer art at best so any help would be appreicated.
