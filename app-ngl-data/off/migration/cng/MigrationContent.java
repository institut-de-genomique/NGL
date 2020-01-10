package controllers.migration.cng;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import models.Constants;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.Valuation;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.laboratory.sample.description.SampleType;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import models.utils.dao.DAOException;
import models.utils.instance.ContainerSupportHelper;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import play.Logger;
import play.mvc.Result;
import validation.ContextValidation;
import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;

/**
 * Update contents in the Container (add missing contents, update properties)
 * This migration replaces MigrationTag (scope larger)
 * 
 * @author dnoisett
 * 04/04/2014
 */
@Repository
public class MigrationContent extends CommonController {
	
	private static final String CONTAINER_COLL_NAME_BCK = InstanceConstants.CONTAINER_COLL_NAME + "_BCKmigrationContent";
	
	private static JdbcTemplate jdbcTemplate;
	
	private static final String CONTAINER_CATEGORY_CODE= "lane";
	private static final String CONTAINER_STATE_CODE="A";
	protected static final String PROJECT_TYPE_CODE_DEFAULT = "default-project";
	protected static final String PROJECT_STATE_CODE_DEFAULT = "IP";
	protected static final String IMPORT_CATEGORY_CODE="sample-import";
	protected static final String SAMPLE_TYPE_CODE_DEFAULT = "default-sample-cng";
	protected static final String SAMPLE_USED_TYPE_CODE = "default-sample-cng";	
	protected static final String IMPORT_TYPE_CODE_DEFAULT = "default-import";
	
	
	@Autowired
	@Qualifier("lims")
	public void setDataSource(DataSource dataSource) {
		MigrationContent.jdbcTemplate = new JdbcTemplate(dataSource);              
	}
	
	
	
	public static List<Container> findContainerToCreate(final ContextValidation contextError) throws DAOException {


		List<Container> results = jdbcTemplate.query("select * from v_flowcell_tongl_reprise order by code, project, code_sample, tag",new Object[]{} 
		,new RowMapper<Container>() {

			@SuppressWarnings("rawtypes")
			public Container mapRow(ResultSet rs, int rowNum) throws SQLException {

				Container container = new Container();
				
				container.traceInformation.setTraceInformation(InstanceHelpers.getUser());
				
				container.code=rs.getString("code");
				Logger.debug("Container code :"+container.code);
				
				container.categoryCode=CONTAINER_CATEGORY_CODE;
				
				if (rs.getString("comment") != null) {
					container.comments=new ArrayList<Comment>();	
					//just one comment for one lane (container)
					container.comments.add(new Comment(rs.getString("comment"), "ngl-test"));
				}
				
				container.state = new State(); 
				container.state.code=CONTAINER_STATE_CODE;
				container.state.user = InstanceHelpers.getUser();
				container.state.date = new Date(); 
				
				container.valuation = new Valuation(); 
				container.valuation.valid= TBoolean.UNSET;
				
				// define container support attributes
				try {
					container.support=ContainerSupportHelper.getContainerSupport("lane", rs.getInt("nb_container"),rs.getString("code_support"),"1",rs.getString("column")); 
				}
				catch(DAOException e) {
					Logger.error("Can't get container support !"); 
				}
				
				container.properties= new HashMap<String, PropertyValue>();
				container.properties.put("limsCode",new PropertySingleValue(rs.getInt("lims_code")));
				
				
				if (rs.getString("project")!=null) {
					container.projectCodes=new HashSet<String>();
					container.projectCodes.add(rs.getString("project"));
				}
				
				if (rs.getString("code_sample")!=null) {
					Content content=new Content();
					content.sampleCode=rs.getString("code_sample");
					
					String sampleTypeCode = SAMPLE_USED_TYPE_CODE;
					SampleType sampleType=null;
					try {
						sampleType = SampleType.find.findByCode(sampleTypeCode);
					} catch (DAOException e) {
						Logger.error("",e);
						return null;
					}
					if( sampleType==null ){
						contextError.addErrors("code", "error.codeNotExist", sampleTypeCode, content.sampleCode);
						return null;
					}		
					
					content.sampleTypeCode = sampleType.code;
					content.sampleCategoryCode = sampleType.category.code;
					
					content.properties = new HashMap<String, PropertyValue>();
					
					if(rs.getString("tag")!=null) { 
						content.properties.put("tag",new PropertySingleValue(rs.getString("tag")));
						content.properties.put("tagCategory",new PropertySingleValue(rs.getString("tagcategory")));
					}
					else {
						content.properties.put("tag",new PropertySingleValue("-1")); // specific value for making comparison, suppress it at the end of the function...
						content.properties.put("tagCategory",new PropertySingleValue("-1"));
					}					

					container.contents.add(content);			
					
					container.sampleCodes=new HashSet<String>();
					container.sampleCodes.add(rs.getString("code_sample"));
				}
				

			
				
				return container;
			}
		});       
		
		//affect all the project codes /samples /tags to the same container (for having unique codes of containers) 
		/// required to have an ordered list (see ORDER BY clause in the sql of the view)
		int pos = 0;
		int x=1;
		int listSize  =  results.size();
		
		while (pos < listSize-1)   {
			
			while ( (pos < listSize-1) && (results.get(pos).code.equals( results.get(pos+x).code))   ) {
				
				// difference between the two projectCode
				if (! results.get(pos).projectCodes.toArray(new String[0])[0].equals(results.get(pos+x).projectCodes.toArray(new String[0])[0])) {
					if (! results.get(pos).projectCodes.contains(results.get(pos+x).projectCodes.toArray(new String[0])[0])) {
						
						results.get(pos).projectCodes.add( results.get(pos+x).projectCodes.toArray(new String[0])[0] ); 
					}
				}
				// difference between the two sampleCode
				if (! results.get(pos).sampleCodes.toArray(new String[0])[0].equals(results.get(pos+x).sampleCodes.toArray(new String[0])[0])) {
					if (! results.get(pos).sampleCodes.contains(results.get(pos+x).sampleCodes.toArray(new String[0])[0])) {
							
						results.get(pos).sampleCodes.add( results.get(pos+x).sampleCodes.toArray(new String[0])[0] );
					}
				}
				
				
				createContent(results, pos, pos+x);
				
								
				// all the difference have been reported on the first sample found (at the position pos)
				// so we can delete the sample at the position (posNext)
				results.remove(pos+x);
				//ajust list size
				listSize--;
			}
			pos++;
		}	
		
		//for remove null tags
		for (Container r : results) {
			Iterator<Content> iterator = r.contents.iterator();
			while(iterator.hasNext()){
				Content cnt = iterator.next();
				if (cnt.properties.get("tag").value.equals("-1")) {
					cnt.properties.remove("tag");
				}
				if (cnt.properties.get("tagCategory").value.equals("-1")) {
					cnt.properties.remove("tagCategory");
				}
				
			}
			
			
		/*	for (int i=0; i<r.contents.size(); i++) {
				if (r.contents.get(i).properties.get("tag").value.equals("-1")) {
					r.contents.get(i).properties.remove("tag");
				}
				if (r.contents.get(i).properties.get("tagCategory").value.equals("-1")) {
					r.contents.get(i).properties.remove("tagCategory");
				}
			}*/
		}
		
		return results;
	}
	
