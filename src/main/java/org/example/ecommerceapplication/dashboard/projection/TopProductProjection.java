package org.example.ecommerceapplication.dashboard.projection;

public interface TopProductProjection {

    Long getProductId();

    String getProductName();

    String getImage();

    long getUnitsSold();
}
