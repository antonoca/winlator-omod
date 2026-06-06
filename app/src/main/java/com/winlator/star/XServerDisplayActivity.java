package com.winlator.star;

import static com.winlator.star.core.AppUtils.showToast;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;

import androidx.compose.ui.platform.ComposeView;
import com.winlator.star.ui.XServerDrawerKt;
import com.winlator.star.ui.XServerDrawerState;
import com.winlator.star.ui.XServerDialogHostKt;
import com.winlator.star.ui.XServerDialogState;
import com.winlator.star.container.Container;
import com.winlator.star.container.ContainerManager;
import com.winlator.star.container.Shortcut;
import com.winlator.star.contentdialog.ContentDialog;
import com.winlator.star.contentdialog.DXVKConfigDialog;
import com.winlator.star.contentdialog.GraphicsDriverConfigDialog;
import com.winlator.star.contentdialog.WineD3DConfigDialog;
import com.winlator.star.contents.ContentProfile;
import com.winlator.star.contents.ContentsManager;
import com.winlator.star.contents.AdrenotoolsManager;
import com.winlator.star.core.AppUtils;
import com.winlator.star.core.DefaultVersion;
import com.winlator.star.core.EnvVars;
import com.winlator.star.core.FileUtils;
import com.winlator.star.core.GPUInformation;
import com.winlator.star.core.KeyValueSet;
import com.winlator.star.core.OnExtractFileListener;
import com.winlator.star.core.PreloaderDialog;
import com.winlator.star.core.ProcessHelper;
import com.winlator.star.core.StringUtils;
import com.winlator.star.core.TarCompressorUtils;
import com.winlator.star.core.WineInfo;
import com.winlator.star.core.WineRegistryEditor;
import com.winlator.star.core.WineRequestHandler;
import com.winlator.star.core.WineStartMenuCreator;
import com.winlator.star.core.Callback;
import com.winlator.star.core.WineThemeManager;
import com.winlator.star.core.WineUtils;
import com.winlator.star.inputcontrols.ControlsProfile;
import com.winlator.star.inputcontrols.ExternalController;
import com.winlator.star.inputcontrols.InputControlsManager;
import com.winlator.star.math.Mathf;
import com.winlator.star.math.XForm;
import com.winlator.star.midi.MidiHandler;
import com.winlator.star.midi.MidiManager;
import com.winlator.star.renderer.GLRenderer;
import com.winlator.star.renderer.effects.CRTEffect;
import com.winlator.star.renderer.effects.ColorEffect;
import com.winlator.star.renderer.effects.FXAAEffect;
import com.winlator.star.renderer.effects.NTSCCombinedEffect;
import com.winlator.star.renderer.effects.ToonEffect;
import com.winlator.star.renderer.effects.FSREffect;
import com.winlator.star.renderer.effects.HDREffect;
import com.winlator.star.widget.FrameRating;
import com.winlator.star.widget.FrameRatingHorizontal;
import com.winlator.star.widget.InputControlsView;
import com.winlator.star.widget.LogView;
import com.winlator.star.widget.TouchpadView;
import com.winlator.star.widget.XServerView;
import com.winlator.star.winhandler.MouseEventFlags;
import com.winlator.star.winhandler.OnGetProcessInfoListener;
import com.winlator.star.winhandler.ProcessInfo;
import com.winlator.star.winhandler.WinHandler;
import com.winlator.star.core.CPUStatus;
import com.winlator.star.xserver.XLock;
import com.winlator.star.xconnector.UnixSocketConfig;
import com.winlator.star.xenvironment.ImageFs;
import com.winlator.star.xenvironment.XEnvironment;
import com.winlator.star.xenvironment.components.ALSAServerComponent;
import com.winlator.star.xenvironment.components.GuestProgramLauncherComponent;
import com.winlator.star.xenvironment.components.PulseAudioComponent;
import com.winlator.star.xenvironment.components.SysVSharedMemoryComponent;
import com.winlator.star.xenvironment.components.XServerComponent;
import com.winlator.star.xserver.Pointer;
import com.winlator.star.xserver.Property;
import com.winlator.star.xserver.ScreenInfo;
import com.winlator.star.xserver.Window;
import com.winlator.star.xserver.WindowManager;
import com.winlator.star.xserver.XServer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.sherlock.com.sun.media.sound.SF2Soundbank;

public class XServerDisplayActivity extends AppCompatActivity {
    public static String NOTIFICATION_CHANNEL_ID = "Winlator";
    public static int NOTIFICATION_ID = -1;
    private XServerView xServerView;
    private InputControlsView inputControlsView;
    private TouchpadView touchpadView;
    private XEnvironment environment;
    private DrawerLayout drawerLayout;
    private ContainerManager containerManager;
    protected Container container;
    private XServer xServer;
    private InputControlsManager inputControlsManager;
    private ImageFs imageFs;
    private FrameRating frameRating = null;
    private FrameRatingHorizontal frameRatingHorizontal = null;
    private Runnable editInputControlsCallback;
    private Shortcut shortcut;
    private String graphicsDriver = Container.DEFAULT_GRAPHICS_DRIVER;
    private HashMap<String, String> graphicsDriverConfig;
    private String audioDriver = Container.DEFAULT_AUDIO_DRIVER;
    private String emulator = Container.DEFAULT_EMULATOR;
    private String dxwrapper = Container.DEFAULT_DXWRAPPER;
    private KeyValueSet dxwrapperConfig;
    private String startupSelection;
    private WineInfo wineInfo;
    private final EnvVars envVars = new EnvVars();
    private boolean firstTimeBoot = false;
    private SharedPreferences preferences;
    private Callback<String> wineDebugLogCallback;
    private java.io.PrintWriter wineDebugWriter;
    private OnExtractFileListener onExtractFileListener;
    private WinHandler winHandler;
    private WineRequestHandler wineRequestHandler;
    private float globalCursorSpeed = 1.0f;
    private short taskAffinityMask = 0;
    private short taskAffinityMaskWoW64 = 0;
    private int frameRatingWindowId = -1;
    private boolean cursorLock; // Flag to track if pointer capture was requested
    private final float[] xform = XForm.getInstance();
    private ContentsManager contentsManager;
    private MidiHandler midiHandler;
    private String midiSoundFont = "";
    private String lc_all = "";
    private String vkbasaltConfig = "";
    PreloaderDialog preloaderDialog = null;
    private Runnable configChangedCallback = null;
    private boolean isPaused = false;
    private boolean isRelativeMouseMovement = false;
    private boolean isMouseDisabled = false;
    private boolean pointerCaptureRequested = false;

    // Inside the XServerDisplayActivity class
    private SensorManager sensorManager;

    // Playtime stats tracking
    private long startTime;
    private SharedPreferences playtimePrefs;
    private String shortcutName;
    private Handler handler;
    private Runnable savePlaytimeRunnable;
    private static final long SAVE_INTERVAL_MS = 1000;

    private Handler  timeoutHandler = new Handler(Looper.getMainLooper());
    private Runnable hideControlsRunnable;

    private boolean isDarkMode;

    private String screenEffectProfile;

    private GuestProgramLauncherComponent guestProgramLauncherComponent;
    private EnvVars overrideEnvVars;

    private void createNotifcationChannel() {
        String name = "Winlator";
        String description = "Winlator XServer Messages";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (configChangedCallback != null) {
            configChangedCallback.run();
            configChangedCallback = null;
        }
    }
    
    private float pickHighestRefreshRate() {
    	android.view.Display display = getWindowManager().getDefaultDisplay();
    	android.view.Display.Mode[] modes = display.getSupportedModes();
    	
    	float maxRefresh = 0f;
    	
    	for (android.view.Display.Mode mode : modes) {
			if (mode.getRefreshRate() > maxRefresh)
    	    	maxRefresh = mode.getRefreshRate();
    	}

    	Log.d("XServerDisplayActivity", "Picking refresh rate " + maxRefresh);

    	return maxRefresh;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppUtils.hideSystemUI(this);
        AppUtils.keepScreenOn(this);
               
        android.view.WindowManager.LayoutParams params = getWindow().getAttributes();
        params.preferredRefreshRate = pickHighestRefreshRate();
        getWindow().setAttributes(params);
        
        setContentView(R.layout.xserver_display_activity);
        com.winlator.star.ui.PreloaderOverlayHelper.attach(this);

        preloaderDialog = new PreloaderDialog(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        cursorLock = preferences.getBoolean("cursor_lock", false);

        // Check for Dark Mode
        isDarkMode = preferences.getBoolean("dark_mode", false);

        boolean isOpenWithAndroidBrowser = preferences.getBoolean("open_with_android_browser", false);
        boolean isShareAndroidClipboard = preferences.getBoolean("share_android_clipboard", false);



        // Check if xinputDisabled extra is passed
        boolean xinputDisabledFromShortcut = false;




        // Initialize SensorManager



        // Record the start time
        startTime = System.currentTimeMillis();

        // Initialize handler for periodic saving
        handler = new Handler(Looper.getMainLooper());
        savePlaytimeRunnable = new Runnable() {
            @Override
            public void run() {
                savePlaytimeData();
                handler.postDelayed(this, SAVE_INTERVAL_MS);
            }
        };
        handler.postDelayed(savePlaytimeRunnable, SAVE_INTERVAL_MS);


        // Handler and Runnable to manage timeout for hiding controls

        boolean isTimeoutEnabled = preferences.getBoolean("touchscreen_timeout_enabled", true);

        hideControlsRunnable = () -> {
            if (isTimeoutEnabled) {
                inputControlsView.setVisibility(View.GONE);
                Log.d("XServerDisplayActivity", "Touchscreen controls hidden after timeout.");
            }
        };


        contentsManager = new ContentsManager(this);
        contentsManager.syncContents();

        drawerLayout = findViewById(R.id.DrawerLayout);

        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override public void onDrawerOpened(@NonNull View drawerView) {

            }
            @Override public void onDrawerClosed(@NonNull View drawerView) {
                // If the user left Relative Mouse enabled, recapture.
                if (isRelativeMouseMovement && !pointerCaptureRequested) {
                    drawerLayout.postDelayed(() -> ensurePointerCapture("drawer-closed"), 2000);
                }
            }
        });
        
        drawerLayout.setOnApplyWindowInsetsListener((view, windowInsets) -> windowInsets.replaceSystemWindowInsets(0, 0, 0, 0));
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        // Wire Compose in-game drawer
        boolean enableLogs = preferences.getBoolean("enable_wine_debug", false) || preferences.getBoolean("enable_box64_logs", false);
        boolean allowMagnifier = !XrActivity.isEnabled(this);
        XServerDrawerState state = XServerDrawerState.INSTANCE;
        state.reset();
        state.setShowLogs(enableLogs);
        state.setShowMagnifier(allowMagnifier);
        state.setIsPaused(isPaused);
        state.setIsRelativeMouseMovement(isRelativeMouseMovement);
        state.setIsMouseDisabled(isMouseDisabled);
        // LSFG initial state comes from container config (set later in setupXEnvironment)
        state.setLsfgEnabled(false);

        state.onClose                  = () -> runOnUiThread(() -> drawerLayout.closeDrawers());
        state.onKeyboard               = () -> AppUtils.showKeyboard(this);
        state.onInputControls          = () -> showInputControlsDialog();
        state.onScreenEffects          = () -> showScreenEffectsDialog();
        state.onGraphicEngine          = () -> showFsrOverlay();
        state.onVibration              = () -> showVibrationDialog();
        state.onLsfgToggle              = () -> {
            boolean current = XServerDrawerState.INSTANCE.getLsfgEnabled();
            XServerDrawerState.INSTANCE.setLsfgEnabled(!current);
        };
        state.onApplyLsfg               = () -> {
            // LSFG settings changed - apply to the running environment
        };
        state.onResetLsfg               = () -> {
            // Reset to GPU defaults using GLES detection
            boolean isMali = false;
            try {
                String r = android.opengl.GLES10.glGetString(android.opengl.GLES10.GL_RENDERER);
                if (r != null) isMali = r.contains("Mali");
            } catch (Exception ignore) {
            }
            XServerDrawerState drawState = XServerDrawerState.INSTANCE;
            drawState.setLsfgMultiplier(2);
            drawState.setLsfgQuality(isMali ? "performance" : "balanced");
            drawState.setLsfgFlowScale(isMali ? 50 : 100);
            drawState.setLsfgMaxLatency(isMali ? 8 : 16);
            drawState.setLsfgGpuArch(isMali ? "mali" : "auto");
        };
        state.onToggleFullscreen       = () -> {
            xServerView.getRenderer().toggleFullscreen();
            touchpadView.toggleFullscreen();
        };
        state.onPauseResume            = () -> {
            if (isPaused) {
                ProcessHelper.resumeAllWineProcesses();
                isPaused = false;
            } else {
                ProcessHelper.pauseAllWineProcesses();
                isPaused = true;
            }
            state.setIsPaused(isPaused);
        };
        state.onPipMode                = () -> enterPictureInPictureMode();
        state.onActiveWindows          = () -> showActiveWindowsDialog();
        state.onTaskManager            = () -> showTaskManagerDialog();
        state.onMagnifier              = () -> showMagnifierOverlay();
        state.onLogs                   = () -> XServerDialogState.INSTANCE.show(XServerDialogState.ActiveDialog.DEBUG);
        state.onExit                   = () -> exit();
        state.onMoveCursorToTouchpoint = () -> MoveCursorToTouchpoint();
        state.onRelativeMouseMovement  = () -> {
            isRelativeMouseMovement = !isRelativeMouseMovement;
            state.setIsRelativeMouseMovement(isRelativeMouseMovement);
            xServer.setRelativeMouseMovement(isRelativeMouseMovement);
        };
        state.onDisableMouse           = () -> {
            isMouseDisabled = !isMouseDisabled;
            state.setIsMouseDisabled(isMouseDisabled);
            touchpadView.setMouseEnabled(!isMouseDisabled);
        };

