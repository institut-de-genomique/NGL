package services.instance.balancesheet;

import java.util.regex.Pattern;

import org.mongojack.DBQuery;

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

	public static ComputationRequest[] requests(Type type) {
		switch(type) {
		case ILLUMINA: return DEFAULT_ILLUMINA_REQUESTS;
		case NANOPORE: return DEFAULT_NANOPORE_REQUESTS;
		default: return new ComputationRequest[]{};
		}
	}

	public static String categoryPath(Collection collection, Category category) {
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

	public static String typePath(Collection collection) {
		switch(collection) {
		case READSETS: return "typeCode";
		case RUNS: return "categoryCode";
		default: return null;
		}
	}

	public static String typeValue(Collection collection, Type type) {
		switch(collection) {
			case READSETS: switch(type) {
				case ILLUMINA: return "rsillumina";
				case NANOPORE: return "rsnanopore";
			}
			case RUNS: switch(type) {
				case ILLUMINA: return "illumina";
				case NANOPORE: return "nanopore";
			}
		}
		return null;
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

		ILLUMINA("illumina", 2008), NANOPORE("nanopore", 2014);

		public final String value;
		public int firstYear;

		private Type(String value, int firstYear) {
			this.value = value;
			this.firstYear = firstYear;
		}

		public static final Type from(String type) {
			switch(type) {
			case "illumina": return ILLUMINA;
			case "nanopore": return NANOPORE;
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
