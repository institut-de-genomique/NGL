import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

import lims.cns.dao.LimsManipDAO;
import lims.models.Manip;
import models.laboratory.experiment.instance.Experiment;
import models.utils.CodeHelper;

import org.junit.Test;

import play.api.modules.spring.Spring;
import utils.AbstractTests;


public class LimsPlaqueTest extends AbstractTests {
	
	@Test
	public void limsManipServicesGetManips(){
		
		LimsManipDAO  limsManipServices = Spring.getBeanOfType(LimsManipDAO.class);
        List<Manip> results = limsManipServices.findManips(13,2,"AAA");
        assertThat(results.size()).isNotNull();

	}
	
	@Test
	public void generateBarCode(){
		
		for(int i = 0; i < 15; i++){
		
			System.out.println("PLE_BGN_"+CodeHelper.getInstance().generateContainerSupportCode());
		
		}
	}
	
	
}
