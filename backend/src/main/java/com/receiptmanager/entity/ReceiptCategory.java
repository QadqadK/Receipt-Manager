package com.receiptmanager.entity;

public enum ReceiptCategory {
    MEDICAL_EXPENSES("Medical Expenses"),
    FOOD_AND_GROCERIES("Food & Groceries"),
    ELECTRONICS("Electronics"),
    OFFICE_SUPPLIES("Office Supplies"),
    TRAVEL_AND_TRANSPORTATION("Travel & Transportation"),
    UTILITIES("Utilities"),
    RENT_HOUSING("Rent/Housing"),
    CLOTHING("Clothing"),
    EDUCATION("Education"),
    CHARITABLE_DONATIONS("Charitable Donations"),
    INSURANCE("Insurance"),
    PROFESSIONAL_SERVICES("Professional Services"),
    OTHER("Other");

    private final String label;

    ReceiptCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
