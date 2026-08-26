package org.kosit.base.functional;

/**
 * Special version of the Supplier interface for methods throwing an exception
 * 
 * @author Philip Helger
 *
 * @param <T> Return type
 * @param <EX> Exception type that is thrown
 */
@FunctionalInterface
public interface ThrowingSupplier<T, EX extends Exception> {

    /**
     * Gets a result.
     *
     * @return a result
     * @throws EX in case of error
     */
    T get() throws EX;
}