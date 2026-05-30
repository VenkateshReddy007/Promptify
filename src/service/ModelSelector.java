public class ModelSelector {

    public String recommendModel(int tokens) {
        String model;
        if (tokens <= 50) {
            model = "Gemini Flash";
        } else if (tokens <= 150) {
            model = "Gemini Pro";
        } else {
            model = "Gemini Advanced";
        }
        System.out.println("[ModelSelector] Action: recommendModel - tokens: " + tokens + " -> recommended: " + model);
        return model;
    }
}
