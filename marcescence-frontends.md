# Frontend Setup Guide

Export your game shortcuts from Winlator as `.desktop` files and drop them into your frontend's library folder. Then configure the frontend to launch them with star using the instructions below.

---

## General / ADB

```sh
adb shell am start \
  -n com.winlator.star/.XServerDisplayActivity \
  --activity-clear-task \
  --activity-clear-top \
  --ez launched_from_frontend true \
  -e shortcut_path /storage/emulated/0/ROMs/windows/game.desktop
```

> `--ez launched_from_frontend true` tells star to exit cleanly back to the frontend when the game closes, rather than restarting the star UI.

---

## Daijishou

Go to **Settings → Players → Add player** (or edit an existing one). Fill in **Player am start arguments**:

```
-n com.winlator.star/.XServerDisplayActivity
--activity-clear-task
--activity-clear-top
--ez launched_from_frontend true
-e shortcut_path {file.path}
```

Set **Player accepted filename regex** to:

```
^(.*)\.desktop$
```

---

## ES-DE

Add this to your `custom_systems/es_find_rules.xml`:

```xml
<emulator name="WINLATOR-STAR">
    <rule type="androidpackage">
        <entry>com.winlator.star/.XServerDisplayActivity</entry>
    </rule>
</emulator>
```

And in `es_systems.xml`:

```xml
<system>
    <name>windows</name>
    <fullname>Microsoft Windows</fullname>
    <path>%ROMPATH%/windows</path>
    <extension>.desktop .DESKTOP</extension>
    <command label="Winlator Star (Standalone)">%EMULATOR_WINLATOR-STAR% %ACTIVITY_CLEAR_TASK% %ACTIVITY_CLEAR_TOP% %EXTRABOOL_launched_from_frontend%=true %EXTRA_shortcut_path%=%ROM%</command>
    <platform>windows</platform>
    <theme>windows</theme>
</system>
```

---

Thanks to xabbu33 for the pull request and tutorial.
