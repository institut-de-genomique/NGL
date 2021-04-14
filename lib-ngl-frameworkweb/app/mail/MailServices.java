package mail;

import static fr.cea.ig.play.IGGlobals.configuration;

import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

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

	private InternetAddress getInternetAddress(String mail) {
		try {
			return new InternetAddress(mail);
		} catch (AddressException e) {
			throw new RuntimeException(e);
		}
	}
	
}
