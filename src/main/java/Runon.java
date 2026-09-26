import java.util.ArrayList;

public class Runon extends DictionaryElement {

    private final String content;

    private final String grammar;
    private final String pronunciation;
    private final String plurals;
    private final String inflections;

    Runon(String content, String type, String context, String plurals, String inflections, String language, String grammar, String pronunciation) {
        super(type, context, language);
        this.content = content.replaceAll("[" + XMLParser.controlChars + "]", "");
        this.plurals = plurals;
        this.grammar = grammar;
        this.inflections = inflections;
        this.pronunciation = pronunciation;

        if (!isWordType() && !isNonwordType()) {
            //throw new RuntimeException("Runon " + content + " has unrecognized type: " + type);
        }
    }


    /**
     *
     */
    ArrayList<String> getScrabbleWords() {
        ArrayList<String> allWords = new ArrayList<>();

        if (isExpurgated()) return allWords;

        if (matchesTileSet(content)) {
            allWords.add(content);
        }

        if (type.equals(POS.n.name())) {
            for (String pl : getPlurals(content, plurals, true)) {
                if (matchesTileSet(pl)) {
                    allWords.add(pl);
                }
            }
        } else if (type.equals(POS.v.name())) {
            for (String inf : getVerbInflections(content, inflections, true)) {
                if (matchesTileSet(inf)) {
                    allWords.add(inf);
                }
            }
            for(String pl : getPlurals(content, "", true)) {
                if (matchesTileSet(pl)) {
                    allWords.add(pl);
                }
            }
        }

        return allWords;
    }

}
