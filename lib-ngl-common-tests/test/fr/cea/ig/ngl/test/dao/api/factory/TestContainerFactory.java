package fr.cea.ig.ngl.test.dao.api.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

import controllers.containers.api.ContainerSupportsSearchForm;
import controllers.containers.api.ContainersSearchForm;
import fr.cea.ig.lfw.utils.Iterables;
import fr.cea.ig.lfw.utils.ZenIterable;
import fr.cea.ig.ngl.support.ListFormWrapper;
import fr.cea.ig.play.test.DevAppTesting;
import fr.cea.ig.util.function.F0;
import fr.cea.ig.util.function.T;
import fr.cea.ig.util.function.T2;
import fr.cea.ig.util.function.T3;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.container.instance.Content;
import models.laboratory.container.instance.LocationOnContainerSupport;
import models.laboratory.sample.instance.Sample;

public class TestContainerFactory {

	private static final play.Logger.ALogger logger = play.Logger.of(TestContainerFactory.class);
	
	private static final String UNIT_NG = "ng";
	// Constants
	public static final double QUANTITY = 1.0;
	public static final double VOL      = 1.0;
	// Container support
	public static final String PLATE_96 = "96-well-plate";
	// Container support or container
	public static final String TUBE     = "tube";
	// conatiner
	public static final String WELL     = "well";
	
	//private static final play.Logger.ALogger logger = play.Logger.of(TestContainerFactory.class);
	
	public static Container container(String user, Sample s, ContainerSupport support) {
		return container(user, Arrays.asList(s), support.code, support.categoryCode);
	}
	
	public static Container container(String user, Collection<Sample> samples, ContainerSupport support) {
		return container(user, samples, support.code, support.categoryCode);
	}
	
	public static Container containerTube(String user, Collection<Sample> samples, ContainerSupport support) {
		return container(user, samples, support.code, TUBE);
	}
	
	public static Container containerWell(String user, Collection<Sample> samples, ContainerSupport support) {
		return container(user, samples, support.code, PLATE_96);
	}
	
	public static ContainerSupport containerSupportTube(String user, Sample s) {
		return containerSupport(user, Arrays.asList(s), TUBE);
	}
	
	public static ContainerSupport containerSupportPlate96(String user, Collection<Sample> samples) {
		return containerSupport(user, samples, PLATE_96);
	}
	
	/*
	 * if more than one sample in the list it will create a pool
	 * @param user
	 * @param samples
	 * @param containerSupportCode
	 * @param containerSupportCategoryCode
	 * @return
	 */
	private static Container container(String user, Collection<Sample> samples, String containerSupportCode, String containerSupportCategoryCode) {
		Container c = new Container();
		c.code          = DevAppTesting.newCode();
		//c.categoryCode = TUBE; // Validation: check only if the code exists. No validation against support categoryCode.
		c.categoryCode  = chooseContainerCategoryCode(containerSupportCategoryCode);
		c.concentration = new PropertySingleValue(QUANTITY/VOL, "ng/µl");
		c.volume        = new PropertySingleValue(VOL, "µl");
		c.quantity      = new PropertySingleValue(QUANTITY, UNIT_NG);
		c.comments      = comments(user);
		c.contents      = new ArrayList<>(samples.size());
		c.projectCodes  = new HashSet<>();
		c.sampleCodes   = new HashSet<>();
		samples.forEach(s -> {
			c.projectCodes.addAll(s.projectCodes);
			c.sampleCodes.add(s.code);
			c.contents.add(content(s, 100.0/samples.size()));
		});
		c.traceInformation = new TraceInformation(user);
		c.support       = supportLocation(containerSupportCode, containerSupportCategoryCode);
//		c.state         = state(user);
		return c;
	}

	private static String chooseContainerCategoryCode(String containerSupportCategoryCode) {
		switch (containerSupportCategoryCode) {
		case TUBE : return TUBE;
		default   : return WELL;
		}
	}
	
	public static ZenIterable<F0<Container>> containers(String user, Collection<Sample> samples, ContainerSupport support) {
		return containers(user, samples, support.code, support.categoryCode);
	}
	
