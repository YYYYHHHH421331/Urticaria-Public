package cn.hackedmc.fucker;

import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.Type;
import cn.hackedmc.urticaria.anticheat.CheatDetector;
import cn.hackedmc.urticaria.bots.BotManager;
import cn.hackedmc.urticaria.command.CommandManager;
import cn.hackedmc.urticaria.command.impl.*;
import cn.hackedmc.urticaria.component.ComponentManager;
import cn.hackedmc.urticaria.manager.TargetManager;
import cn.hackedmc.urticaria.module.api.manager.ModuleManager;
import cn.hackedmc.urticaria.module.impl.combat.*;
import cn.hackedmc.urticaria.module.impl.exploit.*;
import cn.hackedmc.urticaria.module.impl.ghost.*;
import cn.hackedmc.urticaria.module.impl.movement.*;
import cn.hackedmc.urticaria.module.impl.other.*;
import cn.hackedmc.urticaria.module.impl.other.Insults;
import cn.hackedmc.urticaria.module.impl.other.Timer;
import cn.hackedmc.urticaria.module.impl.player.*;
import cn.hackedmc.urticaria.module.impl.render.*;
import cn.hackedmc.urticaria.newevent.bus.impl.EventBus;
import cn.hackedmc.urticaria.packetlog.api.manager.PacketLogManager;
import cn.hackedmc.urticaria.script.ScriptManager;
import cn.hackedmc.urticaria.security.ExploitManager;
import cn.hackedmc.urticaria.ui.theme.ThemeManager;
import cn.hackedmc.urticaria.util.file.FileManager;
import cn.hackedmc.urticaria.util.file.alt.AltManager;
import cn.hackedmc.urticaria.util.file.config.ConfigManager;
import cn.hackedmc.urticaria.util.file.data.DataManager;
import cn.hackedmc.urticaria.util.file.insult.InsultManager;
import cn.hackedmc.urticaria.util.localization.Locale;
import cn.hackedmc.urticaria.util.math.MathConst;
import cn.hackedmc.urticaria.util.value.ConstantManager;
import net.minecraft.util.MathHelper;

public class Fucker {

