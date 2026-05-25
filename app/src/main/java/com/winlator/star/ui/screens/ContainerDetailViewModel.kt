package com.winlator.star.ui.screens

import android.app.Application
import android.content.Context
import android.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.winlator.star.R
import com.winlator.star.box64.Box64Preset
import com.winlator.star.box64.Box64PresetManager
import com.winlator.star.container.Container
import com.winlator.star.container.ContainerManager
import com.winlator.star.contents.ContentProfile
import com.winlator.star.contents.ContentsManager
import com.winlator.star.core.AppUtils
import com.winlator.star.core.DefaultVersion
import com.winlator.star.core.EnvVars
import com.winlator.star.core.GPUInformation
import com.winlator.star.core.PreloaderState
import com.winlator.star.core.StringUtils
import com.winlator.star.core.WineInfo
import com.winlator.star.core.WineRegistryEditor
import com.winlator.star.core.WineThemeManager
import com.winlator.star.contentdialog.GraphicsDriverConfigDialog
import com.winlator.star.fexcore.FEXCoreManager
import com.winlator.star.fexcore.FEXCorePreset
import com.winlator.star.fexcore.FEXCorePresetManager
import com.winlator.star.midi.MidiManager
import com.winlator.star.winhandler.WinHandler
import com.winlator.star.xserver.XKeycode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.util.Locale

data class DriveEntry(val uid: Long = System.nanoTime(), var letter: String, var path: String)
data class WinComponentEntry(val key: String, var selectedIndex: Int, val label: String)

class ContainerDetailViewModel(app: Application) : AndroidViewModel(app) {

    private val context: Context = app.applicationContext
    private lateinit var manager: ContainerManager
    private lateinit var contentsManager: ContentsManager

    var container: Container? = null; private set
    var isEditMode by mutableStateOf(false); private set
    var isSaving by mutableStateOf(false); private set
    private var initialized = false

    // ── Top-level fields ──────────────────────────────────────────────────────
    var containerName by mutableStateOf("")

    var screenSizeEntries by mutableStateOf(emptyList<String>()); private set
    var selectedScreenSize by mutableStateOf(Container.DEFAULT_SCREEN_SIZE)
    var customWidth  by mutableStateOf("")
    var customHeight by mutableStateOf("")

    var wineVersionEntries by mutableStateOf(emptyList<String>()); private set
    var selectedWineVersion by mutableStateOf("")
    var wineVersionEnabled by mutableStateOf(true); private set
    var isArm64EC by mutableStateOf(false); private set

    var graphicsDriverEntries by mutableStateOf(emptyList<String>()); private set
    var selectedGraphicsDriver by mutableStateOf(Container.DEFAULT_GRAPHICS_DRIVER)
    // graphicsDriverConfig stored via dummy View tag in Screen composable
    var graphicsDriverConfig by mutableStateOf(Container.DEFAULT_GRAPHICSDRIVERCONFIG)

    var dxWrapperEntries by mutableStateOf(emptyList<String>()); private set
    var selectedDXWrapper by mutableStateOf(Container.DEFAULT_DXWRAPPER)
    var dxWrapperConfig by mutableStateOf(Container.DEFAULT_DXWRAPPERCONFIG)

    var audioDriverEntries by mutableStateOf(emptyList<String>()); private set
    var selectedAudioDriver by mutableStateOf(Container.DEFAULT_AUDIO_DRIVER)

    var emulatorEntries by mutableStateOf(emptyList<String>()); private set
    var selectedEmulator by mutableStateOf(Container.DEFAULT_EMULATOR)
    var emulatorEnabled by mutableStateOf(false); private set

    var midiEntries by mutableStateOf(emptyList<String>()); private set
    var selectedMidiIndex by mutableStateOf(0)

    var showFPS by mutableStateOf(false)
    var fpsCounterConfig by mutableStateOf(Container.DEFAULT_FPS_COUNTER_CONFIG)
    var fullscreenStretched by mutableStateOf(false)

    var lcAll by mutableStateOf("")
    var lcAllEntries by mutableStateOf(emptyList<String>()); private set

    var enableXInput by mutableStateOf(true)
    var enableDInput by mutableStateOf(false)
    var exclusiveXInput by mutableStateOf(true)

