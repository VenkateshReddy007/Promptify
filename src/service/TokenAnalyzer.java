import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TokenAnalyzer {

    public int estimateTokens(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            System.out.println("[TokenAnalyzer] Action: estimateTokens - prompt: empty, tokens: 0");
            return 0;
        }
        int tokens = prompt.trim().length() / 4;
        System.out.println("[TokenAnalyzer] Action: estimateTokens - length: " + prompt.length() + ", tokens: " + tokens);
        return tokens;
    }

    public String getComplexity(int tokens) {
        String complexity;
        if (tokens <= 50) {
            complexity = "LOW";
        } else if (tokens <= 150) {
            complexity = "MEDIUM";
        } else {
            complexity = "HIGH";
        }
        System.out.println("[TokenAnalyzer] Action: getComplexity - tokens: " + tokens + " -> " + complexity);
        return complexity;
    }

    public String getCostCategory(int tokens) {
        String category;
        if (tokens <= 50) {
            category = "Low";
        } else if (tokens <= 150) {
            category = "Medium";
        } else {
            category = "High";
        }
        System.out.println("[TokenAnalyzer] Action: getCostCategory - tokens: " + tokens + " -> " + category);
        return category;
    }

    public String getSuggestion(String prompt, int tokens) {
        System.out.println("[TokenAnalyzer] Action: getSuggestion - tokens: " + tokens);
        StringBuilder suggestion = new StringBuilder();
        if (tokens > 300) {
            suggestion.append("Prompt too expensive. Break it into smaller tasks. ");
        }
        if (tokens < 10) {
            suggestion.append("Prompt too vague. Add more context and details. ");
        }
        String[] words = prompt.toLowerCase().split("\\s+");
        Map<String, Integer> freq = new HashMap<>();
        for (String w : words) {
            freq.merge(w, 1, Integer::sum);
        }
        List<String> repeated = freq.entrySet().stream()
                .filter(e -> e.getValue() > 2 && e.getKey().length() > 3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        if (!repeated.isEmpty()) {
            suggestion.append("Remove redundant words: ").append(repeated).append(". ");
        }
        if (suggestion.isEmpty()) {
            suggestion.append("Prompt looks good.");
        }
        return suggestion.toString();
    }

    public boolean isWeakPrompt(String prompt, int tokens) {
        boolean weak = tokens < 10 || prompt.split("\\s+").length < 3;
        System.out.println("[TokenAnalyzer] Action: isWeakPrompt - weak: " + weak);
        return weak;
    }
}
