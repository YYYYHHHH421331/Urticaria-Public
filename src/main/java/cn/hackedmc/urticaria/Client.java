package cn.hackedmc.urticaria;

import by.radioegor146.nativeobfuscator.Native;
import cn.hackedmc.urticaria.anticheat.CheatDetector;
import cn.hackedmc.urticaria.bots.BotManager;
import cn.hackedmc.urticaria.command.CommandManager;
import cn.hackedmc.urticaria.component.ComponentManager;
import cn.hackedmc.urticaria.component.impl.event.EntityKillEventComponent;
import cn.hackedmc.urticaria.component.impl.event.EntityTickComponent;
import cn.hackedmc.urticaria.component.impl.hud.AdaptiveRefreshRateComponent;
import cn.hackedmc.urticaria.component.impl.hud.DragComponent;
import cn.hackedmc.urticaria.component.impl.hypixel.APIKeyComponent;
import cn.hackedmc.urticaria.component.impl.hypixel.InventoryDeSyncComponent;
import cn.hackedmc.urticaria.component.impl.module.teleportaura.TeleportAuraComponent;
import cn.hackedmc.urticaria.component.impl.packetlog.PacketLogComponent;
import cn.hackedmc.urticaria.component.impl.patches.GuiClosePatchComponent;
import cn.hackedmc.urticaria.component.impl.performance.ParticleDistanceComponent;
import cn.hackedmc.urticaria.component.impl.player.*;
import cn.hackedmc.urticaria.component.impl.render.*;
import cn.hackedmc.urticaria.component.impl.render.notificationcomponent.NotificationComponent;
import cn.hackedmc.urticaria.component.impl.viamcp.BlockHitboxFixComponent;
import cn.hackedmc.urticaria.component.impl.viamcp.HitboxFixComponent;
import cn.hackedmc.urticaria.component.impl.viamcp.MinimumMotionFixComponent;
import cn.hackedmc.urticaria.creative.RiseTab;
import cn.hackedmc.urticaria.manager.TargetManager;
import cn.hackedmc.urticaria.module.api.manager.ModuleManager;
import cn.hackedmc.urticaria.newevent.bus.impl.EventBus;
import cn.hackedmc.urticaria.packetlog.api.manager.PacketLogManager;
import cn.hackedmc.urticaria.packetlog.impl.FlyingCheck;
import cn.hackedmc.urticaria.script.ScriptManager;
import cn.hackedmc.urticaria.security.ExploitManager;
import cn.hackedmc.urticaria.ui.click.standard.RiseClickGUI;
import cn.hackedmc.urticaria.ui.menu.impl.alt.AltManagerMenu;
import cn.hackedmc.urticaria.ui.music.MusicMenu;
import cn.hackedmc.urticaria.ui.theme.ThemeManager;
import cn.hackedmc.urticaria.util.file.FileManager;
import cn.hackedmc.urticaria.util.file.FileType;
import cn.hackedmc.urticaria.util.file.alt.AltManager;
import cn.hackedmc.urticaria.util.file.config.ConfigFile;
import cn.hackedmc.urticaria.util.file.config.ConfigManager;
import cn.hackedmc.urticaria.util.file.data.DataManager;
import cn.hackedmc.urticaria.util.file.insult.InsultFile;
import cn.hackedmc.urticaria.util.file.insult.InsultManager;
import cn.hackedmc.urticaria.util.localization.Locale;
import cn.hackedmc.urticaria.util.value.ConstantManager;
import cn.hackedmc.fucker.Fucker;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.viamcp.ViaMCP;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("UnusedDeclaration")
@Getter
@Native
public enum Client {

    /**
     * Simple enum instance for our client as enum instances
     * are immutable and are very easy to create and use.
     */
    INSTANCE;

    public static String NAME = "Urticaria";
    public static String VERSION = "2.1";
    public static final String VERSION_FULL = "2.1"; // Used to give more detailed build info on beta builds
    public static final String VERSION_DATE = "August 1, 2023";

    public static boolean DEVELOPMENT_SWITCH = true;
    public static boolean BETA_SWITCH = true;
    public static boolean FIRST_LAUNCH = true;
    public static Type CLIENT_TYPE;

    public boolean noNotify = true;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Setter
    private Locale locale; // The language of the client

    public EventBus eventBus;
    public ModuleManager moduleManager;
    public ComponentManager componentManager;
    public CommandManager commandManager;
    public ExploitManager securityManager;
    public BotManager botManager;
    public ThemeManager themeManager;
    @Setter
    public ScriptManager scriptManager;
    public DataManager dataManager;
    public CheatDetector cheatDetector;

    public FileManager fileManager;

    public ConfigManager configManager;
    public AltManager altManager;
    public InsultManager insultManager;
    public TargetManager targetManager;
    public ConstantManager constantManager;
    public PacketLogManager packetLogManager;

    private ConfigFile configFile;

    public RiseClickGUI standardClickGUI;
    public AltManagerMenu altManagerMenu;
    public MusicMenu musicMenu;

