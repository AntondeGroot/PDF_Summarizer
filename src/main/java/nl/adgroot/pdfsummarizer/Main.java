package nl.adgroot.pdfsummarizer;

import static nl.adgroot.pdfsummarizer.pdf.PDFUtil.isTableOfContentsPage;
import static nl.adgroot.pdfsummarizer.text.TableOfContentConverter.convert;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import nl.adgroot.pdfsummarizer.config.AppConfig;
import nl.adgroot.pdfsummarizer.config.ConfigLoader;
import nl.adgroot.pdfsummarizer.llm.OllamaClient;
import nl.adgroot.pdfsummarizer.notes.Card;
import nl.adgroot.pdfsummarizer.notes.CardParser;
import nl.adgroot.pdfsummarizer.pdf.PdfBoxTextExtractor;
import nl.adgroot.pdfsummarizer.prompts.PromptTemplate;
import nl.adgroot.pdfsummarizer.text.Chapter;

public class Main {

  public static void main(String[] args) throws Exception {
//    if (args.length < 2) {
//      System.out.println("Usage: java -jar app.jar <input.pdf> <obsidianVaultFolder>");
//      System.exit(1);
//    }

//    Path pdfPath = Paths.get(args[0]);

    Path pdfPath = Paths.get(Main.class.getClassLoader().getResource("Learning Docker.pdf").toURI());
//    Path vaultFolder = Paths.get(args[1]);

    Path configPath = Paths.get(Main.class.getClassLoader().getResource("config.json").toURI());
    AppConfig cfg = ConfigLoader.load(configPath);

    PdfBoxTextExtractor extractor = new PdfBoxTextExtractor();
    OllamaClient llm = new OllamaClient(cfg.ollama);
    CardParser parser = new CardParser();
//    ObsidianNoteWriter writer = new ObsidianNoteWriter();

    String topic = filenameToTopic(pdfPath.getFileName().toString());
//    Path outDir = vaultFolder.resolve(ObsidianNoteWriter.safeFileName(topic) + " - Cards");
//    Files.createDirectories(outDir);

    List<String> pages = extractor.extractPages(pdfPath);
    int TOC_begin = 0;
    int TOC_end = 0;
    for (int i = 0; i < 10; i++) {
      if(pages.get(i).toLowerCase().contains("table of contents")){
        TOC_begin = i;
        break;
      }
    }
    for (int i = TOC_begin+1; i < pages.size(); i++) {
      Boolean isTOC = isTableOfContentsPage(pages.get(i));
      System.out.println("page: "+i+": "+pages.get(i));
      System.out.println("repsonse:"+isTOC);
      if(!isTOC){
        TOC_end = i - 1;
        break;
      }
    }

    StringBuilder TOC = new StringBuilder();
    for (int i = TOC_begin; i <= TOC_end; i++) {
      TOC.append(pages.get(i));
    }

    List<Chapter> toc = convert(TOC.toString(),150);
    System.out.println(toc);

    PromptTemplate promptTemplate = PromptTemplate.load(Paths.get(Main.class.getClassLoader().getResource("prompt.txt").toURI()));
    String prompt = promptTemplate.render(Map.of(
        "topic", topic,
        "topicTag", topic.toLowerCase().replace(" ", "-"),
        "section", "test",
        "chunkIndex", "1",
        "chunkCount", "500",
        "created", LocalDate.now().toString(),
        "maxCards", String.valueOf(cfg.cards.maxCardsPerChunk),
        "content", pages.get(100)
    ));

    System.out.println("prompt: "+prompt);


//    String text = TextNormalizer.normalize(raw);

//    List<String> chunks = chunker.chunk(text);

//    for (int i = 0; i < chunks.size(); i++) {

      String md = llm.generate(prompt);

    System.out.println(md);
    List<Card> cards = parser.parse(md);
      for (Card c : cards) {
        System.out.println("card : "+c.markdown);
        System.out.println("nr cards :"+cards.size());
//        writer.writeCard(outDir, c);
      }
//
//      System.out.printf("Chunk %d/%d -> %d cards%n", i + 1, chunks.size(), cards.size());
//
//
//    System.out.println("Done. Notes written to: " + outDir.toAbsolutePath());
  }

  private static String filenameToTopic(String filename) {
    String noExt = filename.replaceAll("(?i)\\.pdf$", "");
    return noExt.replace('_', ' ').replace('-', ' ').trim();
  }
}
