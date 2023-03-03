# ModTheSpire #
ModTheSpire is a tool to load external mods for Slay the Spire without modifying the base game files.

## Usage ##
### Installation ###
1. Download the latest [Release](https://github.com/kiooeht/ModTheSpire/releases).
2. Copy `ModTheSpire.jar` to your Slay the Spire install directory.
    * For Windows, copy `MTS.cmd` to your Slay the Spire install directory.
    * For Linux, copy `MTS.sh` to your Slay the Spire install directory and make it executable.
3. Create a `mods` directory. Place mod JAR files into the `mods` directory.

### Install from sources ###
1. Clone [ModTheSpire](https://gitlab.com/squedgy/ModTheSpire)
2. Create an `install.properties` file in the root of ModTheSpire with a `spire.dir` property whose value is a path to your Slay The Spire directory
3. Execute ModTheSpire's `setupLocal` and `install` tasks
    * `gradlew setupLocal install` 

### Running Mods ###
1. Run ModTheSpire.
    * For Windows, run `MTS.ps1`.
    * For Linux, run `MTS.sh`.
      * You should run the scripts regardless. They prefer using whatever version of java is on your path
      * AND they set necessary flags for playing using modular java versions (9+)
2. Select the mod(s) you want to use.
3. Press 'Play'.

---

## For Modders ##
### Requirements ###
* JDK 8+
* Gradle

### General ###
* ModTheSpire automatically sets the Settings.isModded flag to true, so there is no need to do that yourself.
* [Wiki](https://github.com/kiooeht/ModTheSpire/wiki/SpirePatch)

### Building ###
1. Run `gradlew build`

---

## Changelog ##
See [CHANGELOG](CHANGELOG.md)

## Contributors ##
* kiooeht - Original author
* t-larson - Multi-loading, mod initialization, some UI work
* test447 - Some launcher UI work, Locator
* reckter - Maven setup
* FlipskiZ - Mod initialization
* pk27602017 - UTF-8 support in ModInfo


## Structure

- [agent](/agent) is the basis of the Java agent especially necessary for modular (JDK 9+) java versions.
    - This is really only necessary starting sometime after 11
  (I don't care enough to track down versions 12, 13, 15, 16, and other versions not provided by SDK man)
- [patches](/patches) directory is the base patches that aren't specific to a version of LWJGL
- [corepatches-lwjgl2](/corepatches-lwjgl2) and [corepatches-lwjgl3](corepatches-lwjgl3) directories handle
patches specific to their versions of LWJGL
- [kotlin](/kotlin) and [lwjgl3](/lwjgl3) are used to have shaded/fat jars for their respective libraries
- [core](/core) is the actual ModTheSpire application. It handles finding and executing mod's patches, as well as
displaying the GUI you interact with upon running ModTheSpire
