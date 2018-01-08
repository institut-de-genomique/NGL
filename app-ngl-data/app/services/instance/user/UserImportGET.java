package services.instance.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

import javax.sql.DataSource;


import models.administration.authorisation.User;
import models.laboratory.container.instance.Container;
import models.utils.dao.AbstractDAOMapping;
import models.utils.dao.DAOException;
//import rules.services.RulesException;
import scala.concurrent.duration.FiniteDuration;
import play.Logger;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.MappingSqlQuery;

import play.api.modules.spring.Spring;
import services.instance.AbstractImportData;
import services.instance.AbstractImportDataGET;
import services.instance.container.ContainerImportGET;
import models.administration.authorisation.description.dao.UserDAO;
import models.administration.authorisation.User;

public class UserImportGET extends AbstractImportDataGET{
	
	public UserImportGET(FiniteDuration durationFromStart,FiniteDuration durationFromNextIteration) {
		super("UserImportGET", durationFromStart, durationFromNextIteration);
	}
	@Override
	public void runImport() throws SQLException, DAOException{
		//sélectionner les utilisateurs
				String SQLUsers="SELECT ident, firstname, lastname, mail, password_crypt, actif, description FROM people WHERE unitid =36 AND description IN ('Responsable Technique', 'SU')";
				List<User> resultsUsers = limsServices.findUsersToSynchronize(SQLUsers);
				List<Long> roleIdAdmin =  new ArrayList<Long>();
				List<Long> roleIdWriter =  new ArrayList<Long>();
				roleIdAdmin.add((long) 3); //admin
				roleIdWriter.add((long) 2); //writer
				
				UserDAO userHome = Spring.getBeanOfType(UserDAO.class);
				
				for (User user : resultsUsers) {
					User userInNGL = userHome.findByLogin(user.code);
					if (userInNGL != null){
						Logger.debug("UserImportGET - runImport mapRow- : L'utilisateur existe, on le met à jour = " + user.code);
						user.id = userInNGL.id;
						userHome.update(user);
					}else{
						if (user.active){
							Logger.debug("UserImportGET - runImport mapRow- : L'utilisateur est actif et n'existe pas encore, on le crée = "+ user.code );
							user.id = userHome.save(user);
						}
					}
					//un utilisateur a été créé ou mis à jour, on créé son rôle
					if (user.id != null){
						Logger.debug("UserImportGET - runImport mapRow- : Création ou mise à jour du rôle user_id = "+ user.id );
						if (user.technicaluser == 1){//si le user est responsable technique dans e-SIToul
							userHome.insertUserRoles(user.id,roleIdWriter,true);
							Logger.debug("UserImportGET - runImport mapRow- : ajout du rôle writer");
						}else{
							userHome.insertUserRoles(user.id,roleIdAdmin,true);
							Logger.debug("UserImportGET - runImport mapRow- : ajout du rôle admin");
						}
					}
				}
	}
	
}
