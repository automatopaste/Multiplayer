package data.scripts.plugins.gui;

import cmu.gui.Button;
import cmu.gui.*;
import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.MPModPlugin;
import data.scripts.net.data.packables.entities.ships.ShipData;
import data.scripts.net.data.packables.entities.ships.ClientPlayerData;
import data.scripts.net.data.tables.client.combat.entities.ships.ClientShipTable;
import data.scripts.net.data.tables.client.combat.player.PlayerShip;
import data.scripts.net.data.tables.server.combat.entities.ships.ShipTable;
import data.scripts.net.data.tables.server.combat.players.PlayerShips;
import data.scripts.plugins.MPClientPlugin;
import data.scripts.plugins.MPPlugin;
import data.scripts.plugins.MPServerPlugin;
import org.lazywizard.lazylib.JSONUtils;
import org.lazywizard.lazylib.ui.FontException;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MPUIPlugin extends BaseEveryFrameCombatPlugin {

    private static LazyFont.DrawableString TODRAW14;
    private static LazyFont.DrawableString TODRAW24;

    private ListPanel hostPanel;
    private ListPanel joinPanel;
    private ListPanel widgetPanel;
    private ListPanel selectPanel;
    private GridPanel shipSelectionPanel;
    private enum ActivePanel {
        NONE,
        SELECT,
        HOST,
        JOIN,
        SHIP_SELECT
    }
    private ActivePanel active = ActivePanel.NONE;

    private boolean cameraLockToShip = false;
    private boolean prevCameraLockToShip = false;

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
        if (TODRAW24 == null) {
            try {
                LazyFont fontdraw = LazyFont.loadFont("graphics/fonts/orbitron24aa.fnt");
                TODRAW24 = fontdraw.createText();
                if (Global.getSettings().getScreenScaleMult() > 1f) TODRAW24.setFontSize(24f * Global.getSettings().getScreenScaleMult());
            } catch (FontException ignored) {
            }
        }

        widgetPanel = initWidget();
        selectPanel = initSelect();
        hostPanel = initHostUI();
        joinPanel = initConnectionUI();
    }

    @Override
    public void processInputPreCoreControls(float amount, List<InputEventAPI> events) {
        if (Global.getCurrentState() == GameState.TITLE) {
            return;
        }
        if (TODRAW14 == null) return;

        MPPlugin plugin = (MPPlugin) Global.getCombatEngine().getCustomData().get(MPPlugin.DATA_KEY);
        if (plugin instanceof MPClientPlugin) {
            if (shipSelectionPanel == null) shipSelectionPanel = initShipSelectionUI(plugin);
        } else {
            shipSelectionPanel = null;
        }

        float w = Global.getSettings().getScreenWidthPixels();
        float h = Global.getSettings().getScreenHeightPixels();

        Vector2f root1 = new Vector2f(w - 32f, h - 148f);
        CMUKitUI.render(widgetPanel, root1, events);

        Vector2f root2 = new Vector2f(w - 306f, h - 175f);

        shipSelectionPanel = initShipSelectionUI(plugin);

        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine.isPaused() && plugin != null) {
            CMUKitUI.openGL11ForText();
            TODRAW24.setText("! // PAUSED \\\\ !");
            float w1 = TODRAW24.getWidth();
            float h1 = TODRAW24.getHeight();
            TODRAW24.draw((w - w1) * 0.5f, (h - h1) * 0.5f);
            CMUKitUI.closeGL11ForText();
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
            if (Keyboard.isKeyDown(Keyboard.KEY_L)) {
                if (!prevCameraLockToShip) {
                    cameraLockToShip = !cameraLockToShip;
                    prevCameraLockToShip = true;
                }
            } else {
                prevCameraLockToShip = false;
            }
        }

        if (cameraLockToShip && engine.getPlayerShip() != null) {
            CMUKitUI.openGL11ForText();
            TODRAW24.setText("Camera locked ( toggle with L-CTRL + L )");
            float w1 = TODRAW24.getWidth();
            float h1 = TODRAW24.getHeight();
            TODRAW24.draw((w - w1) * 0.5f, (h - 20f) - (h1 * 0.5f));
            CMUKitUI.closeGL11ForText();

            engine.getViewport().setExternalControl(true);
            engine.getViewport().setCenter(engine.getPlayerShip().getLocation());
        } else {
            engine.getViewport().setExternalControl(false);
        }

        switch (active) {
            case NONE:
                break;
            case SELECT:
                CMUKitUI.render(selectPanel, root2, events);
                break;
            case HOST:
                CMUKitUI.render(hostPanel, root2, events);
                break;
            case JOIN:
                CMUKitUI.render(joinPanel, root2, events);
                break;
            case SHIP_SELECT:
                Vector2f s = new Vector2f((w - shipSelectionPanel.getWidth()) * 0.5f, (h + shipSelectionPanel.getHeight()) * 0.5f);
                CMUKitUI.render(shipSelectionPanel, s, events);
                break;
        }
    }

    private ListPanel initWidget() {
        ListPanel.ListPanelParams panelParams = new ListPanel.ListPanelParams();
        panelParams.x = 26f;
        panelParams.y = 26f;

        return new ListPanel(panelParams, new ListPanel.PanelMaker() {
            @Override
            public void make(ListPanel panel1) {
                Button.ButtonParams buttonParams = new Button.ButtonParams();
                buttonParams.width = 24f;
                buttonParams.height = 24f;
                buttonParams.text = "MP";
                Text.TextParams textParams = new Text.TextParams();
                textParams.align = LazyFont.TextAlignment.CENTER;
                Text text = new Text(new Execute<String>() {
                    @Override
                    public String get() {
                        return "MP";
                    }
                }, TODRAW14, textParams);
                Button button = new Button(buttonParams, text, new Button.ButtonCallback() {
                    @Override
                    public void onClick() {
                        if (active == ActivePanel.NONE) {
                            Global.getCombatEngine().setPaused(true);
                            active = ActivePanel.SELECT;
                        } else {
                            active = ActivePanel.NONE;
                        }

                    }
                });
                panel1.addChild(button);
            }
        });
    }

    private ListPanel initSelect() {
        ListPanel.ListPanelParams panelParams = new ListPanel.ListPanelParams();
        panelParams.x = 300f;
        panelParams.y = 120f;
        panelParams.update = true;
        panelParams.conformToListSize = true;

        return new ListPanel(panelParams, new ListPanel.PanelMaker() {
            @Override
            public void make(ListPanel panel1) {
                Text.TextParams textParams = new Text.TextParams();
                Text text = new Text(new Execute<String>() {
                    @Override
                    public String get() {
                        return "MULTIPLAYER MENU";
                    }
                }, TODRAW24, textParams);
                panel1.addChild(text);

                Text.TextParams buttonTextParams = new Text.TextParams();
                buttonTextParams.align = LazyFont.TextAlignment.CENTER;
                Text buttonText = new Text(new Execute<String>() {
                    @Override
                    public String get() {
                        return "HOST A SERVER";
                    }
                }, TODRAW14, buttonTextParams);
                Button.ButtonParams buttonParams = new Button.ButtonParams();
                buttonParams.width = 120f;
                buttonParams.height = 24f;
                Button.ButtonCallback buttonCallback = new Button.ButtonCallback() {
                    @Override
                    public void onClick() {
                        active = ActivePanel.HOST;
                    }
                };
                Button button = new Button(buttonParams, buttonText, buttonCallback);
                panel1.addChild(button);

                Text.TextParams buttonTextParams2 = new Text.TextParams();
                buttonTextParams2.align = LazyFont.TextAlignment.CENTER;
                Text buttonText2 = new Text(new Execute<String>() {
                    @Override
                    public String get() {
                        return "JOIN A GAME";
                    }
                }, TODRAW14, buttonTextParams2);
                Button.ButtonParams buttonParams2 = new Button.ButtonParams();
                buttonParams2.width = 120f;
                buttonParams2.height = 24f;
                Button.ButtonCallback buttonCallback2 = new Button.ButtonCallback() {
                    @Override
                    public void onClick() {
                        active = ActivePanel.JOIN;
                    }
                };
                Button button2 = new Button(buttonParams2, buttonText2, buttonCallback2);
                panel1.addChild(button2);

                Text.TextParams buttonTextParams3 = new Text.TextParams();
                buttonTextParams3.align = LazyFont.TextAlignment.CENTER;
                Text buttonText3 = new Text(new Execute<String>() {
                    @Override
                    public String get() {
                        return "SELECT SHIP";
                    }
                }, TODRAW14, buttonTextParams3);
                Button.ButtonParams buttonParams3 = new Button.ButtonParams();
                buttonParams3.width = 120f;
                buttonParams3.height = 24f;
                Button.ButtonCallback buttonCallback3 = new Button.ButtonCallback() {
                    @Override
                    public void onClick() {
                        active = ActivePanel.SHIP_SELECT;
                    }
                };
                Button button3 = new Button(buttonParams3, buttonText3, buttonCallback3);
                panel1.addChild(button3);

                final MPPlugin plugin = (MPPlugin) Global.getCombatEngine().getCustomData().get(MPPlugin.DATA_KEY);
                if (plugin instanceof MPClientPlugin) {
                    Text.TextParams buttonTextParams4 = new Text.TextParams();
                    buttonTextParams4.align = LazyFont.TextAlignment.CENTER;
                    buttonTextParams4.color = Color.ORANGE;
                    Text buttonText4 = new Text(new Execute<String>() {
                        @Override
                        public String get() {
                            return "DISCONNECT";
                        }
                    }, TODRAW14, buttonTextParams4);
                    Button.ButtonParams buttonParams4 = new Button.ButtonParams();
                    buttonParams4.width = 120f;
                    buttonParams4.height = 24f;
                    Button.ButtonCallback buttonCallback4 = new Button.ButtonCallback() {
                        @Override
                        public void onClick() {
                            active = ActivePanel.NONE;
                            plugin.stop();
                            Global.getCombatEngine().getCustomData().put((MPPlugin.DATA_KEY), null);
                        }
                    };
                    Button button4 = new Button(buttonParams4, buttonText4, buttonCallback4);
                    panel1.addChild(button4);
                } else if (plugin instanceof MPServerPlugin) {
                    Text.TextParams buttonTextParams4 = new Text.TextParams();
                    buttonTextParams4.align = LazyFont.TextAlignment.CENTER;
                    buttonTextParams4.color = Color.ORANGE;
                    Text buttonText4 = new Text(new Execute<String>() {
                        @Override
                        public String get() {
                            return "STOP SERVER";
                        }
                    }, TODRAW14, buttonTextParams4);
                    Button.ButtonParams buttonParams4 = new Button.ButtonParams();
                    buttonParams4.width = 120f;
                    buttonParams4.height = 24f;
                    Button.ButtonCallback buttonCallback4 = new Button.ButtonCallback() {
                        @Override
                        public void onClick() {
                            active = ActivePanel.NONE;
                            plugin.stop();
                            Global.getCombatEngine().getCustomData().put((MPPlugin.DATA_KEY), null);
                        }
                    };
                    Button button4 = new Button(buttonParams4, buttonText4, buttonCallback4);
                    panel1.addChild(button4);
                }
            }
        });
    }

    private ListPanel initHostUI() {
        ListPanel.ListPanelParams panelParams = new ListPanel.ListPanelParams();
        panelParams.x = 300f;
        panelParams.y = 120f;
        panelParams.conformToListSize = true;

        return new ListPanel(panelParams, new ListPanel.PanelMaker() {
            @Override
            public void make(ListPanel panel1) {
                Text.TextParams textParams = new Text.TextParams();
                Text text = new Text(new Execute<String>() {
                    @Override
                    public String get() {
                        return "HOST MULTIPLAYER GAME";
                    }
                }, TODRAW24, textParams);

                Text.TextParams textParams2 = new Text.TextParams();
                Text text2 = new Text(new Execute<String>() {
                    @Override
                    public String get() {
                        return "ENTER HOST PORT";
                    }
                }, TODRAW14, textParams2);

                String s;
                try (JSONUtils.CommonDataJSONObject data = JSONUtils.loadCommonJSON("mp_cache")) {
                    s = data.getInt("host_port") + "";
                } catch (Exception e) {
                    s = "";
                }
                TextEntryBox.TextEntryBoxParams textEntryBoxParams = new TextEntryBox.TextEntryBoxParams();
                textEntryBoxParams.height = 26f;
                textEntryBoxParams.width = 80f;
                Text.TextParams entryBoxTextParams = new Text.TextParams();
                entryBoxTextParams.align = LazyFont.TextAlignment.LEFT;
                final TextEntryBox textEntryBox = new TextEntryBox(textEntryBoxParams, TODRAW14, entryBoxTextParams);
                textEntryBox.setString(s);

                Text.TextParams buttonTextParams1 = new Text.TextParams();
                buttonTextParams1.align = LazyFont.TextAlignment.CENTER;
                Text buttonText1 = new Text(new Execute<String>() {
                    @Override
                    public String get() {
                        return "CLEAR";
                    }
                }, TODRAW14, buttonTextParams1);
                Button.ButtonParams buttonParams1 = new Button.ButtonParams();
                buttonParams1.width = 60f;
                buttonParams1.height = 20f;
                Button.ButtonCallback buttonCallback1 = new Button.ButtonCallback() {
                    @Override
                    public void onClick() {
                        textEntryBox.setString("");
                    }
                };
                Button button1 = new Button(buttonParams1, buttonText1, buttonCallback1);

                Text.TextParams textParams3 = new Text.TextParams();
                textParams3.color = Color.GRAY;
                final Text text3 = new Text(new Execute<String>() {
                    @Override
                    public String get() {
                        return "VER: " + MPModPlugin.VERSION;
                    }
                }, TODRAW14, textParams3);
                Text.TextParams buttonTextParams = new Text.TextParams();
                buttonTextParams.align = LazyFont.TextAlignment.CENTER;
                Text buttonText = new Text(new Execute<String>() {
                    @Override
                    public String get() {
                        return "HOST";
                    }
                }, TODRAW24, buttonTextParams);
                Button.ButtonParams buttonParams = new Button.ButtonParams();
                buttonParams.width = 120f;
                buttonParams.height = 28f;
                Button.ButtonCallback buttonCallback = new Button.ButtonCallback() {
                    @Override
                    public void onClick() {
                        initServer(textEntryBox.getString(), text3);
                    }
                };
                Button button = new Button(buttonParams, buttonText, buttonCallback);

                panel1.addChild(text);
                panel1.addChild(text2);
                panel1.addChild(textEntryBox);
                panel1.addChild(button1);
                panel1.addChild(button);
                panel1.addChild(text3);
            }
        });
    }

    private ListPanel initConnectionUI() {
        ListPanel.ListPanelParams panelParams = new ListPanel.ListPanelParams();
        panelParams.x = 300f;
        panelParams.y = 150f;
        panelParams.conformToListSize = true;

        return new ListPanel(panelParams, new ListPanel.PanelMaker() {
            @Override
            public void make(ListPanel panel1) {
                Text.TextParams textParams = new Text.TextParams();
                Text text = new Text(new Execute<String>() {
                    @Override
                    public String get() {
                        return "JOIN MULTIPLAYER GAME";
                    }
                }, TODRAW24, textParams);

                Text.TextParams textParams2 = new Text.TextParams();
                Text text2 = new Text(new Execute<String>() {
                    @Override
                    public String get() {
                        return "ENTER HOST IP ADDRESS";
                    }
                }, TODRAW14, textParams2);

                Text.TextParams textParams4 = new Text.TextParams();
                Text text4 = new Text(new Execute<String>() {
                    @Override
                    public String get() {
                        return "ENTER HOST PORT";
                    }
                }, TODRAW14, textParams4);

                String s1;
                int p;
                try (JSONUtils.CommonDataJSONObject data = JSONUtils.loadCommonJSON("mp_cache")) {
                    s1 = data.getString("ip");
                    p = data.getInt("port");
                } catch (Exception e) {
                    s1 = "";
                    p = 0;
                }
                TextEntryBox.TextEntryBoxParams textEntryBoxParams = new TextEntryBox.TextEntryBoxParams();
                textEntryBoxParams.height = 30f;
                textEntryBoxParams.width = 200f;
                Text.TextParams entryBoxTextParams = new Text.TextParams();
                entryBoxTextParams.align = LazyFont.TextAlignment.LEFT;
                final TextEntryBox textEntryBox1 = new TextEntryBox(textEntryBoxParams, TODRAW14, entryBoxTextParams);
                textEntryBox1.setString(s1);
                final TextEntryBox textEntryBox2 = new TextEntryBox(textEntryBoxParams, TODRAW14, entryBoxTextParams);
                textEntryBox2.setString(p + "");

                Text.TextParams buttonTextParams1 = new Text.TextParams();
                buttonTextParams1.align = LazyFont.TextAlignment.CENTER;
                Text buttonText1 = new Text(new Execute<String>() {
                    @Override
                    public String get() {
                        return "CLEAR";
                    }
                }, TODRAW14, buttonTextParams1);
                Button.ButtonParams buttonParams1 = new Button.ButtonParams();
                buttonParams1.width = 60f;
                buttonParams1.height = 20f;
                Button.ButtonCallback buttonCallback1 = new Button.ButtonCallback() {
                    @Override
                    public void onClick() {
                        textEntryBox1.setString("");
                        textEntryBox2.setString("0");
                    }
                };
                Button button1 = new Button(buttonParams1, buttonText1, buttonCallback1);

                Text.TextParams textParams3 = new Text.TextParams();
                textParams3.color = Color.GRAY;
                final Text text3 = new Text(new Execute<String>() {
                    @Override
                    public String get() {
                        return "VER: " + MPModPlugin.VERSION;
                    }
                }, TODRAW14, textParams3);
                Text.TextParams buttonTextParams = new Text.TextParams();
                buttonTextParams.align = LazyFont.TextAlignment.CENTER;
                Text buttonText = new Text(new Execute<String>() {
                    @Override
                    public String get() {
                        return "CONNECT";
                    }
                }, TODRAW24, buttonTextParams);
                Button.ButtonParams buttonParams = new Button.ButtonParams();
                buttonParams.width = 120f;
                buttonParams.height = 28f;
                Button.ButtonCallback buttonCallback = new Button.ButtonCallback() {
                    @Override
                    public void onClick() {
                        int pt;
                        try {
                            pt = Integer.parseInt(textEntryBox2.getString());
                        } catch (NumberFormatException n) {
                            pt = 0;
                        }
                        initClient(textEntryBox1.getString(), pt, text3);
                    }
                };
                Button button = new Button(buttonParams, buttonText, buttonCallback);

                panel1.addChild(text);
                panel1.addChild(text2);
                panel1.addChild(textEntryBox1);
                panel1.addChild(text4);
                panel1.addChild(textEntryBox2);
                panel1.addChild(button1);
                panel1.addChild(button);
                panel1.addChild(text3);
            }
        });
    }

