package com.example.msb.hw;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by MSB on 20.03.2016.
 */
public class XmlPullParserHandler {
    private List<Person> persons= new ArrayList<Person>();
    private Person person;
    private String text;

    public List<Person> getEmployees() {
        return persons;
    }

    public List<Person> parse(InputStream is) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser  parser = factory.newPullParser();

            parser.setInput(is, null);

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagname = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (tagname.equalsIgnoreCase("person")) {
                            // create a new instance of employee
                            person = new Person();
                        }
                        break;

                    case XmlPullParser.TEXT:
                        text = parser.getText();
                        break;

                    case XmlPullParser.END_TAG:
                        if (tagname.equalsIgnoreCase("person")) {
                            // add employee object to list
                            persons.add(person);
                        }  else if (tagname.equalsIgnoreCase("name")) {
                            person.setName(text);
                        } else if (tagname.equalsIgnoreCase("number")) {
                            person.setNumber(text);
                        }
                        break;

                    default:
                        break;
                }
                eventType = parser.next();
            }

        } catch (XmlPullParserException e) {e.printStackTrace();}
        catch (IOException e) {e.printStackTrace();}

        return persons;
    }
}
