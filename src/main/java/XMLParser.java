import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;

public class XMLParser {

    /**
     * These are characters found in at least one headword that by themselves disqualify a word from inclusion.
     * (The entry may still have variants or run-ons that are acceptable.)
     */
    static final String forbiddenChars = "-◆'.,/0-9\\[A-Z]ÅÄÁÆČÇĎÉæñ";

    /**
     * These are characters that are not part of the word per se, but appear in headwords in order to carry
     * information about the word such as divisions in entries, stress indicators, or numbering of homonyms.
     * They are removed when generating Scrabble lists.
     */
    static final String controlChars = "·′¹²³⁴⁵⁶⁷⁸⁹⁰";

    /**
     * These characters appear in at least one headword and are considered variants (typically of non-English origin)
     * of ordinary letters (i.e. a-z). If a word contains one of these letters it will be replaced with its standard
     * English equivalent when generating Scrabble lists.
     */
    static final String allowedChars = "a-záàâäãăçćèéêëęğïíıöôóřșšşśțü";

    /**
     *
     */
    public static void main(String[] args) throws IOException, URISyntaxException {

        FileOutputStream fos2 = new FileOutputStream(FileDescriptor.out);
        PrintStream ps2 = new PrintStream(fos2, true, StandardCharsets.UTF_8);
        System.setOut(ps2);

        File dictFile = new File(XMLParser.class.getResource("dict/fw.xml").toURI());
        File schemaFile = new File(XMLParser.class.getResource("dict/fw.xsd").toURI());

        validateXML(dictFile, schemaFile);

        ArrayList<Entry> entryList = parseXML(dictFile);

        showResults(entryList);
    }


