import java.nio.*;
import java.nio.charset.StandardCharsets;

/**
 * Class responsible for reading resource records.
 * 
 * @author Louis Hogge
 */
public class ResourceRecord{

	// instance variables
	ByteBuffer resourceRecordBB;

	/**
     * Constructor
     * 
     * @param resourceRecord resource records in the form of a byte array
     */
	public ResourceRecord(byte[] resourceRecord){
		this.resourceRecordBB = ByteBuffer.wrap(resourceRecord);
	}

	/**
     * Method to read resource records.
     * 
     * @param response answer message to decode in the form of a byte array
     * @param ANCOUNT number of answer in the form of a short
     * 
     * @see readOffset(byte[], ByteBuffer, byte, byte)
     * @see manageRDATA(short, short, int, int)
     */
	public void readResourceRecord(byte[] response, short ANCOUNT){

		// converting into ByteBuffer allows for a more controlled manner of parsing
		ByteBuffer responseBB = ByteBuffer.wrap(response);

		for(int i = 0; i < ANCOUNT; i++) {

			// we must read the first 2 bits to know if we are in front of a pointer/OFFSET or a label
		 	byte firstByte = resourceRecordBB.get();
			int firstTwoBits = (firstByte & 0b11000000) >>> 6;

			// 2 bits = 0b11 = 3 represent the start of a pointer/OFFSET
			if(firstTwoBits == 0b11) {

				byte currentByte = resourceRecordBB.get();

				// handling domain name compression
		        readOffset(response, responseBB, firstByte, currentByte);

		        // TYPE, CLASS, TTL, RDLENGTH and RDATA
		        short TYPE = resourceRecordBB.getShort();
                short CLASS = resourceRecordBB.getShort();
                int TTL = resourceRecordBB.getInt();
                int RDLENGTH = resourceRecordBB.getShort();

                // managing RDATA and printing answer to StdOut
                manageRDATA(TYPE, CLASS, TTL, RDLENGTH);
			}
			// 2 bits = 0b00 = 0 represents the start of a label
			else if(firstTwoBits == 0b00){
				
			 	boolean stop = false;

        		// If we have a label, the first byte ('firstByte') previously gotten is the length of the domain name part and we get this part here. We then check if we have the terminator 0 octet and in that case we have received the full domain name
			 	if((int)firstByte != 0){
		        	byte[] firstDomainName = new byte[(int)firstByte];
		        	resourceRecordBB.get(firstDomainName, 0, (int)firstByte);
		        }
		        else
		        	stop = true;
			 	
			 	// we go on with the other parts of the domain name
        		while(!stop){

        			// we must read the first 2 bits to know if we are in front of a pointer/OFFSET or a label
		        	byte firstByteLabel = resourceRecordBB.get();
					int firstTwoBitsLabel = (firstByteLabel & 0b11000000) >>> 6;

					// 2 bits = 0b11 = 3 represent the start of a pointer/OFFSET
					if(firstTwoBitsLabel == 0b11) {
						
						byte currentByteLabel = resourceRecordBB.get();
				        
				        // handling domain name compression
						readOffset(response, responseBB, firstByteLabel, currentByteLabel);

						stop = true;
					}
					// 2 bits = 0b00 = 0 represents the start of a label
					else if(firstTwoBitsLabel == 0b00){
						
						// If we have a label, the first byte ('firstByteLabel') previously gotten is the length of the domain name part and we get this part here. We then check if we have the terminator 0 octet and in that case we have received the full domain name
					 	if((int)firstByteLabel != 0){
				        	byte[] domainName = new byte[(int)firstByteLabel];
				        	resourceRecordBB.get(domainName, 0, (int)firstByteLabel);
				        }
				        else
				        	stop = true;
					}
		        }

		        // TYPE, CLASS, TTL, RDLENGTH and RDATA
		        short TYPE = resourceRecordBB.getShort();
                short CLASS = resourceRecordBB.getShort();
                int TTL = resourceRecordBB.getInt();
                int RDLENGTH = resourceRecordBB.getShort();

                // managing RDATA and printing answer to StdOut
                manageRDATA(TYPE, CLASS, TTL, RDLENGTH);
			}
		}
	}

