package mail;

import static fr.cea.ig.play.IGGlobals.configuration;

import java.io.File;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class MailServices {

	private static final play.Logger.ALogger logger = play.Logger.of(MailServices.class);

	private static final String mailSmtpHost() {
		return configuration().getString("mail.smtp.host");
	}
	
	public void sendMail(String from, Set<String> to, String subject, String message) throws MailServiceException {
		Properties properties = System.getProperties();
		properties.put("mail.smtp.host", mailSmtpHost());
		Session session = Session.getInstance(properties, null);
		try {
			Message msg = new MimeMessage(session);
			
			msg.setFrom(getInternetAddress(from));
			msg.setRecipients(Message.RecipientType.TO, to.stream().map(mail -> getInternetAddress(mail)).collect(Collectors.toSet()).toArray(new InternetAddress[0]));
			
			msg.setSubject(subject);
			msg.setContent(message, "text/html");
			Transport.send(msg);
			logger.debug("Mail sent to : " + to);

		} catch (Throwable e) {
			logger.debug("Mail NOT sent => " + e.getMessage());
			throw new MailServiceException(e);
		}
	}

	public void sendMail(String from, Set<String> to, String subject, String message, File file) throws MailServiceException {
		Properties properties = System.getProperties();
		properties.put("mail.smtp.host", mailSmtpHost());

		Session session = Session.getInstance(properties, null);

		try {
			Message msg = new MimeMessage(session);
			
			msg.setFrom(getInternetAddress(from));
			msg.setRecipients(Message.RecipientType.TO, to.stream().map(mail -> getInternetAddress(mail)).collect(Collectors.toSet()).toArray(new InternetAddress[0]));
			
			msg.setSubject(subject);

			MimeBodyPart attachmentPart = new MimeBodyPart();
			
			attachmentPart.attachFile(file);

			BodyPart messageBodyPart = new MimeBodyPart(); 
			messageBodyPart.setContent(message, "text/html");
			
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);
			multipart.addBodyPart(attachmentPart);

			msg.setContent(multipart);

			Transport.send(msg);

			logger.debug("Mail sent to : " + to);
		} catch (Throwable e) {
			logger.debug("Mail NOT sent => " + e.getMessage());
			throw new MailServiceException(e);
		}
	}

	private InternetAddress getInternetAddress(String mail) {
		try {
			return new InternetAddress(mail);
		} catch (AddressException e) {
			throw new RuntimeException(e);
		}
	}
	
}
