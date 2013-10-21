package bruce.common.functional;

public final class Pointer<T> {
	public Pointer() { }
	public Pointer(T initVal) {
		value = initVal;
	}

	public T value;
}
