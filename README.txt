Alchemia — source installation information for modders
------------------------------------------------------
This project targets Minecraft 26.2 on NeoForge, and is built with
ModDevGradle. Java 25 is required; Gradle will download a matching JDK
automatically via the foojay toolchain resolver.

Setup
=====
Import build.gradle into IntelliJ IDEA or Eclipse as a Gradle project.
ModDevGradle generates the run configurations during the Gradle sync — there
is no separate genIntellijRuns/genEclipseRuns step.

Useful commands (use ./gradlew on Mac/Linux, gradlew on Windows):

  ./gradlew build            Build the mod jar into build/libs
  ./gradlew runClient        Launch the development client
  ./gradlew runServer        Launch the development dedicated server
  ./gradlew runData          Run both data generators (see below)

Data generation
===============
Generated resources are split across two roots because each data generator run
deletes output files its own providers did not produce:

  src/generated/client   assets (block states, block/item models)  - runClientData
  src/generated/server   data (recipes, loot tables, world gen, tags) - runServerData

Both roots are on the mod's resource path. `./gradlew runData` runs both.

Versions are declared in gradle.properties (minecraft_version, neo_version).
If your IDE is missing libraries, run `./gradlew --refresh-dependencies`;
`./gradlew clean` resets the build output.
