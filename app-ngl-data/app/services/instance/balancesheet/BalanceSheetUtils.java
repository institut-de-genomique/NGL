package services.instance.balancesheet;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mongojack.DBQuery;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.NGLConfig;
import mail.MailServiceException;
import mail.MailServices;
import models.laboratory.balancesheet.instance.BalanceSheet;
import models.laboratory.balancesheet.instance.Computation;
import models.utils.InstanceConstants;
import services.instance.balancesheet.ComputationRequest.Match;

/**
 *
 * @author aprotat
 *
 */
public final class BalanceSheetUtils {

	private BalanceSheetUtils() {}

	public static final ComputationRequest[] DEFAULT_ILLUMINA_REQUESTS = new ComputationRequest[] {
			new ComputationRequest(Collection.READSETS, Method.SUM, "treatments.ngsrg.default.nbBases.value"),
			new ComputationRequest(Collection.RUNS),
			new ComputationRequest(Collection.RUNS, Match.create("state.code", DBQuery::is, "FE-S")),
			new ComputationRequest(Collection.RUNS, Match.create("instrumentUsed.code", DBQuery::regex, Pattern.compile("^EXT.+")))
	};

	public static final ComputationRequest[] DEFAULT_NANOPORE_REQUESTS = new ComputationRequest[] {
			new ComputationRequest(Collection.READSETS, Method.SUM, "treatments.ngsrg.default.1DForward.value.nbBases"),
			new ComputationRequest(Collection.READSETS, Method.SUM, "treatments.ngsrg.default.1DReverse.value.nbBases"),
			new ComputationRequest(Collection.READSETS, Method.SUM, "treatments.readQuality.default.1DForward.value.nbBases"),
			new ComputationRequest(Collection.READSETS, Method.SUM, "treatments.readQuality.default.1DReverse.value.nbBases"),
			new ComputationRequest(Collection.RUNS),
			new ComputationRequest(Collection.RUNS, Match.create("state.code", DBQuery::is, "FE-S"))
	};
	public static final ComputationRequest[] DEFAULT_PACBIO_REQUESTS = new ComputationRequest[] {
		new ComputationRequest(Collection.READSETS, Method.SUM, "treatments.readQuality.default.reads.value.nbBases"),
		new ComputationRequest(Collection.RUNS),
		new ComputationRequest(Collection.RUNS, Match.create("state.code", DBQuery::is, "FE-S")),
		new ComputationRequest(Collection.RUNS, Match.create("instrumentUsed.code", DBQuery::regex, Pattern.compile("^EXT.+")))
	};
	public static final BalanceSheet findBalanceSheet(String year, String type) {
		return MongoDBDAO.findOne(InstanceConstants.BALANCE_SHEET_COLL_NAME, BalanceSheet.class, DBQuery.and(DBQuery.is("year", year), DBQuery.is("type", type)));
	}

	public static final ComputationRequest[] requests(Type type) {
		switch(type) {
		case SEQ_ILLUMINA: return DEFAULT_ILLUMINA_REQUESTS;
		case SEQ_NANOPORE: return DEFAULT_NANOPORE_REQUESTS;
		case SEQ_PACBIO: return DEFAULT_PACBIO_REQUESTS;
		default: return new ComputationRequest[]{};
		}
	}

	public static final String categoryPath(Collection collection, Category category) {
		switch(collection) {
		case READSETS: switch(category) {
				case MONTH: return new String();
				case SEQUENCING_TYPE: return "runTypeCode";
				case SAMPLE_TYPE: return "sampleOnContainer.sampleTypeCode";
				case PROJECT: return "projectCode";
			}
		case RUNS: switch(category) {
				case MONTH: return new String();
				case SEQUENCING_TYPE: return "typeCode";
				case SAMPLE_TYPE: return null;
				case PROJECT: return null;
			}
		}
		return null;
	}

	public static final String typePath(Collection collection) {
		switch(collection) {
		case READSETS: return "typeCode";
		case RUNS: return "categoryCode";
		default: return null;
		}
	}

	public static final String typeValue(Collection collection, Type type) {
		switch(collection) {
			case READSETS: switch(type) {
				case SEQ_ILLUMINA: return "rsillumina";
				case SEQ_NANOPORE: return "rsnanopore";
				case SEQ_PACBIO: return "rspacbio";
			}
			case RUNS: switch(type) {
				case SEQ_ILLUMINA: return "illumina";
				case SEQ_NANOPORE: return "nanopore";
				case SEQ_PACBIO: return "pacbio";
			}
		}
		return null;
	}
	
