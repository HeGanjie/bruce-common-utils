package bruce.common.functional;

public final class Pointer<T> {
	public Pointer() { }
	public Pointer(T initVal) {
		value = initVal;
	}

	public T value;

	@Override
	public String toString() { return "Pointer [value=" + value + "]"; }
	
	@Override
	public int hashCode() { return 31 + ((value == null) ? 0 : value.hashCode()); }
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Pointer other = (Pointer) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
}
