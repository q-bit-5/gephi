package org.gephi.viz.engine.demo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Arrays;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.Timer;

/**
 * Floating HUD shown at the top-right of an owner {@link JFrame} that displays
 * a small sparkline of frame durations together with their {@code p50} and
 * {@code p99} percentiles, both in milliseconds, computed over the last
 * {@value #WINDOW_SECONDS_HUMAN} of rendering.
 *
 * <p>Frame samples are pushed by the rendering thread via {@link #recordFrame()}
 * (called once per OpenGL {@code display()}); the chart is repainted from the
 * Swing EDT at a fixed cadence. Reads/writes of the underlying ring buffer are
 * synchronized on this instance.</p>
 */
public final class RenderingMetricsHud {

    /** Length of the sliding window over which p50/p99 are computed. */
    private static final long WINDOW_NANOS = 10L * 1_000_000_000L;
    private static final String WINDOW_SECONDS_HUMAN = "10s";

    /**
     * Capacity of the ring buffer. Sized comfortably above what 10s of
     * extreme frame rates would produce (4096 ≈ 409 FPS sustained for 10s).
     */
    private static final int CAPACITY = 4_096;

    private static final int HUD_WIDTH = 220;
    private static final int HUD_HEIGHT = 90;
    private static final int HUD_MARGIN = 10;
    private static final int REPAINT_INTERVAL_MS = 50;

    /** 60 FPS reference line drawn on the chart. */
    private static final double TARGET_FRAME_MS = 1000.0 / 60.0;

    /** Per-sample wall-clock timestamps (ns), parallel to {@link #durationsNs}. */
    private final long[] timestampsNs = new long[CAPACITY];
    /** Per-sample frame durations (ns). */
    private final long[] durationsNs = new long[CAPACITY];
    /** Index of the oldest sample currently in the window. */
    private int head = 0;
    /** Index where the next sample will be written. */
    private int tail = 0;
    /** Number of samples currently in the window (0..CAPACITY). */
    private int size = 0;
    private long lastFrameNanos = 0L;

    private final JFrame anchorFrame;
    private final JWindow window;
    private final ChartPanel chartPanel;
    private final Timer repaintTimer;
    private final ComponentAdapter anchorListener;

    public RenderingMetricsHud(JFrame anchorFrame) {
        this.anchorFrame = anchorFrame;

        this.chartPanel = new ChartPanel();
        this.window = new JWindow(anchorFrame);
        this.window.setContentPane(chartPanel);
        this.window.setSize(HUD_WIDTH, HUD_HEIGHT);
        this.window.setFocusableWindowState(false);
        this.window.setAutoRequestFocus(false);
        this.window.setAlwaysOnTop(true);

        this.anchorListener = new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                repositionToTopRight();
            }

