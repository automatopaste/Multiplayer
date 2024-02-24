package data.scripts.plugins.gui;

import cmu.gui.Button;
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

    // singleton
    public static MPChatboxPlugin INSTANCE = null;

    private static LazyFont.DrawableString TODRAW14;

    private String input;
    private final List<ChatEntry> entries = new ArrayList<>();

    private enum ActivePanel {
        TEXT_ONLY,
        CHAT,
        NONE,
    }
    private ActivePanel active = ActivePanel.TEXT_ONLY;

    private ListPanel widget;
    private ListPanel chatbox;
    private ListPanel text;

    public MPChatboxPlugin() {
        INSTANCE = this;
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
        text = initTextPanel();
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
        Vector2f root2 = new Vector2f(w - 364f, 500f);

        CMUKitUI.render(widget, root1, events);

        switch (active) {
            case TEXT_ONLY:
                CMUKitUI.render(text, root2, events);
                break;
            case CHAT:
                CMUKitUI.render(chatbox, root2, events);
                break;
            case NONE:
                break;
        }
    }

    private ListPanel initWidget() {
        ListPanel.ListPanelParams panelParams = new ListPanel.ListPanelParams();
        panelParams.x = 60f;
        panelParams.y = 26f;

        return new ListPanel(panelParams, new ListPanel.PanelMaker() {
            @Override
            public void make(ListPanel panel1) {
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
                        switch (active) {
                            case TEXT_ONLY:
                                active = ActivePanel.CHAT;
                                break;
                            case CHAT:
                                active = ActivePanel.NONE;
                                break;
                            case NONE:
                                active = ActivePanel.TEXT_ONLY;
                                break;
                        }
                    }
                });
                panel1.addChild(button);
            }
        });
    }

    private ListPanel initChatbox() {
        final ListPanel.ListPanelParams panelParams = new ListPanel.ListPanelParams();
        panelParams.x = 360f;
        panelParams.y = 380f;
        panelParams.update = false;

        final ListPanel textPanel = initTextPanel();

        return new ListPanel(panelParams, new ListPanel.PanelMaker() {
            @Override
            public void make(ListPanel panel1) {
                panel1.addChild(textPanel);

                TextEntryBox.TextEntryBoxParams textEntryBoxParams = new TextEntryBox.TextEntryBoxParams();
                textEntryBoxParams.width = 350f;
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

    private ListPanel initTextPanel() {
        final ListPanel.ListPanelParams textPanelParams = new ListPanel.ListPanelParams();
        textPanelParams.x = 350f;
        textPanelParams.y = 360f;
        textPanelParams.noDeco = true;
        textPanelParams.conformToListSize = true;
        textPanelParams.update = true;

        return new ListPanel(textPanelParams, new ListPanel.PanelMaker() {
            @Override
            public void make(ListPanel panel) {
                List<Text> toAdd = new ArrayList<>();

                float height = 0f;
                for (final ChatEntry entry : entries) {
                    String t;
                    if (entry == null) {
                        t = "_";
                    } else if (entry.systemMessage) {
                        t = entry.username + " " + entry.text;
                    } else {
                        t = entry.username + ": " + entry.text;
                    }

                    final String tt = t;

                    TODRAW14.setText(t);
                    TODRAW14.setMaxWidth(textPanelParams.x - 4f);
                    height += TODRAW14.getHeight();
                    if (height > textPanelParams.y) break;

                    Text.TextParams textParams = new Text.TextParams();
                    textParams.color = Color.WHITE;
                    textParams.maxWidth = textPanelParams.x - 4f;
                    textParams.maxHeight = 50f;
                    Text text = new Text(new Execute<String>() {
                        @Override
                        public String get() {
                            return tt;
                        }
                    }, TODRAW14, textParams);

                    toAdd.add(text);
                }

                for (int i = toAdd.size(); i-- > 0;) {
                    panel.addChild(toAdd.get(i));
                }
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
        public final boolean systemMessage;

        public ChatEntry(String text, String username, byte connectionID) {
            this(text, username, connectionID, false);
        }

        public ChatEntry(String text, String username, byte connectionID, boolean systemMessage) {
            this.text = text;
            this.username = username;
            this.connectionID = connectionID;
            this.systemMessage = systemMessage;
        }
    }
}
