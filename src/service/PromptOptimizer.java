public class PromptOptimizer {

    private final GroqClient groqClient = new GroqClient();

    public String optimizePrompt(String originalPrompt) {
        System.out.println("[PromptOptimizer] Action: optimizePrompt - original: " + originalPrompt);

        String instruction = "Improve the following prompt for an AI system. "
                + "Return ONLY the optimized prompt, nothing else, no explanation, no preamble.\n\n"
                + "Prompt: " + originalPrompt;

        String optimized = groqClient.generateContent(instruction);
        System.out.println("[PromptOptimizer] Action: optimizePrompt - optimized: " + optimized);
        return optimized;
    }
}
