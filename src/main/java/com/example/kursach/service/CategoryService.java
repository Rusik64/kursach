package com.example.kursach.service;

import com.example.kursach.repository.CategoryRepository;
import com.example.kursach.repository.model.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository repo;

    public List<Category> getAll() {
        return repo.findAll();
    }

    public Category getCategoryById(Long id) {
        return repo.findById(id);
    }

    public void createCategory(String name) {
        if (repo.existsByName(name)) {
            throw new RuntimeException("Категория с таким именем уже существует");
        }

        Category category = new Category();
        category.setName(name);
        repo.save(category);
    }

    public void updateCategory(Long id, String name) {
        Category category = repo.findById(id);
        category.setName(name);
        repo.update(category);
    }

    public void deleteCategory(Long id) {
        if (repo.hasProducts(id)) {
            throw new RuntimeException("Нельзя удалить категорию, в которой есть товары");
        }
        repo.delete(id);
    }
}