//    private ListPanel initShipSelectionUI() {
//        ListPanel.ListPanelParams panelParams = new ListPanel.ListPanelParams();
//        panelParams.x = 300f;
//        panelParams.y = 150f;
//        panelParams.conformToListSize = true;
//        panelParams.update = true;
//        return new ListPanel(panelParams, new ListPanel.PanelMaker() {
//            @Override
//            public void make(ListPanel panel) {
//                Text.TextParams textParams = new Text.TextParams();
//                Text text = new Text(new Execute<String>() {
//                    @Override
//                    public String get() {
//                        return "SHIP SELECTION";
//                    }
//                }, TODRAW24, textParams);
//                panel.addChild(text);
//
//                MPPlugin plugin = (MPPlugin) Global.getCombatEngine().getCustomData().get(MPPlugin.DATA_KEY);
//                final MPClientPlugin clientPlugin = (MPClientPlugin) plugin;
//
//                for (final ShipAPI s : Global.getCombatEngine().getShips()) {
//                    if (!s.isFighter() && s.isAlive()) {
//                        Text.TextParams buttonTextParams1 = new Text.TextParams();
//                        buttonTextParams1.align = LazyFont.TextAlignment.LEFT;
//
//                        if (s.getOwner() == 0) buttonTextParams1.color = Color.GREEN;
//                        else if (s.getOwner() == 1) buttonTextParams1.color = Color.RED;
//                        else buttonTextParams1.color = Color.YELLOW;
//
//                        Text buttonText1 = new Text(new Execute<String>() {
//                            @Override
//                            public String get() {
//                                return s.getHullSpec().getNameWithDesignationWithDashClass();
//                            }
//                        }, TODRAW14, buttonTextParams1);
//
//                        Button.ButtonParams buttonParams1 = new Button.ButtonParams();
//                        buttonParams1.width = 280f;
//                        buttonParams1.height = 26f;
//                        Button.ButtonCallback buttonCallback1 = new Button.ButtonCallback() {
//                            @Override
//                            public void onClick() {
//                                clientPlugin.getPlayerShip().setPlayerShipID(s.getFleetMemberId());
//                            }
//                        };
//                        Button button1 = new Button(buttonParams1, buttonText1, buttonCallback1);
//                        panel.addChild(button1);
//                    }
//                }
//            }
//        });
//    }

    private GridPanel initShipSelectionUI(final MPPlugin plugin) {
        final GridPanel.GridParams params = new GridPanel.GridParams();
        params.x = 1000f;
        params.y = 600f;
        params.edgePad = 0f;
        params.update = true;

        return new GridPanel(params, new GridPanel.PanelMaker() {
            @Override
            public void make(GridPanel gridPanel) {
                final CombatEngineAPI engine = Global.getCombatEngine();

                List<ShipAPI> ships = new ArrayList<>();

                if (plugin == null) {
                    return;
                } else if (plugin.getType() == MPPlugin.PluginType.SERVER) {
                    ShipTable shipTable = (ShipTable) plugin.getEntityManagers().get(ShipTable.class);
                    for (ShipData data : shipTable.getShipTable().array()) {
                        if (data == null || data.getShip() == null) continue;

                        ships.add(data.getShip());
                    }
                } else if (plugin.getType() == MPPlugin.PluginType.CLIENT) {
                    ClientShipTable clientShipTable = (ClientShipTable) plugin.getEntityManagers().get(ClientShipTable.class);
                    for (ShipData data : clientShipTable.getShipTable().array()) {
                        if (data == null || data.getShip() == null) continue;

                        ships.add(data.getShip());
                    }
                }

                final int x = 5, y = 4, max = x * y;
                int xi = 0, yi = 0;
                final float dx = params.x / x, dy = params.y / y;

                Element[][] elements = new Element[y][x];

                int i = 0;
                for (final ShipAPI ship : ships) {
                    if (ship.isFighter()) continue;

                    if (i > max - 1) break;

                    ListPanel.ListPanelParams listPanelParams = new ListPanel.ListPanelParams();
                    listPanelParams.x = dx;
                    listPanelParams.y = dy;
                    listPanelParams.mode = ListPanel.ListMode.VERTICAL;
                    listPanelParams.update = true;
                    listPanelParams.conformToListSize = false;

                    elements[yi][xi] = new ListPanel(listPanelParams, new ListPanel.PanelMaker() {
                        @Override
                        public void make(ListPanel listPanel) {
                            Color c;
                            switch (ship.getOwner()) {
                                case 0:
                                    c = Color.GREEN;
                                    break;
                                case 1:
                                    c = Color.RED;
                                    break;
                                case 100:
                                    c = Color.BLUE;
                                    break;
                                default:
                                    c = Color.YELLOW;
                                    break;
                            }

                            Text.TextParams textParams1 = new Text.TextParams();
                            textParams1.align = LazyFont.TextAlignment.LEFT;
                            textParams1.maxWidth = dx;
                            textParams1.maxHeight = dy;
                            textParams1.color = c;
                            Text text1 = new Text(new Execute<String>() {
                                @Override
                                public String get() {
                                    String n = ship.getName();
                                    if (n == null) return "NULL";
                                    return n;
                                }
                            }, TODRAW14, textParams1);

                            Text.TextParams textParams2 = new Text.TextParams();
                            textParams2.align = LazyFont.TextAlignment.LEFT;
                            textParams2.maxWidth = dx;
                            textParams2.maxHeight = dy;
                            textParams2.color = Color.WHITE;
                            Text text2 = new Text(new Execute<String>() {
                                @Override
                                public String get() {
                                    String sf = String.format("%s"
                                            + "\nLOC: [%s, %s]"
                                            + "\nVEL: [%s, %s]",
                                            ship.getHullSpec().getNameWithDesignationWithDashClass(),
                                            (int) ship.getLocation().x,
                                            (int) ship.getLocation().y,
                                            (int) ship.getVelocity().x,
                                            (int) ship.getVelocity().y
                                    );

                                    return sf;
                                }
                            }, TODRAW14, textParams2);

                            Button.ButtonParams buttonParams = new Button.ButtonParams();
                            buttonParams.height = 24f;
                            buttonParams.width = 140f;
                            final Text.TextParams buttonTextParams = new Text.TextParams();
                            buttonTextParams.color = Color.WHITE;
                            buttonTextParams.maxHeight = 16f;
                            buttonTextParams.maxWidth = 40f;
                            buttonTextParams.align = LazyFont.TextAlignment.CENTER;
                            Text buttonText = new Text(new Execute<String>() {
                                @Override
                                public String get() {
                                    if (plugin instanceof MPClientPlugin) {
                                        MPClientPlugin client = (MPClientPlugin) plugin;

                                        ShipAPI h = client.getPlayerShip().getHostShip();
                                        if (h == ship) {
                                            buttonTextParams.color = Color.YELLOW;
                                            return "HOST CONTROL";
                                        }

                                        for (short id : client.getLobbyInput().getPilotedShipIDs().values()) {
                                            ShipData shipData = client.getShipTable().getShipTable().array()[id];

                                            if (shipData == null) {
                                                buttonTextParams.color = Color.GRAY;
                                                return "DATA NULL";
                                            }

                                            if (shipData.getShip() == ship) {
                                                buttonTextParams.color = Color.BLUE;
                                                return "PLAYER CONTROL";
                                            }
                                        }

                                        ShipAPI s = client.getPlayerShip().getActiveShip();
                                        if (s == null) return "ACTIVE";
                                    } else if (plugin instanceof MPServerPlugin) {
                                        MPServerPlugin server = (MPServerPlugin) plugin;

                                        PlayerShips playerShips = (PlayerShips) server.getEntityManagers().get(PlayerShips.class);
                                        ShipTable shipTable = (ShipTable) server.getEntityManagers().get(ShipTable.class);

                                        Short id = shipTable.getRegistered().get(ship);
                                        if (id == null) return "NULL";

                                        if (id.equals(playerShips.getHostShipID())) {
                                            buttonTextParams.color = Color.YELLOW;
                                            return "HOST CONTROL";
                                        }

                                        for (short sid : playerShips.getControlData().keySet()) {
                                            ClientPlayerData clientPlayerData = playerShips.getControlData().get(sid);

                                            if (clientPlayerData.getShip() == ship) {
                                                buttonTextParams.color = Color.BLUE;
                                                return "PLAYER CONTROL";
                                            }
                                        }

                                    }

                                    return "TRANSFER";
                                }
                            }, TODRAW14, buttonTextParams);
                            Button button = new Button(buttonParams, buttonText, new Button.ButtonCallback() {
                                @Override
                                public void onClick() {
                                    switch (plugin.getType()) {
                                        case CLIENT:
                                            MPClientPlugin clientPlugin = (MPClientPlugin) plugin;
                                            PlayerShip playerShip = (PlayerShip) clientPlugin.getEntityManagers().get(PlayerShip.class);

                                            playerShip.requestTransfer(ship);
                                            break;
                                        case SERVER:
                                            MPServerPlugin serverPlugin = (MPServerPlugin) plugin;
                                            PlayerShips playerShips = (PlayerShips) serverPlugin.getEntityManagers().get(PlayerShips.class);

                                            playerShips.transferControl(ship, true, null);
                                            break;
                                    }
                                }
                            });

                            listPanel.addChild(text1);
                            listPanel.addChild(text2);
                            listPanel.addChild(button);
                        }
                    });

                    xi++;
                    yi += Math.max(0, xi - x + 1);
                    xi %= x;

                    i++;
                }

                gridPanel.setChildren(elements);
            }
        });
    }

    private void initServer(String port, Text infoText) {
        int p;
        try {
            p = Integer.parseInt(port);
            if (p < 1026 || p > 65535) throw new NumberFormatException();
        } catch (NumberFormatException n) {
            infoText.setExecute(new Execute<String>() {
                @Override
                public String get() {
                    return "INVALID PORT";
                }
            });
            infoText.setColor(Color.RED);
            return;
        }

        try (JSONUtils.CommonDataJSONObject data = JSONUtils.loadCommonJSON("mp_cache")) {
            data.put("host_port", p);
            data.save();
        } catch (Exception ignored) {
        }

        infoText.setExecute(new Execute<String>() {
            @Override
            public String get() {
                return "SERVER STARTED";
            }
        });
        infoText.setColor(Color.GREEN);

        MPModPlugin.setPlugin(new MPServerPlugin(p));
    }

    private void initClient(String hostname, int port, Text infoText) {
        hostname = hostname.trim().toLowerCase(Locale.ROOT);
        if (hostname.trim().isEmpty()) {
            infoText.setExecute(new Execute<String>() {
                @Override
                public String get() {
                    return "Specify address";
                }
            });
            infoText.setColor(Color.RED);
            return;
        }

        if (hostname.startsWith("localhost")) {
            infoText.setExecute(new Execute<String>() {
                @Override
                public String get() {
                    return "CLIENT STARTED";
                }
            });

            saveAddress(hostname, 8080);

            MPModPlugin.setPlugin(new MPClientPlugin("localhost", 0));
        } else {
            String[] ids = hostname.split("\\.");
            if (ids.length != 4) {
                infoText.setExecute(new Execute<String>() {
                    @Override
                    public String get() {
                        return "INVALID ADDRESS";
                    }
                });
                infoText.setColor(Color.RED);
                return;
            }

            saveAddress(hostname, 8080);

            infoText.setExecute(new Execute<String>() {
                @Override
                public String get() {
                    return "CLIENT STARTED";
                }
            });
            infoText.setColor(Color.GREEN);
            MPModPlugin.setPlugin(new MPClientPlugin(hostname, port));
        }
    }

    private void saveAddress(String hostname, int port) {
        try (JSONUtils.CommonDataJSONObject data = JSONUtils.loadCommonJSON("mp_cache")) {
            data.put("ip", hostname);
            data.put("port", port);
            data.save();
        } catch (Exception ignored) {
        }
    }
}
