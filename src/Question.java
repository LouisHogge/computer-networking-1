import java.io.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;

/**
 * Class responsible for writing and reading question.
 * 
 * @author Louis Hogge
 */
public class Question{

	// constructor
	public Question(){
	}

	/**
     * Method to write a question.
     * 
     * @param domainName domain name in the form of a String
     * @param questionTypeString question type in the form of a String
     * 
     * @return question in the form of a byte array
     */
	public byte[] writeQuestion(String domainName, String questionTypeString){

		// QNAME : splitting the domain name in parts thanks to the dots
		String[] domainNameSplit = domainName.split("\\.");
		int nbOfSplit = domainNameSplit.length;
		byte[][] domainNameSplitBytes = new byte[nbOfSplit][];
		int nbOfBytes = 0;

		// converting each part of the domain name into byte array
		for (int i = 0; i < nbOfSplit; i++) {
		    domainNameSplitBytes[i] = domainNameSplit[i].getBytes(StandardCharsets.UTF_8);
		    nbOfBytes += domainNameSplitBytes[i].length;
		}

		// +1 --> byte 0, +2 --> QTYPE, +2 --> QCLASS ==> +5
		ByteBuffer questionBB = ByteBuffer.allocate(nbOfBytes+nbOfSplit+5);

		// adding the length of each part and adding each part itself to the question
		for (int i = 0; i < nbOfSplit; i++) {
			Integer lengthInOneByte = Integer.valueOf(domainNameSplitBytes[i].length);
			questionBB.put(lengthInOneByte.byteValue());
		    questionBB.put(domainNameSplitBytes[i]);
		}
		questionBB.put((byte)0);

		// QTYPE : adding the type of query to the question
		short questionType = -1;
		if (questionTypeString.equals("A"))
			questionType = 1;
		else if (questionTypeString.equals("TXT"))
			questionType = 16;
		questionBB.putShort(questionType);

		// QCLASS : adding the QCLASS to the question
		short QCLASS = 1;
		questionBB.putShort(QCLASS);

		// converting into byte array
		byte[] question = questionBB.array();

		return question;
	}

	/**
     * Method to read a question.
     * 
     * @param questionBB answer question to decode in the form of a ByteBuffer
     */
	public void readQuestion(ByteBuffer questionBB){

		// QNAME: getting each part of the domain name into byte array
		String QNAME = "";
		int splitLength;
		while ((splitLength = questionBB.get()) > 0) {
		    byte[] split = new byte[splitLength];
		    for (int i = 0; i < splitLength; i++) {
		        split[i] = questionBB.get();
		    }
		    // converting byte array into String
		    QNAME = new String(split, StandardCharsets.UTF_8);
		}

		// QTYPE
		short QTYPE = questionBB.getShort();

		// QCLASS
		short QCLASS = questionBB.getShort();
	}
}
