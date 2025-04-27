package org.example.dto.product;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class ProductPostDto {
    private String name;
    private String description;
    private float price;
    private Integer amount;
    private Integer categoryId;
    private List<MultipartFile> imageFiles;
}
