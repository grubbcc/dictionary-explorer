import java.util.ArrayList;
import java.util.StringJoiner;

/**
 *
 */
class Part extends DictionaryElement {


    //enum POS {adj, adv, article, aux, conj, inf, interj, n, npl, prep, pron, v}

    private final Entry entry;
    private final String grammar;
    private final String inflections;
    private final String plural;
    private final String singular;

    final ArrayList<Variant> variants = new ArrayList<>();

    /**
     *
     */
    Part(Entry entry, String type, String context, String language, String inflections, String plural, String singular, String grammar) {
        super(type, context, language);
        this.entry = entry;
        this.inflections = inflections;
        this.plural = plural;
        this.singular = singular;
        this.grammar = grammar;

        if (!isWordType() && !isNonwordType()) {
            //    throw new RuntimeException("Warning: type " + "\"" + type + "\" not recognized.");
        }
    }



    /**
     *
     */
    void addVariant(Variant var) {
        variants.add(var);
    }

    /**
     *
     */
    final String getType() {
        return type;
    }

    /**
     *
     */
    ArrayList<String> getScrabbleWords() {
        ArrayList<String> allWords = new ArrayList<>();

        if (isNonwordType() || isExpurgated()) return allWords;

        if (isEnglish()) {

            if (type.equals(POS.n.name())) {
                for (String pl : getPlurals(entry.content, plural, true)) {
                    if (matchesTileSet(pl)) {
                        allWords.add(pl);
                    }
                }
            } else if (type.equals(POS.v.name())) {
                for (String inf : getVerbInflections(entry.content, inflections, true)) {
                    if (matchesTileSet(inf)) {
                        allWords.add(inf);
                    }
                }
                //The present tense is identical to the plural(s) of the corresponding noun (if there is one);
                // otherwise infer as if the noun did exist.
                boolean hasNounForm = false;
                for(Part p : entry.getParts()) {
                    if(p.getType().equals("n")) {
                        hasNounForm = true;
                        for (String pl : p.getPlurals(entry.content, p.plural, true)) {
                            if(matchesTileSet(pl)) {
                                allWords.add(pl);
                            }
                        }
                        break;
                    }
                }
                if(!hasNounForm) {
                    for(String pl : getPlurals(entry.content, "", true)) {
                        if(matchesTileSet(pl)) {
                            allWords.add(pl);
                        }
                    }
                }
            } else if (type.equals(POS.npl.name())) {
                for (String sing : getSingular(entry.content, singular)) {
                    if (matchesTileSet(sing)) {
                        allWords.add(sing);
                    }
                }
            } else if(type.equals(POS.adj.name())) {
                for(String inf : getAdjectiveInflections(entry.content, inflections)) {
                    if (matchesTileSet(inf)) {
                        allWords.add(inf);
                    }
                }
            }
        }

        for (Variant var : variants) {
            allWords.addAll(var.getScrabbleWords());
        }


        return allWords;
    }



    /**
     *
     */
    @Override
    public String toString() {
        StringJoiner s = new StringJoiner(", ");
//        for (String pl : getPlurals(true))
//            s.add(pl);
//        for (String inf : getVerbInflections())
//            s.add(inf);
        return s.toString();
    }
}