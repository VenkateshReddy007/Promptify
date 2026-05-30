public class Analysis {
    private int analysisId;
    private int promptId;
    private int tokenCount;
    private String complexity;
    private String recommendedModel;
    private String costCategory;
    private String suggestion;
    private String analyzedAt;

    public Analysis() {
    }

    public Analysis(int analysisId, int promptId, int tokenCount, String complexity,
                    String recommendedModel, String costCategory, String suggestion, String analyzedAt) {
        this.analysisId = analysisId;
        this.promptId = promptId;
        this.tokenCount = tokenCount;
        this.complexity = complexity;
        this.recommendedModel = recommendedModel;
        this.costCategory = costCategory;
        this.suggestion = suggestion;
        this.analyzedAt = analyzedAt;
    }

    public int getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(int analysisId) {
        this.analysisId = analysisId;
    }

    public int getPromptId() {
        return promptId;
    }

    public void setPromptId(int promptId) {
        this.promptId = promptId;
    }

    public int getTokenCount() {
        return tokenCount;
    }

    public void setTokenCount(int tokenCount) {
        this.tokenCount = tokenCount;
    }

    public String getComplexity() {
        return complexity;
    }

    public void setComplexity(String complexity) {
        this.complexity = complexity;
    }

    public String getRecommendedModel() {
        return recommendedModel;
    }

    public void setRecommendedModel(String recommendedModel) {
        this.recommendedModel = recommendedModel;
    }

    public String getCostCategory() {
        return costCategory;
    }

    public void setCostCategory(String costCategory) {
        this.costCategory = costCategory;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public String getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(String analyzedAt) {
        this.analyzedAt = analyzedAt;
    }
}