	protected static ZenIterable<F0<Container>> containers(String user, String containerSupportCode, String containerSupportCategoryCode, Iterable<T3<String,String,Sample>> map) {
		return Iterables.map(map, t -> {
			String line   = t.a;
			String column = t.b;
			Sample s      = t.c;
			return () -> {
				Container c = new Container();
				c.code             = DevAppTesting.newCode();
				c.categoryCode     = chooseContainerCategoryCode(containerSupportCategoryCode);
				c.concentration    = new PropertySingleValue(QUANTITY/VOL, "ng/µl");
				c.volume           = new PropertySingleValue(VOL, "µl");
				c.quantity         = new PropertySingleValue(QUANTITY, UNIT_NG);
				c.comments         = comments(user);
				c.contents         = Arrays.asList(content(s, 100));
				c.projectCodes.addAll(s.projectCodes);
				c.sampleCodes.add(s.code);
				c.traceInformation = new TraceInformation(user);
				c.support          = supportLocation(containerSupportCode, containerSupportCategoryCode, line, column);
//				c.state            = state(user);
				return c;
			};
		});
	}
	
	/**
	 * Cartesian product of lines and columns.
	 * @param lines   lines
	 * @param columns columns
	 * @return        product
	 */
	public static <A,B> ZenIterable<T2<A,B>> product(Iterable<A> lines, Iterable<B> columns) {
		return Iterables.flatMap(lines, l-> Iterables.map(columns, c -> T.t2(l, c)));
	}
	
	/**
	 * Cartesian product of lines and columns as String pairs.
	 * @param lines   lines
	 * @param columns columns
	 * @return        product as string pairs
	 */
	public static ZenIterable<T2<String,String>> coordinates(Iterable<?> lines, Iterable<?> columns) {
		return product(lines, columns)
			   .map(t -> T.t2(t.a.toString(), t.b.toString()));
	}
	
	/**
	 * Maps the elements to the requested coordinates set.
	 * @param lines   lines values
	 * @param columns columns values
	 * @param as      elements
	 * @return        coordinates mapped elements
	 */
	public static <A,B,C> ZenIterable<T3<String,String,C>> coordinates(Iterable<A> lines, Iterable<B> columns, Iterable<C> as) {
		return coordinates(lines, columns)
		                .zip(as)
		                .map(p -> T.t3(p.left.a,  p.left.b, p.right));	
	}
	
	// Could use an explicit enumeration or a mapping for the plate 96 lines (be it from [0-7] or [1-8]).
//	private static final List<String> lines96   = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H");
	/**
	 * Plate 96 lines (A-H).
	 */
	private static final List<String> lines96   = Iterables.range(1, 8).map(i -> Character.toString((char)('A' + i - 1))).toList();
	
	/**
	 * Plate 96 columns (1-12).
	 */
	private static final List<String> columns96 = Iterables.range(1,12).map(i -> Integer.toString(i))                    .toList();
	
	/**
	 * Coordinates and samples using a distinct coordinate in {@link #lines96}x{@link #columns96} range.
	 * @param as samples to map to coordinates
	 * @return   mapped samples
	 */
	public static <A> ZenIterable<T3<String,String,A>> coordinates96(Iterable<A> as) {
		return coordinates(lines96, columns96, as);
	}

	/**
	 * Associates (1,1) coordinates to each argument iterable element.
	 * @param as elements to associate to (1,1)
	 * @return   associted elements
	 */
	public static <A> ZenIterable<T3<String,String,A>> coordinates11(Iterable<A> as) {
		return coordinates(Iterables.repeat(1), Iterables.repeat(1), as);
	}
	