    private RiseTab creativeTab;

    /**
     * The main method when the Minecraft#startGame method is about
     * finish executing our client gets called and that's where we
     * can start loading our own classes and modules.
     */
    public void initClient() {
        // Crack Protection
//        if (!this.validated && !DEVELOPMENT_SWITCH) {
//            return;
//        }

        // Init
        Minecraft mc = Minecraft.getMinecraft();

        // Compatibility
        mc.gameSettings.guiScale = 2;
        mc.gameSettings.ofFastRender = false;
        mc.gameSettings.ofShowGlErrors = DEVELOPMENT_SWITCH;

        // Performance
        mc.gameSettings.ofSmartAnimations = true;
        mc.gameSettings.ofSmoothFps = false;
        mc.gameSettings.ofFastMath = false;

        // Register
//        String[] paths = {
//                "cn.hackedmc.apotheosis.",
//                "hackclient."
//        };
//
//        for (String path : paths) {
//            if (!ReflectionUtil.dirExist(path)) {
//                continue;
//            }
//
//            Class<?>[] classes = ReflectionUtil.getClassesInPackage(path);
//
//            for (Class<?> clazz : classes) {
//                try {
//                    if (clazz.isAnnotationPresent(Hidden.class)) continue;
//
//                    if (Component.class.isAssignableFrom(clazz) && clazz != Component.class) {
//                        this.componentManager.add((Component) clazz.getConstructor().newInstance());
//                    } else if (Module.class.isAssignableFrom(clazz) && clazz != Module.class) {
//                        this.moduleManager.add((Module) clazz.getConstructor().newInstance());
//                    } else if (Command.class.isAssignableFrom(clazz) && clazz != Command.class) {
//                        this.commandManager.add((Command) clazz.getConstructor().newInstance());
//                    } else if (Check.class.isAssignableFrom(clazz) && clazz != Check.class) {
//                        this.packetLogManager.add((Check) clazz.getConstructor().newInstance());
//                    }
//                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
//                         NoSuchMethodException ignored) {}
//            }
//
//            break;
//        }

        Fucker.fuckClass(this.getClass(), this);

        componentManager.add(new EntityKillEventComponent());
        componentManager.add(new EntityTickComponent());
        componentManager.add(new AdaptiveRefreshRateComponent());
        componentManager.add(new DragComponent());
        componentManager.add(new APIKeyComponent());
        componentManager.add(new InventoryDeSyncComponent());
        componentManager.add(new TeleportAuraComponent());
        componentManager.add(new PacketLogComponent());
        componentManager.add(new GuiClosePatchComponent());
        componentManager.add(new ParticleDistanceComponent());
        componentManager.add(new BadPacketsComponent());
        componentManager.add(new BlinkComponent());
        componentManager.add(new FallDistanceComponent());
        componentManager.add(new GUIDetectionComponent());
        componentManager.add(new ItemDamageComponent());
        componentManager.add(new LastConnectionComponent());
        componentManager.add(new PacketlessDamageComponent());
        componentManager.add(new PingSpoofComponent());
        componentManager.add(new RotationComponent());
        componentManager.add(new SelectorDetectionComponent());
        componentManager.add(new SlotComponent());
        componentManager.add(new ESPComponent());
        componentManager.add(new NotificationComponent());
        componentManager.add(new ParticleComponent());
        componentManager.add(new ProjectionComponent());
        componentManager.add(new SmoothCameraComponent());
        componentManager.add(new BlockHitboxFixComponent());
        componentManager.add(new HitboxFixComponent());
        componentManager.add(new MinimumMotionFixComponent());

        this.packetLogManager.add(
                new FlyingCheck()
        );

        Client.INSTANCE.standardClickGUI = new RiseClickGUI();
        Client.INSTANCE.altManagerMenu = new AltManagerMenu();
        Client.INSTANCE.musicMenu = new MusicMenu();

        this.componentManager.init();

        // Init Managers
        this.targetManager.init();
        this.dataManager.init();
        this.moduleManager.init();
        this.securityManager.init();
        this.botManager.init();
        this.commandManager.init();
        this.fileManager.init();
        this.configManager.init();
        this.altManager.init();
        this.insultManager.init();
        this.scriptManager.init();
        this.packetLogManager.init();

        final File file = new File(ConfigManager.CONFIG_DIRECTORY, "latest.json");
        this.configFile = new ConfigFile(file, FileType.CONFIG);
        this.configFile.allowKeyCodeLoading();
        this.configFile.read();

        this.insultManager.update();
        this.insultManager.forEach(InsultFile::read);

        this.creativeTab = new RiseTab();

        ViaMCP.staticInit();
        noNotify = false;
    }

    /**
     * The terminate method is called when the Minecraft client is shutting
     * down, so we can cleanup our stuff and ready ourselves for the client quitting.
     */
    public void terminate() {
//        Fucker.channel.close();
        this.configFile.write();
    }
}

