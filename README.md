# Creeper Pastel - Fabric 1.21.1

Mod de Fabric con el Creeper Pastel (modelo de Blockbench, textura incluida).

## Compilar
1. Instala JDK 21.
2. En esta carpeta: `gradle wrapper --gradle-version 8.10.2` (una vez) y luego `./gradlew build`
   (o abre la carpeta en IntelliJ IDEA y deja que importe el proyecto).
3. El mod queda en `build/libs/creeper-pastel-1.0.0.jar` -> ponlo en `mods/` junto con Fabric API 1.21.1.

## Probar
`./gradlew runClient` y en el juego: `/summon creeperpastel:creeper_pastel` o el huevo en la pestana de huevos generadores.

## Notas
- Se comporta como un creeper (explota, mecha, sonidos). Aparece en el Overworld (ver `CreeperPastelMod.java`).
- El modelo se carga desde `models/creeper_pastel_quads.json` (generado del .bbmodel; original en `blockbench/`).
- Animaciones: idle (cuerpo sube/baja) y walk (patas +-5 grados). La animacion `death` del .bbmodel esta vacia, se usa la vanilla.
- El .rar (`pastel.png`, `pastel1.png`, `pastel2.png`, `font/default.json`) es un resource pack de fuente/particulas, no hace falta para la entidad y no lo inclui.

## Sin instalar Java (GitHub Actions)
1. Crea un repo en github.com y sube todo el contenido de esta carpeta (incluida `.github`).
2. Ve a la pestana Actions -> "build" -> espera a que termine (unos 5 min).
3. Descarga el artefacto `creeper-pastel-mod` -> dentro esta el `.jar` (usa el que NO termina en `-sources`).

## Mancha de pantalla
Al explotar el Creeper Pastel, los jugadores a menos de 16 bloques ven trozos de pastel en la pantalla (~6 s, se desvanece).
Usa `textures/gui/splat1.png` y `splat2.png` (de pastel.png / pastel1.png del pack).
