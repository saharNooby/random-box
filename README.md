# RandomBox
A Spigot plugin adding lootboxes with inventory based GUI.

[SpigotMC plugin page](https://www.spigotmc.org/resources/randombox.3217/) | [Plugin documentation](https://dev.bukkit.org/projects/random_box/pages/main/documentation)

## Build

To build, you need:
- Maven
- JDK 8
- Mojang's authlib 1.5.25 installed into the local repository (it is usually provided with Minecraft client): `mvn install:install-file "-Dfile=.../.minecraft/libraries/com/mojang/authlib/1.5.25/authlib-1.5.25.jar" "-DgroupId=com.mojang" "-DartifactId=authlib" "-Dversion=1.5.25" -Dpackaging=jar`
- `buildSettings.properties` file with `outputDir` property set to path where you want the JAR to appear

`git clone` the repository, `cd` into its dir and run `npm clean package`.