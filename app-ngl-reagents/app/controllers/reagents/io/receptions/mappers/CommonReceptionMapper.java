package controllers.reagents.io.receptions.mappers;

import java.util.Arrays;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import controllers.reagents.io.receptions.mappers.row.CellFinder;
import models.laboratory.common.instance.Comment;
import models.laboratory.reagent.instance.AbstractReception;
import validation.ContextValidation;

public abstract class CommonReceptionMapper extends AbstractReceptionMapper implements CellFinder {
	
	static final int DATE_RECEPTION = 0;
	static final int NOM_FOURNISSEUR = 1;
	static final int NOM_KIT = 2;
	static final int REF_CATALOGUE = 3;
	static final int NUMERO_LOT = 4;
	static final int IDENTIFIANT_UNIQUE = 5;
	static final int DATE_PEREMPTION = 6;
	static final int LABEL_TRAVAIL = 7;
	static final int COMMENTAIRE = 8;
	
	public CommonReceptionMapper(ContextValidation contextValidation) {
		super(contextValidation);
	}
	
	@Override
	public String getRefCatalog(Row row) {
		return getCellValue(row, REF_CATALOGUE);
	}

	@Override
	public String getProvider(Row row) {
		return getCellValue(row, NOM_FOURNISSEUR);
	}
	
	@Override
	public String getKitCatalogName(Row row) {
		return getCellValue(row, NOM_KIT);
	}

	@Override
	public void mapCell(AbstractReception reception, Cell cell, String user) {
		switch (cell.getColumnIndex()) {
		case DATE_RECEPTION:
			reception.receptionDate = this.getDate(cell);
			break;
		case NOM_FOURNISSEUR:
			reception.providerCode = this.getString(cell);
			break;
		case NOM_KIT: 
			reception.kitCatalogName = this.getString(cell);
			break;
		case REF_CATALOGUE:
			reception.catalogRefCode = this.getString(cell);
			break;
		case NUMERO_LOT:
			reception.batchNumber = this.getString(cell);
			break;
		case IDENTIFIANT_UNIQUE:
			reception.fromProviderId = this.getString(cell);
			break;
		case DATE_PEREMPTION:
			reception.expirationDate = this.getDate(cell);
			break;
		case LABEL_TRAVAIL:
			reception.workLabel = this.getString(cell);
			break;
		case COMMENTAIRE:
			reception.comments = Arrays.asList(new Comment(this.getString(cell), user, false));
			break;
		default:
			throw new IllegalArgumentException(
					"Unknown column at cell [row:" + cell.getRowIndex() + "|column:" + cell.getColumnIndex() + "]");
		}
	}

}
