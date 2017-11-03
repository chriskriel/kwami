package net.kwami;

import net.kwami.utils.MyProperties;

public class AppTest {

	public static void main(String[] args) {
		JavaMailProperties mailProps = new JavaMailProperties();
		mailProps.setFromAddress("frs@vodacomtz.corp");
		MyProperties props = mailProps.getSmtpProperties();
		props.put("mail.smtp.host", "smtp.vodacomtz.corp"); 
		props.put("mail.smtp.port", "25");
//		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.ssl.checkserveridentity", "false");		
		props.put("mail.smtp.ssl.trust", "*");
		System.out.println(mailProps.toString());
 	}
}