	/**
	 * Containers that are created from an iterable of samples that are mapped to plate 96 coordinates.
	 * @param user                         trace information user
	 * @param samples                      samples to create containers from
	 * @param containerSupportCode         container support code
	 * @param containerSupportCategoryCode container support category code
	 * @return                             non persisted containers
	 */
	protected static ZenIterable<F0<Container>> containers(String user, Iterable<Sample> samples, String containerSupportCode, String containerSupportCategoryCode) {
		return containers(user, containerSupportCode, containerSupportCategoryCode, coordinates96(samples));
	}
	
//	private static List<Container> containers(String user, Collection<Sample> samples, String containerSupportCode, String containerSupportCategoryCode) {
//		List<Container> containers = new ArrayList<>(samples.size());
//		int line   = 0;
//		int column = 0;
//		for (Sample s : samples) {
//			Container c = new Container();
//			c.code             = DevAppTesting.newCode();
//			c.categoryCode     = chooseContainerCategoryCode(containerSupportCategoryCode);
//			c.concentration    = new PropertySingleValue(QUANTITY/VOL, "ng/µl");
//			c.volume           = new PropertySingleValue(VOL, "µl");
//			c.quantity         = new PropertySingleValue(QUANTITY, UNIT_NG);
//			c.comments         = comments(user);
//			c.contents         = Arrays.asList(content(s, new Double(100)));
//			c.projectCodes.addAll(s.projectCodes);
//			c.sampleCodes.add(s.code);
//			c.traceInformation = new TraceInformation(user);
////			c.support          = support(containerSupportCode, containerSupportCategoryCode, new String("" + line++), new String("" + column++));
//			c.support          = support(containerSupportCode, containerSupportCategoryCode, Integer.toString(line++), Integer.toString(column++));
//			c.state            = state(user);
//			containers.add(c);
//		}
//		return containers;
//	}

//	// Pretty unclear why the state is built using A_QC.
//	/*
//	 * warning: the state will be changed to N (new) state by create method of API
//	 */
//	private static State state(String user) {
//		State s = new State(ContainerStateNames.A_QC, user);
//		s.date = new Date();
//		return s;
//	}

	public static LocationOnContainerSupport supportLocation(String containerSupportCode, String containerSupportCategoryCode) {
		return supportLocation(containerSupportCode, containerSupportCategoryCode, "1", "1");
	}
	
	public static LocationOnContainerSupport supportLocation(String containerSupportCode, String containerSupportCategoryCode, String line, String colum) {
		LocationOnContainerSupport s = new LocationOnContainerSupport();
		s.code         = containerSupportCode;
		s.categoryCode = containerSupportCategoryCode;
		s.line         = line;
		s.column       = colum;
		return s;
	}

	private static Content content(Sample s, double percentage) {
		Content c = new Content(s.code, s.typeCode, s.categoryCode);
		c.percentage  = percentage;
		c.projectCode = s.projectCodes.iterator().next();
		return c;
	}

	private static List<Comment> comments(String user){
		return Arrays.asList(new Comment("very usefull comments", user));
	}
	
	/**
	 * Pretty strong constraint as the container support supposes that the samples are mapped to
	 * containers.
	 * @param user            trace information user
	 * @param samples         samples
	 * @param supportCategory support category
	 * @return                container support
	 */
	private static ContainerSupport containerSupport(String user, Collection<Sample> samples, String supportCategory) {
		ContainerSupport cs = new ContainerSupport();
		cs.code         = DevAppTesting.newCode(); 
		cs.categoryCode = supportCategory;
		cs.comments     = comments(user);
		cs.projectCodes = Iterables.flatMap(samples, s -> s.projectCodes).toSet();
		cs.sampleCodes  = Iterables.map    (samples, s -> s.code).toSet();
		cs.nbContainers = samples.size();
		cs.nbContents   = samples.size();
		return cs;
	}
	
	/**
	 * No querying mode or rendering mode are defined in the created form.
	 * 
	 * @param projectCode code of project
	 * @return            wrapper of ContainersSearchForm
	 * @throws Exception  exception
	 */
	public static ListFormWrapper<Container> wrapper(String projectCode) throws Exception {
		return wrapper(projectCode, null, null);
	}
	
	/**
	 * @param projectCode code of project
	 * @param qmode       querying mode
	 * @param render      rendering mode
	 * @return            wrapper of ContainersSearchForm
	 * @throws Exception  exception
	 */
	public static ListFormWrapper<Container> wrapper(String projectCode, QueryMode qmode, RenderMode render) throws Exception {
		ContainersSearchForm form = new ContainersSearchForm();
		form.projectCodes = new HashSet<>(Arrays.asList(projectCode));
		return new TestListFormWrapperFactory<Container>().wrapping().apply(form, qmode, render); 
	}
	
	/**
	 * No querying mode or rendering mode are defined in the created form.
	 * 
	 * @param projectCode code of project
	 * @return            wrapper of ContainersSearchForm
	 * @throws Exception  exception
	 */
	public static ListFormWrapper<ContainerSupport> wrapperSupport(String projectCode) throws Exception{
		return wrapperSupport(projectCode, null, null);
	}
	
