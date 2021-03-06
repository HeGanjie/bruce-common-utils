package common.functional;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//TODO : make more efficient
public class PersistentSet<E> implements Iterable<E>, PersistentCollection<E> {
	private static final long serialVersionUID = 7284417629409750270L;
	private final Set<E> set;
		
	public PersistentSet() {
		set = Collections.unmodifiableSet(new HashSet<E>());
	}
	
	@SafeVarargs
	public PersistentSet(E... initSet) {
		this(Arrays.asList(initSet));
	}

	public PersistentSet(Collection<E> initColl) {
		this(new HashSet<>(initColl));
	}

	public PersistentSet(Set<E> initSet) {
		set = Collections.unmodifiableSet(initSet);
	}
	
	@Override
	public PersistentSet<E> conj(E e) {
		Set<E> newSet = new HashSet<>(set);
		newSet.add(e);
		return new PersistentSet<>(newSet);
	}

	@Override
	public PersistentSet<E> merge(PersistentCollection<? extends E> c) {
		Set<E> newSet = new HashSet<>(set);
		newSet.addAll(c.getCollection());
		return new PersistentSet<>(newSet);
	}

	@Override
	public boolean contains(E o) {
		return set.contains(o);
	}

	@Override
	public boolean containsAll(PersistentCollection<? extends E> c) {
		return set.containsAll(c.getCollection());
	}

	@Override
	public boolean isEmpty() {
		return set.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return set.iterator();
	}

	@Override
	public PersistentSet<E> disj(E o) {
		Set<E> newSet = new HashSet<>(set);
		newSet.remove(o);
		return new PersistentSet<E>(newSet);
	}

	@Override
	public PersistentSet<E> disjAll(PersistentCollection<? extends E> c) {
		Set<E> newSet = new HashSet<>(set);
		newSet.removeAll(c.getCollection());
		return new PersistentSet<E>(newSet);
	}

	@Override
	public PersistentSet<E> retainAll(PersistentCollection<? extends E> c) {
		Set<E> newSet = new HashSet<>(set);
		newSet.retainAll(c.getCollection());
		return new PersistentSet<E>(newSet);
	}

	@Override
	public int size() {
		return set.size();
	}

	@Override
	public Object[] toArray() {
		return set.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return set.toArray(a);
	}

	@Override
	public Set<E> getCollection() {
		return set;
	}

	public Set<E> getModifiableCollection() {
		return new HashSet<>(set);
	}
	
	@Override
	public int hashCode() {
		return 31 + ((set == null) ? 0 : set.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		PersistentSet<?> other = (PersistentSet<?>) obj;
		if (set == null) {
			if (other.set != null)
				return false;
		} else if (!set.equals(other.set))
			return false;
		return true;
	}

	@Override
	public String toString() { return set.toString(); }
}
