package com.github.rchukh.amazon.paapi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AWSHandler implements SOAPHandler<SOAPMessageContext> {
	private static final Logger LOG = LoggerFactory.getLogger(AWSHandler.class);
	/** Namespace for all AWS Security elements */
	private static final String AUTH_HEADER_NS = "http://security.amazonaws.com/doc/2007-01-01/";
	/** Algorithm used to calculate string hashes */
	private static final String SIGNATURE_ALGORITHM = "HmacSHA256";
	private static final Mac MAC;
	static {
		// init security
		try {
			byte[] bytes = AWSProps.INSTANCE.getSecretKeyId().getBytes("UTF-8");
			SecretKeySpec keySpec = new SecretKeySpec(bytes, SIGNATURE_ALGORITHM);
			MAC = Mac.getInstance(SIGNATURE_ALGORITHM);
			MAC.init(keySpec);
		} catch (IOException | NoSuchAlgorithmException | InvalidKeyException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Create signature for
	 * 
	 * @param action
	 *            The single SOAP body element that is the action the request is taking.
	 * @param timestamp
	 *            The time stamp string as provided in the &lt;aws:Timestamp&gt; header element.
	 * @return A hash calculated according to AWS security rules to be provided in the &lt;aws:signature&gt; header
	 *         element.
	 */
	private static String calculateSignature(String action, String timestamp) {
		String toSign = (action + timestamp);
		byte[] sigBytes = MAC.doFinal(toSign.getBytes());
		return new String(Base64.getEncoder().encode(sigBytes));
	}

	@Override
	public boolean handleMessage(SOAPMessageContext context) {
		try {
			SOAPMessage message = context.getMessage();
			SOAPHeader header = message.getSOAPHeader();
			SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
			if (header == null) {
				header = envelope.addHeader();
			}
			header.addNamespaceDeclaration("aws", AUTH_HEADER_NS);
			// Add access key id
			SOAPElement accessKeyIdElement = header.addChildElement("AWSAccessKeyId", "aws");
			accessKeyIdElement.addTextNode(AWSProps.INSTANCE.getAccessKeyId());
			// Add current timestamp
			String timestamp = Instant.now().toString();
			SOAPElement timestampElement = header.addChildElement("Timestamp", "aws");
			timestampElement.addTextNode(timestamp);
			// Get current operation
			SOAPBody messageBody = message.getSOAPBody();
			if (messageBody == null || messageBody.getFirstChild() == null) {
				throw new IOException("AWS PA-API request missing operation(e.g. ItemSearch) value.");
			}
			String operation = messageBody.getFirstChild().getLocalName();
			// Add signature
			SOAPElement signature = header.addChildElement("Signature", "aws");
			signature.addTextNode(calculateSignature(operation, timestamp));
			// Save changes
			message.saveChanges();
			if (LOG.isDebugEnabled()) {
				ByteArrayOutputStream byteOS = new ByteArrayOutputStream();
				message.writeTo(byteOS);
				LOG.debug("SOAP message: \n" + byteOS.toString("UTF-8"));
			}
		} catch (SOAPException e) {
			LOG.error("Error occurred while adding credentials to SOAP header.", e);
		} catch (IOException e) {
			LOG.error("Error occurred while writing message to output stream.", e);
		}
		return true;
	}

	@Override
	public boolean handleFault(SOAPMessageContext context) {
		return true;
	}

	@Override
	public void close(MessageContext context) {
	}

	@Override
	public Set<QName> getHeaders() {
		return null;
	}
}