	/**
	 * @param projectCode code of project
	 * @param qmode       querying mode
	 * @param render      rendering mode
	 * @return            wrapper of ContainersSearchForm
	 * @throws Exception  exception
	 */
	public static ListFormWrapper<ContainerSupport> wrapperSupport(String projectCode, QueryMode qmode, RenderMode render) throws Exception {
		ContainerSupportsSearchForm form = new ContainerSupportsSearchForm();
		form.projectCodes = Arrays.asList(projectCode);
		return new TestListFormWrapperFactory<ContainerSupport>().wrapping().apply(form, qmode, render);
	}
	
	// ----------------------------------------------------------------------------------------
	// 
	
	/**
	 * This should create a coherent support with a category and the given containers.
	 * @param user            trace information user
	 * @param supportCategory support category
	 * @param containersF     container factories
	 * @return                container support 
	 */
	public static T2<ContainerSupport, List<Container>> containerSupport(String user, String supportCategory, Iterable<Function<String,Container>> containersF) {
		ContainerSupport cs = new ContainerSupport();
		cs.code         = DevAppTesting.newCode();
		logger.debug("instanciating container {}", cs.code);
		List<Container> containers = Iterables.map(containersF, f -> f.apply(cs.code)).toList();
		cs.categoryCode = supportCategory;
		cs.comments     = comments(user);
		cs.projectCodes = Iterables.flatMap(containers, c -> c.projectCodes).toSet();
		cs.sampleCodes  = Iterables.flatMap(containers, c -> c.sampleCodes).toSet();
		cs.nbContainers = containers.size();
		cs.nbContents   = Iterables.sum(Iterables.map(containers, c -> c.contents.size()));
		logger.debug("sample codes {}, project codes {}", Iterables.intercalate(cs.sampleCodes, ", "), Iterables.intercalate(cs.projectCodes, ", "));
		return T.t2(cs, containers);
	}
	
	public static Function<String, LocationOnContainerSupport> supportLocation(String containerSupportCategoryCode, String line, String colum) {
		return containerSupportCode -> {
			LocationOnContainerSupport s = new LocationOnContainerSupport();
			s.code         = containerSupportCode;
			s.categoryCode = containerSupportCategoryCode;
			s.line         = line;
			s.column       = colum;
			return s;
		};
	}
	
	public static ZenIterable<Function<String, Container>> containers1Sample(String user, String containerSupportCategoryCode, Iterable<T3<String,String,Sample>> map) {
		return containersNSamples(user, containerSupportCategoryCode, Iterables.map(map, t -> T.t3(t.a, t.b, Arrays.asList(t.c))));
	}
	
	public static ZenIterable<Function<String, Container>> containers1Sample(String user, String containerSupportCategoryCode, Iterable<T3<String,String,Sample>> map, Function<Container, Container> f) {
		return containersNSamples(user, containerSupportCategoryCode, Iterables.map(map, t -> T.t3(t.a, t.b, Arrays.asList(t.c))), f);
	}
	
	public static ZenIterable<Function<String, Container>> containersNSamples(String user, String containerSupportCategoryCode, Iterable<T3<String,String,List<Sample>>> map) {
		return containersNSamples(user, containerSupportCategoryCode, map, c -> c);
	}

	public static ZenIterable<Function<String, Container>> containersNSamples(String user, String containerSupportCategoryCode, Iterable<T3<String,String,List<Sample>>> map, Function<Container,Container> f) {
		return Iterables.map(map, t -> {
			String       line   = t.a;
			String       column = t.b;
			List<Sample> ss     = t.c;
			return containerSupportCode -> {
				Container c = new Container();
				c.code             = DevAppTesting.newCode();
				c.categoryCode     = chooseContainerCategoryCode(containerSupportCategoryCode);
				c.concentration    = new PropertySingleValue(QUANTITY/VOL, "ng/µl");
				c.volume           = new PropertySingleValue(VOL, "µl");
				c.quantity         = new PropertySingleValue(QUANTITY, UNIT_NG);
				c.comments         = comments(user);
				c.contents         = Iterables.map    (ss, s -> content(s, 100.0 / ss.size())).toList();
				c.projectCodes     = Iterables.flatMap(ss, s -> s.projectCodes).toSet();
				c.sampleCodes      = Iterables.map    (ss, s -> s.code).toSet();
				c.traceInformation = new TraceInformation(user);
				c.support          = supportLocation(containerSupportCode, containerSupportCategoryCode, line, column);
//				c.state            = state(user);
				c = f.apply(c);
				return c;
			};
		});
	}
	
}
