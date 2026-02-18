package nl.adgroot.pdfsummarizer.pdf;

public class PDFUtil {

  /**
   * Heuristically determines whether the given page content represents
   * a Table of Contents (TOC) page.
   *
   * <p>The method counts non-empty lines
   * and checks whether at least 90% of them end with a page number.
   *
   * @param page the textual content of a single PDF page
   * @return {@code true} if the page is likely a TOC page; {@code false} otherwise
   */
  public static boolean isTableOfContentsPage(String page) {
    if (page == null || page.isBlank()) {
      return false;
    }

    String[] lines = page.split("\\R"); // handles \n, \r\n, etc.

    int totalRelevantLines = 0;
    int tocMatches = 0;

    for (String rawLine : lines) {
      String line = rawLine.trim();

      // Skip empty lines
      if (line.isEmpty()) {
        continue;
      }

      totalRelevantLines++;

      // Regex: ends with page number
      if (line.matches(".*\\s+\\d+$")) {
        tocMatches++;
      }

      if (line.equalsIgnoreCase("table of contents")) {
        tocMatches++;
      }
    }

    if (totalRelevantLines == 0) {
      return false;
    }

    double ratio = (double) tocMatches / totalRelevantLines;

    return ratio >= 0.90;
  }
}
