package com.nyrds.pixeldungeon.test;

import org.junit.Test;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import static org.junit.Assert.*;

/**
 * Unit test to validate string and string-array resources by parsing XML files directly
 */
public class StringResourceXmlValidationTest {

    private static final String PROJECT_ROOT = "/home/mike/StudioProjects/remixed-dungeon";
    private static final String VALUES_DIR = PROJECT_ROOT + "/RemixedDungeon/src/main/res/values";

    @Test
    public void validateStringResourceXmlFiles() throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        DocumentBuilder builder = factory.newDocumentBuilder();

        // Get all string resource files
        File valuesDir = new File(VALUES_DIR);
        File[] resourceFiles = valuesDir.listFiles((dir, name) -> 
            name.startsWith("strings") && name.endsWith(".xml"));

        assertNotNull("Values directory should exist", resourceFiles);

        List<String> errors = new ArrayList<>();
        
        for (File resourceFile : resourceFiles) {
            System.out.println("Validating: " + resourceFile.getName());
            
            try {
                Document doc = builder.parse(resourceFile);
                doc.getDocumentElement().normalize();

                // Validate string resources
                NodeList stringNodes = doc.getElementsByTagName("string");
                for (int i = 0; i < stringNodes.getLength(); i++) {
                    Node node = stringNodes.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) node;
                        String name = element.getAttribute("name");
                        String value = element.getTextContent();
                        
                        // Validate name exists
                        assertFalse("String resource name should not be empty in " + resourceFile.getName(), 
                                   name.trim().isEmpty());
                        
                        // Validate value exists
                        assertNotNull("String resource value should not be null in " + resourceFile.getName() + 
                                     ", name: " + name, value);
                        
                        // Check for common XML issues
                        if (value.contains("&") && !value.contains("&amp;") && !value.contains("&lt;") 
                            && !value.contains("&gt;") && !value.contains("&quot;") && !value.contains("&apos;")) {
                            System.out.println("Warning: Possible unescaped ampersand in string resource '" + name + 
                                             "' in file " + resourceFile.getName());
                        }
                    }
                }

                // Validate string-array resources
                NodeList arrayNodes = doc.getElementsByTagName("string-array");
                for (int i = 0; i < arrayNodes.getLength(); i++) {
                    Node arrayNode = arrayNodes.item(i);
                    if (arrayNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element arrayElement = (Element) arrayNode;
                        String arrayName = arrayElement.getAttribute("name");
                        
                        // Validate array name exists
                        assertFalse("String array name should not be empty in " + resourceFile.getName(), 
                                   arrayName.trim().isEmpty());
                        
                        // Validate items within the array
                        NodeList itemNodes = arrayElement.getElementsByTagName("item");
                        for (int j = 0; j < itemNodes.getLength(); j++) {
                            Node itemNode = itemNodes.item(j);
                            String itemValue = itemNode.getTextContent();
                            
                            assertNotNull("String array item should not be null in " + resourceFile.getName() + 
                                         ", array: " + arrayName, itemValue);
                            
                            // Check for common XML issues
                            if (itemValue.contains("&") && !itemValue.contains("&amp;") && !itemValue.contains("&lt;") 
                                && !itemValue.contains("&gt;") && !itemValue.contains("&quot;") && !itemValue.contains("&apos;")) {
                                System.out.println("Warning: Possible unescaped ampersand in string array '" + arrayName + 
                                                 "', item index: " + j + " in file " + resourceFile.getName());
                            }
                        }
                    }
                }
            } catch (SAXException e) {
                errors.add("XML parsing error in " + resourceFile.getName() + ": " + e.getMessage());
            } catch (IOException e) {
                errors.add("IO error reading " + resourceFile.getName() + ": " + e.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            StringBuilder errorMsg = new StringBuilder("Errors found in resource files:\n");
            for (String error : errors) {
                errorMsg.append(error).append("\n");
            }
            fail(errorMsg.toString());
        }

        System.out.println("Successfully validated " + resourceFiles.length + " string resource XML files.");
    }
}