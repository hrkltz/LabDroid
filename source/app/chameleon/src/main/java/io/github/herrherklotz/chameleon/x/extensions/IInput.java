package io.github.herrherklotz.chameleon.x.extensions;

public interface IInput {
	void in(int pId, Object pData) throws NullPointerException;
	
	@FunctionalInterface
	interface in0 {
		void in(int stringValue, Object objValue);
	}
}