    // ── Box64 ─────────────────────────────────────────────────────────────────
    var box64VersionEntries by mutableStateOf(emptyList<String>()); private set
    var selectedBox64Version by mutableStateOf("")
    var box64PresetEntries by mutableStateOf(emptyList<String>()); private set
    var selectedBox64PresetIndex by mutableStateOf(0)
    private var box64PresetIds = emptyList<String>()

    // ── FEXCore (arm64ec only) ────────────────────────────────────────────────
    var fexCoreVersionEntries by mutableStateOf(emptyList<String>()); private set
    var selectedFEXCoreVersion by mutableStateOf(DefaultVersion.FEXCORE)
    var fexCorePresetEntries by mutableStateOf(emptyList<String>()); private set
    var selectedFEXCorePresetIndex by mutableStateOf(0)
    private var fexCorePresetIds = emptyList<String>()

    // ── Startup selection ─────────────────────────────────────────────────────
    var startupSelectionEntries by mutableStateOf(emptyList<String>()); private set
    var selectedStartupSelection by mutableStateOf(Container.STARTUP_SELECTION_ESSENTIAL.toInt())

    // ── Wine Config tab ───────────────────────────────────────────────────────
    var desktopThemeIndex by mutableStateOf(0)   // 0=LIGHT, 1=DARK
    var desktopBgTypeIndex by mutableStateOf(0)  // 0=IMAGE, 1=COLOR
    var desktopBgColorInt by mutableStateOf(Color.parseColor("#0277bd"))
    var mouseWarpEntries by mutableStateOf(emptyList<String>()); private set
    var selectedMouseWarpIndex by mutableStateOf(0)

    // ── Win Components tab ────────────────────────────────────────────────────
    val winComponents = mutableStateListOf<WinComponentEntry>()

    // ── Env Vars tab (managed via AndroidView) ────────────────────────────────
    var envVarsStr by mutableStateOf(Container.DEFAULT_ENV_VARS)

    // ── Drives tab ────────────────────────────────────────────────────────────
    val drives = mutableStateListOf<DriveEntry>()
    val driveLetterOptions: List<String> by lazy {
        (0 until Container.MAX_DRIVE_LETTERS).map { ((it + 68).toChar()).toString() + ":" }
    }

    // ── Advanced tab ─────────────────────────────────────────────────────────
    var cpuList by mutableStateOf(Container.getFallbackCPUList())
    var cpuListWoW64 by mutableStateOf(Container.getFallbackCPUListWoW64())

    // ── XR tab ────────────────────────────────────────────────────────────────
    var primaryControllerEntries by mutableStateOf(emptyList<String>()); private set
    var selectedPrimaryController by mutableStateOf(1)
    val xrMappingIndices = mutableStateListOf<Int>() // 10 items: spinner positions (ordinals)
    var xrKeycodeNames by mutableStateOf(emptyList<String>()); private set

    private val xrDefaults = listOf(
        XKeycode.KEY_A.ordinal,
        XKeycode.KEY_B.ordinal,
        XKeycode.KEY_X.ordinal,
        XKeycode.KEY_Y.ordinal,
        XKeycode.KEY_SPACE.ordinal,
        XKeycode.KEY_ENTER.ordinal,
        XKeycode.KEY_UP.ordinal,
        XKeycode.KEY_DOWN.ordinal,
        XKeycode.KEY_LEFT.ordinal,
        XKeycode.KEY_RIGHT.ordinal
    )

    val xrMappingLabels = listOf(
        "Button A", "Button B", "Button X", "Button Y",
        "Button Grip", "Button Trigger",
        "Thumbstick Up", "Thumbstick Down", "Thumbstick Left", "Thumbstick Right"
    )

    // ── Tab selection ─────────────────────────────────────────────────────────
    var selectedTab by mutableStateOf(0)

    // ─────────────────────────────────────────────────────────────────────────
    fun init(containerId: Int) {
        if (initialized) return
        initialized = true

        manager = ContainerManager(context)
        contentsManager = ContentsManager(context)
        contentsManager.syncContents()

        container = if (containerId > 0) manager.getContainerById(containerId) else null
        isEditMode = container != null

        loadStaticResources()
        loadContainerData()
    }

