public class Prompt {
    private int promptId;
    private String originalPrompt;
    private String createdAt;

    public Prompt() {
    }

    public Prompt(int promptId, String originalPrompt, String createdAt) {
        this.promptId = promptId;
        this.originalPrompt = originalPrompt;
        this.createdAt = createdAt;
    }

    public int getPromptId() {
        return promptId;
    }

    public void setPromptId(int promptId) {
        this.promptId = promptId;
    }

    public String getOriginalPrompt() {
        return originalPrompt;
    }

    public void setOriginalPrompt(String originalPrompt) {
        this.originalPrompt = originalPrompt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
