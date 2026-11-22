package com.epes.model;

/**
 * The five predefined criteria a manager scores (1 to 10) during an evaluation.
 * Each value knows its database column name and the label shown on screen.
 */
public enum Criteria {
    QUALITY("quality", "Quality"),
    TEAMWORK("teamwork", "Teamwork"),
    PUNCTUALITY("punctuality", "Punctuality"),
    PRODUCTIVITY("productivity", "Productivity"),
    COMMUNICATION("communication", "Communication");

    private final String column;
    private final String label;

    Criteria(String column, String label) {
        this.column = column;
        this.label = label;
    }

    /** @return the column name in the "evaluations" table */
    public String getColumn() { return column; }

    /** @return the text shown on the screen */
    public String getLabel() { return label; }
}
