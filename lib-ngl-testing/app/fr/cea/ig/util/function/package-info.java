/**
 * Functional core types.
 * 
 * <p> 
 * CN types are consumers of N arguments that define an accept method that
 * takes said number of arguments. Ignoring method names
 * and thrown exceptions we have equivalent definitions
 * in standard java:
 * <ul>
 *   <li>C0 : {@link java.lang.Runnable}</li>
 *   <li>C1 : {@link java.util.function.Consumer}</li>
 *   <li>C2 : {@link java.util.function.BiConsumer}</li> 
 * </ul>
 * 
 * <p>
 * FN types are functions that take N argument and they define an apply
 * method that take said number of arguments. Ignoring method names and thrown 
 * exceptions we have some equivalent definitions in standard java:
 * <ul>
 *   <li>F0 : {@link java.util.function.Supplier}</li>
 *   <li>F1 : {@link java.util.function.Function}</li>
 *   <li>F2 : {@link java.util.function.BiFunction}</li>  
 * </ul>
 * 
 * <p>
 * CTN types are {@literal C1<C1<TN>>} and thus {@literal CC1<TN>} types.
 * Those types allows some kind of tuple argument conversion but are not really
 * the core types. The CCN being preferred is caused by the java syntax that
 * allow named lambda arguments that helps clarify the user code while the
 * tuple attribute naming (a,b,...) does not. 
 *  
 * <p>
 * CCN types are {@literal C1<CN>} types.
 * We use consumers of consumer to allow nesting of "actions" and provide support
 * for begin-code-end nested structures.
 * <br>
 * Combining CCNs is done by using the 'nest' core method that builds a CC(N+M) 
 * from a CCN and a function from N arguments to a CCM (implementation provides
 * the CCN+CC1 method) . We provide ccM projection methods
 * from CCN to CCM, {@literal N < M}, that allows 'projection' of the arguments.
 * The projection is defined using a tuple TM return type except for the cc1 case that
 * uses the value and not a T1.
 * <p>
 * There is an equivalence between CCNs and CTNs and this is proved by the conversion
 * methods that exists from one to another:
 * <ul>
 *   <li>A CTN cc() method returns a CCN<li>
 *   <li>A CCN ct() method returns a CTN<li>  
 * </ul>
 * 
 * <p>
 * Projections method names are defined based on the return type, ccN for a projection to CCN and ctN
 * for a projection to CTN. There is a generic fmap method that does almost the job
 * but provides a mapping to {@literal CC1<TN>} that is almost CTN but not quite.  
 * 
 * <p>
 * CTN can be considered as implementing the visitor pattern. A tuple visitor has a single
 * visit method and this is can be seen as the consumer accept method. A tuple has an 
 * accept method that takes a consumer and can be seen as the accept method from the
 * visitor pattern. 
 * 
 * <p>
 * The nesting system can be used to implement something similar to the try
 * with resource provided by java (see Examples class).
 */
package fr.cea.ig.util.function;

/*
 * <p>
 * TN types are tuple types. We choose the see tuples as CCN instead
 * of the possibly more natural CTN but the lack of tuple support in java
 * make the CCN choice better syntactically (hopefully).
 * 
 */
