import java.io.*;
import java.nio.*;

/**
 * Class responsible for writing and reading header.
 * 
 * @author Louis Hogge
 */
public class Header{

	// instance variable
	private Short ID;

	/**
     * Constructor
     * 
     * @param ID id of query in the form of a Short
     */
	public Header(Short ID){
		this.ID = ID;
	}

	/**
     * Method to write a header.
     * 
     * @return header in the form of a byte array
     */
	public byte[] writeHeader(){

		// flags
		String QR = "0";
		String Opcode = "0000";
		String AA = "0";
		String TC = "0";
		String RD = "1";
		String RA = "0";
		String Z = "000";
		String RDCODE = "0000";
		String flagsString = QR + Opcode + AA + TC + RD + RA + Z + RDCODE;

		// converting flags from String to short
		short flags = Short.parseShort(flagsString, 2); // base = 2

		// QDCOUNT, ANCOUNT, NSCOUNT and ARCOUNT
		short QDCOUNT = 1;
		short ANCOUNT = 0;
		short NSCOUNT = 0;
		short ARCOUNT = 0;

		ByteBuffer headerBB = ByteBuffer.allocate(12); // 6 shorts = 12 bytes

		// adding all the components to the header
		headerBB.putShort(ID);
		headerBB.putShort(flags);
		headerBB.putShort(QDCOUNT);
		headerBB.putShort(ANCOUNT);
		headerBB.putShort(NSCOUNT);
		headerBB.putShort(ARCOUNT);

		// converting into byte array
		byte[] header = headerBB.array();

		return header;
	}

	/**
     * Method to read a header.
     * 
     * @param headerBB answer header to decode in the form of a ByteBuffer
     * 
     * @return ANCOUNT which is the number of answers received in the form of a short
     */
	public short readHeader(ByteBuffer headerBB){

		short ANCOUNT = 0;

		// ID
		short responseID = headerBB.getShort();

		// check if query ID and response ID is the same
		if (this.ID != responseID){
			System.err.println("Error: query ID and response ID are not the same");
			System.exit(-1);
		}

		// flags
		short flags = headerBB.get();
		int QR = (flags & 0b10000000) >>> 7;
		int opCode = ( flags & 0b01111000) >>> 3;
		int AA = ( flags & 0b00000100) >>> 2;
		int TC = ( flags & 0b00000010) >>> 1;
		int RD = flags & 0b00000001;
		flags = headerBB.get();
		int RA = (flags & 0b10000000) >>> 7;
		int Z = ( flags & 0b01110000) >>> 4;
		int RCODE = flags & 0b00001111;

		// QDCOUNT, ANCOUNT, NSCOUNT and ARCOUNT
		short QDCOUNT = headerBB.getShort();
		ANCOUNT = headerBB.getShort();
		short NSCOUNT = headerBB.getShort();
		short ARCOUNT = headerBB.getShort();

		// check if an answer has been received
		if (ANCOUNT == 0 && NSCOUNT == 0 && ARCOUNT == 0){
			System.err.println("Error: no answer from the server");
			System.exit(-1);
		}

    	return ANCOUNT;
	}
}