    public static void fuckClass(Class<?> clazz, Object instance) {
//        if (channel == null || !channel.isActive() || !login || !HWIDUtil.getUUID().equals(uuid) || instance.getClass() != Client.class) return;
        MathConst.PI = (float) Math.PI;
        MathConst.TO_RADIANS = MathConst.PI / 180.0F;
        MathConst.TO_DEGREES = 180.0F / MathConst.PI;
        for (int i = 0; i <= 360; ++i) {
            MathConst.COSINE[i] = MathHelper.cos(i * MathConst.TO_RADIANS);
            MathConst.SINE[i] = MathHelper.sin(i * MathConst.TO_RADIANS);
        }

        ((Client) instance).setLocale(Locale.EN_US);
        Client.CLIENT_TYPE = Type.BASIC;
        ((Client) instance).moduleManager = new ModuleManager();
        ((Client) instance).componentManager = new ComponentManager();
        ((Client) instance).commandManager = new CommandManager();
        ((Client) instance).fileManager = new FileManager();
        ((Client) instance).configManager = new ConfigManager();
        ((Client) instance).altManager = new AltManager();
        ((Client) instance).insultManager = new InsultManager();
        ((Client) instance).dataManager = new DataManager();
        ((Client) instance).securityManager = new ExploitManager();
        ((Client) instance).botManager = new BotManager();
        ((Client) instance). themeManager = new ThemeManager();
        ((Client) instance).scriptManager = new ScriptManager();
        ((Client) instance).targetManager = new TargetManager();
        ((Client) instance).cheatDetector = new CheatDetector();
        ((Client) instance).constantManager = new ConstantManager();
        ((Client) instance).packetLogManager = new PacketLogManager();
        ((Client) instance).eventBus = new EventBus<>();

        ((Client) instance).moduleManager.add(new AntiBot());
        ((Client) instance).moduleManager.add(new ComboOneHit());
        ((Client) instance).moduleManager.add(new Criticals());
        ((Client) instance).moduleManager.add(new KillAura());
        ((Client) instance).moduleManager.add(new NewTeleportAura());
        ((Client) instance).moduleManager.add(new Regen());
        ((Client) instance).moduleManager.add(new TeleportAura());
        ((Client) instance).moduleManager.add(new Velocity());
        ((Client) instance).moduleManager.add(new Teams());

// Exploit
        ((Client) instance).moduleManager.add(new ConsoleSpammer());
        ((Client) instance).moduleManager.add(new Crasher());
        ((Client) instance).moduleManager.add(new Disabler());
        ((Client) instance).moduleManager.add(new GodMode());
        ((Client) instance).moduleManager.add(new KeepContainer());
        ((Client) instance).moduleManager.add(new LightningTracker());
        ((Client) instance).moduleManager.add(new NoRotate());
        ((Client) instance).moduleManager.add(new PingSpoof());
        ((Client) instance).moduleManager.add(new StaffDetector());

// Ghost
        ((Client) instance).moduleManager.add(new AimAssist());
        ((Client) instance).moduleManager.add(new AimBacktrack());
        ((Client) instance).moduleManager.add(new AutoClicker());
        ((Client) instance).moduleManager.add(new AutoPlace());
        ((Client) instance).moduleManager.add(new Backtrack());
        ((Client) instance).moduleManager.add(new ClickAssist());
        ((Client) instance).moduleManager.add(new Eagle());
        ((Client) instance).moduleManager.add(new FastPlace());
        ((Client) instance).moduleManager.add(new GuiClicker());
        ((Client) instance).moduleManager.add(new HitBox());
        ((Client) instance).moduleManager.add(new KeepSprint());
        ((Client) instance).moduleManager.add(new NoClickDelay());
        ((Client) instance).moduleManager.add(new Reach());
        ((Client) instance).moduleManager.add(new SafeWalk());
        ((Client) instance).moduleManager.add(new WTap());

// Movement
        ((Client) instance).moduleManager.add(new Flight());
        ((Client) instance).moduleManager.add(new InventoryMove());
        ((Client) instance).moduleManager.add(new Jesus());
        ((Client) instance).moduleManager.add(new LongJump());
        ((Client) instance).moduleManager.add(new NoClip());
        ((Client) instance).moduleManager.add(new NoWeb());
        ((Client) instance).moduleManager.add(new NoSlow());
        ((Client) instance).moduleManager.add(new Phase());
        ((Client) instance).moduleManager.add(new PotionExtender());
        ((Client) instance).moduleManager.add(new Sneak());
        ((Client) instance).moduleManager.add(new Speed());
        ((Client) instance).moduleManager.add(new Sprint());
        ((Client) instance).moduleManager.add(new Step());
        ((Client) instance).moduleManager.add(new Strafe());
        ((Client) instance).moduleManager.add(new TargetStrafe());
        ((Client) instance).moduleManager.add(new Teleport());
        ((Client) instance).moduleManager.add(new WallClimb());

// Other
        ((Client) instance).moduleManager.add(new AntiAFK());
        ((Client) instance).moduleManager.add(new AntiExploit());
        ((Client) instance).moduleManager.add(new AutoGG());
        ((Client) instance).moduleManager.add(new AutoGroomer());
        ((Client) instance).moduleManager.add(new cn.hackedmc.urticaria.module.impl.other.CheatDetector());
        ((Client) instance).moduleManager.add(new ClickSounds());
        ((Client) instance).moduleManager.add(new ClientSpoofer());
        ((Client) instance).moduleManager.add(new Debugger());
        ((Client) instance).moduleManager.add(new DiscordPresence());
        ((Client) instance).moduleManager.add(new AutoPlay());
        ((Client) instance).moduleManager.add(new Insults());
        ((Client) instance).moduleManager.add(new MurderMystery());
        ((Client) instance).moduleManager.add(new NoGuiClose());
        ((Client) instance).moduleManager.add(new NoPitchLimit());
        ((Client) instance).moduleManager.add(new Nuker());
        ((Client) instance).moduleManager.add(new PlayerNotifier());
        ((Client) instance).moduleManager.add(new ServerProtocol());
        ((Client) instance).moduleManager.add(new Spammer());
        ((Client) instance).moduleManager.add(new Spotify());
        ((Client) instance).moduleManager.add(new Timer());
        ((Client) instance).moduleManager.add(new Translator());

// Player
        ((Client) instance).moduleManager.add(new AntiSuffocate());
        ((Client) instance).moduleManager.add(new AntiVoid());
        ((Client) instance).moduleManager.add(new AutoHead());
        ((Client) instance).moduleManager.add(new AutoPot());
        ((Client) instance).moduleManager.add(new AutoSoup());
        ((Client) instance).moduleManager.add(new AutoThrow());
        ((Client) instance).moduleManager.add(new AutoGApple());
        ((Client) instance).moduleManager.add(new AutoTool());
        ((Client) instance).moduleManager.add(new Blink());
        ((Client) instance).moduleManager.add(new Breaker());
        ((Client) instance).moduleManager.add(new ChestAura());
        ((Client) instance).moduleManager.add(new FastBreak());
        ((Client) instance).moduleManager.add(new FastUse());
        ((Client) instance).moduleManager.add(new FastLadder());
        ((Client) instance).moduleManager.add(new AntiFireBall());
        ((Client) instance).moduleManager.add(new FlagDetector());
        ((Client) instance).moduleManager.add(new InventorySync());
        ((Client) instance).moduleManager.add(new Manager());
        ((Client) instance).moduleManager.add(new NoFall());
        ((Client) instance).moduleManager.add(new Scaffold());
        ((Client) instance).moduleManager.add(new Stealer());
        ((Client) instance).moduleManager.add(new Twerk());

// Render
        ((Client) instance).moduleManager.add(new Ambience());
        ((Client) instance).moduleManager.add(new Animations());
        ((Client) instance).moduleManager.add(new AppleSkin());
        ((Client) instance).moduleManager.add(new BPSCounter());
        ((Client) instance).moduleManager.add(new XPCounter());
        ((Client) instance).moduleManager.add(new ChestESP());
        ((Client) instance).moduleManager.add(new ClickGUI());
        ((Client) instance).moduleManager.add(new CPSCounter());
        ((Client) instance).moduleManager.add(new Effects());
        ((Client) instance).moduleManager.add(new ESP());
        ((Client) instance).moduleManager.add(new NoFOV());
        ((Client) instance).moduleManager.add(new Footprint());
        ((Client) instance).moduleManager.add(new FPSCounter());
        ((Client) instance).moduleManager.add(new FreeCam());
        ((Client) instance).moduleManager.add(new FreeLook());
        ((Client) instance).moduleManager.add(new FullBright());
        ((Client) instance).moduleManager.add(new Glint());
        ((Client) instance).moduleManager.add(new Health());
        ((Client) instance).moduleManager.add(new InventoryHUD());
// ((Client) instance).moduleManager.add(new KeyBinds());
        ((Client) instance).moduleManager.add(new HotBar());
        ((Client) instance).moduleManager.add(new HurtCamera());
        ((Client) instance).moduleManager.add(new HurtColor());
        ((Client) instance).moduleManager.add(new Interface());
        ((Client) instance).moduleManager.add(new ItemPhysics());
        ((Client) instance).moduleManager.add(new KeyStrokes());
        ((Client) instance).moduleManager.add(new KillEffect());
// ((Client) instance).moduleManager.add(new MusicPlayer());
        ((Client) instance).moduleManager.add(new NameTags());
        ((Client) instance).moduleManager.add(new NoCameraClip());
        ((Client) instance).moduleManager.add(new OffscreenESP());
        ((Client) instance).moduleManager.add(new Projectiles());
        ((Client) instance).moduleManager.add(new Particles());
        ((Client) instance).moduleManager.add(new ProjectionESP());
        ((Client) instance).moduleManager.add(new Radar());
        ((Client) instance).moduleManager.add(new ScoreBoard());
        ((Client) instance).moduleManager.add(new SessionStats());
        ((Client) instance).moduleManager.add(new SniperOverlay());
        ((Client) instance).moduleManager.add(new Streamer());
        ((Client) instance).moduleManager.add(new TargetInfo());
        ((Client) instance).moduleManager.add(new Tracers());
        ((Client) instance).moduleManager.add(new UnlimitedChat());
        ((Client) instance).moduleManager.add(new ViewBobbing());

        ((Client) instance).commandManager.add(new Bind());
        ((Client) instance).commandManager.add(new Clip());
        ((Client) instance).commandManager.add(new Config());
        ((Client) instance).commandManager.add(new DeveloperReload());
        ((Client) instance).commandManager.add(new Friend());
        ((Client) instance).commandManager.add(new Help());
        ((Client) instance).commandManager.add(new cn.hackedmc.urticaria.command.impl.Insults());
        ((Client) instance).commandManager.add(new Name());
        ((Client) instance).commandManager.add(new Panic());
        ((Client) instance).commandManager.add(new Say());
        ((Client) instance).commandManager.add(new IRC());
        ((Client) instance).commandManager.add(new Script());
        ((Client) instance).commandManager.add(new Stuck());
        ((Client) instance).commandManager.add(new Toggle());

    }

    public static void init() {
    }
}
