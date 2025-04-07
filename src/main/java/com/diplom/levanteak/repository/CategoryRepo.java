package com.diplom.levanteak.repository;

import com.diplom.levanteak.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepo extends JpaRepository<Category, Long> {
}
