package com.example.msb.hw;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class MainActivity extends AppCompatActivity {

    String name;
    String phoneNumber;
    ListView lv;
    Context context;
    List<String> list = new ArrayList<String>();    //To set list the listview
    List<String> list2 = new ArrayList<String>();   //To save current contect
    String[] resultList;

    private DocumentBuilderFactory docFactory;  //Parsing xml file
    private DocumentBuilder docBuilder;         //Parsing xml file
    private Document doc;                       //Parsing xml file
    private Element rootElement;                //Parsing xml file
    private List<Person> persons;               //List of person class
    private HashMap<String, String> hmPersonRecover;       //To hold persons which are in the xml filecurrent contact
    private HashMap<String, String> hmPersonCurrent;       //To hold persons which are in
    private CustomAdapter ca;                              //My custom adapter


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hmPersonRecover = new HashMap<String ,String>();
        hmPersonCurrent = new HashMap<String ,String>();

        ReadContact("");
        context = this;
        lv=(ListView) findViewById(R.id.lisview);
        ca = new CustomAdapter(this, list);
        lv.setAdapter(ca);
    }

    /*This function is used read phone contact which take an optional parameter and this parameter is used to filter the number according to the operator*/
    private void ReadContact(String operator) {

        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
        while (phones.moveToNext())
        {
            name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            if(phoneNumber.startsWith("+9"))
            {
                phoneNumber = SanitizePhoneNumber(phoneNumber);
            }

            if(operator.equalsIgnoreCase("avea"))
            {
                if(phoneNumber.startsWith("0506"))
                    list.add(name + ":" + phoneNumber);
            }
            if(operator.equalsIgnoreCase("turkcell"))
            {
                if(phoneNumber.startsWith("0554"))
                    list.add(name + ":" + phoneNumber);
            }
            if(operator.equalsIgnoreCase("vodafone"))
            {
                if(phoneNumber.startsWith("0545"))
                    list.add(name + ":" + phoneNumber);
            }
            if(operator == "")
            {
                list.add(name + ":" + phoneNumber);
                list2.add(name + ":" + phoneNumber);
            }
            hmPersonCurrent.put(name, phoneNumber);
        }

        phones.close();

    }
    /*Sanitize phone number Ex: +908976541236 to 08976541236*/
    private String SanitizePhoneNumber(String phoneNumber) {

        return phoneNumber.substring(2);
    }
    /*To  save all contact to xml file*/
    public void onClickSaveButton(View v)
    {
        Log.i("SaveButton", "Save Button");
        try {
            docFactory = DocumentBuilderFactory.newInstance();
            docBuilder = docFactory.newDocumentBuilder();

            doc = docBuilder.newDocument();
            rootElement = doc.createElement("persons");
            doc.appendChild(rootElement);

            for(String person : list2) {
                resultList = person.split(":");
                SaveToXML(resultList[0], resultList[1]);
            }

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            FileOutputStream fOut = null;
            try {
                fOut = openFileOutput("persons.xml",MODE_PRIVATE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            StreamResult result = new StreamResult(fOut);

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);

            transformer.transform(source, result);

            System.out.println("File saved!");
            Toast.makeText(this,"Person Saved", Toast.LENGTH_SHORT).show();

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }
    }

    /*To recover the numbers*/
    public void onClickRecoverButton(View v)
    {
        Log.i("RecoverButton", "Recover Button");
        if(fileExistance("persons.xml")) {
            FileInputStream fIn = null;
            InputStream is;
            //open file
            try {
                fIn = openFileInput("persons.xml");
                Log.d("FileError", "Open File");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.d("FileError", "File does not exixst");
            }

            try {
                XmlPullParserHandler parser = new XmlPullParserHandler();
                is = fIn;
                persons = parser.parse(is);
                Log.d("Input", "is.read() - fIn.read()" + is.read() + fIn.read());
                list.clear();
                for (Person p : persons) {
                    list.add(p.getName() + ":" + p.getNumber());
                    hmPersonRecover.put(p.getName(), p.getNumber());
                }

                Backup(hmPersonCurrent, hmPersonRecover);

                lv.setAdapter(ca);

            } catch (Exception e) {
                e.printStackTrace();
                Log.d("Input", "Input Stream Exception");
            }
        }else{
            Toast.makeText(getApplicationContext(), "First press save button", Toast.LENGTH_LONG).show();
        }
    }
    /* Check if internal file exist */
    private boolean fileExistance(String fname){
        File file = getBaseContext().getFileStreamPath(fname);
        return file.exists();
    }

    /* Check which numbers are deleted*/
    private void Backup(HashMap<String, String> current, HashMap<String, String> recover)
    {

        for(Map.Entry<String, String> entry : recover.entrySet())
        {
            if(!current.containsKey(entry.getKey()))
            {
                AddToContact(entry.getKey(), entry.getValue());
            }
        }
    }

    /*  Add deleted numbers to cantact */
    private void AddToContact(String name, String number)
    {

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        int rawContactInsertIndex = ops.size();

        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());

        //Phone Number
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,
                        rawContactInsertIndex)
                .withValue(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
                .withValue(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, "1").build());

        //Display name/Contact name
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Contacts.Data.RAW_CONTACT_ID,
                        rawContactInsertIndex)
                .withValue(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                .build());
        try {
            ContentProviderResult[] res = getContentResolver().applyBatch(
                    ContactsContract.AUTHORITY, ops);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /* Radio group to filer the number */
    public void onClickClickedButton(View v)
    {
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio_group);
        int id = radioGroup.getCheckedRadioButtonId();

        switch (id)
        {
            case R.id.avea:
                Toast.makeText(this,"Avea filter for 0506", Toast.LENGTH_SHORT).show();
                list.clear();
                ReadContact("avea");
                ca.notifyDataSetChanged();
                break;
            case R.id.turkcell:
                Toast.makeText(this,"Turkcell filter for 0554", Toast.LENGTH_SHORT).show();
                list.clear();
                ReadContact("turkcell");
                ca.notifyDataSetChanged();
                break;
            case R.id.vodafone:
                Toast.makeText(this,"Vodafone filter for 0545", Toast.LENGTH_SHORT).show();
                list.clear();
                ReadContact("vodafone");
                ca.notifyDataSetChanged();
                break;
            case R.id.all:
                Toast.makeText(this,"All Contact", Toast.LENGTH_SHORT).show();
                list.clear();
                ReadContact("");
                ca.notifyDataSetChanged();
            default:
                break;
        }
    }

    /* To save xml */
    private void SaveToXML(String p_name, String p_number)
    {
        // person elements
        Element pPerson = doc.createElement("person");
        rootElement.appendChild(pPerson);

        // firstname elements
        Element firstname = doc.createElement("name");
        firstname.appendChild(doc.createTextNode(p_name));
        pPerson.appendChild(firstname);

        // number elements
        Element firstNumber = doc.createElement("number");
        firstNumber.appendChild(doc.createTextNode(p_number));
        pPerson.appendChild(firstNumber);
        Log.i("SaveToXML", "Save tot XML");
    }

}
