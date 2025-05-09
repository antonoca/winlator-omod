<p align="center">
	<img src="lomodlogo.png" width="376" height="128" alt="Winlator" />  
</p>

<p align="left">
  <img src="https://img.shields.io/github/downloads/antonoca/winlator-omod/total" alt="Total Downloads" width="150">
</p>

# Winlator@Omod
Winlator@Omod is an Android application that lets you to run Windows (x86_64) applications with Wine and Box86/Box64. Originally from [BrunoSX](https://github.com/brunodev85/Winlator), modified by [antonoca](https://github.com/antonoca/)​

Join us in our official Telegram channel - [Click Here](https://t.me/omodreleases)​

# Installation
1. Download and install the APK (signed.apk or signed-benchproot.apk).
2. Launch the app and wait for the installation process to finish
3. Enjoy your applications or games on the application for Android!

[Latest signed.apk](https://github.com/antonoca/winlator-omod/releases/tag/8231f24)

[Latest signed-benchproot.apk](https://github.com/antonoca/winlator-omod/releases/tag/3d01ba8)

---- 

- signed.apk is the winlator 10 version fork with native glibc and the one who is continued mainly.

- signed-benchproot.apk is the winlator 9 version fork with proot + benchmark package name that does not require the user to uninstall the normal winlator and speeds up the games in certain devices. this is not recommended as it can ruin your phone as its like doing a very long benchmark so be careful with this version.

----
# Testing videos by the community.

## Biomorph 2025
[![Youtube](https://img.youtube.com/vi/5VPGw3KTaiE/0.jpg)](https://www.youtube.com/watch?v=5VPGw3KTaiE)

## Assassins Creed Origins (Snapdragon 8 gen 3)
[![Youtube](https://img.youtube.com/vi/_QaRLDuOZGg/0.jpg)](https://www.youtube.com/watch?v=_QaRLDuOZGg)

## Harvestella 2022 (Snapdragon 8 gen 3)
[![Youtube](https://img.youtube.com/vi/rUaSPvY1HEA/0.jpg)](https://www.youtube.com/watch?v=rUaSPvY1HEA)

## Sleeping Dogs (Snapdragon 8 gen 3)
[![Youtube](https://img.youtube.com/vi/wQ__wGjtom8/0.jpg)](https://www.youtube.com/watch?v=wQ__wGjtom8)

## Ten Bells (Snapdragon 8 gen 3)
[![Youtube](https://img.youtube.com/vi/yvhavPfdfLM/0.jpg)](https://www.youtube.com/watch?v=yvhavPfdfLM)

**(ps. even tho the logo is different, the emulator is the same, i changed the logo. this is just the new repo as the old one got hacked and deleted, basically a new beginning.)**

# Troubleshooting

- If you are experiencing performance issues, try changing the Box64 preset to `Performance` in Container Settings -> Advanced Tab.
- For applications that use .NET Framework, try installing `Wine Mono` found in Start Menu -> System Tools -> Installers.
- If some older games don't open, try adding the environment variable `MESA_EXTENSION_MAX_YEAR=2003` in Container Settings -> Environment Variables.
- Try running the games using the shortcut on the Winlator home screen, there you can define individual settings for each game.
- To display low resolution games correctly, try to enabling the `Force Fullscreen` option in the shortcut settings.
- To improve stability in games that uses Unity Engine, try changing the Box64 preset to `Stability` or in the shortcut settings add the exec argument `-force-gfx-direct`.

# Third-party apps
- GLIBC Patches by [Termux Pacman](https://github.com/termux-pacman/glibc-packages)
- Wine ([winehq.org](https://www.winehq.org/))
- Box86/Box64 by [ptitseb](https://github.com/ptitSeb)
- Mesa (Turnip/Zink/VirGL) ([mesa3d.org](https://www.mesa3d.org))
- DXVK ([github.com/doitsujin/dxvk](https://github.com/doitsujin/dxvk))
- VKD3D ([gitlab.winehq.org/wine/vkd3d](https://gitlab.winehq.org/wine/vkd3d))
- D8VK ([github.com/AlpyneDreams/d8vk](https://github.com/AlpyneDreams/d8vk))
- CNC DDraw ([github.com/FunkyFr3sh/cnc-ddraw](https://github.com/FunkyFr3sh/cnc-ddraw))
- Ubuntu RootFs ([Focal Fossa](https://releases.ubuntu.com/focal))
- Certain parts of the prefix - ([Ajay Prefix](https://github.com/ajay9634/Ajay-prefix))
