package bruce.common.functional;


import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import bruce.common.utils.CommonUtils;

public final class SortableList<TSource> {
	final List<TSource> sourceList;
	final SortableList<TSource> parent;
	final Comparator<TSource> comparator;
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	private static <_Comparable_, TSource> Comparator<TSource> createComparator(final Func1<_Comparable_, TSource> selector, boolean desc) {
		if (desc) {
			return new Comparator<TSource>() {
				@Override
				public int compare(TSource t, TSource t2) {
					return ((Comparable) selector.call(t2)).compareTo(selector.call(t));
				}
			};
		}
		return new Comparator<TSource>() {
			@Override
			public int compare(TSource t, TSource t2) {
				return ((Comparable) selector.call(t)).compareTo(selector.call(t2));
			}
		};
	}

	public <_Comparable_> SortableList(List<TSource> list, Func1<_Comparable_, TSource> selector, boolean desc) {
		parent = null;
		sourceList = list;
		comparator = createComparator(selector, desc);
	}
	
	private <_Comparable_> SortableList(SortableList<TSource> sortableList, Func1<_Comparable_, TSource> selector, boolean desc) {
		parent = sortableList;
		sourceList = null;
		comparator = createComparator(selector, desc);
	}

	public <_Comparable_> SortableList<TSource> thenBy(Func1<_Comparable_, TSource> selector) {
		return new SortableList<TSource>(this, selector, false);
	}
	
	public <_Comparable_> SortableList<TSource> thenByDescending(Func1<_Comparable_, TSource> selector) {
		return new SortableList<TSource>(this, selector, true);
	}
	
	private Comparator<? super TSource> createComparatorChain() {
		final List<Comparator<TSource>> comparators = new LinkedList<Comparator<TSource>>();
		SortableList<TSource> s = this;
		do {
			comparators.add(0, s.comparator);
		} while ((s = s.parent) != null);
		
		return new Comparator<TSource>() {
			@Override
			public int compare(TSource t1, TSource t2) {
				int compareResult = 0, i = 0;
				Comparator<TSource> comparator = null;
				do {
					comparator = comparators.get(i++);
					compareResult = comparator.compare(t1, t2);
				} while (compareResult == 0 && CommonUtils.isLegalIndex(comparators, i));
				return compareResult;
			}
		};
	}

	private List<TSource> getSourceList() {
		if (sourceList == null) return parent.getSourceList();
		return sourceList;
	}

	@SuppressWarnings("unchecked")
	public List<TSource> toList() {
		TSource[] array = (TSource[])getSourceList().toArray();
		Arrays.sort(array, createComparatorChain());
		return Arrays.asList(array);
	}

}
