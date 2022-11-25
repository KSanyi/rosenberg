package hu.kits.rosenberg;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import hu.kits.rosenberg.Dictionary.DictionaryEntry;

public class DictionaryParser {

    public static Dictionary parseDictionary(InputStream inputStream) {
        
        List<DictionaryEntry> entries = new ArrayList<>();
        
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        try {
            XMLEventReader reader = xmlInputFactory.createXMLEventReader(inputStream);
            XMLEvent event;
            event = reader.nextTag();
            verifyStartTag(event, "root");
            
            while (reader.hasNext()) {
                event = reader.nextTag();
                if(isEndTag(event, "root")) {
                    break;
                }
                verifyStartTag(event, "Lemma");
                
                //String dicType = readElementContent(reader, "Lemma.DicType");
                //String index = readElementContent(reader, "Lemma.LemmaIndex");
                String pocket = readNextTagContent(reader, "Lemma.LemmaPocket");
                
                //System.out.println(dicType);
                
                StringBuilder descriptionBuilder = new StringBuilder();
                
                event = reader.nextEvent();
                while(!isEndTag(event, "Lemma")) {
                    descriptionBuilder.append(event.toString());
                    event = reader.nextEvent();
                }
                
                entries.add(new DictionaryEntry(pocket, descriptionBuilder.toString()));
            }
            return new Dictionary(entries);
        } catch (XMLStreamException ex) {
            throw new DictionaryParseException("Error parsing dictionary", ex);
        }
    }

    private static boolean isStartTag(XMLEvent event, String tagName) {
        return event.isStartElement() && event.asStartElement().getName().getLocalPart().equals(tagName);
    }
    
    private static boolean isEndTag(XMLEvent event, String tagName) {
        return event.isEndElement() && event.asEndElement().getName().getLocalPart().equals(tagName);
    }
    
    private static void verifyStartTag(XMLEvent nextEvent, String tagName) {
        if (nextEvent.isStartElement()) {
            StartElement startElement = nextEvent.asStartElement();
            if(startElement.getName().getLocalPart().equals(tagName)) {
                return;
            }
        }
        throw new DictionaryParseException("Cant find expected <" + tagName + "> in line " + nextEvent.getLocation().getLineNumber());
    }
    
    private static void verifyEndTag(XMLEvent nextEvent, String tagName) {
        if (nextEvent.isEndElement()) {
            EndElement endElement = nextEvent.asEndElement();
            if(endElement.getName().getLocalPart().equals(tagName)) {
                return;
            }
        }
        throw new DictionaryParseException("Cant find expectec <" + tagName + "> in line " + nextEvent.getLocation().getLineNumber());
    }
    
    private static String readElementContent(XMLEventReader reader, String elementName) throws XMLStreamException {
        XMLEvent event = reader.nextTag();
        verifyStartTag(event, elementName);
        event = reader.nextEvent();
        String content = event.asCharacters().getData();
        event = reader.nextTag();
        verifyEndTag(event, elementName);
        return content;
    }
    
    public static class DictionaryParseException extends RuntimeException {
        
        public DictionaryParseException(String description) {
            super(description);
        }
        
        public DictionaryParseException(String description, Throwable cause) {
            super(description, cause);
        }
        
    }
    
    private static String readNextTagContent(XMLEventReader reader, String tagName) throws XMLStreamException {
        XMLEvent event = reader.nextEvent();
        while(!event.isStartElement() || !event.asStartElement().getName().getLocalPart().equals(tagName)) {
            event = reader.nextEvent();
        }
        String content = reader.nextEvent().asCharacters().getData();
        event = reader.nextEvent();
        return content;
    }
    
}
