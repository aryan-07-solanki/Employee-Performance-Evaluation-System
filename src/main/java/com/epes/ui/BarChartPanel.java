package com.epes.ui;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

/** Draws a simple vertical bar chart (values from 0 to maxValue) with a title and one label under each bar. */
public class BarChartPanel extends JPanel {

    private static final Color BAR_COLOR = new Color(30, 111, 168);
    private static final Color GRID_COLOR = new Color(225, 228, 232);

    private final String title;
    private final double maxValue;
    private List<String> labels = new ArrayList<>();
    private List<Double> values = new ArrayList<>();

    public BarChartPanel(String title, double maxValue) {
        this.title = title;
        this.maxValue = maxValue;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(420, 260));
    }

    /** Replaces the data (both lists must have the same size) and redraws. */
    public void setData(List<String> labels, List<Double> values) {
        this.labels = new ArrayList<>(labels);
        this.values = new ArrayList<>(values);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int left = 40, right = 20, top = 40, bottom = 40;
        int plotW = getWidth() - left - right;
        int plotH = getHeight() - top - bottom;

        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.setColor(Color.DARK_GRAY);
        g2.drawString(title, left, 24);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        for (int i = 0; i <= 5; i++) {
            double value = maxValue * i / 5;
            int y = top + plotH - (int) Math.round(plotH * i / 5.0);
            g2.setColor(GRID_COLOR);
            g2.drawLine(left, y, left + plotW, y);
            g2.setColor(Color.GRAY);
            g2.drawString(String.format("%.0f", value), left - 25, y + 4);
        }

        int n = values.size();
        if (n == 0) {
            g2.setColor(Color.GRAY);
            g2.drawString("No data yet", left + plotW / 2 - 30, top + plotH / 2);
            g2.dispose();
            return;
        }

        FontMetrics fm = g2.getFontMetrics();
        int slot = plotW / n;
        int barW = Math.min(60, slot * 2 / 3);
        for (int i = 0; i < n; i++) {
            int barH = (int) Math.round(plotH * Math.min(values.get(i), maxValue) / maxValue);
            int x = left + i * slot + (slot - barW) / 2;
            int y = top + plotH - barH;
            g2.setColor(BAR_COLOR);
            g2.fillRect(x, y, barW, barH);

            String value = String.format("%.1f", values.get(i));
            g2.setColor(Color.DARK_GRAY);
            g2.drawString(value, x + (barW - fm.stringWidth(value)) / 2, y - 5);

            String label = labels.get(i);
            while (label.length() > 3 && fm.stringWidth(label) > slot - 4) {   // shorten long names
                label = label.substring(0, label.length() - 2);
            }
            g2.setColor(Color.GRAY);
            g2.drawString(label, left + i * slot + (slot - fm.stringWidth(label)) / 2, top + plotH + 18);
        }
        g2.dispose();
    }
}
