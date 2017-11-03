package net.kwami;
import javax.mail.Session;

import net.kwami.utils.Configurator;
import net.kwami.utils.MyProperties;

public class App {


	public static void main(String[] args) {
		final String fromEmail = "frs@vodacomtz.corp";
		String toEmail = "chris.kriel@bcx.co.za";
		
		System.out.println("TLSEmail Start");
		JavaMailProperties mailProps = Configurator.get(JavaMailProperties.class);
		MyProperties props = mailProps.getSmtpProperties();
//		props.put("mail.smtp.host", "smtp.vodacomtz.corp"); 
//		props.put("mail.smtp.port", "25");
//		props.put("mail.smtp.auth", "true");
//		props.put("mail.smtp.starttls.enable", "true");
//		props.put("mail.smtp.ssl.checkserveridentity", "false");		
//		props.put("mail.smtp.ssl.trust", "*");		
                //create Authenticator object to pass in Session.getInstance argument
//		Authenticator auth = new Authenticator() {
			//override the getPasswordAuthentication method
//			protected PasswordAuthentication getPasswordAuthentication() {
//				return new PasswordAuthentication(fromEmail, password);
//			}
//		};
		Session session = Session.getInstance(props, null);
		
		EmailUtil.sendEmail(session, mailProps.getFromAddress(), "FRS TLS Email Test", "Notify Chris Kriel you received");
		
	}
}
