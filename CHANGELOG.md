# Changelog

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
