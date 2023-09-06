package fr.cea.ig.authentication.validators;

import static org.apache.commons.lang3.StringUtils.isBlank;

import javax.inject.Singleton;

import org.springframework.security.crypto.bcrypt.BCrypt;

import fr.cea.ig.authentication.ILoginPasswordValidator;
import models.administration.authorisation.User;
// import models.administration.authorisation.description.dao.AuthenticateDAO;
import models.administration.authorisation.description.dao.UserDAO;
// import play.api.modules.spring.Spring;

/**
 * Login password validation based on the user DAO implementation.
 * 
 * @author vrd
 *
 */
@Singleton
public class UserDAOLoginPasswordValidator implements ILoginPasswordValidator {

	/**
	 * Logger.
	 */
	private static final play.Logger.ALogger logger = play.Logger.of(UserDAOLoginPasswordValidator.class);
	
	/**
	 * Validate the login password pair against data from the user DAO.
	 */
	@Override
	public void validate(String login, String password) throws ValidationFailedException {
		if (isBlank(login))
			throw new ValidationFailedException("empty login name '" + login + "'");
		if (isBlank(password))
			throw new ValidationFailedException("empty password");
		User.find.get().findByLogin(login);
		// Cannot get the instance through Spring
		// AuthenticateDAO auth = Spring.getBeanOfType(AuthenticateDAO.class);
//		UserDAO auth = (UserDAO)new User.UserFinder().getInstance();
//		UserDAO auth = new User.UserFinder().getInstance();
		UserDAO auth = User.find.get();
		if (!auth.isUserActive(login)) {
			logger.debug("inactive user {}, authentication failed",login);
			throw new ValidationFailedException("inactive user '" + login + "'"); 
		}
		String passwordInDB = auth.getUserPassword(login);
		if (isBlank(passwordInDB))
			throw new ValidationFailedException("no password or blank in db for user '" + login + "'");
		// Hashed pass must start with $2a$ and not $2y$ otherwise 
		// a bad salt exception is thrown.
		if (passwordInDB.startsWith("$2y$"))
			passwordInDB = "$2a$" + passwordInDB.substring(4);
		if (!BCrypt.checkpw(password,passwordInDB))
			throw new ValidationFailedException("bad password");
	}

}
