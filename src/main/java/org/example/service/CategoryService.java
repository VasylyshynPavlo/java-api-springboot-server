package org.example.service;

import lombok.AllArgsConstructor;
import org.example.dto.category.CategoryItemDTO;
import org.example.dto.category.CategoryPostDto;
import org.example.entities.CategoryEntity;
import org.example.mapper.CategoryMapper;
import org.example.repository.ICategoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class CategoryService {
    private ICategoryRepository categoryRepository;
    private FileService fileService;
    private CategoryMapper CategoryMapper;

    public List<CategoryItemDTO> getAllCategories() {
        return CategoryMapper.toDto(categoryRepository.findAll());
    }

    public CategoryItemDTO getCategoryById(Integer id) {
        return CategoryMapper.toDto(categoryRepository.findById(id).get());
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
        }
        return categoryRepository.save(entity);
    }

    public boolean updateCategory(Integer id, CategoryPostDto category) {
        var res = categoryRepository.findById(id);
        if (res.isEmpty()){
            return false;
        }
        var entity = res.get();
        entity.setName(category.getName());
        entity.setDescription(category.getDescription());

        var newImageFile = category.getImageFile();
        if (newImageFile!=null && !newImageFile.isEmpty()){
            var newImagePath = fileService.replace(entity.getImage(), category.getImageFile());
            entity.setImage(newImagePath);
        }
        categoryRepository.save(entity);
        return true;
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
