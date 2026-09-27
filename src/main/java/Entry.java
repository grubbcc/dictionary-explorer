import java.util.ArrayList;
import java.util.StringJoiner;

class Entry extends DictionaryElement {

    private final ArrayList<Part> parts = new ArrayList<>();
    private final ArrayList<Variant> variants = new ArrayList<>();
    private final ArrayList<Runon> runons = new ArrayList<>();

    private final String headword;
    final String content;
    private final String pronunciation;
    private final String comment;

    /**
     *
     */
    Entry(String headword, String context, String lang, String pronunciation, String comment) {
        super(null, context, lang);
        this.headword = headword.trim();
        this.content = headword.replaceAll("[" + XMLParser.controlChars + "]", "").trim();
        this.pronunciation = pronunciation;
        this.comment = comment;
    }

    /**
     *
     */
    ArrayList<String> getScrabbleWords(boolean infer) {
        ArrayList<String> allWords = new ArrayList<>();
        if (isExpurgated()) return allWords;

        if (matchesTileSet(content) && isEnglish()) {
            for (Part p : parts) {
                if (p.isWordType()) {
                    allWords.add(content);
                    break;
                }
            }
        }
        if (isEnglish()) {
            for (Part p : parts) {
                allWords.addAll(p.getScrabbleWords(infer));
            }

            for (Runon r : runons) {
                allWords.addAll(r.getScrabbleWords(infer));
            }
        }
        return allWords;
    }

    /**
     *
     */
    void addPart(Part part) {
        parts.add(part);
    }

    /**
     *
     */
    void addRunon(Runon runon) {
        runons.add(runon);
    }

    /**
     *
     */
    ArrayList<Part> getParts() {
        return parts;
    }

    /**
     *
     */
    @Override
    public String toString() {
        StringJoiner s = new StringJoiner(", ");
        s.add(content);
        for (Part p : parts)
            s.add(p.toString());
        return s.toString();
    }


}
