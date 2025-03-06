package org.example.service;

import lombok.AllArgsConstructor;
import org.example.entities.CategoryEntity;
import org.example.mapper.CategoryMapper;
import org.example.repository.ICategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.example.dto.category.*;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class CategoryService {
    private ICategoryRepository categoryRepository;
    private FileService fileService;
    private CategoryMapper categoryMapper;

    public List<CategoryItemDTO> getAllCategories() {
        return categoryMapper.toDto(categoryRepository.findAll());
    }

    public CategoryItemDTO getCategoryById(Integer id) {
        return categoryMapper.toDto(categoryRepository.findById(id).get());
    }
    public CategoryEntity createCategory(CategoryPostDto category) {
        var entity = new CategoryEntity();
        entity.setName(category.getName());
        entity.setDescription(category.getDescription());
        entity.setCreationTime(LocalDateTime.now());
        var newImageFile = category.getImageFile();
        if (newImageFile!=null && !newImageFile.isEmpty()){
            var imagePath = fileService.load(newImageFile);
            entity.setImage(imagePath);
            System.out.println("Received file: " + newImageFile.getOriginalFilename());
        }
        return categoryRepository.save(entity);
    }

    public boolean deleteCategory(Integer id) {
        var res = categoryRepository.findById(id);
        if (res.isEmpty()){
            return false;
        }
        fileService.remove(res.get().getImage());
        categoryRepository.deleteById(id);
        return true;
    }
}
