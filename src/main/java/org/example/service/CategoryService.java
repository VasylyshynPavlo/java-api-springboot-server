package org.example.service;

import org.example.entities.CategoryEntity;
import org.example.repository.ICategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    //Автоматично робиться Dependency Injection -
    @Autowired
    private ICategoryRepository ICategoryRepository;

    public List<CategoryEntity> getAllCategories() {
        return ICategoryRepository.findAll();
    }
}
