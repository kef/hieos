/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2008-2009 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vangent.hieos.logbrowser.log.db;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import org.apache.log4j.Logger;

/**
 * Class describing a message to log in database. It contains several action log, read
 *  and delete a message.
 * @author jbmeyer
 *
 */
public class Message {

    private String messageID;
    private MainTable mainMessage;
    private Hashtable<String, Vector<GenericTable>> miscVectors;
    static Vector<String> logTypeList = new Vector<String>();
    private Connection connection;

    @SuppressWarnings("unused")
    private Message() {
    }

    private final static Logger logger = Logger.getLogger(Message.class);

    static {
        logTypeList.add("other");
        logTypeList.add("error");
        logTypeList.add("http");
        logTypeList.add("soap");
    }

    /**
     *
     * @param c Connection. The database connection.
     * @param id String. The identifier of the message.
     * @throws LoggerException
     */
    public Message(Connection c, String id) throws LoggerException {
        connection = c;
        mainMessage = new MainTable(connection);
        mainMessage.setMessageId(id);
        miscVectors = new Hashtable<String, Vector<GenericTable>>();
        messageID = id;
    }

    /**
     *
     * @param c Connection. The database connection.
     * @param id String. The identifier of the message.
     * @throws LoggerException
     */
    public Message(Connection c) throws LoggerException {
        connection = c;
        mainMessage = new MainTable(connection);
        miscVectors = new Hashtable<String, Vector<GenericTable>>();
    }

    public void setTimeStamp(Timestamp timestamp) {
        mainMessage.setTimestamp(timestamp);
    }

    public void setSecure(String isSecure) {
        mainMessage.setSecure(isSecure);
    }

    public void setTestMessage(String testMessage) {
        mainMessage.setTest(testMessage);
    }

    public void setPass(String pass) {
        mainMessage.setPass(pass);
    }

    public void setIP(String ip) throws LoggerException {
        try {
            mainMessage.setIpAddress(InetAddress.getByName(ip));
        } catch (UnknownHostException e) {
            throw new LoggerException("IP : setIP... : " + e.getMessage());
        }
    }

    public void setCompanyName(String companyName) {
        this.setCompanyName(companyName);
    }
    
    /**
     * Read all message types with the current MessageID and store all data in the mainMessage attribute and the hashmap miscVector
     * @throws LoggerException
     */
    public void readMessage() throws LoggerException {
        if (messageID == null) {
            logger.error("Message:readMessage() messageID is null");
            throw new LoggerException("Message:readMessage() messageID is null");
        }
        if (logger.isDebugEnabled()){
            logger.debug("Retrieving Data for MESSAGE ID: " + messageID);
        }
        mainMessage.readFromDB(messageID);

        // Read all messages in the LogDetail table 
        GenericTable gt = null;
        Vector<GenericTable> vectGenTable = new Vector<GenericTable>();
        try{
            gt = new GenericTable(this);
            vectGenTable = gt.readFromDB(messageID);
        } finally {
            gt.close();
        }

        Vector<GenericTable> vectSoapMsg = new Vector<GenericTable>();
        Vector<GenericTable> vectHttpMsg = new Vector<GenericTable>();
        Vector<GenericTable> vectOtherMsg = new Vector<GenericTable>();
        Vector<GenericTable> vectErrorMsg = new Vector<GenericTable>();
        
        // Group the messages by message/log type
        for (GenericTable msg: vectGenTable ){
            if(msg.getParameterType().equals("soap"))
                vectSoapMsg.add(msg);
            else if(msg.getParameterType().equals("http"))
                vectHttpMsg.add(msg);
            else if(msg.getParameterType().equals("other"))
                vectOtherMsg.add(msg);
            else if(msg.getParameterType().equals("error"))
                vectErrorMsg.add(msg);
        }
        miscVectors.put("soap", vectSoapMsg);
        miscVectors.put("http", vectHttpMsg);
        miscVectors.put("other", vectOtherMsg);
        miscVectors.put("error", vectErrorMsg);
    }

    /**
     * 
     * @throws LoggerException
     */
    public void deleteMessage() throws LoggerException {
        mainMessage.deleteMessage(messageID);
    }

    /**
     * Method used to display the message in the xds log reader. This method format the message in XML displaying first the
     * list of table ( nodes ) available and then the content of the message.
     * @return
     */
    public String toXml() {
        StringBuffer buff = new StringBuffer();
        StringBuffer buffNodeNames = new StringBuffer();
        buffNodeNames.append("<Nodes>");
        buffNodeNames.append("<Node name='mainMessage' />");
        buff.append(mainMessage.toXml());
        Iterator<String> it = logTypeList.iterator();
        while (it.hasNext()) {
            String logType = it.next();
            Vector<GenericTable> v = miscVectors.get(logType);
            buffNodeNames.append("<Node name='" + logType + "' />");
            buff.append("<" + logType + ">");
            for (int i = 0; i < v.size(); i++) {
                buff.append(v.elementAt(i).toXml());
            }
            buff.append("</" + logType + ">");
        }
        buffNodeNames.append("</Nodes>");
        return "<message number='" + messageID + "'>" + buffNodeNames.toString() + buff.toString() + "</message>";
    }

    /**
     * 
     * @return
     */
    public HashMap<String, HashMap<String, Object>> toHashMap() {
        HashMap<String, HashMap<String, Object>> values = new HashMap<String, HashMap<String, Object>>();
        values.put("main", mainMessage.toHashMap());
        Iterator<String> it = logTypeList.iterator();
        while (it.hasNext()) {
            String logType = it.next();
            Vector<GenericTable> v = miscVectors.get(logType);
            HashMap<String, Object> thisLogType = new HashMap<String, Object>();
            for (int i = 0; i < v.size(); i++) {
                String[] parm = v.elementAt(i).toStringArray();
                String key = parm[0].replaceAll(" ", "_");
                String value = parm[1];
                Object oldValueObject = thisLogType.get(key);
                if (oldValueObject == null) {
                    thisLogType.put(key, value);
                } else {
                    if (oldValueObject instanceof String) {
                        ArrayList<String> newValue = new ArrayList<String>();
                        newValue.add((String) oldValueObject);
                        newValue.add(value);
                        thisLogType.put(key, newValue);
                    } else {
                        ArrayList<String> newValue = (ArrayList<String>) oldValueObject;
                        newValue.add(value);
                        thisLogType.put(key, newValue);
                    }
                }
            }
            values.put(logType, thisLogType);
        }
        return values;
    }

    /**
     * 
     * @return
     */
    public String toJSon() {
        StringBuffer buff = new StringBuffer();
        buff.append("{\"message\" : { \n" +
                "\"number\": \"" + messageID + "\" , \n  ");
        buff.append("\"table\": \n\t[");
        buff.append(mainMessage.toJSon());
        Iterator<String> it = logTypeList.iterator();
        while (it.hasNext()) {
            String logType = it.next();
            Vector<GenericTable> v = miscVectors.get(logType);
            buff.append(",\n{\"name\" : \"" + logType + "\",\n");
            buff.append("\"values\" : [");
            for (int i = 0; i < v.size(); i++) {
                buff.append(v.elementAt(i).toJSon());
                if (i < v.size()) {
                    buff.append(",\n");
                }
            }
            buff.append("]\n}");
        }
        buff.append("]\n}\n}");
        return buff.toString();
    }

    /**
     * 
     * @return
     */
    public Connection getConnection() {
        return connection;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public void close() {
        mainMessage.close();
    }
}
