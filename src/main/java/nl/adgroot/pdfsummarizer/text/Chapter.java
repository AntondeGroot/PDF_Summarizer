package nl.adgroot.pdfsummarizer.text;

public class Chapter {
  public String title;
  public int start;
  public int end;

  public Chapter(String title, int start, int end){
    this.title = title;
    this.start = start;
    this.end = end;
  }

  public Chapter(String title){
    this.title = title;
  }

  @Override
  public String toString() {
    return title+": "+start+"-"+end;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Chapter c)) return false;
    return start == c.start && end == c.end && java.util.Objects.equals(title, c.title);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(title, start, end);
  }
}