    private fun loadStaticResources() {
        val res = context.resources
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        screenSizeEntries = res.getStringArray(R.array.screen_size_entries).toList()

        // Wine versions (base + downloaded profiles)
        val wineList = res.getStringArray(R.array.wine_entries).toMutableList()
        for (p in contentsManager.getProfiles(ContentProfile.ContentType.CONTENT_TYPE_WINE))
            wineList.add(ContentsManager.getEntryName(p))
        for (p in contentsManager.getProfiles(ContentProfile.ContentType.CONTENT_TYPE_PROTON))
            wineList.add(ContentsManager.getEntryName(p))
        wineVersionEntries = wineList

        graphicsDriverEntries = res.getStringArray(R.array.graphics_driver_entries).toList()
        dxWrapperEntries  = res.getStringArray(R.array.dxwrapper_entries).toList()
        audioDriverEntries = res.getStringArray(R.array.audio_driver_entries).toList()
        emulatorEntries   = res.getStringArray(R.array.emulator_entries).toList()
        lcAllEntries      = res.getStringArray(R.array.some_lc_all).toList()
        startupSelectionEntries = res.getStringArray(R.array.startup_selection_entries).toList()
        mouseWarpEntries  = listOf(
            context.getString(R.string.disable),
            context.getString(R.string.enable),
            context.getString(R.string.force)
        )
        primaryControllerEntries = res.getStringArray(R.array.xr_controllers).toList()
        xrKeycodeNames = XKeycode.values().map { it.name }

        // Box64 presets
        val b64Presets = Box64PresetManager.getPresets("box64", context)
        box64PresetEntries = b64Presets.map { it.name }
        box64PresetIds     = b64Presets.map { it.id }

        // FEXCore presets
        val fexPresets = FEXCorePresetManager.getPresets(context)
        fexCorePresetEntries = fexPresets.map { it.name }
        fexCorePresetIds     = fexPresets.map { it.id }

        // MIDI
        val midiList = mutableListOf("-- ${context.getString(R.string.disabled)} --")
        midiList.add(MidiManager.DEFAULT_SF2_FILE)
        try {
            val sfDir = File(context.filesDir, MidiManager.SF_DIR)
            sfDir.listFiles()?.forEach { midiList.add(it.name) }
        } catch (_: Exception) {}
        midiEntries = midiList
    }