	/**
     * Method to handle domain name compression.
     * 
     * @param response answer message to decode in the form of a byte array
     * @param responseBB answer message to decode in the form of a ByteBuffer
     * @param highByte first octet of the offset
     * @param lowByte second octet of the offset
     */
	private void readOffset(byte[] response, ByteBuffer responseBB, byte highByte, byte lowByte){

		// reset position of response ByteBuffer
		responseBB.position(0);

		// change "11" bits to 00 to get offset
        highByte = (byte)(highByte & 0b00111111);

        // concatenation of 2 bytes to form offset
		short offsetHighByte = (short)highByte;
		offsetHighByte <<= 8;
		int offset = (offsetHighByte | lowByte);

        // creation of offset bytebuffer to parse domain name and bin bytebuffer to throw useless first bytes
        byte[] offsetBytes = new byte[response.length - offset];
        byte[] binBB = new byte[offset];
		responseBB.get(binBB, 0, offset);
		responseBB.get(offsetBytes, 0, response.length - offset);

		// converting into ByteBuffer allows for a more controlled manner of parsing
		ByteBuffer offsetBB = ByteBuffer.wrap(offsetBytes);

		// retrieving the domain name at the calculated offset
        boolean stop = false;
        while(!stop){

        	// getting the domain name part length
        	int domainNameLength = offsetBB.get();

        	// we must read the first 2 bits to know if we are in front of a pointer/OFFSET or a label
        	byte domainNameLengthByte = (byte)domainNameLength;
			int firstTwoBits = (domainNameLengthByte & 0b11000000) >>> 6;

			// 2 bits = 0b11 = 3 represent the start of a pointer/OFFSET
			if(firstTwoBits == 0b11) {

				byte currentByte = offsetBB.get();

				// handling domain name compression
				readOffset(response, responseBB, domainNameLengthByte, currentByte);

				stop = true;
			}
			// 2 bits = 0b00 = 0 represents the start of a label
			else if(firstTwoBits == 0b00){
				
				// getting the domain name part if terminator 0 octet not met
	        	if(domainNameLength != 0){
		        	byte[] domainName = new byte[domainNameLength];
		        	offsetBB.get(domainName, 0, domainNameLength);
		        }
		        else
		        	stop = true;
			}
        }
	}

	/**
     * Method to manage RDATA and print answer to StdOut.
     * 
     * @param TYPE RR type codes in the form of a short
     * @param CLASS class of the data in the RDATA field in the form of a short
     * @param TTL time interval (in seconds) that the resource record may be cached before it should be discarded in the form of an int
     * @param RDLENGTH length of RDATA in the form of an int
     */
	private void manageRDATA(short TYPE, short CLASS, int TTL, int RDLENGTH){

		// variable to know if we must print to stdout
		boolean print = true;

		// RDATA that will be printed
		String rdataString = "";

		// TYPE that will be printed
		String typeString = "error";

		// if type = A et class = IN
        if (TYPE == 1 && CLASS == 1){

        	typeString = "A";

        	// retrieving all the domain name parts
        	for(int j = 0; j < RDLENGTH; j++) {

        		// getting the domain name part
        		int ipPart = resourceRecordBB.get();

        		// if ip address overflow into the negative we underflow it back to the correct value. If it’s already a correct nothing change
                ipPart = ipPart & 0b11111111;

                // translating bytes to String and concatenating domain name parts to form full domain name
                String s = Integer.toString(ipPart);
            	rdataString = rdataString.concat(s);
				if(j < RDLENGTH - 1)
					rdataString = rdataString.concat(".");
        	}
        }
        // if type = TXT et class = IN
        else if(TYPE == 16 && CLASS == 1){

        	typeString = "TXT";

        	// getting String length
        	int stringLength = resourceRecordBB.get();

        	// I think this line is useless for String
            stringLength = stringLength & 0b11111111;

            // getting bytes of rdata String
            ByteBuffer rdataBB = ByteBuffer.allocate(stringLength);
            for(int k = 0; k < stringLength; k ++){
            	byte b = resourceRecordBB.get();
            	rdataBB.put(b);
            }
            byte[] rdataByteArray = rdataBB.array();
     		
     		// translating bytes to String and concatenating to form full rdata String
            String s = new String(rdataByteArray, StandardCharsets.UTF_8);
            rdataString = rdataString.concat(s);
		}
        else{
			// creation of bin bytebuffer to throw bytes of not handled answer types
	        for(int j = 0; j < RDLENGTH; j++){
	        	byte bin = resourceRecordBB.get();
	        }
			print = false;
		}

		if (print){
			// writing Answer to stdout
			System.out.printf("Answer (TYPE=" + typeString + ", TTL=%d, DATA=\"" + rdataString + "\")\n", TTL);
		}
	}
}
