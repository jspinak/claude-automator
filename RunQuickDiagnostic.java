import io.github.jspinak.brobot.tools.diagnostics.QuickMatchComparison;

public class RunQuickDiagnostic {
    public static void main(String[] args) {
        // Run the diagnostic with the claude-prompt-3 pattern
        String patternPath = "images/prompt/claude-prompt-3.png";
        System.out.println("Running diagnostic from wrapper class...");
        QuickMatchComparison.main(new String[] {patternPath});
    }
}