	public static final void clearComputationLists(Computation computation) {
		computation.monthly.clear();
		computation.by.sequencingTypes.clear();
		computation.by.sampleTypes.clear();
		computation.by.projects.clear();
	}
	
	private static final String EXPEDITEUR_KEY = "reporting.email.from";

	private static final String DESTINATAIRES_KEY = "reporting.email.to";

	private static final String ENV_KEY = "ngl.env";
	
	public static void sendSuccessMail(MailServices mailServices, NGLConfig nglConfig, String subject, String message) throws MailServiceException, UnsupportedEncodingException {
		Config config = ConfigFactory.load();
		final String EXPEDITEUR = config.getString(EXPEDITEUR_KEY);
		final Set<String> DESTINATAIRES = Stream.of(config.getString(DESTINATAIRES_KEY).split(",")).collect(Collectors.toSet());
		final String ENVIRONMENT = config.getString(ENV_KEY);
		final String INSTITUTE = nglConfig.getInstitute();
		final String isoMessage = new String(message.getBytes(), "iso-8859-1");

		String infoMessage = "<b>" + isoMessage + "</b><br/>";
		String infoSubject = "[NGL-DATA] [" + ENVIRONMENT + "] [" + INSTITUTE + "] " + subject;
		
		mailServices.sendMail(EXPEDITEUR, DESTINATAIRES, infoSubject, infoMessage);
	}

	/**
	 * Category to aggregate statistic by.
	 * @author aprotat
	 *
	 */
	public static enum Category {

		MONTH("month"),
		SEQUENCING_TYPE("sequencingType"),
		SAMPLE_TYPE("sampleType"),
		PROJECT("project");

		public final String name;

		private Category(String name) {
			this.name = name;
		}

		public static final Category fromName(String name) {
			switch(name) {
			case "month": return MONTH;
			case "sequencingType": return SEQUENCING_TYPE;
			case "sampleType": return SAMPLE_TYPE;
			case "project": return PROJECT;
			default: return null;
			}
		}

	}

	/**
	 * MongoDB collection (data) to use for balance sheet statistic.
	 * @author aprotat
	 *
	 */
	public static enum Collection {

		READSETS("readsets", InstanceConstants.READSET_ILLUMINA_COLL_NAME, "runSequencingStartDate",
				Category.MONTH,
				Category.SEQUENCING_TYPE,
				Category.SAMPLE_TYPE,
				Category.PROJECT
				),
		RUNS("runs", InstanceConstants.RUN_ILLUMINA_COLL_NAME, "sequencingStartDate",
				Category.MONTH,
				Category.SEQUENCING_TYPE
				);

		public final String name;
		public final String colectionPath;
		public final String startDatePath;
		public final Category[] categories;

		private Collection(String name, String path, String startDate, Category... categories) {
			this.name = name;
			colectionPath = path;
			startDatePath = startDate;
			this.categories = categories;
		}

		public static final Collection from(String name) {
			switch(name) {
			case "readsets": return READSETS;
			case "runs": return RUNS;
			default: return null;
			}
		}

	}

	/**
	 * Type of data to use for statistic: illumina or nanopore.
	 * @author aprotat
	 *
	 */
	public static enum Type {

		SEQ_ILLUMINA("bi-illumina"), SEQ_NANOPORE("bi-nanopore"),SEQ_PACBIO("bi-pacbio");

		public final String value;

		private Type(String value) {
			this.value = value;
		}

		public static final Type from(String type) {
			switch(type) {
			case "bi-illumina": return SEQ_ILLUMINA;
			case "bi-nanopore": return SEQ_NANOPORE;
			case "bi-pacbio" : return SEQ_PACBIO;
			default: return null;
			}
		}

	}

	/**
	 * Statistic method to apply to data.
	 * @author aprotat
	 *
	 */
	public static enum Method {
		COUNT("count"), SUM("sum"), MEAN("mean");

		public final String value;

		private Method(String value) {
			this.value = value;
		}

		public static final Method from(String value) {
			switch(value) {
			case "count": return COUNT;
			case "sum": return SUM;
			case "mean": return MEAN;
			default: return null;
			}
		}
	};

}
