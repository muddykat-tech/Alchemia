
# Alchemia FAQ

## Can Alchemia support custom effects from another mod?

<div class="spoiler">
<p>Not at the moment, but in the future this will be possible.</p>
<p>Currently the effects you can brew come from a fixed list built into Alchemia, so effects added by other mods won't appear on the potion map.</p>
</div>

## Can you add new potion bases?

<div class="spoiler">
<p>Yes, with no code. Bases are defined in <code>config/alchemia-brew-bases.json</code>, which by default have a Water and Honey base.</p>
<p>To add an entry to the <code>bases</code> list, use the following template:</p>
<pre><code>{
  "id": "milk",
  "fills": [
    { "item": "minecraft:milk_bucket", "levels": 4, "remainder": "minecraft:bucket" }
  ],
  "tint": "#F2F2F2",
  "texture": "minecraft:block/white_concrete",
  "instability_cap": 3,
  "effects": ["Regeneration", "Healing", "Saturation"],
  "dead_space": "field"
}</code></pre>
<ul>
<li><strong><code>fills</code></strong>: what item adds the base into the cauldron. Use either an <code>item</code> (with an optional <code>remainder</code> handed back) or a <code>fluid</code> (with an optional <code>amount</code> in millibuckets). <code>levels</code> is how much it fills by valid range is 1 to 4.</li>
<li><strong><code>effects</code></strong>: which effects can appear on this base's map. If blank it will attempt to add all effects (not recommended, but possible)</li>
<li><strong><code>dead_space</code></strong>: The type of noise that creates the <code>"rings"</code> (layered walls with gaps) or <code>"field"</code> (scattered patches).</li>
<li><strong>Other options</strong> are tint, biome tint, texture, instability cap and unbrewed result.</li>
<li><strong>Map layout:</strong> each base has its own potion map, so effects are in different places on each one.</li>
<li><strong>Display name:</strong> add a translation for <code>alchemia.base.&lt;id&gt;</code> (for example in a resource pack). Without one the name shows as the raw key.</li>
</ul>
</div>

## Can you remove or add conditions for potion effects?

<div class="spoiler">
<p>Yes. Conditions are set via the server config <code>alchemia-server.toml</code> under <code>[world_effects]</code>, in the <code>conditions</code> list. Each entry is set as <code>Effect = condition, condition, ...</code>.</p>
<pre><code>[world_effects]
conditions = [
  "Dolphins_Grace = rain",
  "Levitation@2 = above:90",
  "Levitation@3 = above:90, night",
  "Darkness = biome:minecraft:deep_dark",
  "Fire_Protection = biome:#minecraft:is_nether|minecraft:desert"
]</code></pre>
<ul>
<li><strong>Effect</strong>: an Alchemia effect name (<code>Dolphins_Grace</code>) or an effect id (<code>minecraft:dolphins_grace</code>).</li>
<li><strong><code>@level</code></strong>: with this suffix (<code>Levitation@2</code>), the effect can always be brewed, but that level and every level above it need the listed conditions.</li>
<li><strong>Conditions</strong>:
<ul>
<li>Weather and time: <code>rain</code>, <code>thunder</code>, <code>clear</code>, <code>day</code>, <code>night</code>.</li>
<li>Moon phase: <code>full_moon</code>, <code>new_moon</code>, <code>moonlight:&lt;0-1&gt;</code> (<code>0.5</code> is a half moon). Moon conditions also need night.</li>
<li>Cauldron height: <code>above:&lt;y&gt;</code>, <code>below:&lt;y&gt;</code>.</li>
<li>Biome: <code>biome:&lt;id or #tag&gt;|...</code>, which allows the effect if the cauldron's biome matches any of the listed ids or tags.</li>
</ul>
</li>
</ul>
<p><strong>To remove a condition</strong>, delete its line. To make every effect always brewable, empty the list (<code>conditions = []</code>).</p>
<p><strong>Imbuements still apply.</strong> A brew imbued with bark or a biome item counts as being in that biome or at that altitude, so it can still activate biome- and height-locked effects. These items are configured separately in <code>config/alchemia-imbuements.json</code>.</p>
<p><strong>Existing worlds</strong> have the condition list in the config file created with the world itself. You'll just need to edit the file in that world's <code>serverconfig</code> folder to change them.</p>
</div>
