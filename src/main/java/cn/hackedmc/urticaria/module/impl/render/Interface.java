package cn.hackedmc.urticaria.module.impl.render;

import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.module.impl.render.interfaces.*;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.render.Render2DEvent;
import cn.hackedmc.urticaria.util.font.FontManager;
import cn.hackedmc.urticaria.util.localization.Localization;
import cn.hackedmc.urticaria.Type;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.impl.motion.PreUpdateEvent;
import cn.hackedmc.urticaria.util.font.Font;
import cn.hackedmc.urticaria.util.math.MathUtil;
import cn.hackedmc.urticaria.util.shader.RiseShaders;
import cn.hackedmc.urticaria.util.vector.Vector2d;
import cn.hackedmc.urticaria.util.vector.Vector2f;
import cn.hackedmc.urticaria.value.Value;
import cn.hackedmc.urticaria.value.impl.BooleanValue;
import cn.hackedmc.urticaria.value.impl.DragValue;
import cn.hackedmc.urticaria.value.impl.ModeValue;
import cn.hackedmc.urticaria.value.impl.SubMode;
import cn.hackedmc.fucker.Fucker;
import lombok.Getter;
import lombok.Setter;
import util.time.StopWatch;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

@Getter
@Setter
@ModuleInfo(name = "module.render.interface.name", description = "module.render.interface.description", category = Category.RENDER, autoEnabled = true)
public final class Interface extends Module {
    public static Interface INSTANCE;
    private final ModeValue mode = new ModeValue("Mode", this, () -> Client.CLIENT_TYPE != Type.BASIC) {{
        add(new ModernInterface("Modern", (Interface) this.getParent()));
        add(new NovoInterface("Novo", (Interface) this.getParent()));
        add(new UrticariaInterface("Urticaria", (Interface) this.getParent()));
        add(new WurstInterface("Wurst", (Interface) this.getParent()));
        setDefault("Novo");
    }};

    public final DragValue logoDrag = new DragValue("", this, new Vector2d(200, 200), () -> !mode.getValue().getName().equalsIgnoreCase("urticaria"));

    private final ModeValue modulesToShow = new ModeValue("Modules to Show", this, () -> Client.CLIENT_TYPE != Type.BASIC) {{
        add(new SubMode("All"));
        add(new SubMode("Exclude render"));
        add(new SubMode("Only bound"));
        setDefault("All");
    }};

    public final BooleanValue irc = new BooleanValue("Show IRC Message", this, true);
    public final BooleanValue limitChatWidth = new BooleanValue("Limit Chat Width", this, false);
    public final BooleanValue smoothHotBar = new BooleanValue("Smooth Hot Bar", this, true);

    public BooleanValue suffix = new BooleanValue("Suffix", this, true, () -> Client.CLIENT_TYPE != Type.BASIC);
    public BooleanValue lowercase = new BooleanValue("Lowercase", this, false, () -> Client.CLIENT_TYPE != Type.BASIC);
    public BooleanValue removeSpaces = new BooleanValue("Remove Spaces", this, true, () -> Client.CLIENT_TYPE != Type.BASIC);

    public ModeValue notifyMode = new ModeValue("Notification Mode", this)
            .add(new SubMode("Off"))
            .add(new SubMode("Basic"))
            .add(new SubMode("Central"))
            .setDefault("Off");

    public BooleanValue showToggle = new BooleanValue("Show Toggle", this, true, () -> notifyMode.getValue().getName().equalsIgnoreCase("Off"));

    public BooleanValue shaders = new BooleanValue("Shaders", this, true);
    public BooleanValue lessShaders = new BooleanValue("Less Shaders", this, false, () -> !shaders.getValue());
    private ArrayList<ModuleComponent> allModuleComponents = new ArrayList<>(),
            activeModuleComponents = new ArrayList<>();
    private SubMode lastFrameModulesToShow = (SubMode) modulesToShow.getValue();

    private final StopWatch stopwatch = new StopWatch();
    private final StopWatch updateTags = new StopWatch();
    private final Font productSansMedium18 = FontManager.getProductSansRegular(18);

    public Font font = FontManager.getProductSansMedium(21);
    public Font font2 = FontManager.getProductSansMedium(20);

    public Font widthComparator = nunitoNormal;
    public float moduleSpacing = 12, edgeOffset;

    public Interface() {
        createArrayList();
        INSTANCE = this;
    }

    public void createArrayList() {
        allModuleComponents.clear();
        Client.INSTANCE.getModuleManager().getAll().stream()
                .sorted(Comparator.comparingDouble(module -> -widthComparator.width(Localization.get(module.getDisplayName()))))
                .forEach(module -> allModuleComponents.add(new ModuleComponent(module)));
    }

