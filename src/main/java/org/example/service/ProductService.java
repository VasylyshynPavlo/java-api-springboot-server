package org.example.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.example.dto.product.ProductItemDto;
import org.example.dto.product.ProductPostDto;
import org.example.entities.CategoryEntity;
import org.example.entities.ProductEntity;
import org.example.entities.ProductImageEntity;
import org.example.mapper.ProductMapper;
import org.example.repository.ICategoryRepository;
import org.example.repository.IProductImageRepository;
import org.example.repository.IProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProductService {
    private FileService fileService;
    private ProductMapper productMapper;
    private IProductRepository productRepository;
    private ICategoryRepository categoryRepository;
    private IProductImageRepository productImageRepository;

    public List<ProductItemDto> getAllProducts() {
        var entities = productRepository.findAll();
        return productMapper.toDto(entities);
    }

    public ProductItemDto getProductById(Integer id) {
        var res = productRepository.findById(id);
        return res.isPresent()
                ? productMapper.toDto(res.get())
                : null;
    }

    public ProductItemDto createProduct(ProductPostDto product) {
        var entity = new ProductEntity();
        entity.setName(product.getName());
        entity.setDescription(product.getDescription());
        entity.setCreationTime(LocalDateTime.now());
        entity.setAmount(product.getAmount());
        entity.setPrice(product.getPrice());

        var categoryId = product.getCategoryId();
        if (categoryRepository.existsById(categoryId)){
            var category = new CategoryEntity();
            category.setId(categoryId);
            entity.setCategory(category);
        }
        productRepository.save(entity);

        var imageFiles = product.getImageFiles();
        if (imageFiles != null) {
            var priority = 1;
            for (var file : imageFiles) {
                if (file == null || file.isEmpty()) continue;
                var imageName = fileService.load(file);
                var img = new ProductImageEntity();
                img.setPriority(priority++);
                img.setName(imageName);
                img.setProduct(entity);
                productImageRepository.save(img);
            }
        }
        return productMapper.toDto(entity);
    }

    public boolean updateProduct(Integer id, ProductPostDto product) {
        var res = productRepository.findById(id);
        if (res.isEmpty()) {
            return false;
        }

        var entity = res.get();
        entity.setName(product.getName());
        entity.setDescription(product.getDescription());
        entity.setAmount(product.getAmount());
        entity.setPrice(product.getPrice());

        var newCategoryId = product.getCategoryId();
        if (!Objects.equals(newCategoryId, entity.getCategory().getId()) && categoryRepository.existsById(newCategoryId)) {
            var category = new CategoryEntity();
            category.setId(newCategoryId);
            entity.setCategory(category);
        }

        productRepository.save(entity);

        if(product.getImageFiles() == null) {
            List<ProductImageEntity> productImageEntities = entity.getImages();
            Set<String> currentImageNames = productImageEntities != null
                    ? productImageEntities.stream().map(ProductImageEntity::getName).collect(Collectors.toSet())
                    : Collections.emptySet();
            for (var imageName : currentImageNames)
            {
                fileService.remove(imageName);
                productImageRepository.deleteByName(imageName);
            }
            return true;
        }

        List<String> oldImagesName = product.getImageFiles().stream()
                .filter(item -> Objects.equals(item.getContentType(), "old-image"))
                .map(MultipartFile::getOriginalFilename)
                .toList();

        List<ProductImageEntity> productImageEntities = entity.getImages();
        Set<String> currentImageNames = productImageEntities != null
                ? productImageEntities.stream().map(ProductImageEntity::getName).collect(Collectors.toSet())
                : Collections.emptySet();

        List<String> imagesToRemove = currentImageNames.stream()
                .filter(imageName -> !oldImagesName.contains(imageName))
                .toList();

        int priority = 1;
        for (var item : product.getImageFiles()) {
            String imageName = item.getOriginalFilename();
            if (Objects.equals(item.getContentType(), "old-image")) {
                productImageRepository.updatePriorityByName(imageName, priority);
            } else {
                var newImageName = fileService.load(item);
                var img = new ProductImageEntity();
                img.setPriority(priority);
                img.setName(newImageName);
                img.setProduct(entity);
                productImageRepository.save(img);
            }
            priority++;
        }

        for (String imageToRemove : imagesToRemove) {
            fileService.remove(imageToRemove);
            productImageRepository.deleteByName(imageToRemove);
        }

        return true;
    }


    public boolean deleteProduct(Integer id) {
        var res = productRepository.findById(id);
        if (res.isEmpty()) {
            return false;
        }
        var entity = res.get();

        var productImageEntities = entity.getImages();

        if (productImageEntities != null) {
            for (var productImage : productImageEntities) {
                fileService.remove(productImage.getName());
                productImageRepository.deleteByName(productImage.getName());
            }
        }

        System.out.println("Product to delete: " + id);

        productRepository.deleteById(id);
        return true;
    }

}