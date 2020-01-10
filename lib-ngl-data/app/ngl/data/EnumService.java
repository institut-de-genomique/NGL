package ngl.data;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import models.utils.Model;
import models.utils.ModelDAOs;
import play.data.validation.ValidationError;

public abstract class EnumService<T extends Enum<T>,U extends Model> {
	
	private static final play.Logger.ALogger logger = play.Logger.of(EnumService.class); 
	
	private final ModelDAOs mdao;
	
	private final Class<T> e;
	
	private final Function<T,U> factory;
	
	public EnumService(ModelDAOs mdao, Class<T> e, Function<T,U> factory) {
		this.mdao    = mdao;
		this.e       = e;
		this.factory = factory;
	}
	
	public void saveData(Map<String,List<ValidationError>> errors) {
		for (T v : e.getEnumConstants()) {
			U u = factory.apply(v);
			logger.debug("save enum {} {} {}", e, v , u);
			mdao.saveModel(u,errors);
		}
	}
	
}