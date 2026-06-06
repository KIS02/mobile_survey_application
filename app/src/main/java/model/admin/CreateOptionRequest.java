package model.admin;

public class CreateOptionRequest {
    private String optionText;
    private short optionOrder;

    public CreateOptionRequest(String optionText, short optionOrder) {
        this.optionText = optionText;
        this.optionOrder = optionOrder;
    }

    public String getOptionText() { return optionText; }
    public short getOptionOrder() { return optionOrder; }
}
