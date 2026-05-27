package model;

import java.util.List;

public class CategoryUpdateRequest {

    private List<Long> categoryIds;

    public CategoryUpdateRequest(List<Long> categoryIds) {
        this.categoryIds = categoryIds;
    }

    public List<Long> getCategoryIds() { return categoryIds; }
}