    private fun loadContainerData() {
        val c = container
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        containerName = if (c != null) c.name else "${context.getString(R.string.container)}-${manager.getNextContainerId()}"
        wineVersionEnabled = !isEditMode

        // Screen size
        val ssValue = c?.screenSize ?: Container.DEFAULT_SCREEN_SIZE
        val ssFound = screenSizeEntries.indexOfFirst {
            StringUtils.parseIdentifier(it).equals(ssValue, ignoreCase = true)
        }
        if (ssFound >= 0) {
            selectedScreenSize = ssValue
        } else {
            selectedScreenSize = "custom"
            val parts = ssValue.split("x")
            customWidth  = parts.getOrElse(0) { "" }
            customHeight = parts.getOrElse(1) { "" }
        }

        // Wine version
        selectedWineVersion = c?.wineVersion ?: wineVersionEntries.firstOrNull() ?: ""
        refreshWineDependent(selectedWineVersion)

        // Box64 version: refreshWineDependent() resets to entry 0 (correct on
        // Wine-version change since the list differs for arm64ec). On initial
        // load we override that to honor the container's saved selection.
        c?.box64Version
            ?.takeIf { it.isNotEmpty() && box64VersionEntries.contains(it) }
            ?.let { selectedBox64Version = it }

        // Graphics driver (load as display name for dropdown)
        selectedGraphicsDriver   = identifierToDisplay(c?.graphicsDriver ?: Container.DEFAULT_GRAPHICS_DRIVER, graphicsDriverEntries)
        graphicsDriverConfig     = c?.graphicsDriverConfig ?: Container.DEFAULT_GRAPHICSDRIVERCONFIG
        selectedDXWrapper        = identifierToDisplay(c?.getDXWrapper() ?: Container.DEFAULT_DXWRAPPER, dxWrapperEntries)
        dxWrapperConfig          = c?.getDXWrapperConfig() ?: Container.DEFAULT_DXWRAPPERCONFIG

        // Audio / emulator (load as display name)
        selectedAudioDriver = identifierToDisplay(c?.audioDriver ?: Container.DEFAULT_AUDIO_DRIVER, audioDriverEntries)
        selectedEmulator    = identifierToDisplay(c?.emulator ?: Container.DEFAULT_EMULATOR, emulatorEntries)

        // MIDI
        val midiVal = c?.getMIDISoundFont() ?: ""
        selectedMidiIndex = if (midiVal.isEmpty()) 0
                            else midiEntries.indexOf(midiVal).takeIf { it >= 0 } ?: 0

        showFPS           = c?.isShowFPS == true
        fpsCounterConfig  = c?.getFPSCounterConfig() ?: Container.DEFAULT_FPS_COUNTER_CONFIG
        fullscreenStretched = c?.isFullscreenStretched == true

        val locale = java.util.Locale.getDefault()
        lcAll = c?.getLC_ALL() ?: "${locale.language}_${locale.country}.UTF-8"

        // Input type
        val inputType: Int = c?.inputType ?: WinHandler.DEFAULT_INPUT_TYPE.toInt()
        enableXInput   = (inputType and WinHandler.FLAG_INPUT_TYPE_XINPUT.toInt()) != 0
        enableDInput   = (inputType and WinHandler.FLAG_INPUT_TYPE_DINPUT.toInt()) != 0
        exclusiveXInput = c?.isExclusiveXInput ?: true
        if (!exclusiveXInput) {
            enableXInput = true; enableDInput = true
        }

        // Box64 preset
        val b64Preset = c?.box64Preset ?: prefs.getString("box64_preset", Box64Preset.COMPATIBILITY) ?: Box64Preset.COMPATIBILITY
        selectedBox64PresetIndex = box64PresetIds.indexOf(b64Preset).takeIf { it >= 0 } ?: 0

        // FEXCore preset
        val fexPreset = c?.getFEXCorePreset() ?: prefs.getString("fexcore_preset", FEXCorePreset.INTERMEDIATE) ?: FEXCorePreset.INTERMEDIATE
        selectedFEXCorePresetIndex = fexCorePresetIds.indexOf(fexPreset).takeIf { it >= 0 } ?: 0

        // FEXCore version
        loadFEXCoreVersions()
        selectedFEXCoreVersion = c?.getFEXCoreVersion() ?: DefaultVersion.FEXCORE

        // Startup selection
        selectedStartupSelection = (c?.startupSelection ?: Container.STARTUP_SELECTION_ESSENTIAL).toInt()

        // CPU lists
        cpuList      = c?.getCPUList(true) ?: Container.getFallbackCPUList()
        cpuListWoW64 = c?.getCPUListWoW64(true) ?: Container.getFallbackCPUListWoW64()

        // Wine Config (desktop theme)
        val themeStr = c?.desktopTheme ?: WineThemeManager.DEFAULT_DESKTOP_THEME
        val themeInfo = WineThemeManager.ThemeInfo(themeStr)
        desktopThemeIndex   = themeInfo.theme.ordinal
        desktopBgTypeIndex  = themeInfo.backgroundType.ordinal
        desktopBgColorInt   = themeInfo.backgroundColor

        // Mouse warp (from registry, only in edit mode)
        if (c != null) {
            val userRegFile = File(c.rootDir, ".wine/user.reg")
            try {
                WineRegistryEditor(userRegFile).use { reg ->
                    val mw = reg.getStringValue("Software\\Wine\\DirectInput", "MouseWarpOverride", "disable")
                    selectedMouseWarpIndex = when (mw.lowercase(Locale.ENGLISH)) {
                        "enable" -> 1
                        "force"  -> 2
                        else     -> 0
                    }
                }
            } catch (_: Exception) {}
        }

        // Win Components
        loadWinComponents(c?.winComponents ?: Container.DEFAULT_WINCOMPONENTS)

        // Env vars
        envVarsStr = c?.envVars ?: Container.DEFAULT_ENV_VARS

        // Drives
        drives.clear()
        for (entry in Container.drivesIterator(c?.drives ?: Container.DEFAULT_DRIVES)) {
            drives.add(DriveEntry(letter = entry[0], path = entry[1]))
        }

        // XR
        selectedPrimaryController = c?.primaryController ?: 1
        val xcodes = XKeycode.values()
        val xrMappings = Container.XrControllerMapping.values()
        xrMappingIndices.clear()
        for ((i, mapping) in xrMappings.withIndex()) {
            val defaultOrdinal = xrDefaults.getOrElse(i) { 0 }
            val idx = if (c != null) {
                val byteId = c.getControllerMapping(mapping)
                xcodes.indexOfFirst { it.id == byteId }.takeIf { it >= 0 } ?: defaultOrdinal
            } else {
                defaultOrdinal
            }
            xrMappingIndices.add(idx)
        }
    }

