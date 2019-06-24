package org.maddev.paint;

import org.maddev.Config;
import org.maddev.Main;
import org.maddev.Store;
import org.maddev.helpers.bank.BankCache;
import org.maddev.helpers.walking.CustomWalker;
import org.maddev.helpers.walking.MovementHelper;
import org.maddev.tasks.GrandExchange;
import org.maddev.tasks.zanaris.Zanaris;
import org.maddev.ws.WebSocket;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.event.listeners.RenderListener;
import org.rspeer.runetek.event.types.RenderEvent;
import org.rspeer.script.ScriptMeta;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ScriptPaint implements RenderListener {

    private static final int BASE_X = 6;
    private static final int BASE_Y = 6;

    private static final int DEFAULT_WIDTH_INCR = 20;

    private static final int BASE_HEIGHT = 20;
    private static final int LINE_HEIGHT = 20;

    private static final Color FOREGROUND = Color.WHITE;
    private static final Color BACKGROUND = Color.BLACK;
    private static final Stroke STROKE = new BasicStroke(1.8f);

    private final Map<String, PaintStatistic> stats;

    private Color outline;

    public ScriptPaint(Main context) {
        stats = new LinkedHashMap<>();
        outline = new Color(240, 0, 73);
        ScriptMeta meta = context.getMeta();
        stats.put(meta.name(), new PaintStatistic(true, () -> "v" + meta.version() + " by " + meta.developer()));
        stats.put("Runtime", new PaintStatistic(() -> context.getRuntime().toElapsedString()));
        stats.put("Status", new PaintStatistic(() -> Store.getTask() == null ? "None" : Store.getTask()));
        stats.put("Action", new PaintStatistic(() -> Store.getAction() == null ? "None" : Store.getAction()));
        stats.put("Should Walk", new PaintStatistic(() -> String.valueOf(MovementHelper.getInstance().isShouldWalk())));
        stats.put("Destination", new PaintStatistic(() -> {
            if(!Movement.isDestinationSet()) {
                return "None";
            }
            Position p = Movement.getDestination();
            return p.getX() + ", " + p.getY() + ", " + p.getFloorLevel();
        }));
        stats.put("WS", new PaintStatistic(() -> WebSocket.getInstance().isConnected() ? "Connected." : "Not Connected."));
        stats.put("Cache Last Update", new PaintStatistic(() -> BankCache.getLastUpdate() + " seconds ago"));
        stats.put("Essence Quantity", new PaintStatistic(() -> String.valueOf(GrandExchange.getEssenceQuantity())));
        stats.put("Eclectic Quantity", new PaintStatistic(() -> String.valueOf(GrandExchange.getEclecticQuantity())));
        stats.put("Nature Quantity", new PaintStatistic(() -> String.valueOf(GrandExchange.getNatureQuantity())));
        stats.put("Purchaser Active", new PaintStatistic(() -> GrandExchange.isPurchaserActive() ? "Yes" : "No"));
        stats.put("Total Mules", new PaintStatistic(() -> String.valueOf(Config.MULE_NAMES.size())));
        stats.put("Mule Time", new PaintStatistic(() -> String.valueOf(Config.MULE_WHEN_TIME)));
        stats.put("Mule When Gold", new PaintStatistic(() -> String.valueOf(Config.MULE_WHEN_GOLD)));
    }

    public Color getOutline() {
        return outline;
    }

    public void setOutline(Color outline) {
        this.outline = outline;
    }

    public void submit(String key, PaintStatistic tracker) {
        stats.put(key, tracker);
    }

    @Override
    public void notify(RenderEvent e) {
        Graphics2D g = (Graphics2D) e.getSource();
        Composite defaultComposite = g.getComposite();

        int width = 180;
        int currentX = BASE_X + (DEFAULT_WIDTH_INCR / 2);
        int currentY = BASE_Y + (LINE_HEIGHT / 2);

        g.setStroke(STROKE);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(FOREGROUND);

        for (Map.Entry<String, PaintStatistic> entry : stats.entrySet()) {
            PaintStatistic stat = entry.getValue();
            String string = entry.getKey() + (stat.isHeading() ? " - " : ": ") + stat.toString();
            int currentWidth = g.getFontMetrics().stringWidth(string);
            if (currentWidth > width) {
                width = currentWidth;
            }
        }

        g.setComposite(AlphaComposite.SrcOver.derive(0.5f));
        g.setColor(BACKGROUND);
        g.fillRoundRect(BASE_X, BASE_Y, width + DEFAULT_WIDTH_INCR, (stats.size() * LINE_HEIGHT) + BASE_HEIGHT, 7, 7);

        g.setComposite(defaultComposite);
        g.setColor(outline);
        g.drawRoundRect(BASE_X, BASE_Y, width + DEFAULT_WIDTH_INCR, (stats.size() * LINE_HEIGHT) + BASE_HEIGHT, 7, 7);

        g.setColor(FOREGROUND);
        for (Map.Entry<String, PaintStatistic> entry : stats.entrySet()) {
            PaintStatistic stat = entry.getValue();

            String string = entry.getKey() + (stat.isHeading() ? " - " : ": ") + stat.toString();
            int drawX = currentX;
            if (stat.isHeading()) {
                drawX = BASE_X + ((width + DEFAULT_WIDTH_INCR) - g.getFontMetrics().stringWidth(string)) / 2;
                g.setColor(outline);
                g.drawRect(BASE_X, currentY + (LINE_HEIGHT / 2) - BASE_Y + 1, width + DEFAULT_WIDTH_INCR, LINE_HEIGHT);

                g.setComposite(AlphaComposite.SrcOver.derive(0.1f));
                g.fillRect(BASE_X, currentY + (LINE_HEIGHT / 2) - BASE_Y + 1, width + DEFAULT_WIDTH_INCR, LINE_HEIGHT);
                g.setComposite(defaultComposite);

                g.setFont(g.getFont().deriveFont(Font.BOLD));
            } else {
                g.setFont(g.getFont().deriveFont(Font.PLAIN));
            }

            g.setColor(FOREGROUND);
            g.drawString(string, drawX, currentY += LINE_HEIGHT);
        }
    }
}
