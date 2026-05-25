package model;

import java.util.List;

public class UserCategoryUpdateRequest {

    private List<Long> categoryIds;

    public UserCategoryUpdateRequest(List<Long> categoryIds) {
        this.categoryIds = categoryIds;
    }

    public List<Long> getCategoryIds() {
        return categoryIds;
    }
}