    private fun refreshWineDependent(wineVersion: String) {
        val wineInfo = WineInfo.fromIdentifier(context, contentsManager, wineVersion)
        if (wineInfo == null) {
            isArm64EC = false
            emulatorEnabled = false
            box64VersionEntries = emptyList()
            selectedBox64Version = ""
            return
        }
        isArm64EC    = wineInfo.isArm64EC()
        emulatorEnabled = isArm64EC

        // Box64 versions
        val b64Array = if (isArm64EC)
            context.resources.getStringArray(R.array.wowbox64_version_entries).toMutableList()
        else
            context.resources.getStringArray(R.array.box64_version_entries).toMutableList()

        val b64Type = if (isArm64EC) ContentProfile.ContentType.CONTENT_TYPE_WOWBOX64
                      else           ContentProfile.ContentType.CONTENT_TYPE_BOX64
        for (p in contentsManager.getProfiles(b64Type)) {
            val name = ContentsManager.getEntryName(p)
            val dash = name.indexOf('-')
            b64Array.add(name.substring(dash + 1))
        }
        box64VersionEntries = b64Array
        selectedBox64Version = box64VersionEntries.firstOrNull() ?: ""
    }

    private fun loadFEXCoreVersions() {
        val list = context.resources.getStringArray(R.array.fexcore_version_entries).toMutableList()
        for (p in contentsManager.getProfiles(ContentProfile.ContentType.CONTENT_TYPE_FEXCORE)) {
            val name = ContentsManager.getEntryName(p)
            val dash = name.indexOf('-')
            list.add(name.substring(dash + 1))
        }
        fexCoreVersionEntries = list
    }

    private fun loadWinComponents(wincomponents: String) {
        winComponents.clear()
        val res = context.resources
        for (parts in com.winlator.star.core.KeyValueSet(wincomponents)) {
            val key   = parts[0]
            val idx   = parts[1].toIntOrNull() ?: 0
            val resId = res.getIdentifier(key, "string", context.packageName)
            val label = if (resId != 0) res.getString(resId) else key
            winComponents.add(WinComponentEntry(key, idx, label))
        }
    }

    /** Finds the display name in [entries] whose identifier matches [id]. Falls back to first entry. */
    private fun identifierToDisplay(id: String, entries: List<String>): String =
        entries.firstOrNull { StringUtils.parseIdentifier(it) == id }
            ?: entries.firstOrNull()
            ?: id

    fun onWineVersionChanged(version: String) {
        selectedWineVersion = version
        refreshWineDependent(version)
    }

    fun onExclusiveXInputChanged(checked: Boolean) {
        exclusiveXInput = checked
        if (!checked) {
            enableXInput = true; enableDInput = true
        } else {
            if (enableXInput && enableDInput) enableDInput = false
        }
    }

    // ── Confirm ───────────────────────────────────────────────────────────────
    fun confirm(
        resolvedGraphicsDriverConfig: String,
        resolvedDXWrapperConfig: String,
        resolvedFPSCounterConfig: String,
        resolvedEnvVars: String,
        resolvedCPUList: String,
        resolvedCPUListWoW64: String,
        resolvedColorAsString: String,
        onDone: () -> Unit
    ) {
        isSaving = true
        PreloaderState.show(context.getString(R.string.creating_container))
        val cleanup = {
            PreloaderState.hide()
            isSaving = false
            onDone()
        }
        viewModelScope.launch(Dispatchers.Main) {
            doConfirm(
                resolvedGraphicsDriverConfig,
                resolvedDXWrapperConfig,
                resolvedFPSCounterConfig,
                resolvedEnvVars,
                resolvedCPUList,
                resolvedCPUListWoW64,
                resolvedColorAsString,
                onComplete = cleanup
            )
        }
    }

