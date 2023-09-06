package services.instance.balancesheet;

@FunctionalInterface
public interface ResultReducer {

	Double reduce(Double previousValue, Integer previousNbElements, Double newValue, Integer newNbElement);

}
