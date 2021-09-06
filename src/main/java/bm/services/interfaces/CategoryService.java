package bm.services.interfaces;

import bm.DTO.Category;
import bm.DTO.Post;

import java.util.List;

public interface CategoryService {
    public Category addCategory(Category category);

    public Category updateCategory(Category category);

    public List<Category> listAllCategories(int offset, int limit);

    public void deleteCategory(long categoryId);

    public Category findCategoryByPostId(long postId);
}
