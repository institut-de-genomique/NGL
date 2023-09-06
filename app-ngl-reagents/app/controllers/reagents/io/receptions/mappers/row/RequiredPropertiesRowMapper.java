package controllers.reagents.io.receptions.mappers.row;

import org.apache.poi.ss.usermodel.Row;

public interface RequiredPropertiesRowMapper {
	
	public String getRefCatalog(Row row);

	public String getProvider(Row row);
	
	public String getKitCatalogName(Row row);
	
	public default RequiredProperties getRequiredProperties(Row row) {
		String providerCode = getProvider(row);
		String kitCatalogName = getKitCatalogName(row);
		String catalogRefCode = getRefCatalog(row);
		return new RequiredProperties(providerCode, kitCatalogName, catalogRefCode);
	}
	
	public static final class RequiredProperties {
		
		private final String providerCode;
		private final String kitCatalogName;
		private final String catalogRefCode;
		
		public RequiredProperties(String providerCode, String kitCatalogName, String catalogRefCode) {
			this.providerCode = providerCode;
			this.kitCatalogName = kitCatalogName;
			this.catalogRefCode = catalogRefCode;
		}

		public String getProviderCode() {
			return providerCode;
		}

		public String getKitCatalogName() {
			return kitCatalogName;
		}

		public String getCatalogRefCode() {
			return catalogRefCode;
		}
		
	}

}
