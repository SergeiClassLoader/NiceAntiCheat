package pro.cyrent.anticheat.util;

@FunctionalInterface
public interface Callback<T> {
   void call(T var1);
}