    public void sortArrayList() {
//        ArrayList<ModuleComponent> components = new ArrayList<>();
//        Client.INSTANCE.getModuleManager().getAll().forEach(module -> components.add(new ModuleComponent(module)));
//
        activeModuleComponents = allModuleComponents.stream()
                .filter(moduleComponent -> moduleComponent.getModule().shouldDisplay(this))
                .sorted(Comparator.comparingDouble(module -> -module.getNameWidth() - module.getTagWidth()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    StopWatch lastUpdate = new StopWatch();

    @EventLink()
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (lastUpdate.finished(1000)) {
            threadPool.execute(() -> {
                for (final ModuleComponent moduleComponent : allModuleComponents) {
                    moduleComponent.setTranslatedName(Localization.get(moduleComponent.getModule().getDisplayName()));
                }
            });
        }
    };

    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {
        if (lessShaders.getValue() && shaders.getValue()) {
            RiseShaders.GAUSSIAN_BLUR_SHADER.setTryLessRender(true);
            RiseShaders.POST_BLOOM_SHADER.setTryLessRender(true);
            RiseShaders.UI_BLOOM_SHADER.setTryLessRender(true);
            RiseShaders.UI_POST_BLOOM_SHADER.setTryLessRender(true);
        } else if (shaders.getValue()) {
            RiseShaders.GAUSSIAN_BLUR_SHADER.setTryLessRender(false);
            RiseShaders.POST_BLOOM_SHADER.setTryLessRender(false);
            RiseShaders.UI_BLOOM_SHADER.setTryLessRender(false);
            RiseShaders.UI_POST_BLOOM_SHADER.setTryLessRender(false);
        }
        
        Color logoColor = this.getTheme().getFirstColor();
        /*
        final String name = "Username:" + Fucker.name;
        final String rank = "Rank:" + (Fucker.rank == Fucker.Rank.CUSTOM ? Fucker.customTag : Fucker.rank.getDisplayName());
        final String online = "Online:" + Fucker.usernames.size();

         */
        final String name =  "Urticaria";
        final String rank = "IDEA READY";
        final String online = "Offline State";
        this.productSansMedium18.drawStringWithShadow(name, event.getScaledResolution().getScaledWidth() - this.productSansMedium18.width(name) - 2, event.getScaledResolution().getScaledHeight() - this.productSansMedium18.height() * 3 - 2, new Color(-1).getRGB());
        this.productSansMedium18.drawStringWithShadow(rank, event.getScaledResolution().getScaledWidth() - this.productSansMedium18.width(rank) - 2, event.getScaledResolution().getScaledHeight() - this.productSansMedium18.height() * 2 - 2, new Color(-1).getRGB());
        this.productSansMedium18.drawStringWithShadow(online, event.getScaledResolution().getScaledWidth() - this.productSansMedium18.width(online) - 2, event.getScaledResolution().getScaledHeight() - this.productSansMedium18.height() - 2, new Color(-1).getRGB());

        this.productSansMedium18.drawStringWithShadow( "Username:", event.getScaledResolution().getScaledWidth() - this.productSansMedium18.width(name) - this.productSansMedium18.width("Username:") - 4, event.getScaledResolution().getScaledHeight() - this.productSansMedium18.height() * 3 - 2, logoColor.getRGB());
        this.productSansMedium18.drawStringWithShadow("Rank:", event.getScaledResolution().getScaledWidth() - this.productSansMedium18.width(rank) - this.productSansMedium18.width("Rank:") - 4, event.getScaledResolution().getScaledHeight() - this.productSansMedium18.height() * 2 - 2, logoColor.getRGB());
        this.productSansMedium18.drawStringWithShadow("Online:", event.getScaledResolution().getScaledWidth() - this.productSansMedium18.width(online) - this.productSansMedium18.width("Online:") - 4, event.getScaledResolution().getScaledHeight() - this.productSansMedium18.height() - 2, logoColor.getRGB());

        for (final ModuleComponent moduleComponent : allModuleComponents) {
            if (moduleComponent.getModule().isEnabled()) {
                moduleComponent.animationTime = Math.min(moduleComponent.animationTime + stopwatch.getElapsedTime() / 100.0F, 10);
            } else {
                moduleComponent.animationTime = Math.max(moduleComponent.animationTime - stopwatch.getElapsedTime() / 100.0F, 0);
            }
        }

        threadPool.execute(() -> {
            if (updateTags.finished(50)) {
                updateTags.reset();

                for (final ModuleComponent moduleComponent : activeModuleComponents) {
                    if (moduleComponent.animationTime == 0) {
                        continue;
                    }

                    for (final Value<?> value : moduleComponent.getModule().getValues()) {
                        if (value instanceof ModeValue) {
                            final ModeValue modeValue = (ModeValue) value;

                            moduleComponent.setTag(modeValue.getValue().getName());
                            break;
                        }

                        moduleComponent.setTag("");
                    }
                }

                this.sortArrayList();
            }

            final float screenWidth = event.getScaledResolution().getScaledWidth();
            final Vector2f position = new Vector2f(0, 0);
            for (final ModuleComponent moduleComponent : activeModuleComponents) {
                if (moduleComponent.animationTime == 0) {
                    continue;
                }

                moduleComponent.targetPosition = new Vector2d(screenWidth - moduleComponent.getNameWidth() - moduleComponent.getTagWidth(), position.getY());

                if (!moduleComponent.getModule().isEnabled() && moduleComponent.animationTime < 10) {
                    moduleComponent.targetPosition = new Vector2d(screenWidth + moduleComponent.getNameWidth() + moduleComponent.getTagWidth(), position.getY());
                } else {
                    position.setY(position.getY() + moduleSpacing);
                }

                float offsetX = edgeOffset;
                float offsetY = edgeOffset;

                moduleComponent.targetPosition.x -= offsetX;
                moduleComponent.targetPosition.y += offsetY;

                if (Math.abs(moduleComponent.getPosition().getX() - moduleComponent.targetPosition.x) > 0.5 || Math.abs(moduleComponent.getPosition().getY() - moduleComponent.targetPosition.y) > 0.5 || (moduleComponent.animationTime != 0 && moduleComponent.animationTime != 10)) {
                    for (int i = 0; i < stopwatch.getElapsedTime(); ++i) {
                        moduleComponent.position.x = MathUtil.lerp(moduleComponent.position.x, moduleComponent.targetPosition.x, 1.5E-2F);
                        moduleComponent.position.y = MathUtil.lerp(moduleComponent.position.y, moduleComponent.targetPosition.y, 1.5E-2F);
                    }
                } else {
                    moduleComponent.position = moduleComponent.targetPosition;
                }
            }

            stopwatch.reset();
        });
    };
}