    private fun doConfirm(
        gdConfig: String,
        dxConfig: String,
        fpsConfig: String,
        envVarsIn: String,
        cpuListIn: String,
        cpuListWoW64In: String,
        colorAsString: String,
        onComplete: () -> Unit
    ) {
        // Finalize graphics driver config (ensure version is set)
        var finalGDConfig = gdConfig
        try {
            val cfg = GraphicsDriverConfigDialog.parseGraphicsDriverConfig(gdConfig)
            if (cfg["version"].isNullOrEmpty()) {
                cfg["version"] = if (GPUInformation.isDriverSupported(DefaultVersion.WRAPPER_ADRENO, context))
                    DefaultVersion.WRAPPER_ADRENO else DefaultVersion.WRAPPER
                finalGDConfig = GraphicsDriverConfigDialog.toGraphicsDriverConfig(cfg)
            }
        } catch (_: Exception) {}

        val screenSize   = buildScreenSize()
        val graphicsDriver = StringUtils.parseIdentifier(selectedGraphicsDriver)
        val dxWrapper    = StringUtils.parseIdentifier(selectedDXWrapper)
        val audioDriver  = StringUtils.parseIdentifier(selectedAudioDriver)
        val emulator     = StringUtils.parseIdentifier(selectedEmulator)
        val midiSoundFont = if (selectedMidiIndex == 0) "" else midiEntries.getOrElse(selectedMidiIndex) { "" }
        val wincomponents = winComponents.joinToString(",") { "${it.key}=${it.selectedIndex}" }
        val drivesStr = buildDrivesString()
        val desktopThemeStr = buildDesktopThemeStr(colorAsString)
        val box64Preset = box64PresetIds.getOrElse(selectedBox64PresetIndex) { Box64Preset.COMPATIBILITY }
        val fexcorePreset = fexCorePresetIds.getOrElse(selectedFEXCorePresetIndex) { FEXCorePreset.INTERMEDIATE }
        val controllerMapping = buildControllerMapping()

        var inputType = 0
        if (enableXInput) inputType = inputType or WinHandler.FLAG_INPUT_TYPE_XINPUT.toInt()
        if (enableDInput) inputType = inputType or WinHandler.FLAG_INPUT_TYPE_DINPUT.toInt()

        val c = container
        if (c != null) {
            // Edit mode
            c.name               = containerName
            c.screenSize         = screenSize
            c.envVars            = envVarsIn
            c.setCPUList(cpuListIn)
            c.setCPUListWoW64(cpuListWoW64In)
            c.graphicsDriver     = graphicsDriver
            c.graphicsDriverConfig = finalGDConfig
            c.setDXWrapper(dxWrapper)
            c.setDXWrapperConfig(dxConfig)
            c.audioDriver        = audioDriver
            c.emulator           = emulator
            c.winComponents      = wincomponents
            c.drives             = drivesStr
            c.setShowFPS(showFPS)
            c.setFPSCounterConfig(fpsConfig)
            c.setFullscreenStretched(fullscreenStretched)
            c.setExclusiveXInput(exclusiveXInput)
            c.setInputType(inputType)
            c.setStartupSelection(selectedStartupSelection.toByte())
            c.setBox64Version(selectedBox64Version)
            c.setBox64Preset(box64Preset)
            c.setFEXCoreVersion(selectedFEXCoreVersion)
            c.setFEXCorePreset(fexcorePreset)
            c.desktopTheme       = desktopThemeStr
            c.setMidiSoundFont(midiSoundFont)
            c.setLC_ALL(lcAll)
            c.setPrimaryController(selectedPrimaryController)
            c.setControllerMapping(controllerMapping)
            c.saveData()
            saveMouseWarp(c)
            onComplete()
        } else {
            // Create mode
            val data = JSONObject().apply {
                put("name", containerName)
                put("screenSize", screenSize)
                put("envVars", envVarsIn)
                put("cpuList", cpuListIn)
                put("cpuListWoW64", cpuListWoW64In)
                put("graphicsDriver", graphicsDriver)
                put("graphicsDriverConfig", finalGDConfig)
                put("dxwrapper", dxWrapper)
                put("dxwrapperConfig", dxConfig)
                put("audioDriver", audioDriver)
                put("emulator", emulator)
                put("wincomponents", wincomponents)
                put("drives", drivesStr)
                put("showFPS", showFPS)
                put("fpsCounterConfig", fpsConfig)
                put("fullscreenStretched", fullscreenStretched)
                put("exclusiveXInput", exclusiveXInput)
                put("inputType", inputType)
                put("startupSelection", selectedStartupSelection)
                put("box64Version", selectedBox64Version)
                put("box64Preset", box64Preset)
                put("fexcoreVersion", selectedFEXCoreVersion)
                put("fexcorePreset", fexcorePreset)
                put("desktopTheme", desktopThemeStr)
                put("wineVersion", selectedWineVersion)
                put("midiSoundFont", midiSoundFont)
                put("lc_all", lcAll)
                put("primaryController", selectedPrimaryController)
                put("controllerMapping", controllerMapping)
            }
            manager.createContainerAsync(data, contentsManager) { created ->
                container = created
                if (created != null) {
                    saveMouseWarp(created)
                } else {
                    Toast.makeText(context, context.getString(R.string.creating_container) + " failed. Check Wine version and try again.", Toast.LENGTH_LONG).show()
                }
                onComplete()
            }
        }
    }