    /**
     * Iterates through the XML file and converts each <entry> element into a corresponding Object.
     *
     * @param dictFile The file containing the dictionray data.
     * @return A list of Entries in the order in which they were added.
     */
    private static ArrayList<Entry> parseXML(File dictFile) {
        ArrayList<Entry> entryList = new ArrayList<>();

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(dictFile);
            doc.getDocumentElement().normalize();

            NodeList entries = doc.getElementsByTagName("entry");

            //loop through entries
            for (int e = 0; e < entries.getLength(); e++) {

                //loop through keywords (should be only one for valid Scrabble words)
                if (entries.item(e).getNodeType() == Node.ELEMENT_NODE) {
                    Element entryElement = (Element) entries.item(e);
                    NodeList keywords = entryElement.getElementsByTagName("key");

                    //Entries with more than one keyword are references to other entries
                    if (keywords.getLength() > 1) continue;

                    Element keyElement = (Element) keywords.item(0);
                    String keyword = keyElement.getTextContent();
                    String keywordContext = keyElement.getAttribute("con");
                    String pronunciation = keyElement.getAttribute("pron");
                    String lang = keyElement.getAttribute("lang");
                    String comment = keyElement.getAttribute("comment");

                    Entry newEntry = new Entry(keyword, keywordContext, lang, pronunciation, comment);
                    entryList.add(newEntry);

                    /* Parts of speech */
                    NodeList parts = entryElement.getElementsByTagName("part");
                    for (int p = 0; p < parts.getLength(); p++) {

                        Element partElement = (Element) parts.item(p);
                        String types = partElement.getAttribute("type");
                        String partContext = partElement.getAttribute("con");
                        String partLang = partElement.getAttribute("language");
                        String inflections = null;
                        if (partElement.hasAttribute("inf"))
                            inflections = partElement.getAttribute("inf");
                        String plural = null;
                        if (partElement.hasAttribute("pl"))
                            plural = partElement.getAttribute("pl");
                        String singular = partElement.getAttribute("sing");
                        String grammar = partElement.getAttribute("gram");

                        for (String type : types.split(",")) {
                            type = type.trim();
                            Part newPart = new Part(newEntry, type, partContext, partLang, inflections, plural, singular, grammar);
                            newEntry.addPart(newPart);

                            /* Variants of part */
                            NodeList partVars = partElement.getElementsByTagName("var");
                            for (int v = 0; v < partVars.getLength(); v++) {
                                Element varElement = (Element) partVars.item(v);
                                String content = varElement.getTextContent();
                                String varCon = varElement.getAttribute("con");

                                String varInf = null;
                                if (varElement.hasAttribute("inf"))
                                    varInf = varElement.getAttribute("inf");
                                String varLang = varElement.getAttribute("lang");
                                String varPlural = null;
                                if (varElement.hasAttribute("pl"))
                                    varPlural = varElement.getAttribute("pl");
                                String varGram = varElement.getAttribute("gram");
                                String varPron = varElement.getAttribute("pron");
                                newPart.addVariant(new Variant(type, content, varCon, varInf, varLang, varPlural, varGram, varPron));
                            }
                        }
                    }
                    /* Variants of headword */
                    NodeList vars = entryElement.getElementsByTagName("var");
                    for (int v = 0; v < vars.getLength(); v++) {
                        Element varElement = (Element) vars.item(v);

                        //Variants of parts are handled elsewhere
                        if (!varElement.getParentNode().equals(entryElement)) continue;

                        String content = varElement.getTextContent();
                        String varCon = varElement.getAttribute("con");
                        String varInf = null;
                        if (varElement.hasAttribute("inf")) varInf = varElement.getAttribute("inf");
                        String varLang = varElement.getAttribute("lang");
                        String varPlural = null;
                        if (varElement.hasAttribute("pl")) varPlural = varElement.getAttribute("pl");
                        String varGram = varElement.getAttribute("gram");
                        String varPron = varElement.getAttribute("pron");
                        for (Part p : newEntry.getParts()) {
                            p.addVariant(new Variant(p.type, content, varCon, varInf, varLang, varPlural, varGram, varPron));
                        }
                    }

                    /* Run-ons */
                    NodeList runons = entryElement.getElementsByTagName("runon");
                    for (int r = 0; r < runons.getLength(); r++) {
                        Element runonElement = (Element) runons.item(r);
                        String types = runonElement.getAttribute("type");
                        String runonCon = runonElement.getAttribute("con");
                        String runonGram = runonElement.getAttribute("gram");
                        String runonPron = runonElement.getAttribute("pron");
                        String runonPlural = null;
                        if (runonElement.hasAttribute("pl"))
                            runonPlural = runonElement.getAttribute("pl");
                        String runonInf = null;
                        if (runonElement.hasAttribute("inf"))
                            runonInf = runonElement.getAttribute("inf");
                        for (String type : types.split(",")) {
                            type = type.trim();
                            String content = runonElement.getTextContent();
                            newEntry.addRunon(new Runon(content, type, runonCon, runonPlural, runonInf, lang, runonGram, runonPron));
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return entryList;
    }

    /**
     * Determines whether the dictionary file is formatted correctly according to the provided schema.
     *
     * @param dictFile   The dictionary-containing XML file to be validated.
     * @param schemaFile File containing the schema against which to compare the dictionary file.
     */
    static void validateXML(File dictFile, File schemaFile) {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source dict = new StreamSource(dictFile);
        try {
            Schema schema = schemaFactory.newSchema(schemaFile);
            Validator validator = schema.newValidator();
            validator.validate(dict);
            System.out.println(dict.getSystemId() + " is valid");
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        } catch (SAXException se) {
            System.out.println(dictFile.getName() + " encountered an error:\n" + se);
        }
    }


    /**
     * Filters the list of Entries and displays the results to the console.
     *
     * @param entryList A list of Entries containing dictionary data.
     */
    private static void showResults(ArrayList<Entry> entryList) {
        Trie nwl = new Trie("NWL23");
        //       Trie csw = new Trie("CSW24");
        LinkedHashSet<String> words = new LinkedHashSet<>();
        for (Entry entry : entryList) {
            if (entry.isExpurgated()) continue;
            words.addAll(entry.getScrabbleWords());
        }
        for (String word : words) {
            String formattedWord = scrabbleFormat(word);
            if (!nwl.contains(formattedWord) && formattedWord.length() <= 8) {
                System.out.println(formattedWord);
            }
        }
    }


    /**
     * Replaces diacritics with the corresponding tile in the standard English Scrabble set.
     * Converts to uppercase.
     * Removes extraneous whitespace.
     *
     * @param letters A dictionary headword, inflection, variant, or run-on.
     */
    private static String scrabbleFormat(String letters) {
        if (StringUtils.containsAny(letters, forbiddenChars))
            throw new IllegalArgumentException("Warning: Input " + letters + " contains a character that cannot be" +
                    " represented using the Scrabble tile set.");

        return letters
                .replaceAll("[" + controlChars + "]", "")
                .replaceAll("[áàâäãă]", "a")
                .replaceAll("[çć]", "c")
                .replaceAll("[èéêëę]", "e")
                .replaceAll("ğ", "g")
                .replaceAll("[ïíı]", "i")
                .replaceAll("ñ", "n")
                .replaceAll("[öôó]", "o")
                .replaceAll("ř", "r")
                .replaceAll("[șšşś]", "s")
                .replaceAll("ț", "t")
                .replaceAll("ü", "u")
                .toUpperCase().trim();
    }

}
