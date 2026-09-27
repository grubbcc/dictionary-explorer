import java.util.ArrayList;

class Variant extends DictionaryElement {

    private final String content;
    private final String plural;
    private final String grammar;
    private final String inflections;
    private final String pronunciation;


    /**
     *
     */
    Variant(String type, String content, String context, String inflections, String language, String plural, String grammar, String pronunciation) {
        super(type, context, language);
        this.content = content.replaceAll("[" + XMLParser.controlChars + "]", "");
        this.plural = plural;
        this.grammar = grammar;
        this.inflections = inflections;
        this.pronunciation = pronunciation;

        if (!isWordType() && !isNonwordType()) {
            //throw new RuntimeException("Variant " + content + " has unrecognized type: " + type);
        }
    }



    /**
     *
     */
    ArrayList<String> getScrabbleWords(boolean infer) {
        ArrayList<String> allWords = new ArrayList<>();

        if(isExpurgated()) return allWords;

        if (isEnglish()) {
            if (matchesTileSet(content)) {
                allWords.add(content);
            }

            if (type.equals(POS.n.name())) {
                for (String pl : getPlurals(content, plural, infer)) {
                    if (matchesTileSet(pl)) {
                        allWords.add(pl);
                    }
                }
            }
            else if(type.equals(POS.v.name())) {
                for (String inf : getVerbInflections(content, inflections,infer)) {
                    if (matchesTileSet(inf)) {
                        allWords.add(inf);
                    }
                }
                for(String pl : getPlurals(content, plural, infer)) {
                    if (matchesTileSet(pl)) {
                        allWords.add(pl);
                    }
                }
            }
        }
        return allWords;
    }


}