    private fun buildScreenSize(): String {
        if (selectedScreenSize.equals("custom", ignoreCase = true)) {
            val w = customWidth.trim()
            val h = customHeight.trim()
            if (w.matches(Regex("[0-9]+")) && h.matches(Regex("[0-9]+"))) {
                val wi = w.toInt(); val hi = h.toInt()
                if (wi % 2 == 0 && hi % 2 == 0) return "${wi}x${hi}"
            }
            return Container.DEFAULT_SCREEN_SIZE
        }
        return StringUtils.parseIdentifier(selectedScreenSize)
    }

    private fun buildDesktopThemeStr(colorAsString: String): String {
        val theme   = WineThemeManager.Theme.values()[desktopThemeIndex]
        val bgType  = WineThemeManager.BackgroundType.values()[desktopBgTypeIndex]
        var str = "${theme},${bgType},$colorAsString"
        if (bgType == WineThemeManager.BackgroundType.IMAGE) {
            val wallpaper = WineThemeManager.getUserWallpaperFile(context)
            str += "," + if (wallpaper.isFile) wallpaper.lastModified() else "0"
        }
        return str
    }

    private fun buildDrivesString(): String =
        drives.filter { it.path.isNotBlank() }.joinToString("") { "${it.letter}:${it.path}" }

    private fun buildControllerMapping(): String {
        val xcodes = XKeycode.values()
        val bytes = ByteArray(xrMappingIndices.size) { i ->
            xcodes.getOrElse(xrMappingIndices.getOrElse(i) { 0 }) { xcodes[0] }.id
        }
        return String(bytes)
    }

    private fun saveMouseWarp(c: Container) {
        val userRegFile = File(c.rootDir, ".wine/user.reg")
        if (!userRegFile.exists()) return
        try {
            WineRegistryEditor(userRegFile).use { reg ->
                val value = when (selectedMouseWarpIndex) {
                    1    -> "enable"
                    2    -> "force"
                    else -> "disable"
                }
                reg.setStringValue("Software\\Wine\\DirectInput", "MouseWarpOverride", value)
            }
        } catch (_: Exception) {}
    }

    fun addDrive() {
        if (drives.size >= Container.MAX_DRIVE_LETTERS) return
        val letter = driveLetterOptions.getOrElse(drives.size) { "D:" }.first().toString()
        drives.add(DriveEntry(letter = letter, path = ""))
    }

    fun removeDrive(uid: Long) {
        drives.removeAll { it.uid == uid }
    }

    fun updateDriveLetter(uid: Long, letter: String) {
        drives.indexOfFirst { it.uid == uid }.takeIf { it >= 0 }?.let { i ->
            drives[i] = drives[i].copy(letter = letter)
        }
    }

    fun updateDrivePath(uid: Long, path: String) {
        drives.indexOfFirst { it.uid == uid }.takeIf { it >= 0 }?.let { i ->
            drives[i] = drives[i].copy(path = path)
        }
    }
}
