import java.util.ArrayList;

abstract class DictionaryElement {

    final protected String type;
    final protected String context;
    final protected String language;

    /**
     *
     */
    DictionaryElement(String type, String context, String language) {
        this.type = type;
        this.context = context;
        this.language = language;
    }

    /**
     *
     */
    final protected boolean isWordType() {
        for (POS p : POS.values())
            if (p.name().equals(type)) return true;
        return false;
    }

    /**
     *
     */
    final protected boolean isNonwordType() {
        for (NONWORD n : NONWORD.values())
            if (n.name().equals(type)) return true;
        return false;
    }

    /**
     *
     */
    final protected ArrayList<String> getPlurals(String head, String plural, boolean infer) {
        ArrayList<String> plurals = new ArrayList<>();
        if (plural == null && infer) {
            if (head.matches(".*s|.*x|.+z|.+j|.+sh|.+ch")) {
                plurals.add(head + "es");
            } else if (head.matches(".*[bcdfghjklmnpqrstvxz]y"))
                plurals.add(head.substring(0, head.length() - 1) + "ies");
            else
                plurals.add(head + "s");
        } else if(plural != null && !plural.isBlank()) {
            for (String p : plural.split(",")) {
                if(!matchesTileSet(p.trim())) continue;
                if (p.trim().startsWith("·")) {
                    p = p.replaceAll("·", "").trim();
                    plurals.add(head.substring(0, head.lastIndexOf(p.charAt(0))) + p);
                } else {
                    plurals.add(p.replaceAll("·", "").trim());
                }
            }
        }
        return plurals;
    }

    /**
     *
     */
    final protected ArrayList<String> getSingular(String head, String singular) {
        ArrayList<String> plurals = new ArrayList<>();

        for (String p : singular.split(",")) {
            if (p.isBlank()) continue;
            if(!matchesTileSet(p.trim())) continue;
            if (p.trim().startsWith("·")) {
                plurals.add(head.substring(0, head.lastIndexOf(p.charAt(1))) + p);
            }
        }

        return plurals;
    }

    /**
     *
     */
    final protected ArrayList<String> getVerbInflections(String head, String inflections, boolean infer) {
        ArrayList<String> inf = new ArrayList<>();
        if (inflections == null && infer) {
            if (head.matches(".*s|.*x|.+z|.+j|.+sh|.+ch")) {
        //        inf.add(head + "es");
                inf.add(head + "ed");
                inf.add(head + "ing");
            } else if (head.matches(".*[bcdfghjklmnpqrstvxz]y")) {
      //          inf.add(head.substring(0, head.length() - 1) + "ies");
                inf.add(head.substring(0, head.length() - 1) + "ied");
                inf.add(head + "ing");
            } else if(head.endsWith("ee")) {
     //           inf.add(head + "s");
                inf.add(head + "d");
                inf.add(head + "ing");
            }
            else if (head.endsWith("e")) {
      //          inf.add(head + "s");
                inf.add(head + "d");
                inf.add(head.substring(0, head.length() - 1) + "ing");
            } else {
      //          inf.add(head + "s");
                inf.add(head + "ed");
                inf.add(head + "ing");
            }
        } else if (inflections != null && !inflections.isBlank()) {
            for (String i : inflections.split(",")) {
                if (i.trim().startsWith("·")) {
                    i = i.trim();
                    inf.add(head.substring(0, head.lastIndexOf(i.charAt(1))) + i);
                } else {
                    inf.add(i.replaceAll("·", "").trim());
                }
            }
        }
        if (inf.contains("deonian")) {

            System.out.println(head);
            System.exit(1);
        }
        return inf;
    }


    /**
     *
     */
    final protected ArrayList<String> getAdjectiveInflections(String head, String inflections) {
        ArrayList<String> inf = new ArrayList<>();
        if(inflections == null || inflections.isBlank()) return inf;
        for (String i : inflections.split(",")) {

            if (i.trim().startsWith("·")) {
                i = i.trim();
                inf.add(head.substring(0, head.lastIndexOf(i.charAt(1))) + i);
            } else {
                inf.add(i.replaceAll("·", "").trim());
            }
        }
        //Also should handle the rare cases where adjectives have plurals (maybe just reclassify as inflections)

        return inf;
    }

    /**
     *
     */
    final protected boolean matchesTileSet(String letters) {
        return letters.matches("[" + XMLParser.allowedChars + XMLParser.controlChars + "]+")
                && letters.replaceAll(XMLParser.controlChars, "").length() >= 2;
    }

    abstract ArrayList<String> getScrabbleWords();

    final protected boolean isExpurgated() {
        return context.contains("slur");
    }

    final protected boolean isEnglish() {
        return language.equals("English") || language.isEmpty();
    }
}
