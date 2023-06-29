import java.nio.*;

/**
 * Class responsible for writing and reading message.
 * 
 * @author Louis Hogge
 * 
 * @see Header.java
 * @see Question.java
 * @see ResourceRecord.java
 */
public class Message{

	// instance variables
	private int headerBytesLength;
	private int questionBytesLength;
	Header header;
	Question question;

	/**
     * Constructor
     * 
     * @param ID id of query in the form of a Short
     * 
     * @see Header.java
     * @see Question.java
     */
	public Message(Short ID){
		header = new Header(ID);
		question = new Question();
	}

	/**
     * Method to write a message.
     * 
     * @param domainName domain name in the form of a String
     * @param questionTypeString question type in the form of a String
     * 
     * @see Header.java
     * @see writeHeader()
     * 
     * @see Question.java
     * @see writeQuestion()
     * 
     * @return message in the form of a byte array
     */
	public byte[] writeMessage(String domainName, String questionTypeString){

		// writing of the header
		byte[] headerBytes = header.writeHeader();
		headerBytesLength = headerBytes.length;

		// writing of the question
		byte[] questionBytes = question.writeQuestion(domainName, questionTypeString);
		questionBytesLength = questionBytes.length;

		// calculation of the message length
		Integer messageLength = Integer.valueOf(headerBytesLength + questionBytesLength);

		// concatenation of the message length, the header and the question
		ByteBuffer messageBB = ByteBuffer.allocate(2 + headerBytesLength + questionBytesLength);
		messageBB.putShort(messageLength.shortValue());
        messageBB.put(headerBytes);
        messageBB.put(questionBytes);
		
		// converting into byte array
		byte[] message = messageBB.array();

		return message;
	}

	/**
     * Method to read a message.
     * 
     * @param response answer message to decode in the form of a byte array
     * 
     * @see Header.java
     * @see readHeader(ByteBuffer)
     * 
     * @see Question.java
     * @see readQuestion(ByteBuffer)
     * 
     * @see ResourceRecord.java
     * @see readResourceRecord(byte[], short)
     */
	public void readMessage(byte[] response){

		// convert response into a ByteBuffer in order to separate it between header, question and resource records
		ByteBuffer responseBB = ByteBuffer.wrap(response);

		// header part
		byte[] headerBytes = new byte[headerBytesLength];
	    responseBB.get(headerBytes, 0, headerBytes.length);
	    ByteBuffer headerBB = ByteBuffer.wrap(headerBytes);
		short ANCOUNT = header.readHeader(headerBB);

		// question part --> not specifically necessary
		byte[] questionBytes = new byte[questionBytesLength];
	    responseBB.get(questionBytes, 0, questionBytes.length);
	    ByteBuffer questionBB = ByteBuffer.wrap(questionBytes);
		question.readQuestion(questionBB);

		// resource records part --> only necessary function of response parsing
		byte[] resourceRecordBytes = new byte[response.length - headerBytesLength - questionBytesLength];
	    responseBB.get(resourceRecordBytes, 0, resourceRecordBytes.length);
	    ResourceRecord resourceRecord = new ResourceRecord(resourceRecordBytes);
	    resourceRecord.readResourceRecord(response, ANCOUNT);
	}
}
