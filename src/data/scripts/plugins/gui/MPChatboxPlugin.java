package data.scripts.plugins.gui;

import cmu.gui.Button;
import cmu.gui.Panel;
import cmu.gui.*;
import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import org.lazywizard.lazylib.ui.FontException;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MPChatboxPlugin extends BaseEveryFrameCombatPlugin {

    private static LazyFont.DrawableString TODRAW14;

    private String input;
    private final List<ChatEntry> entries = new ArrayList<>();

    private enum ActivePanel {
        NONE,
        CHAT
    }
    private ActivePanel active = ActivePanel.NONE;

    private Panel widget;
    private Panel chatbox;

    public MPChatboxPlugin() {

    }

    @Override
    public void init(CombatEngineAPI engine) {
        if (TODRAW14 == null) {
            try {
                LazyFont fontdraw = LazyFont.loadFont("graphics/fonts/victor14.fnt");
                TODRAW14 = fontdraw.createText();
                if (Global.getSettings().getScreenScaleMult() > 1f) TODRAW14.setFontSize(14f * Global.getSettings().getScreenScaleMult());
            } catch (FontException ignored) {
            }
        }

        widget = initWidget();
        chatbox = initChatbox();
    }

    @Override
    public void processInputPreCoreControls(float amount, List<InputEventAPI> events) {
        if (Global.getCurrentState() == GameState.TITLE) {
            return;
        }
        if (TODRAW14 == null) return;

        float w = Global.getSettings().getScreenWidthPixels();
        float h = Global.getSettings().getScreenHeightPixels();

        Vector2f root1 = new Vector2f(w - 92f, h - 148f);
        Vector2f root2 = new Vector2f(w - 264f, 500f);

        CMUKitUI.render(widget, root1, events);

        switch (active) {
            case NONE:
                break;
            case CHAT:
                CMUKitUI.render(chatbox, root2, events);
                break;
        }
    }

    private Panel initWidget() {
        Panel.PanelParams panelParams = new Panel.PanelParams();
        panelParams.x = 60f;
        panelParams.y = 26f;

        return new Panel(panelParams, new Panel.PanelMaker() {
            @Override
            public void make(Panel panel1) {
                Button.ButtonParams buttonParams = new Button.ButtonParams();
                buttonParams.width = 58f;
                buttonParams.height = 24f;
                Text.TextParams textParams = new Text.TextParams();
                textParams.align = LazyFont.TextAlignment.CENTER;
                Text text = new Text(new Execute<String>() {
                    @Override
                    public String get() {
                        return "CHAT";
                    }
                }, TODRAW14, textParams);
                Button button = new Button(buttonParams, text, new Button.ButtonCallback() {
                    @Override
                    public void onClick() {
                        if (active == ActivePanel.NONE) {
                            active = ActivePanel.CHAT;
                        } else {
                            active = ActivePanel.NONE;
                        }
                    }
                });
                panel1.addChild(button);
            }
        });
    }

    private Panel initChatbox() {
        final Panel.PanelParams panelParams = new Panel.PanelParams();
        panelParams.x = 260f;
        panelParams.y = 180f;
        panelParams.update = false;

        final Panel.PanelParams chatPanelParams = new Panel.PanelParams();
        chatPanelParams.x = 250f;
        chatPanelParams.y = 160f;
        chatPanelParams.noDeco = true;
        chatPanelParams.conformToListSize = true;
        chatPanelParams.update = true;

        final Panel chatPanel = new Panel(chatPanelParams, new Panel.PanelMaker() {
            @Override
            public void make(Panel panel) {
                List<Text> toAdd = new ArrayList<>();

                float height = 0f;
                for (final ChatEntry entry : entries) {
                    final String t = entry == null ? "_" : entry.username + ": " + entry.text;

                    TODRAW14.setText(t);
                    TODRAW14.setMaxWidth(chatPanelParams.x - 4f);
                    height += TODRAW14.getHeight();
                    if (height > chatPanelParams.y) break;

                    Text.TextParams textParams = new Text.TextParams();
                    textParams.color = Color.WHITE;
                    textParams.maxWidth = chatPanelParams.x - 4f;
                    textParams.maxHeight = 50f;
                    Text text = new Text(new Execute<String>() {
                        @Override
                        public String get() {
                            return t;
                        }
                    }, TODRAW14, textParams);

                    toAdd.add(text);
                }

                for (int i = toAdd.size(); i-- > 0;) {
                    panel.addChild(toAdd.get(i));
                }
            }
        });

        return new Panel(panelParams, new Panel.PanelMaker() {
            @Override
            public void make(Panel panel1) {
                panel1.addChild(chatPanel);

                TextEntryBox.TextEntryBoxParams textEntryBoxParams = new TextEntryBox.TextEntryBoxParams();
                textEntryBoxParams.width = 250f;
                textEntryBoxParams.height = 24f;
                Text.TextParams textParams1 = new Text.TextParams();
                final TextEntryBox textEntryBox = new TextEntryBox(textEntryBoxParams, TODRAW14, textParams1);
                panel1.addChild(textEntryBox);

                Button.ButtonParams buttonParams = new Button.ButtonParams();
                buttonParams.width = 42f;
                buttonParams.height = 17f;
                Text.TextParams textParams = new Text.TextParams();
                textParams.align = LazyFont.TextAlignment.CENTER;
                Text text = new Text(new Execute<String>() {
                    @Override
                    public String get() {
                        return "SEND";
                    }
                }, TODRAW14, textParams);
                Button button = new Button(buttonParams, text, new Button.ButtonCallback() {
                    @Override
                    public void onClick() {
                        input = textEntryBox.getString();
                        textEntryBox.setString("");
                    }
                });
                panel1.addChild(button);
            }
        });
    }

    public String getInput() {
        String out = input;
        input = null;
        return out;
    }

    public void addEntry(ChatEntry entry) {
        entries.add(0, entry);
    }

    public static class ChatEntry {
        public final String text;
        public String username;
        public final byte connectionID;

        public ChatEntry(String text, String username, byte connectionID) {
            this.text = text;
            this.username = username;
            this.connectionID = connectionID;
        }
    }
}
