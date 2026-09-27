# Forgotten Relics + — high-version port

**English** | [简体中文](README.md)

A **1.21.1 / NeoForge** port of **Forgotten Relics**.

Forgotten Relics was created by **Integral** (a.k.a. Extegral / VictorShadow). It is an add-on built around
**Thaumcraft** and **Botania**, centred on *discovering and wielding powerful relics*. This repository brings it
to a modern version (1.21.1 + NeoForge): the numbers, recipes and research structure follow the 1.7.10 original
as closely as possible, while the code is **re-implemented** against the 1.21.1 APIs rather than transliterated.

- MCMod.cn entry: <https://www.mcmod.cn/class/28217.html>
- 1.7.10 original (**the only behavioural reference and asset source**): <https://github.com/jss2a98aj/Forgotten-Relics>
- 1.12.2 port (used for behaviour comparison only, see "Code provenance" below): <https://github.com/NNYYOONNIIOO/Forgotten-Relics-RE>
- Issue tracker: <https://github.com/beiwucn/Forgotten-Relics-Plus/issues>

---

## Licence

**This project is licensed under the [MIT](https://opensource.org/license/mit) licence.**

Licence chain:

| Project | Version | Author | Licence |
| --- | --- | --- | --- |
| Forgotten Relics (original) | 1.7.10 | Integral / Extegral / VictorShadow | [WTFPL](https://github.com/jss2a98aj/Forgotten-Relics/blob/master/LICENSE) |
| Forgotten Relics RE (1.12.2 port) | 1.12.2 | NNYYOONNIIOO et al. | [CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/deed.en) (**no code from it is used here**) |
| **Forgotten Relics + (this project)** | 1.21.1 | beiwu, gali2009 et al. | **[MIT](https://opensource.org/license/mit)** |

### Code provenance

The **only behavioural reference for this project is the 1.7.10 original** (WTFPL, which attaches no conditions).
All code is written independently against the 1.21.1 / NeoForge APIs and **contains no code from the 1.12.2
port (RE)**. That port is licensed CC BY-NC-SA 4.0, so building on its code would drag in both the
non-commercial and the share-alike restrictions — which is exactly why this project moved to MIT.
RE is used only to compare in-game behaviour; item textures and text come straight from the 1.7.10 original.

> **About the name**: this project ports the original **Forgotten Relics**, so it keeps the original's name.
> The 1.12.2 port rebranded its Chinese name to "失落遗物学：重现"; that name belongs to that port's author
> and is deliberately not used here. The "+" is this project's own suffix, marking this 1.21.1 branch only —
> it does not imply any endorsement by either upstream author.

### You are free to

- use this for **anything, including commercial purposes** (paid modpacks, monetised servers, ...);
- modify, redistribute and ship it closed-source, or relicense it;
- your only obligation is to keep the copyright notice and the licence text.

### Please note

- MIT covers **this project's code and documentation only**. Minecraft itself, and dependencies such as
  Thaumaturge, Curios and Botania, have their own licences — comply with each of them.
- This is an **unofficial** port. It is not affiliated with, nor endorsed by, the original author or the
  author of the 1.12.2 port.
- Copyright in the original artwork, research text and game design remains with the original authors.

---

## Requirements

| Component | Version |
| --- | --- |
| Minecraft | 1.21.1 |
| NeoForge | 21.1.250 or newer |
| Java | 21 |

### Dependencies

All of these are **required**:

| Mod | Version | Notes |
| --- | --- | --- |
| [Thaumaturge](https://www.curseforge.com/minecraft/mc-mods/thaumaturge) | 0.4.0+ | Thaumcraft for modern versions; provides arcane crafting, aspects and the research system |
| [Botania](https://www.curseforge.com/minecraft/mc-mods/botania) | 457+ | Provides some materials and the wand API |
| [Curios](https://www.curseforge.com/minecraft/mc-mods/curios) | 9.0.0+ | Accessory slots (the 1.12.2 equivalent of Baubles) |
| [TerraBlender](https://www.curseforge.com/minecraft/mc-mods/terrablender) | 4.1.0.0+ | A Thaumaturge dependency, declared here as well |

> Note: Botania's version must be written as `457-SNAPSHOT` to match. The version declared inside the jar is
> `457-SNAPSHOT`, which by Maven ordering sorts **below** `457`, so asking for `457` is instead judged
> as "version not satisfied".

---

## Project layout

```
src/main/java/com/beiwu/forgottenrelics_re/
├── ForgottenRelics.java        # main mod class
├── FRCommonEvents.java         # behaviour dispatcher: knows no concrete item, just forwards events
├── api/                        # behaviour interfaces (wearer tick, incoming damage, break speed, ally protection, rechargeable)
├── client/                     # client rendering and keybinds (curio rendering, model layer registration)
├── config/FRConfig.java        # config (equivalent of the original RelicsConfigHandler)
├── items/                      # item implementations; each implements the behaviour interfaces it needs
├── network/                    # network payloads
├── registry/                   # item, armour material, data component and creative tab registration
└── utils/                      # helpers (cooldowns, worn-item iteration, sounds, damage types, ...)

src/main/resources/
├── assets/forgotten_relics/    # textures, models, language files
│   └── textures/models/armor/  # armour layer textures
├── data/forgotten_relics/
│   ├── damage_type/            # custom damage types
│   ├── recipe/infusion/        # infusion recipes
│   └── thaumaturge/            # research category and entries
└── data/curios/tags/item/      # accessory slot assignment
```

**All code comments are written in Chinese.**

---

## Credits

- **Integral** (Extegral / VictorShadow) — author of the original Forgotten Relics;
- **NNYYOONNIIOO** — the 1.12.2 port Forgotten Relics RE, used here only to compare behaviour;
- The **Thaumaturge** team — Thaumcraft for modern versions;
- The **Botania** and **Curios** teams — dependencies.
