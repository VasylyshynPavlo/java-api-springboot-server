package org.example.dto.product;

import lombok.Data;

import java.util.List;

@Data
public class ProductItemDto {
    private Integer id;
    private String name;
    private String description;
    private String dateCreated;
    private float price;
    private Integer amount;
    private Integer categoryId;
    private String categoryName;
    private List<String> images;
}
