package com.emmano.inventory_api.category;

import org.springframework.transaction.annotation.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@SpringBootTest
@Transactional
public class CategoryRepositoryTest {
    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void shouldSaveAndFindCategory(){
        String uniqueName = "Electronics-" + UUID.randomUUID();
        Category category = new Category(uniqueName,
                "Electronic Products and Accessory");
        Category savedCategory = categoryRepository.saveAndFlush(category);
        Optional<Category> result = categoryRepository.findByNameIgnoreCase(uniqueName.toUpperCase(Locale.ROOT));

        Assertions.assertThat(savedCategory.getId()).isNotNull();
        Assertions.assertThat(result).isPresent();
        Assertions.assertThat(result.get().getName()).isEqualTo(uniqueName);
        Assertions.assertThat(result.get().getDescription()).isEqualTo("Electronic Products and Accessory");
        Assertions.assertThat(result.get().getCreatedAt()).isNotNull();
    }

}