	/**
	 * 
	 * @param results
	 * @param posCurrent
	 * @param posNext
	 * @return
	 * @throws DAOException
	 */
	private static List<Container>  createContent(List<Container> results, int posCurrent, int posNext) throws DAOException{

		Content content=new Content();
		content.sampleCode= results.get(posNext).sampleCodes.toArray(new String[0])[0];
		
		SampleType sampleType=null;
		sampleType = SampleType.find.findByCode(SAMPLE_USED_TYPE_CODE);	
		content.sampleTypeCode = sampleType.code;
		content.sampleCategoryCode = sampleType.category.code;
		
		content.properties = new HashMap<String, PropertyValue>();
		Iterator<Content> itr =  results.get(posNext).contents.iterator();
		Content contt = itr.next(); 
		content.properties.put("tag",new PropertySingleValue( contt.properties.get("tag").value  ));
		content.properties.put("tagCategory",new PropertySingleValue( contt.properties.get("tagCategory").value  ));
		
		results.get(posCurrent).contents.add(content); 
		
		return results;
	}

	
	
	public static Result migration() {
		
		int n=0;
		
		JacksonDBCollection<Container, String> containersCollBck = MongoDBDAO.getCollection(CONTAINER_COLL_NAME_BCK, Container.class);
		if (containersCollBck.count() == 0) {
	
			backUpContainer();
			
			Logger.info("Migration container starts");
		
			//find collection up to date
			ContextValidation contextError = new ContextValidation(Constants.NGL_DATA_USER);
			List<Container> newContainers = null;
			try {
				newContainers = findContainerToCreate(contextError);
			} catch (DAOException e) {
				Logger.debug("ERROR in findContainerToCreate():" + e.getMessage());
			}
			
			//find current collection
			List<Container> oldContainers = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class).toList();

			//delete all contents
			for (Container oldContainer : oldContainers) {
				WriteResult r = (WriteResult) MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("code", oldContainer.code),   
						DBUpdate.unset("contents"));
							
			}
			
			Logger.info("Remove old contents OK");

			//iteration over current collection
			for (Container oldContainer : oldContainers) {
				
				for (Container newContainer : newContainers) {
					
					if (oldContainer.code.equals(newContainer.code)) {	
					 
						WriteResult r = (WriteResult) MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("code", oldContainer.code),   
								DBUpdate.set("contents", oldContainer.contents));
							
						
						n++;
						break;
					}
				}
				
			}	//end for containers
						
		} else {
			Logger.info("Migration containers already executed !");
		}
		
		Logger.info("Migration container (tag) Finish : " + n + " contents of containers updated !");
		return ok("Migration container (tag) Finish");
	}

	private static void backUpContainer() {
		Logger.info("\tCopie "+InstanceConstants.CONTAINER_COLL_NAME+" starts");
		MongoDBDAO.save(CONTAINER_COLL_NAME_BCK, MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class).toList());
		Logger.info("\tCopie "+InstanceConstants.CONTAINER_COLL_NAME+" ended");
	}
	

}
