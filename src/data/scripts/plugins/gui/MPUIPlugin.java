package data.scripts.plugins.gui;

import cmu.gui.Button;
import cmu.gui.Panel;
import cmu.gui.*;
import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.MPModPlugin;
import data.scripts.plugins.MPClientPlugin;
import data.scripts.plugins.MPPlugin;
import data.scripts.plugins.MPServerPlugin;
import org.lazywizard.lazylib.JSONUtils;
import org.lazywizard.lazylib.ui.FontException;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;

public class MPUIPlugin extends BaseEveryFrameCombatPlugin {

    private static LazyFont.DrawableString TODRAW14;
    private static LazyFont.DrawableString TODRAW24;

    private Panel hostPanel;
    private Panel joinPanel;
    private Panel widgetPanel;
    private Panel selectPanel;
    private Panel shipSelectionPanel;
    private enum ActivePanel {
        NONE,
        SELECT,
        HOST,
        JOIN,
        SHIP_SELECT
    }
    private ActivePanel active = ActivePanel.NONE;

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
    public void advance(float amount, List<InputEventAPI> events) {
        if (Global.getCurrentState() == GameState.TITLE) {
            return;
        }
        if (TODRAW14 == null) return;

        MPPlugin plugin = (MPPlugin) Global.getCombatEngine().getCustomData().get(MPPlugin.DATA_KEY);
        if (plugin instanceof MPClientPlugin) {
            if (shipSelectionPanel == null) shipSelectionPanel = initShipSelectionUI();
        } else {
            shipSelectionPanel = null;
        }

        float w = Global.getSettings().getScreenWidthPixels();
        float h = Global.getSettings().getScreenHeightPixels();

        Vector2f root1 = new Vector2f(w - 32f, h - 148f);
        CMUKitUI.render(widgetPanel, root1, events);

        Vector2f root2 = new Vector2f(w - 306f, h - 175f);

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
                CMUKitUI.render(shipSelectionPanel, root2, events);
                break;
        }
    }

    private Panel initWidget() {
        Panel.PanelParams panelParams = new Panel.PanelParams();
        panelParams.x = 26f;
        panelParams.y = 26f;

        return new Panel(panelParams, new Panel.PanelMaker() {
            @Override
            public void make(Panel panel1) {
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

    private Panel initSelect() {
        Panel.PanelParams panelParams = new Panel.PanelParams();
        panelParams.x = 300f;
        panelParams.y = 120f;
        panelParams.update = true;
        panelParams.conformToListSize = true;

        return new Panel(panelParams, new Panel.PanelMaker() {
            @Override
            public void make(Panel panel1) {
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

                final MPPlugin plugin = (MPPlugin) Global.getCombatEngine().getCustomData().get(MPPlugin.DATA_KEY);
                if (plugin instanceof MPClientPlugin) {
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
                        }
                    };
                    Button button4 = new Button(buttonParams4, buttonText4, buttonCallback4);
                    panel1.addChild(button4);
                }
            }
        });
    }

    private Panel initHostUI() {
        Panel.PanelParams panelParams = new Panel.PanelParams();
        panelParams.x = 300f;
        panelParams.y = 120f;
        panelParams.conformToListSize = true;

        return new Panel(panelParams, new Panel.PanelMaker() {
            @Override
            public void make(Panel panel1) {
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

    private Panel initConnectionUI() {
        Panel.PanelParams panelParams = new Panel.PanelParams();
        panelParams.x = 300f;
        panelParams.y = 150f;
        panelParams.conformToListSize = true;

        return new Panel(panelParams, new Panel.PanelMaker() {
            @Override
            public void make(Panel panel1) {
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

                String s;
                try (JSONUtils.CommonDataJSONObject data = JSONUtils.loadCommonJSON("mp_cache")) {
                    s = data.getString("ip");
                } catch (Exception e) {
                    s = "";
                }
                TextEntryBox.TextEntryBoxParams textEntryBoxParams = new TextEntryBox.TextEntryBoxParams();
                textEntryBoxParams.height = 30f;
                textEntryBoxParams.width = 200f;
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
                        return "CONNECT";
                    }
                }, TODRAW24, buttonTextParams);
                Button.ButtonParams buttonParams = new Button.ButtonParams();
                buttonParams.width = 120f;
                buttonParams.height = 28f;
                Button.ButtonCallback buttonCallback = new Button.ButtonCallback() {
                    @Override
                    public void onClick() {
                        initClient(textEntryBox.getString(), text3);
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

    private Panel initShipSelectionUI() {
        Panel.PanelParams panelParams = new Panel.PanelParams();
        panelParams.x = 300f;
        panelParams.y = 150f;
        panelParams.conformToListSize = true;
        panelParams.update = true;
        return new Panel(panelParams, new Panel.PanelMaker() {
            @Override
            public void make(Panel panel) {
                Text.TextParams textParams = new Text.TextParams();
                Text text = new Text(new Execute<String>() {
                    @Override
                    public String get() {
                        return "SHIP SELECTION";
                    }
                }, TODRAW24, textParams);
                panel.addChild(text);

                MPPlugin plugin = (MPPlugin) Global.getCombatEngine().getCustomData().get(MPPlugin.DATA_KEY);
                final MPClientPlugin clientPlugin = (MPClientPlugin) plugin;

                for (final ShipAPI s : Global.getCombatEngine().getShips()) {
                    if (!s.isFighter() && s.isAlive()) {
                        Text.TextParams buttonTextParams1 = new Text.TextParams();
                        buttonTextParams1.align = LazyFont.TextAlignment.LEFT;

                        if (s.getOwner() == 0) buttonTextParams1.color = Color.GREEN;
                        else if (s.getOwner() == 1) buttonTextParams1.color = Color.RED;
                        else buttonTextParams1.color = Color.YELLOW;

                        Text buttonText1 = new Text(new Execute<String>() {
                            @Override
                            public String get() {
                                return s.getHullSpec().getNameWithDesignationWithDashClass();
                            }
                        }, TODRAW14, buttonTextParams1);

                        Button.ButtonParams buttonParams1 = new Button.ButtonParams();
                        buttonParams1.width = 280f;
                        buttonParams1.height = 26f;
                        Button.ButtonCallback buttonCallback1 = new Button.ButtonCallback() {
                            @Override
                            public void onClick() {
                                clientPlugin.getPlayerShip().setPlayerShipID(s.getFleetMemberId());
                            }
                        };
                        Button button1 = new Button(buttonParams1, buttonText1, buttonCallback1);
                        panel.addChild(button1);
                    }
                }
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

    private void initClient(String text, Text infoText) {
        if (text.trim().isEmpty()) {
            infoText.setExecute(new Execute<String>() {
                @Override
                public String get() {
                    return "Specify address";
                }
            });
            infoText.setColor(Color.RED);
            return;
        }

        String[] ids = text.split(" ");
        if (ids.length != 1) {
            infoText.setExecute(new Execute<String>() {
                @Override
                public String get() {
                    return "INVALID ADDRESS";
                }
            });
            infoText.setColor(Color.RED);
            return;
        }

        String[] address = ids[0].split(":");
        String host = address[0];
        if (address.length < 2) {
            infoText.setExecute(new Execute<String>() {
                @Override
                public String get() {
                    return "PORT NOT SPECIFIED";
                }
            });
            infoText.setColor(Color.RED);
            return;
        }
        int port = Integer.parseInt(address[1]);

        try (JSONUtils.CommonDataJSONObject data = JSONUtils.loadCommonJSON("mp_cache")) {
            data.put("ip", text);
            data.save();
        } catch (Exception ignored) {
        }

        infoText.setExecute(new Execute<String>() {
            @Override
            public String get() {
                return "CLIENT STARTED";
            }
        });
        infoText.setColor(Color.GREEN);
        MPModPlugin.setPlugin(new MPClientPlugin(host, port));
    }
}
