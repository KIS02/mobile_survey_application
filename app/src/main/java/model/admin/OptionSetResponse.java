package model.admin;

import java.util.List;

public class OptionSetResponse {
    private Long id;
    private String name;
    private List<ItemResponse> items;

    public Long getId() { return id; }
    public String getName() { return name; }
    public List<ItemResponse> getItems() { return items; }

    public static class ItemResponse {
        private Long id;
        private String optionText;
        private Short optionOrder;

        public Long getId() { return id; }
        public String getOptionText() { return optionText; }
        public Short getOptionOrder() { return optionOrder; }
    }
}