            @Override
            public void componentResized(ComponentEvent e) {
                repositionToTopRight();
            }
        };
        anchorFrame.addComponentListener(anchorListener);

        this.repaintTimer = new Timer(REPAINT_INTERVAL_MS, e -> chartPanel.repaint());
        this.repaintTimer.setCoalesce(true);
    }

    public void start() {
        repositionToTopRight();
        window.setVisible(true);
        repaintTimer.start();
    }

    public void dispose() {
        repaintTimer.stop();
        anchorFrame.removeComponentListener(anchorListener);
        window.dispose();
    }

    /**
     * Records the time elapsed since the last call. Safe to call from the
     * OpenGL rendering thread. Also evicts any entries older than the
     * configured window so subsequent reads see only the last
     * {@link #WINDOW_NANOS} of samples.
     */
    public synchronized void recordFrame() {
        final long now = System.nanoTime();
        if (lastFrameNanos != 0L) {
            if (size == CAPACITY) {
                // Defensive: overwrite the oldest entry. Should be rare given the capacity.
                head = (head + 1) % CAPACITY;
                size--;
            }
            timestampsNs[tail] = now;
            durationsNs[tail] = now - lastFrameNanos;
            tail = (tail + 1) % CAPACITY;
            size++;

            final long cutoff = now - WINDOW_NANOS;
            while (size > 0 && timestampsNs[head] < cutoff) {
                head = (head + 1) % CAPACITY;
                size--;
            }
        }
        lastFrameNanos = now;
    }

    /** Snapshot of the current window's durations, in insertion (i.e. time) order. */
    private synchronized long[] snapshotSamples() {
        final long[] out = new long[size];
        for (int i = 0; i < size; i++) {
            out[i] = durationsNs[(head + i) % CAPACITY];
        }
        return out;
    }

    private void repositionToTopRight() {
        final int x = anchorFrame.getX() + anchorFrame.getWidth()
            - HUD_WIDTH - HUD_MARGIN - anchorFrame.getInsets().right;
        final int y = anchorFrame.getY() + anchorFrame.getInsets().top + HUD_MARGIN;
        window.setLocation(x, y);
    }

    /**
     * Returns the value at the given percentile {@code p} (in {@code [0, 1]})
     * of an already-sorted-ascending array, expressed in milliseconds.
     * Uses nearest-rank ordering.
     */
    private static double percentileMs(long[] sortedNs, double p) {
        if (sortedNs.length == 0) {
            return 0.0;
        }
        final int rank = (int) Math.ceil(p * sortedNs.length);
        final int idx = Math.max(0, Math.min(sortedNs.length - 1, rank - 1));
        return sortedNs[idx] / 1_000_000.0;
    }

    private final class ChartPanel extends JPanel {

        private static final long serialVersionUID = 1L;

        ChartPanel() {
            setOpaque(true);
            setBackground(new Color(0, 0, 0, 220));
            setPreferredSize(new Dimension(HUD_WIDTH, HUD_HEIGHT));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            final Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                final int w = getWidth();
                final int h = getHeight();

                g2.setColor(new Color(255, 255, 255, 60));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);

                final long[] samples = snapshotSamples();

                g2.setFont(getFont().deriveFont(Font.BOLD, 11f));
                if (samples.length < 2) {
                    g2.setColor(Color.LIGHT_GRAY);
                    g2.drawString("Collecting frame samples...", 8, h / 2 + 4);
                    return;
                }

                final long[] sorted = samples.clone();
                Arrays.sort(sorted);
                final double p50 = percentileMs(sorted, 0.50);
                final double p99 = percentileMs(sorted, 0.99);

                final int chartX = 6;
                final int chartY = 22;
                final int chartW = w - 12;
                final int chartH = h - chartY - 6;

                final double maxMs = Math.max(p99 * 1.2, TARGET_FRAME_MS * 1.5);

                g2.setColor(Color.WHITE);
                g2.drawString(
                    String.format("p50 %5.2f ms   p99 %5.2f ms", p50, p99),
                    8, 14);

                final int y60 = chartY + chartH
                    - (int) Math.round(TARGET_FRAME_MS / maxMs * chartH);
                g2.setColor(new Color(0, 200, 120, 120));
                g2.drawLine(chartX, y60, chartX + chartW, y60);
                g2.setFont(getFont().deriveFont(Font.PLAIN, 9f));
                g2.drawString("60 FPS", chartX + chartW - 36, y60 - 2);

                drawSparkline(g2, samples, chartX, chartY, chartW, chartH, maxMs);
            } finally {
                g2.dispose();
            }
        }

        /**
         * Draws the sparkline by max-pooling samples per chart-pixel, so the
         * full window's samples (≈600 at 60 FPS over 10s) render cleanly on
         * a ~200 px wide chart and any spikes remain visible.
         */
        private void drawSparkline(Graphics2D g2, long[] samples,
                                   int chartX, int chartY, int chartW, int chartH,
                                   double maxMs) {
            if (chartW <= 0 || samples.length == 0) {
                return;
            }
            final int bins = chartW;
            final double[] binMaxMs = new double[bins];
            Arrays.fill(binMaxMs, Double.NaN);

            for (int i = 0; i < samples.length; i++) {
                int bin = (int) ((long) i * (bins - 1) / Math.max(1, samples.length - 1));
                if (bin < 0) {
                    bin = 0;
                } else if (bin >= bins) {
                    bin = bins - 1;
                }
                final double ms = samples[i] / 1_000_000.0;
                if (Double.isNaN(binMaxMs[bin]) || ms > binMaxMs[bin]) {
                    binMaxMs[bin] = ms;
                }
            }

            g2.setColor(new Color(80, 180, 255, 230));
            int prevX = -1;
            int prevY = -1;
            for (int b = 0; b < bins; b++) {
                if (Double.isNaN(binMaxMs[b])) {
                    continue;
                }
                final int x = chartX + b;
                final int y = chartY + chartH
                    - (int) Math.round(Math.min(binMaxMs[b], maxMs) / maxMs * chartH);
                if (prevX >= 0) {
                    g2.drawLine(prevX, prevY, x, y);
                }
                prevX = x;
                prevY = y;
            }
        }
    }
}
