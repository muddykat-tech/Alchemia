# Changelog

## 1.2

- All ingredients now have textures thanks to Azlan, everything feels way more cohesive and polished due to this as well :D 
- The cauldron now takes one active ingredient at a time. An Add Ingredient button creates the internal queue that the brew plan follows, a more complex route can now be planned in advance.
- The potion map is now measured in pixels rather than grid cells. Paths are now splines sampled along a length, This allows all ingredients to have more control in the path and curve smoothly rather than just listing a path via stepping cells, rotation, grinding and erasing move the path work by pixels now as well instead of by cell.
- Dead space is now a smooth signed field rather than a grid of blocks the edges are interpolated between samples, so blobs, rings and the hollows around effects all have curved outlines, and a brew's path will stop exactly where the drawn edge is rather than at a block boundary.
- Rough rings of dead space now wall the map into layers between the rarity bands, with a few gaps left in each ring so a brew can still walk through without teleporting. Ring count, thickness, gaps and roughness are configurable.
- Dead space has more options based on the brew base via a "dead_space" field. Water has gated rings. Honey has a field of patches scattered across the whole map that thicken the further they sit from the centre. Patch size and how much calmer the middle is than the rim are configurable.
- The active ingredient now shows its full potential path with an X where it currently ends, while queued ingredients show the committed route. 
- The brew marker now travels along the path instead of jumping cell to cell.
- The map's centre is marked with a hollow ink square, and the faint threads that lead to each effect now point to that marker rather than the player brew icon.
- Paths are drawn at three times the resolution before being scaled back down, so curves and the end markers look better. Every path is now using the same standard that Potion Craft itself uses.
- A teleport's endpoint is marked with a small spiral instead of the X that normal paths use.
- A teleport's committed path now follows the path it actually takes instead of cutting straight to the end, spirals and other paths now fills in correctly when ground.
- Fable Bismuth has a square spiral now.
- Geodes can no longer be found above ground, fixed the bug that was added when increased height range was used for mountain biome spawns. They still have the old height range, but a geode now only generates when it has at least 16 blocks of cover above it.
- Crystals now generate as five kinds of geode instead of one, and geodes of every kind are considerably rarer than they were - a standard geode keeps is the same as the old implementation, but much rarer. Miniature geodes are five blocks across with a single cluster in the middle, and are the common variant. Giant geodes are very deep and rare, some can be roomy enough to build in, and often have crystal spikes that taper up from the floor and down from the ceiling. Supermassive geodes are twice as rare again and better than twice the size, can have spikes up to twenty blocks long. Surface geodes are half buried in the ground with a mouth open to the sky.
- A surface geode has a top covering based on the biome.      
- Crystal blocks can now be crafted back into four of their crystals.
- Each plant now draws its growth stages from its own texture folder instead of one shared set per plant type, same way that crystals did. Plants with no art will fall back to the shared placeholder stage by stage, so art can be added a plant or even a single stage at a time.
- Evergreen Fern, Goldthorn and Thornstick now grow two blocks tall. The top half appears once the crop is halfway grown, and needs open air above it to do so; breaking either half harvests the plant.
- Mageberry is now a two-block berry bush. Once mature it ripens into a fruiting state, and right-clicking a fruiting bush picks its berries and sends it back to mature so it can fruit again. Bone meal still works on it.
- Tangleweed grows as a network. A mature Tangleweed node sends branches, up to four blocks away along a connected chain, and no branch ever touches more than three others. Cutting a branch drops everything grown beyond it, and breaking the breaks the entire plant, all drop the tangleweed ingredient.
- Flameweed now grows like kelp.
- Sulphur Shelf is now a fungus that grows on the sides of living trees instead of on the floor. Its spores infect a log and an infected log keeps sprouting young shelves on its open sides as long as leaves of its own tree are within four blocks. Spores can also be planted directly on the side of an infected log. Wild infected trunks turn up in forests, taigas, jungles and swamps.
- Added an ingredient tool slot to the cauldron menu, in its own panel below the brew status. It takes the mortar and pestle, nigredo, albedo or citrinitas, names itself after whatever is in it, and holding its button works the active ingredient so grind, erase or rotate etc.
- The path preview in an ingredient's tooltip is now drawn on a torn scrap of parchment with a hard dark edge instead of a plain box and is now centred in the tooltip rather than hugging its left edge.
- Windbloom, Terraria, Lava Root, Bloodthorn, Dragon Pepper, Icefruit and Mageberry now have proper paths of their own.
- Added support to update the default placement of the unground ingredients, currently done for Windbloom, Terraria, Lava Root, Icefruit and Mageberry.
- The dashes of a path no longer slide along it as an ingredient is ground. The unreached part keeps its hollow dashes where they are and grinding fills them in. In the grinding overlay and the cauldron the mark now moves along the curve to its new place rather than jumping and or cutting corners.
- Cloud Crystal's teleport now has an S-shaped path instead of a straight line, swinging right and then left as it climbs.
- The Alchemist's Guide now records an ingredient as soon as you have held it, rather than when you first add it to a cauldron. A guide in your inventory notes every ingredient you are carrying and every one you have picked up before, and checks again whenever it is opened. Recipes and effects are still recorded by brewing.
- The brewing details on an ingredient's tooltip, and the path shown while holding Shift, now need an Alchemist's Guide in your inventory. Without one, ingredients, monster drops and brew modifiers show no brewing lines at all; keeps the mod a bit cleaner.
- The "Alchemia: Brewing Effect" heading on tooltips is a darker tan now.
- Brewing is now done by hand. The cauldron has a Stir button: hold it and the brew moves slowly and smoothly along the planned route, using up each ingredient's path as it goes. Holding Shift stirs at a quarter of the speed for fine placement. Let go and the brew stays exactly where it is.
- Effects are no longer picked up automatically. The Brew button lights up while the potion icon is within an effect's reach, and pressing it adds that effect at the strength on offer there. It will not replace an effect with a weaker one, and it will not overload the brew. If Redstone is in its slot, it'd be added on the same press.
- Ingredients added by hand or thrown into the cauldron are now added to the planned path, rather than instant brewing. 
- Teleport ingredients now have nice animations.
- Paths on the cauldron's map are drawn in a dark brown ink at about three-quarters opacity now.
- Ingredient pages in the Alchemist's Guide use the same parchment path panel as the item tooltips, laid over the book page, with the flask, mortar and landing marks. The alignment now sits under the name, the teleport distance is gone, and a Notes section below the path is set aside for lore / ingredient info.
- Path dashes and teleport dots are painted from small brush-stroke sprites instead of solid blocks. The cauldron's GUI has the strokes drawn at twice the tooltip size and are sized to the map rather than the screen, so zooming in or out keeps a path looking the same instead of re-spacing its dashes.
- Potency no longer appears on ingredient tooltips or on the guide's effect pages.
- Some effects now depend on the world around the cauldron. Dolphin's Grace requires rain, Bad (Raid) Omen and Trial Omen can only be brewed whilst under a new moon at night, and Breath of the Nautilus only when the cauldron is in a beach or an ocean biome. This is configured in the server config under world_effects.
- While its conditions are unmet, such an effect shows on the potion map as a flat grey mark on an even grey circle, and it cannot be brewed. The circle's rim lights up softly while the brew is within its reach. Hovering it only says it is dormant until a brew has been stirred into its reach; after that the guide remembers what it is waiting for, a note fades in over the cauldron screen to say so, and the hover, the Brew button and the guide's effect page all show it.
- Some effects are only partly held back. They can always be brewed at level I, but their stronger levels wait on the world: Levitation reaches level II with the cauldron at Y 90 or higher and level III there at night, and Luck reaches level II at night under a half moon or fuller and level III under a full moon. Brewing in their core while a level is held back gives the strongest level on offer instead. On the map the held-back part of the circle is greyed over, and once your guide has noted the effect the hover, the Brew button and the guide's effect page list what each level needs. These are set in the same server config, with a level after the effect name (Levitation@2 = above:90), and new above:, below: and moonlight: conditions are available.
- Monster Drops became a "lure" type ingredient, this essentially means that they only drags part of the planned path, on their own they do nothing.
- Recorded recipes now remember how far each stretch was stirred and when Brew was pressed, so the guide replays a brew exactly. Recipes recorded before this change replay as a full stir and a brew for each ingredient - will need to test effects of this; luckily the mod isn't super popular so won't affect a lot of ppl that much
- Grinding has twice as many steps (24 instead of 12) and takes the same time overall, so an ingredient's landing point tuned better.
- On 26.x only for now: the 1.20.1 cauldron still has the six-slot layout and brews the whole queue at once, will be updated soon, this entry will be removed once done.
- Added block versions of potions for decoration / storage

## 1.1.2

- Added a Minecraft 26.3 build.
- The potion map's noise generator is now part of the mod instead of borrowing Minecraft's, this makes sure every version generates the same map from the same world seed.
- Merged the separate version branches into a single IDE project, so Alchemia now builds for multiple Minecraft versions from one source tree.
- Fixed several alchemist guide bugs and stopped duplicate recipes from being recorded.
- Improved root generation so roots are easier to find in world, and polished the Alchemy Machine menu and block particles.

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
