package com.epes.ui;

import com.epes.model.Evaluation;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Draws a line chart of the total scores (0 to 10) of a list of evaluations, oldest on the left. */
public class LineChartPanel extends JPanel {

    private static final DateTimeFormatter LABEL_FORMAT = DateTimeFormatter.ofPattern("dd MMM yy");
    private static final Color LINE_COLOR = new Color(30, 111, 168);
    private static final Color GRID_COLOR = new Color(225, 228, 232);

    private List<Evaluation> data = new ArrayList<>();

    public LineChartPanel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(640, 300));
    }

    /** Replaces the chart data (oldest evaluation first) and redraws. */
    public void setData(List<Evaluation> evaluations) {
        this.data = new ArrayList<>(evaluations);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int left = 50, right = 30, top = 45, bottom = 50;
        int plotW = getWidth() - left - right;
        int plotH = getHeight() - top - bottom;

        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.setColor(Color.DARK_GRAY);
        g2.drawString("Performance score over time (out of 10)", left, 25);

        // horizontal grid lines and the numbers 0, 2, 4, 6, 8, 10 on the left
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        for (int value = 0; value <= 10; value += 2) {
            int y = top + plotH - (int) Math.round(plotH * value / 10.0);
            g2.setColor(GRID_COLOR);
            g2.drawLine(left, y, left + plotW, y);
            g2.setColor(Color.GRAY);
            g2.drawString(String.valueOf(value), left - 25, y + 4);
        }

        if (data.isEmpty()) {
            g2.setColor(Color.GRAY);
            String text = "No evaluations yet";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, left + (plotW - fm.stringWidth(text)) / 2, top + plotH / 2);
            g2.dispose();
            return;
        }

        int n = data.size();
        int[] xs = new int[n];
        int[] ys = new int[n];
        for (int i = 0; i < n; i++) {
            xs[i] = n == 1 ? left + plotW / 2 : left + (int) Math.round(plotW * i / (double) (n - 1));
            ys[i] = top + plotH - (int) Math.round(plotH * data.get(i).getTotalScore() / 10.0);
        }

        g2.setColor(LINE_COLOR);
        g2.setStroke(new BasicStroke(2.5f));
        for (int i = 0; i < n - 1; i++) {
            g2.drawLine(xs[i], ys[i], xs[i + 1], ys[i + 1]);
        }

        g2.setStroke(new BasicStroke(1f));
        FontMetrics fm = g2.getFontMetrics();
        for (int i = 0; i < n; i++) {
            g2.setColor(Color.WHITE);
            g2.fillOval(xs[i] - 5, ys[i] - 5, 10, 10);
            g2.setColor(LINE_COLOR);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(xs[i] - 5, ys[i] - 5, 10, 10);

            String score = String.format("%.1f", data.get(i).getTotalScore());
            g2.setColor(Color.DARK_GRAY);
            g2.drawString(score, xs[i] - fm.stringWidth(score) / 2, ys[i] - 11);

            String date = data.get(i).getEvalDate().format(LABEL_FORMAT);
            g2.setColor(Color.GRAY);
            g2.drawString(date, xs[i] - fm.stringWidth(date) / 2, top + plotH + 20);
        }
        g2.dispose();
    }
}
