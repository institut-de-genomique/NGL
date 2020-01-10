package fr.cea.ig.ngl.domain;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.mongojack.DBUpdate;

public class Type {
	
	// We quite need a name as we use those definitions to map the 
	// the incoming data to some update data. The update data
	// is the DTO content and the copy management is done using
	// the defs for the operation. We are halfway done using the
	// deserialized DTO. 
	public static class Def<T,A> {
		//String          name;
		BiConsumer<DBUpdate.Builder,T> qb;
		Function<T,A>   getter;
		BiConsumer<T,A> setter;
		
		public Def(BiConsumer<DBUpdate.Builder,T> qb,	BiConsumer<T,A> setter, Function<T,A> getter) {
			this.qb     = qb;
			this.getter = getter;
			this.setter = setter;
		}
		public Def(final String name,	BiConsumer<T,A> setter, Function<T,A> getter) {
//			this.name   = name;
//			this.getter = getter;
//			this.setter = setter;
			this((q,t) -> q.set(name,getter.apply(t)),setter,getter);
		}
		public void copy(T from, T to) {
			setter.accept(to, getter.apply(from));
		}
		// This is really the update generator that should be
		// overridable.
		public void build(DBUpdate.Builder b, T t) {
			// b.set(name, getter.apply(t));
			qb.accept(b, t);
		}
		// We use a setter closure for the setup, could be
		// some specific closure that allows access to the definition.
		public interface Closure<T,A> extends Consumer<T> {
			Def<T, A> getDef();
		}
		public Closure<T,A> setup(final A a) {
			return new Closure<T,A>() {
				@Override
				public void accept(final T t) { Def.this.setter.accept(t, a); }
				@Override
				public Def<T, A> getDef() { return Def.this; }
			};
		}
	}

}
