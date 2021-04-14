package controllers.reagents.io;

import java.util.Arrays;
import java.util.NoSuchElementException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import com.google.common.collect.Iterators;

import models.laboratory.common.instance.Comment;
import models.laboratory.reagent.instance.AbstractReception;

public class IlluminaReceptionMapper extends AbstractReceptionMapper {

	static final String ILLUMINA_TYPE = "illumina-depot-reagent";
	static final String IMPORT_TYPE_FILE = "illumina-depot-reagents-reception";

	static final int DATE_RECEPTION = 0;
	static final int NOM_FOURNISSEUR = 1;
	static final int NOM_KIT = 2;
	static final int REF_CATALOGUE = 3;
	static final int NUMERO_LOT = 4;
	static final int IDENTIFIANT_UNIQUE = 5;
	static final int DATE_PEREMPTION = 6;
	static final int LABEL_TRAVAIL = 7;
	static final int COMMENTAIRE = 8;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractReception withCorrectTypes(AbstractReception reception) {
		reception.typeCode = ILLUMINA_TYPE;
		reception.importTypeCode = IMPORT_TYPE_FILE;
		return reception;
	}

	@Override
	public String getRefCatalog(Row row) {
		try {
			final Cell cellRefCatalog = Iterators.find(row.cellIterator(),
					(Cell cell) -> cell.getColumnIndex() == REF_CATALOGUE);
			return this.passCell(cellRefCatalog) ? null : this.getString(cellRefCatalog);
		} catch(final NoSuchElementException e) {
			// no ref catalog found
			return null;
		}
	}

	@Override
	public String getProvider(Row row) {
		try {
			final Cell cellProvider = Iterators.find(row.cellIterator(),
					(Cell cell) -> cell.getColumnIndex() == NOM_FOURNISSEUR);
			return this.passCell(cellProvider) ? null : this.getString(cellProvider);
		} catch (final NoSuchElementException e) {
			// no provider found
			return null;
		}
	}
	
	@Override
	public String getKitCatalogName(Row row) {
		try {
			final Cell cellKitCatalog = Iterators.find(row.cellIterator(),
					(Cell cell) -> cell.getColumnIndex() == NOM_KIT);
			return this.passCell(cellKitCatalog) ? null : this.getString(cellKitCatalog);
		} catch (final NoSuchElementException e) {
			// no kit catalog found
			return null;
		}
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
