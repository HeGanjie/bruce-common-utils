package bruce.common.functional;

public interface Func2<Rtn, TSource, TSource2> {
	Rtn call(TSource t, TSource2 t2);
}