        ComposeView drawerComposeView = findViewById(R.id.XServerDrawerComposeView);
        XServerDrawerKt.setupComposeView(drawerComposeView);

        // Dialog host: a full-size ComposeView on top of the game surface for
        // in-game dialogs and floating overlays (magnifier, FSR panel).
        FrameLayout xServerDisplay = findViewById(R.id.FLXServerDisplay);
        ComposeView dialogHostView = new ComposeView(this);
        dialogHostView.setLayoutParams(new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        xServerDisplay.addView(dialogHostView);
        XServerDialogHostKt.setupDialogHost(dialogHostView);

        imageFs = ImageFs.find(this);

        // Prepare dev/input directory - actual event files created after shortcut is loaded
        File devInputDir = new File(imageFs.getRootDir(), "dev/input");
        if (devInputDir.exists() || devInputDir.mkdirs()) {
            for (int i = 0; i < 4; i++) {
                File eventFile = new File(devInputDir, "event" + i);
                if (eventFile.exists()) eventFile.delete();
            }
        }

        // Initialize the WinHandler
        winHandler = new WinHandler(this);
        winHandler.setFakeInputPath(devInputDir.getAbsolutePath());

        String screenSize = Container.DEFAULT_SCREEN_SIZE;
        containerManager = new ContainerManager(this);
        container = containerManager.getContainerById(getIntent().getIntExtra("container_id", 0));

        // Log shortcut_path
        String shortcutPath = getIntent().getStringExtra("shortcut_path");
        Log.d("XServerDisplayActivity", "Shortcut Path: " + shortcutPath);


        // Determine container ID
        int containerId = getIntent().getIntExtra("container_id", 0);
        Log.d("XServerDisplayActivity", "Container ID from Intent: " + containerId);
        if (containerId == 0) {
            Log.d("XServerDisplayActivity", "Container ID is 0, attempting to parse from .desktop file");
            // Proceed with .desktop file parsing
        }


        // If container_id is 0, read from the .desktop file
        if (containerId == 0 && shortcutPath != null && !shortcutPath.isEmpty()) {
            File shortcutFile = new File(shortcutPath);
            containerId = parseContainerIdFromDesktopFile(shortcutFile);
            Log.d("XServerDisplayActivity", "Parsed Container ID from .desktop file: " + containerId);
        }

        // Initialize playtime tracking
        playtimePrefs = getSharedPreferences("playtime_stats", MODE_PRIVATE);
        shortcutName = getIntent().getStringExtra("shortcut_name");

        // Ensure shortcutPath is not null before proceeding
        if (shortcutPath != null && !shortcutPath.isEmpty()) {
            if (shortcutName == null || shortcutName.isEmpty()) {
                shortcutName = parseShortcutNameFromDesktopFile(new File(shortcutPath));
                Log.d("XServerDisplayActivity", "Parsed Shortcut Name from .desktop file: " + shortcutName);
            }
        } else {
            Log.d("XServerDisplayActivity", "No shortcut path provided, skipping shortcut parsing.");
        }

        // Increment play count at the start of a session
        incrementPlayCount();

        // Log the final container_id
        Log.d("XServerDisplayActivity", "Final Container ID: " + containerId);

        // Retrieve the container and check if it's null
        container = containerManager.getContainerById(containerId);

        if (container == null) {
            Log.e("XServerDisplayActivity", "Failed to retrieve container with ID: " + containerId);
            finish();  // Gracefully exit the activity to avoid crashing
            return;
        }

        containerManager.activateContainer(container);

        if (shortcutPath != null && !shortcutPath.isEmpty()) {
            shortcut = new Shortcut(container, new File(shortcutPath));
        }

        // Pre-create all 4 event files so Wine registers every slot at startup.
        // Wine scans /dev/input/ once on boot — slots that don't exist then are never seen,
        // even if created later. OSC takes slot 0; physical controllers need slots 1-3.
        for (int i = 0; i < 4; i++) {
            try { new File(devInputDir, "event" + i).createNewFile(); } catch (Exception e) {}
        }
        Log.d("XServerDisplayActivity", "Pre-created 4 controller event file(s)");

        taskAffinityMask = (short) ProcessHelper.getAffinityMask(container.getCPUList(true));
        taskAffinityMaskWoW64 = (short) ProcessHelper.getAffinityMask(container.getCPUListWoW64(true));

        if (shortcut != null) {
            taskAffinityMask = (short) ProcessHelper.getAffinityMask(shortcut.getExtra("cpuList", container.getCPUList(true)));
            taskAffinityMaskWoW64 = taskAffinityMask;
        }

        // Determine the class name for the startup workarounds
        String wmClass = shortcut != null ? shortcut.getExtra("wmClass", "") : "";
        Log.d("XServerDisplayActivity", "Startup wmClass: " + wmClass);

        firstTimeBoot = container.getExtra("appVersion").isEmpty();

        String wineVersion = container.getWineVersion();
        wineInfo = WineInfo.fromIdentifier(this, contentsManager, wineVersion);

        imageFs.setWinePath(wineInfo.path);

        ProcessHelper.removeAllDebugCallbacks();
        XServerDialogState.INSTANCE.clearLog();
        if (enableLogs) {
            LogView.setFilename(getExecutable());
            ProcessHelper.addDebugCallback(line -> XServerDialogState.INSTANCE.appendLog(line));
        }

        graphicsDriver = container.getGraphicsDriver();
        String graphicsDriverConfig = container.getGraphicsDriverConfig();
        audioDriver = container.getAudioDriver();
        emulator = container.getEmulator();
        midiSoundFont = container.getMIDISoundFont();
        dxwrapper = container.getDXWrapper();
        String fpsCounterConfig = container.getFPSCounterConfig();
        String dxwrapperConfig = container.getDXWrapperConfig();
        screenSize = container.getScreenSize();
        winHandler.setInputType((byte) container.getInputType());
        lc_all = container.getLC_ALL();

        // Log the entire intent to verify the extras
        Intent intent = getIntent();
        Log.d("XServerDisplayActivity", "Intent Extras: " + intent.getExtras());

        if (shortcut != null) {
            graphicsDriver = shortcut.getExtra("graphicsDriver", container.getGraphicsDriver());
            graphicsDriverConfig = shortcut.getExtra("graphicsDriverConfig", container.getGraphicsDriverConfig());
            audioDriver = shortcut.getExtra("audioDriver", container.getAudioDriver());
            emulator = shortcut.getExtra("emulator", container.getEmulator());
            dxwrapper = shortcut.getExtra("dxwrapper", container.getDXWrapper());
            dxwrapperConfig = shortcut.getExtra("dxwrapperConfig", container.getDXWrapperConfig());
            screenSize = shortcut.getExtra("screenSize", container.getScreenSize());
            lc_all = shortcut.getExtra("lc_all", container.getLC_ALL());
            String inputType = shortcut.getExtra("inputType");
            if (!inputType.isEmpty()) winHandler.setInputType(Byte.parseByte(inputType));
            String xinputDisabledString = shortcut.getExtra("disableXinput", "false");
            xinputDisabledFromShortcut = parseBoolean(xinputDisabledString);
            // Pass the value to WinHandler
            winHandler.setXInputDisabled(xinputDisabledFromShortcut);
            String sharpnessEffect = shortcut.getExtra("sharpnessEffect", "None");
            if (!sharpnessEffect.equals("None")) {
                double sharpnessLevel = Double.parseDouble(shortcut.getExtra("sharpnessLevel", "100"));
                double sharpnessDenoise = Double.parseDouble(shortcut.getExtra("sharpnessDenoise", "100"));
                vkbasaltConfig = "effects=" + sharpnessEffect.toLowerCase() + ";" + "casSharpness=" + sharpnessLevel / 100 + ";" + "dlsSharpness=" + sharpnessLevel / 100  + ";" + "dlsDenoise=" + sharpnessDenoise / 100 + ";" + "enableOnLaunch=True";
            }
            Log.d("XServerDisplayActivity", "XInput Disabled from Shortcut: " + xinputDisabledFromShortcut);
        }

        this.graphicsDriverConfig = GraphicsDriverConfigDialog.parseGraphicsDriverConfig(graphicsDriverConfig);
        this.dxwrapperConfig = DXVKConfigDialog.parseConfig(dxwrapperConfig);

        if (!wineInfo.isWin64()) {
            onExtractFileListener = (file, size) -> {
                String path = file.getPath();
                if (path.contains("system32/")) return null;
                return new File(path.replace("syswow64/", "system32/"));
            };
        }

        if (shortcut == null)
            preloaderDialog.show(container.getName(), null);
        else {
            preloaderDialog.show(shortcut.name, shortcut.icon);
        }

        inputControlsManager = new InputControlsManager(this);
        xServer = new XServer(new ScreenInfo(screenSize));
        xServer.setWinHandler(winHandler);

        boolean[] winStarted = {false};

        // Add the OnWindowModificationListener for dynamic workarounds
        xServer.windowManager.addOnWindowModificationListener(new WindowManager.OnWindowModificationListener() {
            @Override
            public void onUpdateWindowContent(Window window) {
                if (!winStarted[0] && window.isApplicationWindow()) {
                    xServerView.getRenderer().setCursorVisible(true);
                    preloaderDialog.closeOnUiThread();
                    winStarted[0] = true;
                }
                    
                if (frameRatingWindowId == window.id) {
                    if (frameRating != null) frameRating.update();
                    if (frameRatingHorizontal != null) frameRatingHorizontal.update();
                }
            }
           
            @Override
            public void onMapWindow(Window window) {
                // Log the class name of the mapped window
                Log.d("XServerDisplayActivity", "onMapWindow: Detected window className: " + window.getClassName());
                assignTaskAffinity(window);
            }

            @Override
            public void onModifyWindowProperty(Window window, Property property) {
                changeFrameRatingVisibility(window, property);
            }    

            @Override
            public void onUnmapWindow(Window window) {
                changeFrameRatingVisibility(window, null);
            }
        });

        if (!midiSoundFont.equals("")) {
            InputStream in = null;
            InputStream finalIn = in;
            MidiManager.OnMidiLoadedCallback callback = new MidiManager.OnMidiLoadedCallback() {
                @Override
                public void onSuccess(SF2Soundbank soundbank) {
                    midiHandler = new MidiHandler();
                    midiHandler.setSoundBank(soundbank);
                    midiHandler.start();
                }

                @Override
                public void onFailed(Exception e) {
                    try {
                        finalIn.close();
                    } catch (Exception e2) {}
                }
            };
            try {
                if (midiSoundFont.equals(MidiManager.DEFAULT_SF2_FILE)) {
                    in = getAssets().open(MidiManager.SF2_ASSETS_DIR + "/" + midiSoundFont);
                    MidiManager.load(in, callback);
                } else
                    MidiManager.load(new File(MidiManager.getSoundFontDir(this), midiSoundFont), callback);
            } catch (Exception e) {}
        }

        // Check if a profile is defined by the shortcut
        String controlsProfile = shortcut != null ? shortcut.getExtra("controlsProfile", "") : "";

        createNotifcationChannel();

        Intent notificationIntent = new Intent(this, XServerDisplayActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_ab_gear_0011)
                .setContentTitle("Winlator")
                .setContentText("Winlator is running, do not kill or swipe this notification")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false);

        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, builder.build());

        Runnable runnable = () -> {
            setupUI();
            if (controlsProfile.isEmpty()) {
                // No profile defined, run the simulated dialog confirmation for input controls
                simulateConfirmInputControlsDialog();
            }
            Executors.newSingleThreadExecutor().execute(() -> {
                setupWineSystemFiles();
                extractGraphicsDriverFiles();
                changeWineAudioDriver();
                try {
                    setupXEnvironment();
                } catch (PackageManager.NameNotFoundException e) {
                    throw new RuntimeException(e);
                }
            });
        };

        if (xServer.screenInfo.height > xServer.screenInfo.width) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            configChangedCallback = runnable;
        } else
              runnable.run();
    }

    // Method to parse container_id from .desktop file
    private int parseContainerIdFromDesktopFile(File desktopFile) {
        int containerId = 0;
        if (desktopFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(desktopFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("container_id:")) {
                        containerId = Integer.parseInt(line.split(":")[1].trim());
                        break;
                    }
                }
            } catch (IOException | NumberFormatException e) {
                Log.e("XServerDisplayActivity", "Error parsing container_id from .desktop file", e);
            }
        }
        return containerId;
    }

    private boolean parseBoolean(String value) {
        // Return true for "true", "1", "yes" (case-insensitive)
        if ("true".equalsIgnoreCase(value) || "1".equals(value) || "yes".equalsIgnoreCase(value)) {
            return true;
        }
        // Return false for any other value, including "false", "0", "no"
        return false;
    }

    // Inside XServerDisplayActivity class
    private void handleCapturedPointer(MotionEvent event) {
        boolean handled = false;

        int actionButton = event.getActionButton();
        switch (event.getAction()) {
            case MotionEvent.ACTION_BUTTON_PRESS:
                if (actionButton == MotionEvent.BUTTON_PRIMARY) {
                    if (xServer.isRelativeMouseMovement())
                        xServer.getWinHandler().mouseEvent(MouseEventFlags.LEFTDOWN, 0, 0, 0);
                    else
                        xServer.injectPointerButtonPress(Pointer.Button.BUTTON_LEFT);
                } else if (actionButton == MotionEvent.BUTTON_SECONDARY) {
                    if (xServer.isRelativeMouseMovement())
                        xServer.getWinHandler().mouseEvent(MouseEventFlags.RIGHTDOWN, 0, 0, 0);
                    else
                        xServer.injectPointerButtonPress(Pointer.Button.BUTTON_RIGHT);
                } else if (actionButton == MotionEvent.BUTTON_TERTIARY) {
                    if (xServer.isRelativeMouseMovement())
                        xServer.getWinHandler().mouseEvent(MouseEventFlags.MIDDLEDOWN, 0, 0, 0);
                    else
                        xServer.injectPointerButtonPress(Pointer.Button.BUTTON_MIDDLE); // Add this line for middle mouse button press
                }
                handled = true;
                break;
            case MotionEvent.ACTION_BUTTON_RELEASE:
                if (actionButton == MotionEvent.BUTTON_PRIMARY) {
                    if (xServer.isRelativeMouseMovement())
                        xServer.getWinHandler().mouseEvent(MouseEventFlags.LEFTUP, 0, 0, 0);
                    else
                        xServer.injectPointerButtonRelease(Pointer.Button.BUTTON_LEFT);
                } else if (actionButton == MotionEvent.BUTTON_SECONDARY) {
                    if (xServer.isRelativeMouseMovement())
                        xServer.getWinHandler().mouseEvent(MouseEventFlags.RIGHTUP, 0, 0, 0);
                    else
                        xServer.injectPointerButtonRelease(Pointer.Button.BUTTON_RIGHT);
                } else if (actionButton == MotionEvent.BUTTON_TERTIARY) {
                    if (xServer.isRelativeMouseMovement())
                        xServer.getWinHandler().mouseEvent(MouseEventFlags.MIDDLEUP, 0, 0, 0);
                    else
                        xServer.injectPointerButtonRelease(Pointer.Button.BUTTON_MIDDLE); // Add this line for middle mouse button release
                }
                handled = true;
                break;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_HOVER_MOVE:
                float[] transformedPoint = XForm.transformPoint(xform, event.getX(), event.getY());
                if (xServer.isRelativeMouseMovement())
                    xServer.getWinHandler().mouseEvent(MouseEventFlags.MOVE, (int)transformedPoint[0], (int)transformedPoint[1], 0);
                else
                    xServer.injectPointerMoveDelta((int)transformedPoint[0], (int)transformedPoint[1]);
                handled = true;
                break;
            case MotionEvent.ACTION_SCROLL:
                float scrollY = event.getAxisValue(MotionEvent.AXIS_VSCROLL);
                if (scrollY <= -1.0f) {
                    if (xServer.isRelativeMouseMovement())
                        xServer.getWinHandler().mouseEvent(MouseEventFlags.WHEEL, 0, 0, (int)scrollY * 270);
                    else {
                        xServer.injectPointerButtonPress(Pointer.Button.BUTTON_SCROLL_DOWN);
                        xServer.injectPointerButtonRelease(Pointer.Button.BUTTON_SCROLL_DOWN);
                    }
                } else if (scrollY >= 1.0f) {
                    if (xServer.isRelativeMouseMovement())
                        xServer.getWinHandler().mouseEvent(MouseEventFlags.WHEEL, 0, 0,(int)scrollY * 270);
                    else {
                        xServer.injectPointerButtonPress(Pointer.Button.BUTTON_SCROLL_UP);
                        xServer.injectPointerButtonRelease(Pointer.Button.BUTTON_SCROLL_UP);
                    }
                }
                handled = true;
                break;
        }
    }

    private void ensurePointerCapture(String reason) {
        if (!isRelativeMouseMovement || touchpadView == null) return;

        final int[] tries = {0};
        Runnable attempt = new Runnable() {
            @Override public void run() {
                if (!hasWindowFocus()) { touchpadView.postDelayed(this, 50); return; }
                if (!touchpadView.isAttachedToWindow()) { touchpadView.postDelayed(this, 50); return; }

                // Make sure the view can take focus
                touchpadView.setFocusableInTouchMode(true);
                touchpadView.requestFocus();

                touchpadView.requestPointerCapture();
                touchpadView.setOnCapturedPointerListener((v, e) -> { handleCapturedPointer(e); return true; });
                pointerCaptureRequested = true;

            }
        };
        // Try quickly a few times to dodge transient focus transitions
        touchpadView.postDelayed(attempt, 50); // First attempt
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MainActivity.EDIT_INPUT_CONTROLS_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (editInputControlsCallback != null) {
                editInputControlsCallback.run();
                editInputControlsCallback = null;
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        if (environment != null) {
            xServerView.onResume();
            environment.onResume();
        }
        startTime = System.currentTimeMillis();
        handler.postDelayed(savePlaytimeRunnable, SAVE_INTERVAL_MS);
        ProcessHelper.resumeAllWineProcesses();
    }

    @Override
    public void onPause() {
        super.onPause();

        // Check if we are entering Picture-in-Picture mode
        if (!isInPictureInPictureMode()) {
            // Only pause environment and xServerView if not in PiP mode
            if (environment != null) {
                environment.onPause();
                xServerView.onPause();
            }
        }

        savePlaytimeData();
        handler.removeCallbacks(savePlaytimeRunnable);
        ProcessHelper.pauseAllWineProcesses();
    }


    private void savePlaytimeData() {
        long endTime = System.currentTimeMillis();
        long playtime = endTime - startTime;

        // Ensure that playtime is not negative
        if (playtime < 0) {
            playtime = 0;
        }

        SharedPreferences.Editor editor = playtimePrefs.edit();
        String playtimeKey = shortcutName + "_playtime";

        // Accumulate the playtime into totalPlaytime
        long totalPlaytime = playtimePrefs.getLong(playtimeKey, 0) + playtime;
        editor.putLong(playtimeKey, totalPlaytime);
        editor.apply();

        // Reset startTime to the current time for the next interval
        startTime = System.currentTimeMillis();
    }


    private void incrementPlayCount() {
        SharedPreferences.Editor editor = playtimePrefs.edit();
        String playCountKey = shortcutName + "_play_count";
        int playCount = playtimePrefs.getInt(playCountKey, 0) + 1;
        editor.putInt(playCountKey, playCount);
        editor.apply();
    }

    private void exit() {
        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID);
        preloaderDialog.showOnUiThread(R.string.shutdown);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                savePlaytimeData(); // Save on destroy
                handler.removeCallbacks(savePlaytimeRunnable);
                if (midiHandler != null) midiHandler.stop();
                // Unregister sensor listener to avoid memory leaks
                if (environment != null) environment.stopEnvironmentComponents();
                if (preloaderDialog != null && preloaderDialog.isShowing()) preloaderDialog.closeOnUiThread();
                if (winHandler != null) winHandler.stop();
                if (wineRequestHandler != null) wineRequestHandler.stop();
                /* Gracefully terminate all running wine processes */
                ProcessHelper.terminateAllWineProcesses();
                /* Wait until all processes have gracefully terminated, forcefully killing them only after a certain amount of time */
                long start = System.currentTimeMillis();
                while (!ProcessHelper.listRunningWineProcesses().isEmpty()) {
                    long elapsed = System.currentTimeMillis() - start;
                    if (elapsed >= 1500) {
                        break;
                    }
                }
                preloaderDialog.closeOnUiThread();
                AppUtils.restartApplication(getApplicationContext());
            }
        }, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wineDebugLogCallback != null) {
            ProcessHelper.removeDebugCallback(wineDebugLogCallback);
            wineDebugLogCallback = null;
        }
        if (wineDebugWriter != null) {
            wineDebugWriter.close();
            wineDebugWriter = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        savePlaytimeData();
        handler.removeCallbacks(savePlaytimeRunnable);
    }

    private void releasePointerCaptureIfNeeded(String reason) {
        if (pointerCaptureRequested && touchpadView != null) {
            touchpadView.releasePointerCapture();
            touchpadView.setOnCapturedPointerListener(null);
            pointerCaptureRequested = false;
            Log.d("PointerCapture", "Released: " + reason);
        }
    }

    @Override
    public void onBackPressed() {
        if (environment != null) {
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
            else drawerLayout.closeDrawers();
        }
    }

    private void openXServerDrawer() {
        if (environment != null) {
            releasePointerCaptureIfNeeded("open-drawer/shortcut");
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START);
            } else {
                drawerLayout.closeDrawers();
            }
        }
    }


    
    private void showVibrationDialog() {
        if (winHandler == null) return;
        int max = winHandler.getMaxControllers();
        java.util.List<android.util.Pair<String, Boolean>> slots = new java.util.ArrayList<>();
        for (int i = 0; i < max; i++) {
            slots.add(new android.util.Pair<>(
                getString(R.string.vibration_slot, i + 1),
                winHandler.isVibrationEnabledForSlot(i)));
        }
        // Convert android.util.Pair to kotlin.Pair for XServerDialogState
        java.util.List<kotlin.Pair<String, Boolean>> kSlots = new java.util.ArrayList<>();
        for (android.util.Pair<String, Boolean> p : slots) {
            kSlots.add(new kotlin.Pair<>(p.first, p.second));
        }
        XServerDialogState ds = XServerDialogState.INSTANCE;
        ds.setVibrationSlots(kSlots);
        ds.onVibrationSlotChanged = (slot, enabled) -> winHandler.setVibrationEnabledForSlot(slot, enabled);
        ds.show(XServerDialogState.ActiveDialog.VIBRATION);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus && cursorLock) {
            touchpadView.requestPointerCapture();
            touchpadView.setOnCapturedPointerListener(new View.OnCapturedPointerListener() {
                @Override
                public boolean onCapturedPointer(View view, MotionEvent event) {
                    handleCapturedPointer(event);
                    return true;
                }
            });
        }
        else if (!hasFocus) {
            touchpadView.releasePointerCapture();
            touchpadView.setOnCapturedPointerListener(null);
        }
    }

    // private void extractInputDLLs() {
    //     String inputAsset = "input_dlls.tzst";
    //     File wineFolder = new File(imageFs.getWinePath() + "/lib/wine/");
    //     boolean success = TarCompressorUtils.extract(TarCompressorUtils.Type.ZSTD, this, inputAsset, wineFolder);
    //     if (!success)
    //         Log.d("XServerDisplayActivity", "Failed to extract input dlls");
    // }

    private void setupWineSystemFiles() {
        String appVersion = String.valueOf(AppUtils.getVersionCode(this));
        String imgVersion = String.valueOf(imageFs.getVersion());
        boolean containerDataChanged = false;

        if (!container.getExtra("appVersion").equals(appVersion) || !container.getExtra("imgVersion").equals(imgVersion)) {
            applyGeneralPatches(container);
            container.putExtra("appVersion", appVersion);
            container.putExtra("imgVersion", imgVersion);
            containerDataChanged = true;
        }

        String dxwrapper = this.dxwrapper;

        if (dxwrapper.contains("dxvk")) {
            String dxvkWrapper = "dxvk-" + dxwrapperConfig.get("version");
            String vkd3dWrapper = "vkd3d-" + dxwrapperConfig.get("vkd3dVersion");
            String ddrawrapper = dxwrapperConfig.get("ddrawrapper");
            dxwrapper = dxvkWrapper + ";" + vkd3dWrapper + ";" + ddrawrapper;
        }

        if (!dxwrapper.equals(container.getExtra("dxwrapper"))) {
            if (extractDXWrapperFiles(dxwrapper)) {
                container.putExtra("dxwrapper", dxwrapper);
                containerDataChanged = true;
            }
        }

        String wincomponents = shortcut != null ? shortcut.getExtra("wincomponents", container.getWinComponents()) : container.getWinComponents();
        if (!wincomponents.equals(container.getExtra("wincomponents"))) {
            extractWinComponentFiles();
            container.putExtra("wincomponents", wincomponents);
            containerDataChanged = true;
        }

        String desktopTheme = container.getDesktopTheme();
        if (!(desktopTheme+","+xServer.screenInfo).equals(container.getExtra("desktopTheme"))) {
            WineThemeManager.apply(this, new WineThemeManager.ThemeInfo(desktopTheme), xServer.screenInfo);
            container.putExtra("desktopTheme", desktopTheme+","+xServer.screenInfo);
            containerDataChanged = true;
        }

        WineStartMenuCreator.create(this, container);
        WineUtils.createDosdevicesSymlinks(container);
        
        // Configure Wine joystick registry keys based on DInput setting
        int inputType = container.getInputType();
        if (shortcut != null) {
            String shortcutInputType = shortcut.getExtra("inputType");
            if (!shortcutInputType.isEmpty()) {
                inputType = Byte.parseByte(shortcutInputType);
            }
        }
        boolean dinputEnabled = (inputType & WinHandler.FLAG_INPUT_TYPE_DINPUT) == WinHandler.FLAG_INPUT_TYPE_DINPUT;
        
        boolean exclusiveXInput = container.isExclusiveXInput();
        if (shortcut != null) {
            String extra = shortcut.getExtra("exclusiveXInput");
            if (!extra.isEmpty()) exclusiveXInput = extra.equals("1");
        }
        
        WineUtils.setJoystickRegistryKeys(container, dinputEnabled, exclusiveXInput);

        if (shortcut != null)
            startupSelection = shortcut.getExtra("startupSelection", String.valueOf(container.getStartupSelection()));
        else
            startupSelection = String.valueOf(container.getStartupSelection());

        if (!startupSelection.equals(container.getExtra("startupSelection"))) {
            WineUtils.changeServicesStatus(container, startupSelection);
            container.putExtra("startupSelection", startupSelection);
            containerDataChanged = true;
        }
        if (containerDataChanged) container.saveData();
    }

    private void setupXEnvironment() throws PackageManager.NameNotFoundException {

        // Set environment variables
        envVars.put("LC_ALL", lc_all);
        envVars.put("WINEPREFIX", imageFs.wineprefix);

        boolean enableWineDebug = preferences.getBoolean("enable_wine_debug", false);
        String wineDebugChannels = preferences.getString("wine_debug_channels", SettingsFragment.DEFAULT_WINE_DEBUG_CHANNELS);
        // Always include +err,+warn so the debug log captures crash info even when verbose debug is off.
        // Prepend them to whatever the user has selected; "-all" baseline is dropped so errors surface.
        String wineDebugValue;
        if (enableWineDebug && !wineDebugChannels.isEmpty()) {
            wineDebugValue = "+" + wineDebugChannels.replace(",", ",+");
        } else {
            wineDebugValue = "+err,+warn,+fixme,-all";
        }
        envVars.put("WINEDEBUG", wineDebugValue);

        // ── Wine debug file log ────────────────────────────────────────────────
        // Writes all Wine stdout/stderr to a readable file.
        // Path: /sdcard/Android/data/com.winlator.star/files/wine_debug.log
        try {
            File logDir = getExternalFilesDir(null);
            if (logDir != null) {
                logDir.mkdirs();
                File logFile = new File(logDir, "wine_debug.log");
                wineDebugWriter = new java.io.PrintWriter(
                        new java.io.BufferedWriter(new java.io.FileWriter(logFile, false)), true);
                // Header: print context that helps diagnose the crash
                wineDebugWriter.println("=== Wine Debug Log ===");
                wineDebugWriter.println("WINEDEBUG: " + wineDebugValue);
                wineDebugWriter.println("WINEPREFIX: " + imageFs.wineprefix);
                wineDebugWriter.println("Container ID: " + (container != null ? container.id : "null"));
                if (shortcut != null) {
                    wineDebugWriter.println("Shortcut file: " + shortcut.file.getPath());
                    wineDebugWriter.println("Shortcut path (resolved): " + shortcut.path);
                } else {
                    wineDebugWriter.println("Shortcut: null (launching Wine File Manager)");
                }
                // DX wrapper diagnostic
                wineDebugWriter.println("--- DX Wrapper State ---");
                wineDebugWriter.println("dxwrapper type: " + this.dxwrapper);
                wineDebugWriter.println("dxwrapperConfig (raw): " + (container != null ? container.getDXWrapperConfig() : "null"));
                wineDebugWriter.println("vkd3dVersion (parsed): " + dxwrapperConfig.get("vkd3dVersion"));
                wineDebugWriter.println("dxvk version (parsed): " + dxwrapperConfig.get("version"));
                wineDebugWriter.println("ddrawrapper (parsed): " + dxwrapperConfig.get("ddrawrapper"));
                String cachedDxwrapper = (container != null ? container.getExtra("dxwrapper") : "none");
                wineDebugWriter.println("cached dxwrapper extra: " + cachedDxwrapper);
                if (this.dxwrapper.contains("dxvk")) {
                    String expectedDxvkWrapper = "dxvk-" + dxwrapperConfig.get("version");
                    String expectedVkd3dWrapper = "vkd3d-" + dxwrapperConfig.get("vkd3dVersion");
                    String expectedDdra = dxwrapperConfig.get("ddrawrapper");
                    String expectedFull = expectedDxvkWrapper + ";" + expectedVkd3dWrapper + ";" + expectedDdra;
                    wineDebugWriter.println("expected full string: " + expectedFull);
                    wineDebugWriter.println("extraction will run: " + (!expectedFull.equals(cachedDxwrapper)));
                }
                wineDebugWriter.println("--- End DX Wrapper State ---");
                wineDebugWriter.println("=== Wine output below ===");
                wineDebugLogCallback = line -> {
                    if (wineDebugWriter != null) {
                        wineDebugWriter.println(line);
                    }
                };
                ProcessHelper.addDebugCallback(wineDebugLogCallback);
                Log.d("WineDebug", "Wine debug log → " + logFile.getAbsolutePath());
            }
        } catch (Exception e) {
            Log.e("WineDebug", "Failed to open wine debug log file", e);
        }

        // Clear any temporary directory
        String rootPath = imageFs.getRootDir().getPath();
        FileUtils.clear(imageFs.getTmpDir());


        guestProgramLauncherComponent = new GuestProgramLauncherComponent(
                contentsManager,
                contentsManager.getProfileByEntryName(container.getWineVersion()),
                shortcut
        );

        // Additional container checks and environment configuration
        if (container != null) {
            if (Byte.parseByte(startupSelection) == Container.STARTUP_SELECTION_AGGRESSIVE) {
                // winHandler.killProcess("services.exe"); 
            }
            guestProgramLauncherComponent.setContainer(this.container);
            guestProgramLauncherComponent.setWineInfo(this.wineInfo);

            String guestExecutable = "wine explorer /desktop=shell," + xServer.screenInfo + " " + getWineStartCommand();

            guestProgramLauncherComponent.setGuestExecutable(guestExecutable);

            envVars.putAll(container.getEnvVars());

            if (shortcut != null) envVars.putAll(shortcut.getExtra("envVars"));

            if (!envVars.has("WINEESYNC")) {
                envVars.put("WINEESYNC", "1");
            }

            ArrayList<String> bindingPaths = new ArrayList<>();
            for (String[] drive : container.drivesIterator()) {
                bindingPaths.add(drive[1]);
            }

            guestProgramLauncherComponent.setBindingPaths(bindingPaths.toArray(new String[0]));

            guestProgramLauncherComponent.setBox64Preset(
                    shortcut != null
                            ? shortcut.getExtra("box64Preset", container.getBox64Preset())
                            : container.getBox64Preset()
            );

            guestProgramLauncherComponent.setFEXCorePreset(
                    shortcut != null
                            ? shortcut.getExtra("fexcorePreset", container.getFEXCorePreset())
                            : container.getFEXCorePreset()
            );
        }

        // Merge overrideEnvVars if present
        if (overrideEnvVars != null) {
            envVars.putAll(overrideEnvVars);
            overrideEnvVars.clear(); // Clear overrideEnvVars as per smali logic
        }

        // Create our overall XEnvironment with various components
        environment = new XEnvironment(this, imageFs);
        environment.addComponent(
                new SysVSharedMemoryComponent(
                        xServer,
                        UnixSocketConfig.createSocket(rootPath, UnixSocketConfig.SYSVSHM_SERVER_PATH)
                )
        );
        environment.addComponent(
                new XServerComponent(
                        xServer,
                        UnixSocketConfig.createSocket(rootPath, UnixSocketConfig.XSERVER_PATH)
                )
        );

        // Audio driver logic
        if (audioDriver.equals("alsa")) {
            envVars.put("ANDROID_ALSA_SERVER", rootPath + UnixSocketConfig.ALSA_SERVER_PATH);
            envVars.put("ANDROID_ASERVER_USE_SHM", "true");
            environment.addComponent(
                    new ALSAServerComponent(
                            UnixSocketConfig.createSocket(rootPath, UnixSocketConfig.ALSA_SERVER_PATH)
                    )
            );
        } else if (audioDriver.equals("pulseaudio")) {
            envVars.put("PULSE_SERVER", rootPath + UnixSocketConfig.PULSE_SERVER_PATH);
            environment.addComponent(
                    new PulseAudioComponent(
                            UnixSocketConfig.createSocket(rootPath, UnixSocketConfig.PULSE_SERVER_PATH)
                    )
            );
        }

        // Pass final envVars to the launcher
        guestProgramLauncherComponent.setEnvVars(envVars);
        guestProgramLauncherComponent.setTerminationCallback((status) -> exit());

        // Add the launcher to our environment
        environment.addComponent(guestProgramLauncherComponent);

        // Initialize fake input for controller emulation - MUST be before Wine starts! Deleting old ones should also be done here ofc.
        // Initialize fake input for controller emulation - MUST be before Wine starts!
        File devInputDir = new File(imageFs.getRootDir(), "dev/input");
        if (devInputDir.exists() || devInputDir.mkdirs()) {
             // Cleanup moved to onCreate
        }

        // Start all environment components (XServer, Audio, Wine, etc.)
        environment.startEnvironmentComponents();

        // Start the WinHandler (writes events to the file)
        winHandler.start();

        if (wineRequestHandler != null) wineRequestHandler.start();

        // Reset dxwrapper config
        dxwrapperConfig = null;

        // Copy container LSFG config to drawer state
        if (container != null) {
            XServerDrawerState drawState = XServerDrawerState.INSTANCE;
            drawState.setLsfgEnabled(container.isLsfgEnabled());
            drawState.setLsfgMultiplier(container.getLsfgMultiplier());
            drawState.setLsfgQuality(container.getLsfgQuality());
            drawState.setLsfgFlowScale(container.getLsfgFlowScale());
            drawState.setLsfgMaxLatency(container.getLsfgMaxLatency());
            drawState.setLsfgGpuArch(container.getLsfgGpuArch());
        }
        
    }

    private void createWrapperScript(String path, String content) {
        File scriptFile = new File(path);
        FileUtils.writeString(scriptFile, content);
        scriptFile.setExecutable(true);
    }

    private void setupUI() {
        FrameLayout rootView = findViewById(R.id.FLXServerDisplay);
        xServerView = new XServerView(this, xServer);
        final GLRenderer renderer = xServerView.getRenderer();
        renderer.setCursorVisible(false);

        if (shortcut != null) {
            renderer.setUnviewableWMClasses("explorer.exe");
        }

        xServer.setRenderer(renderer);
        rootView.addView(xServerView);

        globalCursorSpeed = preferences.getFloat("cursor_speed", 1.0f);
        touchpadView = new TouchpadView(this, xServer, timeoutHandler, hideControlsRunnable);
        touchpadView.setSensitivity(globalCursorSpeed);
        touchpadView.setMouseEnabled(!isMouseDisabled);
        touchpadView.setFourFingersTapCallback(() -> {
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)) drawerLayout.openDrawer(GravityCompat.START);
        });
        rootView.addView(touchpadView);

        inputControlsView = new InputControlsView(this, timeoutHandler, hideControlsRunnable);
        inputControlsView.setOverlayOpacity(preferences.getFloat("overlay_opacity", InputControlsView.DEFAULT_OVERLAY_OPACITY));
        inputControlsView.setTouchpadView(touchpadView);
        inputControlsView.setXServer(xServer);
        inputControlsView.setVisibility(View.GONE);
        rootView.addView(inputControlsView);


        startTouchscreenTimeout();

        // Inside onCreate(), after initializing controls
        boolean isTimeoutEnabled = preferences.getBoolean("touchscreen_timeout_enabled", false);
        if (isTimeoutEnabled) {
            startTouchscreenTimeout();
        }

        if (container != null && container.isShowFPS()) {
            String fpsConfigString = container.getFPSCounterConfig();
            com.winlator.star.core.KeyValueSet fpsConfig = new com.winlator.star.core.KeyValueSet(fpsConfigString);
            boolean isHorizontal = fpsConfig.get("hudMode", "vertical").equals("horizontal");

            if (isHorizontal) {
                frameRatingHorizontal = new FrameRatingHorizontal(this);
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    android.view.Gravity.TOP | android.view.Gravity.CENTER_HORIZONTAL
                );
                lp.topMargin = 10;
                frameRatingHorizontal.setLayoutParams(lp);
                frameRatingHorizontal.applyConfig(fpsConfigString);
                frameRatingHorizontal.setVisibility(View.GONE);
                rootView.addView(frameRatingHorizontal);
            } else {
                frameRating = new FrameRating(this, graphicsDriverConfig);
                frameRating.applyConfig(fpsConfigString);
                frameRating.setVisibility(View.GONE);
                rootView.addView(frameRating);
            }
        }

        // Get the fullscreen stretched extra from the shortcut if available
        String shortcutFullscreenStretched = shortcut != null ? shortcut.getExtra("fullscreenStretched") : null;

        // Proceed based on container and shortcut settings
        boolean shouldStretch = false;

        if (shortcut != null && shortcutFullscreenStretched != null) {
            // Shortcut exists and has a valid setting
            shouldStretch = shortcutFullscreenStretched.equals("1");
        } else if (container != null && container.isFullscreenStretched()) {
            // No shortcut or shortcut doesn't override, use the container's setting
            shouldStretch = true;
        }

        if (shouldStretch) {
            // Toggle fullscreen mode based on the final decision
            renderer.toggleFullscreen();
            touchpadView.toggleFullscreen();
        }

        if (shortcut != null) {
            String controlsProfile = shortcut.getExtra("controlsProfile");
            if (!controlsProfile.isEmpty()) {
                ControlsProfile profile = inputControlsManager.getProfile(Integer.parseInt(controlsProfile));
                if (profile != null) showInputControls(profile);
            }

            String simTouchScreen = shortcut.getExtra("simTouchScreen");
            touchpadView.setSimTouchScreen(simTouchScreen.equals("1"));
        }

        AppUtils.observeSoftKeyboardVisibility(drawerLayout, renderer::setScreenOffsetYRelativeToCursor);
    }



    private ActivityResultLauncher<Intent> controlsEditorActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (editInputControlsCallback != null) {
                    editInputControlsCallback.run();
                    editInputControlsCallback = null;
                }
            }
    );

    private String parseShortcutNameFromDesktopFile(File desktopFile) {
        String shortcutName = "";
        if (desktopFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(desktopFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("Name=")) {
                        shortcutName = line.split("=")[1].trim();
                        break;
                    }
                }
            } catch (IOException e) {
                Log.e("XServerDisplayActivity", "Error reading shortcut name from .desktop file", e);
            }
        }
        return shortcutName;
    }

    private void setTextColorForDialog(ViewGroup viewGroup, int color) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof ViewGroup) {
                // If the child is a ViewGroup, recursively apply the color
                setTextColorForDialog((ViewGroup) child, color);
            } else if (child instanceof TextView) {
                // If the child is a TextView, set its text color
                ((TextView) child).setTextColor(color);
            }
        }
    }

    private void showInputControlsDialog() {
        ArrayList<ControlsProfile> profiles = inputControlsManager.getProfiles(true);
        ArrayList<String> profileNames = new ArrayList<>();
        int selectedPosition = 0;
        for (int i = 0; i < profiles.size(); i++) {
            ControlsProfile profile = profiles.get(i);
            if (inputControlsView.getProfile() != null && profile.id == inputControlsView.getProfile().id)
                selectedPosition = i + 1;
            profileNames.add(profile.getName());
        }

        XServerDialogState ds = XServerDialogState.INSTANCE;
        ds.setInputProfiles(profileNames);
        ds.setSelectedProfileIdx(selectedPosition);
        ds.setShowTouchscreen(inputControlsView.isShowTouchscreenControls());
        ds.setTimeoutEnabled(preferences.getBoolean("touchscreen_timeout_enabled", false));
        ds.setHapticsEnabled(preferences.getBoolean("touchscreen_haptics_enabled", false));

        ds.onInputControlsConfirm = (profileIndex, showTouchscreen, timeout, haptics) -> {
            inputControlsView.setShowTouchscreenControls(showTouchscreen);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("touchscreen_timeout_enabled", timeout);
            editor.putBoolean("touchscreen_haptics_enabled", haptics);
            editor.apply();
            if (timeout) startTouchscreenTimeout();
            else touchpadView.setOnTouchListener(null);
            if (profileIndex > 0) showInputControls(inputControlsManager.getProfiles().get(profileIndex - 1));
            else hideInputControls();
        };

        ds.onInputControlsSettings = () -> {
            int currentIdx = ds.getSelectedProfileIdx().getValue();
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("edit_input_controls", true);
            intent.putExtra("selected_profile_id",
                currentIdx > 0 ? inputControlsManager.getProfiles().get(currentIdx - 1).id : 0);
            editInputControlsCallback = () -> {
                hideInputControls();
                inputControlsManager.loadProfiles(true);
            };
            controlsEditorActivityResultLauncher.launch(intent);
        };

        ds.show(XServerDialogState.ActiveDialog.INPUT_CONTROLS);
    }

    private void simulateConfirmInputControlsDialog() {
        // Simulate setting the relative mouse movement and touchscreen controls from preferences

        boolean isShowTouchscreenControls = preferences.getBoolean("show_touchscreen_controls_enabled", false); // default is false (hidden)
        inputControlsView.setShowTouchscreenControls(isShowTouchscreenControls);

        boolean isTimeoutEnabled = preferences.getBoolean("touchscreen_timeout_enabled", false);
        boolean isHapticsEnabled = preferences.getBoolean("touchscreen_haptics_enabled", false);

        // Apply these settings as if the user confirmed the dialog
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("touchscreen_timeout_enabled", isTimeoutEnabled);
        editor.putBoolean("touchscreen_haptics_enabled", isHapticsEnabled);
        editor.apply();

        // If no profile is selected, hide the controls
        int selectedProfileIndex = preferences.getInt("selected_profile_index", -1); // Default to -1 for no profile

        if (selectedProfileIndex >= 0 && selectedProfileIndex < inputControlsManager.getProfiles().size()) {
            // A profile is selected, show the controls
            ControlsProfile profile = inputControlsManager.getProfiles().get(selectedProfileIndex);
            showInputControls(profile);
        } else {
            // No profile selected, ensure the controls are hidden
            hideInputControls();
        }

        // Timeout logic should only apply if the controls are visible
        if (isTimeoutEnabled && inputControlsView.getVisibility() == View.VISIBLE) {
            startTouchscreenTimeout(); // Start timeout if enabled and controls are visible
        } else {
            touchpadView.setOnTouchListener(null); // Disable the timeout listener if not needed
        }

        Log.d("XServerDisplayActivity", "Input controls simulated confirmation executed.");
    }

    private void startTouchscreenTimeout() {
        boolean isTimeoutEnabled = preferences.getBoolean("touchscreen_timeout_enabled", false);

        if (isTimeoutEnabled) {
            // Show controls initially and set up touch event listeners
            inputControlsView.setVisibility(View.VISIBLE);
            Log.d("XServerDisplayActivity", "Timeout is enabled, setting up timeout logic.");

            // Attach the OnTouchListener to reset the timeout on touch events
            touchpadView.setOnTouchListener((v, event) -> {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
                    // Reset the timeout on any touch event
                    //Log.d("XServerDisplayActivity", "Touch detected, resetting timeout.");

                    // Keep the controls visible
                    inputControlsView.setVisibility(View.VISIBLE);

                    // Remove any pending hide callbacks and reset the timeout
                    timeoutHandler.removeCallbacks(hideControlsRunnable);
                    timeoutHandler.postDelayed(hideControlsRunnable, 5000); // Reset timeout
                }

                return false; // Allow the touch event to propagate
            });

            // Reset the timeout when the controls are initially displayed
            timeoutHandler.removeCallbacks(hideControlsRunnable);
            timeoutHandler.postDelayed(hideControlsRunnable, 5000); // Hide after 5 seconds of inactivity
        } else {
            // If timeout is disabled, keep the controls always visible
            Log.d("XServerDisplayActivity", "Timeout is disabled, controls will stay visible.");

            inputControlsView.setVisibility(View.VISIBLE); // Ensure controls are visible
            timeoutHandler.removeCallbacks(hideControlsRunnable); // Remove any existing hide callbacks
            touchpadView.setOnTouchListener(null); // Remove the touch listener
        }
    }

    private void showInputControls(ControlsProfile profile) {
        inputControlsView.setVisibility(View.VISIBLE);
        inputControlsView.requestFocus();
        inputControlsView.setProfile(profile);

        touchpadView.setSensitivity(profile.getCursorSpeed() * globalCursorSpeed);
        touchpadView.setPointerButtonRightEnabled(false);

        inputControlsView.invalidate();
        winHandler.sendGamepadState();
    }

    private void hideInputControls() {
        inputControlsView.setShowTouchscreenControls(true);
        inputControlsView.setVisibility(View.GONE);
        inputControlsView.setProfile(null);

        touchpadView.setSensitivity(globalCursorSpeed);
        touchpadView.setPointerButtonLeftEnabled(true);
        touchpadView.setPointerButtonRightEnabled(true);

        inputControlsView.invalidate();
        winHandler.sendGamepadState();
    }

    private void extractGraphicsDriverFiles() {
    // 1. Retrieve the selected driver name from the config
    String selectedDriver = graphicsDriverConfig.get("graphicsDriver");
    if (selectedDriver == null) selectedDriver = graphicsDriverConfig.get("graphics_driver");
    
    String adrenoToolsDriverId = graphicsDriverConfig.get("version");

    Log.d("GraphicsDriverExtraction", "Selected Driver from Config: " + selectedDriver);
    Log.d("GraphicsDriverExtraction", "Adrenotools DriverID: " + adrenoToolsDriverId);

    File rootDir = imageFs.getRootDir();

    // Perform wrapper extraction based on selected version
    if (graphicsDriver.startsWith("wrapper-original")) {
        Log.d("GraphicsDriverExtraction", "Extracting: graphics_driver/wrapper-original.tzst");
        TarCompressorUtils.extract(TarCompressorUtils.Type.ZSTD, this, "graphics_driver/wrapper-original.tzst", rootDir);
    } 
    else if (graphicsDriver.startsWith("wrapper-leegao")) {
        Log.d("GraphicsDriverExtraction", "Extracting: graphics_driver/wrapper-leegao.tzst");
        TarCompressorUtils.extract(TarCompressorUtils.Type.ZSTD, this, "graphics_driver/wrapper-leegao.tzst", rootDir);
    } 
    else if (graphicsDriver.startsWith("wrapper-legacy")) {
        Log.d("GraphicsDriverExtraction", "Extracting: graphics_driver/wrapper-legacy.tzst");
        TarCompressorUtils.extract(TarCompressorUtils.Type.ZSTD, this, "graphics_driver/wrapper-legacy.tzst", rootDir);
    }
    else if (graphicsDriver.startsWith("wrapper-gamenative")) {
        Log.d("GraphicsDriverExtraction", "Extracting: graphics_driver/wrapper-gamenative.tzst");
        TarCompressorUtils.extract(TarCompressorUtils.Type.ZSTD, this, "graphics_driver/wrapper-gamenative.tzst", rootDir);
    }

    // Original logic for DXWrapper and environment variables
    if (dxwrapper.contains("dxvk")) {
        DXVKConfigDialog.setEnvVars(this, dxwrapperConfig, envVars);
        String version = dxwrapperConfig.get("version");
        if (version != null && version.equals("1.11.1-sarek")) {
            Log.d("GraphicsDriverExtraction", "Disabling Wrapper PATCH_OPCONSTCOMP SPIR-V pass");
            envVars.put("WRAPPER_NO_PATCH_OPCONSTCOMP", "1");
        }
    }
    else {
        WineD3DConfigDialog.setEnvVars(this, dxwrapperConfig, envVars);
    }

    boolean useDRI3 = preferences.getBoolean("use_dri3", true);
    if (!useDRI3) {
        envVars.put("MESA_VK_WSI_DEBUG", "sw");
    }

    envVars.put("VK_ICD_FILENAMES", imageFs.getShareDir() + "/vulkan/icd.d/wrapper_icd.aarch64.json");
    envVars.put("GALLIUM_DRIVER", "zink");

    // 2. SHARED LIBS EXTRACTION (First Time Boot Only)
    if (firstTimeBoot) {
        Log.d("XServerDisplayActivity", "First time container boot, re-extracting layers and extra_libs");
        TarCompressorUtils.extract(TarCompressorUtils.Type.ZSTD, this, "layers" + ".tzst", rootDir);
        TarCompressorUtils.extract(TarCompressorUtils.Type.ZSTD, this, "graphics_driver/extra_libs.tzst", rootDir);
    }

    // 3. Adrenotools Integration
    if (adrenoToolsDriverId != null && !adrenoToolsDriverId.equals("System")) {
        AdrenotoolsManager adrenotoolsManager = new AdrenotoolsManager(this);
        adrenotoolsManager.setDriverById(envVars, imageFs, adrenoToolsDriverId);
    }

    // --- Environment Variable Setup ---
    String vulkanVersion = graphicsDriverConfig.get("vulkanVersion");
    String vulkanVersionPatch = GPUInformation.getVulkanVersion(adrenoToolsDriverId, this).split("\\.")[2];
    vulkanVersion = (vulkanVersion != null ? vulkanVersion : "1.3") + "." + vulkanVersionPatch;
    envVars.put("WRAPPER_VK_VERSION", vulkanVersion);

    String blacklistedExtensions = graphicsDriverConfig.get("blacklistedExtensions");
    envVars.put("WRAPPER_EXTENSION_BLACKLIST", blacklistedExtensions != null ? blacklistedExtensions : "");

    String gpuName = graphicsDriverConfig.get("gpuName");
    String dxvkVersion = dxwrapperConfig.get("version");
    if (gpuName != null && !gpuName.equals("Device") && dxvkVersion != null && !dxvkVersion.equals("1.11.1-sarek")) {
        envVars.put("WRAPPER_DEVICE_NAME", gpuName);
        envVars.put("WRAPPER_DEVICE_ID", WineD3DConfigDialog.getDeviceIdFromGPUName(this, gpuName));
        envVars.put("WRAPPER_VENDOR_ID", WineD3DConfigDialog.getVendorIdFromGPUName(this, gpuName));
    }

    String maxDeviceMemory = graphicsDriverConfig.get("maxDeviceMemory");
    if (maxDeviceMemory != null && Integer.parseInt(maxDeviceMemory) > 0)
        envVars.put("WRAPPER_VMEM_MAX_SIZE", maxDeviceMemory);
    
    String presentMode = graphicsDriverConfig.get("presentMode");
    if (presentMode != null) {
        if (presentMode.contains("immediate")) {
            envVars.put("WRAPPER_MAX_IMAGE_COUNT", "1");
        }
        envVars.put("MESA_VK_WSI_PRESENT_MODE", presentMode);
    }

    String resourceType = graphicsDriverConfig.get("resourceType");
    if (resourceType != null) envVars.put("WRAPPER_RESOURCE_TYPE", resourceType);

    String syncFrame = graphicsDriverConfig.get("syncFrame");
    if (syncFrame != null && syncFrame.equals("1"))
        envVars.put("MESA_VK_WSI_DEBUG", "forcesync");

    String disablePresentWait = graphicsDriverConfig.get("disablePresentWait");
    if (disablePresentWait != null) envVars.put("WRAPPER_DISABLE_PRESENT_WAIT", disablePresentWait);

    String bcnEmulation = graphicsDriverConfig.get("bcnEmulation");
    String bcnEmulationType = graphicsDriverConfig.get("bcnEmulationType");

    if (bcnEmulation != null) {
        switch (bcnEmulation) {
            case "auto" -> {
                if ("compute".equals(bcnEmulationType) && GPUInformation.getVendorID(null, null) != 0x5143) {
                    envVars.put("ENABLE_BCN_COMPUTE", "1");
                    envVars.put("BCN_COMPUTE_AUTO", "1");
                }
                envVars.put("WRAPPER_EMULATE_BCN", "3");
            }
            case "full" -> {
                if ("compute".equals(bcnEmulationType) && GPUInformation.getVendorID(null, null) != 0x5143) {
                    envVars.put("ENABLE_BCN_COMPUTE", "1");
                    envVars.put("BCN_COMPUTE_AUTO", "0");
                }
                envVars.put("WRAPPER_EMULATE_BCN", "2");
            }
            case "none" -> envVars.put("WRAPPER_EMULATE_BCN", "0");
            default -> envVars.put("WRAPPER_EMULATE_BCN", "1");
        }
    }

    String bcnEmulationCache = graphicsDriverConfig.get("bcnEmulationCache");
    if (bcnEmulationCache != null) envVars.put("WRAPPER_USE_BCN_CACHE", bcnEmulationCache);

    if (vkbasaltConfig != null && !vkbasaltConfig.isEmpty()) {
        envVars.put("ENABLE_VKBASALT", "1");
        envVars.put("VKBASALT_CONFIG", vkbasaltConfig);
    }
}
    
    
    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        boolean handledByWinHandler = false;
        boolean handledByTouchpadView = false;

        // Let winHandler process the event if available
        if (winHandler != null) {
            handledByWinHandler = winHandler.onGenericMotionEvent(event);
            if (handledByWinHandler) {
                //Log.d("XServerDisplayActivity", "Event handled by winHandler");
            }
        }

        // Let touchpadView process the event if available
        if (touchpadView != null) {
            handledByTouchpadView = touchpadView.onExternalMouseEvent(event);
            if (handledByTouchpadView) {
                //Log.d("XServerDisplayActivity", "Event handled by touchpadView");
            }
        }

        // Pass the event to the super method to ensure system-level handling
        boolean handledBySuper = super.dispatchGenericMotionEvent(event);
        if (!handledBySuper) {
            //Log.d("XServerDisplayActivity", "Event not handled by super");
        }

        // Combine the results: any handler consuming the event indicates it was handled
        return handledByWinHandler || handledByTouchpadView || handledBySuper;
    }


    private static final int RECAPTURE_DELAY_MS = 10000; // 10 seconds

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        // Handle the PlayStation or Xbox Home button to open the drawer
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_MODE || event.getKeyCode() == KeyEvent.KEYCODE_HOME || event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_SELECT) {
                boolean handled = inputControlsView.onKeyEvent(event) || (winHandler != null && winHandler.onKeyEvent(event)) && (xServer != null && xServer.keyboard.onKeyEvent(event));
                return true;
            }
        }

        // Fallback to existing input handling
        return (!inputControlsView.onKeyEvent(event) && !winHandler.onKeyEvent(event) && xServer.keyboard.onKeyEvent(event)) ||
                (!ExternalController.isGameController(event.getDevice()) && super.dispatchKeyEvent(event));
    }

    public InputControlsView getInputControlsView() {
        return inputControlsView;
    }

    private static final String TAG = "DXWrapperExtraction";

    private boolean extractDXWrapperFiles(String dxwrapper) {
        final String[] dlls = {"d3d10.dll", "d3d10_1.dll", "d3d10core.dll", "d3d11.dll", "d3d12.dll", "d3d12core.dll", "d3d8.dll", "d3d9.dll", "dxgi.dll", "ddraw.dll", "d3dimm.dll"};

        File rootDir = imageFs.getRootDir();
        File windowsDir = new File(rootDir, ImageFs.WINEPREFIX + "/drive_c/windows");

        if (dxwrapper.contains("dxvk")) {
            Log.d(TAG, "Extracting DXVK wrapper files, version: " + dxwrapper);

            String dxvkWrapper = dxwrapper.split(";")[0];
            String vkd3dWrapper = dxwrapper.split(";")[1];
            String ddrawrapper = dxwrapper.split(";")[2];
            
            ContentProfile dxvkProfile = contentsManager.getProfileByEntryName(dxvkWrapper);
            if (dxvkProfile != null) {
                Log.d(TAG, "Applying user-defined DXVK content profile: " + dxvkWrapper);
                contentsManager.applyContent(dxvkProfile);
            } else {
                Log.d(TAG, "Extracting fallback DXVK .tzst archive: " + dxvkWrapper);
                TarCompressorUtils.extract(TarCompressorUtils.Type.ZSTD, this, "dxwrapper/" + dxvkWrapper + ".tzst", windowsDir, onExtractFileListener);

                if (compareVersion(dxvkWrapper, "2.4") < 0) {
                    Log.d(TAG, "Extracting d8vk as part of DXVK version " + dxvkWrapper);
                    TarCompressorUtils.extract(TarCompressorUtils.Type.ZSTD, this, "dxwrapper/d8vk-" + DefaultVersion.D8VK + ".tzst", windowsDir, onExtractFileListener);
                }
            }

            boolean vkd3dOk;
            if (vkd3dWrapper.contains("None")) {
                Log.d(TAG, "No VKD3D has been selected, restoring original d3d12");
                restoreOriginalDllFiles(new String[]{"d3d12.dll", "d3d12core.dll"});
                vkd3dOk = true;
            } else {
                ContentProfile vkd3dProfile = contentsManager.getProfileByEntryName(vkd3dWrapper);
                if (vkd3dProfile != null) {
                    Log.d(TAG, "Applying user-defined VKD3D content profile: " + vkd3dWrapper);
                    contentsManager.applyContent(vkd3dProfile);
                    vkd3dOk = true;
                } else {
                    Log.d(TAG, "Extracting fallback VKD3D .tzst archive: " + vkd3dWrapper);
                    vkd3dOk = TarCompressorUtils.extract(TarCompressorUtils.Type.ZSTD, this, "dxwrapper/" + vkd3dWrapper + ".tzst", windowsDir, onExtractFileListener);
                    if (!vkd3dOk) Log.e(TAG, "VKD3D extraction failed: " + vkd3dWrapper);
                }
            }
            if (!vkd3dOk) return false;

            Log.d(TAG, "Extracting nglide wrapper");
TarCompressorUtils.extract(TarCompressorUtils.Type.ZSTD, this, "ddrawrapper/nglide.tzst", windowsDir, onExtractFileListener);

if (ddrawrapper.contains("None")) {
    Log.d(TAG, "No DDRaw wrapper has been selected, restoring original ddraw files");
    restoreOriginalDllFiles(new String[]{ "ddraw.dll", "d3dimm.dll" });
}
else {
    if (ddrawrapper.equals("cnc-ddraw")) {
        envVars.put("CNC_DDRAW_CONFIG_FILE", "C:\\windows\\syswow64\\ddraw.ini");
    }
    // Fixed: Ensure no hidden characters (\u200b) exist before 'else if'
    else if (ddrawrapper.equals("dgvoodoo")) {
        Log.d(TAG, "Applying dgvoodoo ddrawrapper");
        TarCompressorUtils.extract(TarCompressorUtils.Type.ZSTD, this, "ddrawrapper/dgvoodoo.tzst", windowsDir, onExtractFileListener);
    }

    Log.d(TAG, "Extracting ddrawrapper " + ddrawrapper);
    // Only extract if it wasn't already handled specifically above
    if (!ddrawrapper.equals("dgvoodoo")) {
        TarCompressorUtils.extract(TarCompressorUtils.Type.ZSTD, this, "ddrawrapper/" + ddrawrapper + ".tzst", windowsDir, onExtractFileListener);
    }
}

Log.d(TAG, "Finished extraction of DXVK wrapper files, version: " + dxwrapper);
return true;
} else if (dxwrapper.contains("wined3d")) {
    Log.d(TAG, "Restoring original DLL files for wined3d.");
    restoreOriginalDllFiles(dlls);
        }
        return true;
    }

    private static int compareVersion(String varA, String varB) {
        int[] a = parseSemverLoose(varA);
        int[] b = parseSemverLoose(varB);

        if (a[0] != b[0]) return a[0] - b[0];
        if (a[1] != b[1]) return a[1] - b[1];
        return a[2] - b[2];
    }

    private static final Pattern SEMVER_LOOSE =
            Pattern.compile("(\\d+)\\.(\\d+)(?:\\.(\\d+))?");

    private static int[] parseSemverLoose(String s) {
        if (s == null) return new int[]{0, 0, 0};

        Matcher m = SEMVER_LOOSE.matcher(s);

        String g1 = null, g2 = null, g3 = null;
        while (m.find()) {
            g1 = m.group(1);
            g2 = m.group(2);
            g3 = m.group(3);
        }

        if (g1 == null || g2 == null) {
            return new int[]{0, 0, 0};
        }

        int major = safeParseInt(g1);
        int minor = safeParseInt(g2);
        int patch = safeParseInt(g3);
        return new int[]{major, minor, patch};
    }

    private static int safeParseInt(String s) {
        if (s == null || s.isEmpty()) return 0;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }
    
    private void extractWinComponentFiles() {
        Log.d("XServerDisplayActivity", "Extracting WinComponents");
        File rootDir = imageFs.getRootDir();
        File windowsDir = new File(rootDir, ImageFs.WINEPREFIX+"/drive_c/windows");
        File systemRegFile = new File(rootDir, ImageFs.WINEPREFIX+"/system.reg");

        try {
            JSONObject wincomponentsJSONObject = new JSONObject(FileUtils.readString(this, "wincomponents/wincomponents.json"));
            ArrayList<String> dlls = new ArrayList<>();
            String wincomponents = shortcut != null ? shortcut.getExtra("wincomponents", container.getWinComponents()) : container.getWinComponents();

            Iterator<String[]> oldWinComponentsIter = new KeyValueSet(container.getExtra("wincomponents", Container.FALLBACK_WINCOMPONENTS)).iterator();

            for (String[] wincomponent : new KeyValueSet(wincomponents)) {
                if (wincomponent[1].equals(oldWinComponentsIter.next()[1]) && !firstTimeBoot) continue;
                String identifier = wincomponent[0];
                boolean useNative = wincomponent[1].equals("1");

                if (useNative) {
                    TarCompressorUtils.extract(TarCompressorUtils.Type.ZSTD, this, "wincomponents/"+identifier+".tzst", windowsDir, onExtractFileListener);
                }
                else {
                    JSONArray dlnames = wincomponentsJSONObject.getJSONArray(identifier);
                    for (int i = 0; i < dlnames.length(); i++) {
                        String dlname = dlnames.getString(i);
                        dlls.add(!dlname.endsWith(".exe") ? dlname+".dll" : dlname);
                    }
                }
                Log.d("XServerDisplayActivity", "Setting wincomponent " + identifier + " to " + String.valueOf(useNative));
                WineUtils.overrideWinComponentDlls(this, container, identifier, useNative);
                WineUtils.setWinComponentRegistryKeys(systemRegFile, identifier, useNative, this);
            }

            if (!dlls.isEmpty()) restoreOriginalDllFiles(dlls.toArray(new String[0]));
        }
        catch (JSONException e) {}
    }

    private void restoreOriginalDllFiles(final String... dlls) {
        File rootDir = imageFs.getRootDir();
        File windowsDir = new File(rootDir, ImageFs.WINEPREFIX+"/drive_c/windows");
        File system32dlls = null;
        File syswow64dlls = null;

        if (wineInfo.isArm64EC())
            system32dlls = new File(imageFs.getWinePath() + "/lib/wine/aarch64-windows");
        else
            system32dlls = new File(imageFs.getWinePath() + "/lib/wine/x86_64-windows");

        syswow64dlls = new File(imageFs.getWinePath() + "/lib/wine/i386-windows");


        for (String dll : dlls) {
            File srcFile = new File(system32dlls, dll);
            File dstFile = new File(windowsDir, "system32/" + dll);
            FileUtils.copy(srcFile, dstFile);
            srcFile = new File(syswow64dlls, dll);
            dstFile = new File(windowsDir, "syswow64/" + dll);
            FileUtils.copy(srcFile, dstFile);
        }
   }

    private String getWineStartCommand() {
        // Initialize overrideEnvVars if not already done
        EnvVars envVars = getOverrideEnvVars();

        // Define default arguments
        String args = "";

        if (shortcut != null) {
            String execArgs = shortcut.getExtra("execArgs");
            execArgs = !execArgs.isEmpty() ? " " + execArgs : "";

            if (shortcut.path.endsWith(".lnk")) {
                args += "\"" + shortcut.path + "\"" + execArgs;
            } else {
                String exeDir = FileUtils.getDirname(shortcut.path);
                String filename = FileUtils.getName(shortcut.path);

                int dotIndex = filename.lastIndexOf(".");
                int spaceIndex = (dotIndex != -1) ? filename.indexOf(" ", dotIndex) : -1;

                if (spaceIndex != -1) {
                    execArgs = filename.substring(spaceIndex + 1) + execArgs;
                    filename = filename.substring(0, spaceIndex);
                }

                args += "/dir " + StringUtils.escapeDOSPath(exeDir) + " \"" + filename + "\"" + execArgs;
            }
        } else {
            // Append EXTRA_EXEC_ARGS from overrideEnvVars if it exists
            if (envVars.has("EXTRA_EXEC_ARGS")) {
                args += " " + envVars.get("EXTRA_EXEC_ARGS");
                envVars.remove("EXTRA_EXEC_ARGS"); // Remove the key after use
            } else {
                args += "\"wfm.exe\"";
            }
        }
        // Construct the final command
        String command = "winhandler.exe " + args;

        return command;
    }

    private String getExecutable() {
        String filename = "";
        if (shortcut != null) {
            filename = FileUtils.getName(shortcut.path);
        }
        else
            filename = "wfm.exe";
        return filename;
    }


    public XServer getXServer() {
        return xServer;
    }

    public WinHandler getWinHandler() {
        return winHandler;
    }

    public XServerView getXServerView() {
        return xServerView;
    }

    public Container getContainer() {
        return container;
    }

    public void setDXWrapper(String dxwrapper) {
        this.dxwrapper = dxwrapper;
    }

    public EnvVars getOverrideEnvVars() {
        if (overrideEnvVars == null) {
            overrideEnvVars = new EnvVars();
        }
        return overrideEnvVars;
    }

    private void changeWineAudioDriver() {
        if (!audioDriver.equals(container.getExtra("audioDriver"))) {
            File rootDir = imageFs.getRootDir();
            File userRegFile = new File(rootDir, ImageFs.WINEPREFIX+"/user.reg");
            try (WineRegistryEditor registryEditor = new WineRegistryEditor(userRegFile)) {
                if (audioDriver.equals("alsa")) {
                    registryEditor.setStringValue("Software\\Wine\\Drivers", "Audio", "alsa");
                }
                else if (audioDriver.equals("pulseaudio")) {
                    registryEditor.setStringValue("Software\\Wine\\Drivers", "Audio", "pulse");
                }
            }
            container.putExtra("audioDriver", audioDriver);
            container.saveData();
        }
    }

    private void applyGeneralPatches(Container container) {
        File rootDir = imageFs.getRootDir();
        TarCompressorUtils.extract(TarCompressorUtils.Type.ZSTD, this, "container_pattern_common.tzst", rootDir);
        TarCompressorUtils.extract(TarCompressorUtils.Type.ZSTD, this, "pulseaudio.tzst", new File(getFilesDir(), "pulseaudio"));
        WineUtils.applySystemTweaks(this, wineInfo);
        container.putExtra("graphicsDriver", null);
        container.putExtra("desktopTheme", null);
    }

    private void assignTaskAffinity(Window window) {
        if (taskAffinityMask == 0 || taskAffinityMaskWoW64 == 0) return;
        int processId = window.getProcessId();
        String className = window.getClassName();
        int processAffinity = window.isWoW64() ? taskAffinityMaskWoW64 : taskAffinityMask;

        if (processId > 0) {
            winHandler.setProcessAffinity(processId, processAffinity);
        }
        else if (!className.isEmpty()) {
            winHandler.setProcessAffinity(window.getClassName(), processAffinity);
        }
    }

    private void changeFrameRatingVisibility(Window window, Property property) {
        if (frameRating == null && frameRatingHorizontal == null) return;

        if (property != null) {
            if (frameRatingWindowId == -1 && property.nameAsString().contains("_MESA_DRV")) {
                frameRatingWindowId = window.id;
                Log.d("XServerDisplayActivity", "Showing hud for Window " + window.getName());

                runOnUiThread(() -> {
                    if (frameRating != null) frameRating.setVisibility(View.VISIBLE);
                    if (frameRatingHorizontal != null) frameRatingHorizontal.setVisibility(View.VISIBLE);
                });

                if (frameRating != null) frameRating.update();
                if (frameRatingHorizontal != null) frameRatingHorizontal.update();
            }
            if (property.nameAsString().contains("_MESA_DRV_ENGINE_NAME")) {
                runOnUiThread(() -> {
                    if (frameRating != null) frameRating.setRenderer(property.toString());
                    if (frameRatingHorizontal != null) frameRatingHorizontal.setRenderer(property.toString());
                });
            }
            if (property.nameAsString().contains("_MESA_DRV_GPU_NAME")) {
                runOnUiThread(() -> {
                    if (frameRating != null) frameRating.setGpuName(property.toString());
                });
            }
        }
        else if (frameRatingWindowId != -1) {
            frameRatingWindowId = -1;
            Log.d("XServerDisplayActivity", "Hiding hud for Window " + window.getName());
            runOnUiThread(() -> {
                if (frameRating != null) {
                    frameRating.setVisibility(View.GONE);
                    frameRating.reset();
                }
                if (frameRatingHorizontal != null) {
                    frameRatingHorizontal.setVisibility(View.GONE);
                    frameRatingHorizontal.reset();
                }
            });
        }
    }


    public String getScreenEffectProfile() {
        return screenEffectProfile;
    }

    public void setScreenEffectProfile(String screenEffectProfile) {
        this.screenEffectProfile = screenEffectProfile;
    }

    private void MoveCursorToTouchpoint() {
        // Toggle the preference value
        boolean currentValue = preferences.getBoolean("move_cursor_to_touchpoint", false);
        boolean newValue = !currentValue;
        
        preferences.edit().putBoolean("move_cursor_to_touchpoint", newValue).apply();
        
        // Update the touchpadView state
        if (touchpadView != null) {
            touchpadView.setMoveCursorToTouchpoint(newValue);
        }
    } // Closes MoveCursorToTouchpoint

    private void showActiveWindowsDialog() {
        ArrayList<com.winlator.star.xserver.Window> activeWindows = new ArrayList<>();
        ArrayList<android.graphics.Bitmap> activeIcons = new ArrayList<>();
        try {
            try (XLock lock = xServer.lock(XServer.Lockable.WINDOW_MANAGER, XServer.Lockable.DRAWABLE_MANAGER)) {
                findAppWindowsForCompose(xServer.windowManager.rootWindow, activeWindows);
                for (com.winlator.star.xserver.Window w : activeWindows) {
                    activeIcons.add(xServer.pixmapManager.getWindowIcon(w));
                }
            }
        } catch (Exception e) {
            Log.e("XServerDisplayActivity", "Error reading windows", e);
        }

        ArrayList<XServerDialogState.ActiveWindow> windowInfoList = new ArrayList<>();
        for (int i = 0; i < activeWindows.size(); i++) {
            com.winlator.star.xserver.Window w = activeWindows.get(i);
            String title = w.getName();
            String cls   = w.getClassName() != null ? w.getClassName() : "";
            if (title == null || title.isEmpty()) title = cls;
            if (title.isEmpty()) title = "Unnamed Window";
            windowInfoList.add(new XServerDialogState.ActiveWindow(
                title, cls, activeIcons.get(i), null, w.getHandle()));
        }

        XServerDialogState ds = XServerDialogState.INSTANCE;
        ds.setAwWindows(windowInfoList);
        ds.onWindowClick = (cls, handle) -> {
            WinHandler wh = getWinHandler();
            if (wh != null) wh.bringToFront(cls, handle);
        };
        ds.show(XServerDialogState.ActiveDialog.ACTIVE_WINDOWS);

        GLRenderer renderer = xServerView != null ? xServerView.getRenderer() : null;
        if (renderer != null) {
            float density = getResources().getDisplayMetrics().density;
            int previewW = (int)(240 * density);
            int previewH = (int)(160 * density);
            for (int i = 0; i < activeWindows.size(); i++) {
                final int idx = i;
                final com.winlator.star.xserver.Window win = activeWindows.get(i);
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() ->
                    renderer.captureScreenshot(win, previewW, previewH, bitmap -> {
                        if (bitmap != null) runOnUiThread(() -> ds.updateAwScreenshot(idx, bitmap));
                    }), idx * 100L);
            }
        }
    }

    private void findAppWindowsForCompose(com.winlator.star.xserver.Window parent,
                                          ArrayList<com.winlator.star.xserver.Window> result) {
        if (parent == null) return;
        for (com.winlator.star.xserver.Window child : parent.getChildren()) {
            if (child.attributes.isMapped()) {
                String className = child.getClassName();
                boolean isSystem = false;
                if (className != null) {
                    String cls = className.toLowerCase();
                    if (cls.contains("progman") || cls.contains("shell_traywnd") || cls.equals("explorer.exe"))
                        isSystem = true;
                }
                String title  = child.getName();
                boolean hasTitle = title != null && !title.isEmpty();
                boolean hasClass = className != null && !className.isEmpty();
                if (!isSystem && (hasTitle || hasClass)) {
                    if (child.getWidth() < xServer.screenInfo.width || child.getHeight() < xServer.screenInfo.height
                            || child.getParent() != xServer.windowManager.rootWindow
                            || (title != null && !title.isEmpty()
                                && !title.equalsIgnoreCase("Default - Wine desktop"))) {
                        result.add(child);
                        continue;
                    }
                }
            }
            findAppWindowsForCompose(child, result);
        }
    }

    private void showScreenEffectsDialog() {
        GLRenderer r = xServerView != null ? xServerView.getRenderer() : null;
        XServerDialogState ds = XServerDialogState.INSTANCE;

        ColorEffect ce   = r != null ? (ColorEffect)        r.getEffectComposer().getEffect(ColorEffect.class)        : null;
        FXAAEffect  fxaa = r != null ? (FXAAEffect)         r.getEffectComposer().getEffect(FXAAEffect.class)         : null;
        CRTEffect   crt  = r != null ? (CRTEffect)          r.getEffectComposer().getEffect(CRTEffect.class)          : null;
        ToonEffect  toon = r != null ? (ToonEffect)         r.getEffectComposer().getEffect(ToonEffect.class)         : null;
        NTSCCombinedEffect ntsc = r != null ? (NTSCCombinedEffect) r.getEffectComposer().getEffect(NTSCCombinedEffect.class) : null;

        ds.setSeBrightness(ce   != null ? ce.getBrightness() * 100f : 0f);
        ds.setSeContrast  (ce   != null ? ce.getContrast()   * 100f : 0f);
        ds.setSeGamma     (ce   != null ? ce.getGamma()             : 1.0f);
        ds.setSeFxaa      (fxaa != null);
        ds.setSeCrt       (crt  != null);
        ds.setSeToon      (toon != null);
        ds.setSeNtsc      (ntsc != null);

        java.util.Set<String> rawSet = new java.util.LinkedHashSet<>(
            preferences.getStringSet("screen_effect_profiles", new java.util.LinkedHashSet<>()));
        final ArrayList<String> profileNames = new ArrayList<>();
        for (String p : rawSet) profileNames.add(p.split(":")[0]);
        ds.setSeProfiles(profileNames);

        String currentProfile = getScreenEffectProfile();
        int selIdx = 0;
        for (int i = 0; i < profileNames.size(); i++) {
            if (profileNames.get(i).equals(currentProfile)) { selIdx = i + 1; break; }
        }
        ds.setSeSelectedProfile(selIdx);

        ds.onScreenEffectsApply = (brightness, contrast, gamma, fxaaEn, crtEn, toonEn, ntscEn, profileIndex) -> {
            if (r == null) return;
            applyScreenEffects(r, brightness, contrast, gamma, fxaaEn, crtEn, toonEn, ntscEn);
            if (profileIndex > 0 && profileIndex - 1 < profileNames.size()) {
                String name = profileNames.get(profileIndex - 1);
                saveScreenEffectProfile(name, brightness, contrast, gamma, fxaaEn, crtEn, toonEn, ntscEn);
                setScreenEffectProfile(name);
            }
        };

        ds.onSeAddProfile = name -> {
            java.util.Set<String> profiles = new java.util.LinkedHashSet<>(
                preferences.getStringSet("screen_effect_profiles", new java.util.LinkedHashSet<>()));
            boolean exists = false;
            for (String p : profiles) { if (p.split(":")[0].equals(name)) { exists = true; break; } }
            if (!exists) {
                profiles.add(name + ":");
                preferences.edit().putStringSet("screen_effect_profiles", profiles).apply();
                profileNames.add(name);
                ds.setSeProfiles(new ArrayList<>(profileNames));
            }
        };

        ds.onSeRemoveProfile = name -> {
            java.util.Set<String> profiles = new java.util.LinkedHashSet<>(
                preferences.getStringSet("screen_effect_profiles", new java.util.LinkedHashSet<>()));
            profiles.removeIf(p -> p.split(":")[0].equals(name));
            preferences.edit().putStringSet("screen_effect_profiles", profiles).apply();
            profileNames.removeIf(n -> n.equals(name));
            ds.setSeProfiles(new ArrayList<>(profileNames));
            ds.setSeSelectedProfile(0);
        };

        ds.show(XServerDialogState.ActiveDialog.SCREEN_EFFECTS);
    }

    private void applyScreenEffects(GLRenderer r, float brightness, float contrast, float gamma,
                                    boolean fxaaEn, boolean crtEn, boolean toonEn, boolean ntscEn) {
        ColorEffect ce = (ColorEffect) r.getEffectComposer().getEffect(ColorEffect.class);
        if (brightness == 0 && contrast == 0 && gamma == 1.0f) {
            if (ce != null) r.getEffectComposer().removeEffect(ce);
        } else {
            if (ce == null) ce = new ColorEffect();
            ce.setBrightness(brightness / 100f);
            ce.setContrast(contrast / 100f);
            ce.setGamma(gamma);
            r.getEffectComposer().addEffect(ce);
        }
        FXAAEffect fxaa = (FXAAEffect) r.getEffectComposer().getEffect(FXAAEffect.class);
        if (fxaaEn) { if (fxaa == null) r.getEffectComposer().addEffect(new FXAAEffect()); }
        else if (fxaa != null) r.getEffectComposer().removeEffect(fxaa);

        CRTEffect crt = (CRTEffect) r.getEffectComposer().getEffect(CRTEffect.class);
        if (crtEn) { if (crt == null) r.getEffectComposer().addEffect(new CRTEffect()); }
        else if (crt != null) r.getEffectComposer().removeEffect(crt);

        ToonEffect toon = (ToonEffect) r.getEffectComposer().getEffect(ToonEffect.class);
        if (toonEn) { if (toon == null) r.getEffectComposer().addEffect(new ToonEffect()); }
        else if (toon != null) r.getEffectComposer().removeEffect(toon);

        NTSCCombinedEffect ntsc = (NTSCCombinedEffect) r.getEffectComposer().getEffect(NTSCCombinedEffect.class);
        if (ntscEn) { if (ntsc == null) r.getEffectComposer().addEffect(new NTSCCombinedEffect()); }
        else if (ntsc != null) r.getEffectComposer().removeEffect(ntsc);
    }

    private void saveScreenEffectProfile(String name, float brightness, float contrast, float gamma,
                                         boolean fxaa, boolean crt, boolean toon, boolean ntsc) {
        com.winlator.star.core.KeyValueSet settings = new com.winlator.star.core.KeyValueSet();
        settings.put("brightness",  brightness);
        settings.put("contrast",    contrast);
        settings.put("gamma",       gamma);
        settings.put("fxaa",        fxaa);
        settings.put("crt_shader",  crt);
        settings.put("toon_shader", toon);
        settings.put("ntsc_effect", ntsc);
        java.util.Set<String> oldProfiles = new java.util.LinkedHashSet<>(
            preferences.getStringSet("screen_effect_profiles", new java.util.LinkedHashSet<>()));
        java.util.Set<String> newProfiles = new java.util.LinkedHashSet<>();
        for (String p : oldProfiles) {
            String n = p.split(":")[0];
            newProfiles.add(n.equals(name) ? name + ":" + settings.toString() : p);
        }
        preferences.edit().putStringSet("screen_effect_profiles", newProfiles).apply();
    }

    private void showFsrOverlay() {
        GLRenderer r = xServerView != null ? xServerView.getRenderer() : null;
        XServerDialogState ds = XServerDialogState.INSTANCE;

        FSREffect fsr = r != null ? (FSREffect) r.getEffectComposer().getEffect(FSREffect.class) : null;
        HDREffect hdr = r != null ? (HDREffect) r.getEffectComposer().getEffect(HDREffect.class) : null;

        ds.setFsrEnabled(fsr != null);
        ds.setFsrMode   (fsr != null ? fsr.getMode()  : 0);
        ds.setFsrLevel  (fsr != null ? fsr.getLevel() : 1.0f);
        ds.setHdrEnabled(hdr != null);

        ds.onFsrUpdate = (enabled, mode, level, hdrEn) -> {
            if (r == null) return;
            FSREffect cur = (FSREffect) r.getEffectComposer().getEffect(FSREffect.class);
            if (cur != null) r.getEffectComposer().removeEffect(cur);
            if (enabled) {
                FSREffect newFsr = new FSREffect();
                newFsr.setLevel(level);
                newFsr.setMode(mode);
                r.getEffectComposer().addEffect(newFsr);
            }
            HDREffect curHdr = (HDREffect) r.getEffectComposer().getEffect(HDREffect.class);
            if (curHdr != null) r.getEffectComposer().removeEffect(curHdr);
            if (hdrEn) {
                HDREffect newHdr = new HDREffect();
                newHdr.setStrength(1.0f);
                r.getEffectComposer().addEffect(newHdr);
            }
        };

        ds.setFsrVisible(true);
    }

    private void showMagnifierOverlay() {
        GLRenderer r = xServerView != null ? xServerView.getRenderer() : null;
        XServerDialogState ds = XServerDialogState.INSTANCE;

        ds.setMagnifierZoom(r != null ? r.getMagnifierZoom() : 1.0f);
        ds.onMagnifierZoom = delta -> {
            if (r == null) return;
            float z = Mathf.clamp(r.getMagnifierZoom() + delta, 1.0f, 3.0f);
            r.setMagnifierZoom(z);
            ds.setMagnifierZoom(z);
        };
        ds.onMagnifierHide = () -> ds.setMagnifierVisible(false);
        ds.setMagnifierVisible(true);
    }

    private void showTaskManagerDialog() {
        XServerDialogState ds = XServerDialogState.INSTANCE;

        ds.onTmRefresh = () -> {
            if (winHandler != null) winHandler.listProcesses();
            updateTmCpuMemory(ds);
        };

        ds.onTmDismissed = () -> {
            if (winHandler != null) winHandler.setOnGetProcessInfoListener(null);
            ds.setTmProcesses(new ArrayList<>());
        };

        ds.onTmNewTask = () -> ContentDialog.prompt(this, R.string.new_task, "taskmgr.exe",
            command -> { if (winHandler != null) winHandler.exec(command); });

        ds.onTmBringToFront = name -> {
            if (winHandler != null) winHandler.bringToFront(name);
        };

        ds.onTmKillProcess = name -> ContentDialog.confirm(this, R.string.do_you_want_to_end_this_process,
            () -> { if (winHandler != null) winHandler.killProcess(name); });

        ds.onTmSetAffinity = (pid, mask) -> {
            if (winHandler != null) winHandler.setProcessAffinity(pid, mask);
        };

        if (winHandler != null) {
            winHandler.setOnGetProcessInfoListener(new OnGetProcessInfoListener() {
                private final ArrayList<XServerDialogState.TmProcess> buffer = new ArrayList<>();

                @Override
                public void onGetProcessInfo(int index, int numProcesses, ProcessInfo info) {
                    android.graphics.Bitmap icon = null;
                    try (XLock lock = xServer.lock(XServer.Lockable.WINDOW_MANAGER)) {
                        com.winlator.star.xserver.Window w = xServer.windowManager.findWindowWithProcessId(info.pid);
                        if (w != null) icon = xServer.pixmapManager.getWindowIcon(w);
                    } catch (Exception ignored) {}

                    final android.graphics.Bitmap finalIcon = icon;
                    runOnUiThread(() -> {
                        if (index == 0) buffer.clear();
                        buffer.add(new XServerDialogState.TmProcess(
                            index, info.pid, info.name,
                            info.getFormattedMemoryUsage(), info.wow64Process, finalIcon));
                        if (numProcesses == 0 || index == numProcesses - 1) {
                            ds.setTmProcesses(new ArrayList<>(buffer));
                            ds.setTmCount(numProcesses);
                        }
                    });
                }
            });
        }

        updateTmCpuMemory(ds);
        ds.show(XServerDialogState.ActiveDialog.TASK_MANAGER);
    }

    private void updateTmCpuMemory(XServerDialogState ds) {
        try {
            short[] clocks = CPUStatus.getCurrentClockSpeeds();
            int total = 0; short maxClock = 0;
            ArrayList<String> cores = new ArrayList<>();
            for (int i = 0; i < clocks.length; i++) {
                short max = CPUStatus.getMaxClockSpeed(i);
                cores.add(clocks[i] + "/" + max + " MHz");
                total += clocks[i];
                if (max > maxClock) maxClock = max;
            }
            int avg = clocks.length > 0 ? total / clocks.length : 0;
            int pct = maxClock > 0 ? (int)(((float) avg / maxClock) * 100) : 0;
            ds.setTmCpuCores(cores);
            ds.setTmCpuTitle("CPU (" + pct + "%)");

            android.app.ActivityManager am =
                (android.app.ActivityManager) getSystemService(ACTIVITY_SERVICE);
            android.app.ActivityManager.MemoryInfo mi = new android.app.ActivityManager.MemoryInfo();
            am.getMemoryInfo(mi);
            long used = mi.totalMem - mi.availMem;
            int memPct = (int)(((double) used / mi.totalMem) * 100);
            ds.setTmMemTitle("Memory (" + memPct + "%)");
            ds.setTmMemInfo(StringUtils.formatBytes(used, false) + " / " +
                StringUtils.formatBytes(mi.totalMem));
        } catch (Exception ignored) {}
    }


} // Closes the XServerDisplayActivity class



















































