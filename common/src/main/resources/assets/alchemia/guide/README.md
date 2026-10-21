# Primer pages

Each section of the Alchemist's Primer (the guide pages reached from the top-right corner of the Contents) has its own
text file here, one folder per language: `<language>/<section>.txt`, such as `en_us/bases.txt`. To translate the
primer, copy `en_us` to your language's code (`de_de`, `ja_jp`, ...) and translate the files. A resource pack can ship
them too, under `assets/alchemia/guide/<language>/`. Any section your language doesn't have is shown in English.

Which sections exist, their order, and which chapter belong where are hardcoded in `GuidePrimer.CHAPTERS`.

## A section's file

```
Potion Bases

@photo bases_water bases_honey
@items center #bases
The text of the section, as it shows in the guide.
```

1. **Line 1** is the section's title. For a chapter's first section it is the chapter's name, shown as
   "II. Potion Bases"; for the others it is the small heading at the top of the section.
2. **Line 2** is the chapter's line in the primer's index. Leave it blank to use the title. Only a chapter's first
   section uses it.
3. **Everything after that** is the section, laid out from top to bottom.

A section takes a single page by default, the next section will start on the page after it. A chapter will always opens on a left-hand
page, so a blank page is put in before it when needed. 

`@page` starts a new page for the section, everything written below it goes onto that page, which opens with a break bar. 

It can be used as many times as you like; an `@page` with nothing
after it leaves a blank page for the section.

```
Lures

@photo lure_1 lure_2 lure_3
What a lure is. This sits under the photo on the section's first page.
@page
How to queue one. This starts at the top of the next page.
```

Text wraps by itself; don't wrap lines yourself. Every line you write starts a new line in the guide, and a blank
line adds a small gap between paragraphs. When a page is full the section carries on to the next page anyway, so
nothing is ever cut off, and text never runs into the page number.

## Directives

A line starting with `@` places something on the page instead of text:

| Directive | What it places |
|---|---|
| `@photo [size=..] [align=..] [angle=..] name name ...` | A framed picture from `textures/gui/guide/primer/<name>.png`. With more than one name, small arrows flip between them in order. See below for the options. |
| `@items <align> slot slot ...` | A row of up to 4 item slots. `<align>` is `left`, `center` or `right` and can be left out (centre). Text flows beside a `left` or `right` row. |
| `@craft recipe recipe ...` | A crafting grid filled in from the game's own crafting recipes, named by recipe id (`minecraft:glass_bottle`). Name several to flip between them. Ingredients that accept several items cycle through them. |
| `@manual_craft result*count "row" "row" "row" K=item ...` | A crafting grid you lay out yourself. Rows are quoted, up to 3 characters each; each `K=item` says what a letter stands for. |
| `@break` | A break bar. |
| `@page` | Starts a new page; everything below it goes there. |

`@craft` and `@manual_craft` lines written one after another join into a single grid with arrows to flip between
them, in the order written.

### Photo options

| Option | Values |
|---|---|
| `size=` | `wide` (the default, 256×144), `small` (192×108), `square` (144×144) or `portrait` (108×144). The image file must be that size. |
| `align=` | `left`, `center` (the default) or `right`. Text flows beside a `left` or `right` photo. |
| `angle=` | A tilt in degrees, from `-15` to `15`, turning the photo about its centre. Meant for decoration. |

An item slot is an item id, such as `minecraft:cauldron`. Join ids with `|` to make the slot cycle through them:
`minecraft:honey_block|minecraft:honey_bottle`. `#bases` fills in one slot per potion base, cycling through supported items.

## Unlocking a chapter

A chapter is open from the start unless its first section has `@unlock` lines. Each one names something the player
must have done; once all of them are true, the chapter opens for good (it stays open even if, say, the item is put
away later) and a "Chapter Unlocked" toast shows its numeral.

| Line | Needs |
|---|---|
| `@unlock` | The "Bubble, Bubble" advancement (making an Alchemical Cauldron). |
| `@unlock advancement <id>` | The advancement `<id>`, such as `minecraft:story/mine_diamond`. |
| `@unlock hold <item>` | The item somewhere in the player's inventory, such as `alchemia:mortar_and_pestle`. |

Until then the index shows the chapter as "???", and its tooltip lists what is still needed. Only a chapter's first
section is checked for `@unlock`.

## Links

Write `[[target|words]]` anywhere in the text to make `words` a link. The target is one of the guide's chapters
(`recipes`, `effects`, `ingredients`, `opus`, `contents`) or a primer chapter as `primer:<chapter>`, such as
`primer:bases`.

## Translation information 

The primer's buttons, the index heading, the locked "???" line and its tooltip, as well as the "%s. %s" chapter heading
format are in `lang/<language>.json`, under `alchemia.guide.primer.*`.
